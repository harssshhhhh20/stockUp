import React, { createContext, useCallback, useContext, useEffect, useMemo, useState } from "react";
import AsyncStorage from "@react-native-async-storage/async-storage";
import { AuthApi, StoreApi } from "../api/endpoints";
import {
  api,
  clearTokens,
  getAccessToken,
  setSessionExpiredHandler,
  setTokens,
} from "../api/client";
import { StoreResponse, UserProfile } from "../api/types";

type Mode = "customer" | "merchant";

/** Where onboarding should send someone, derived from one profile call. */
export type OnboardingStep =
  | "choose-role"
  | "complete-profile"
  | "setup-shop"
  | "done";

const ROLE_INTENT_KEY = "stockup.roleIntent";

type AuthContextValue = {
  booting: boolean;
  signedIn: boolean;
  profile: UserProfile | null;
  email: string | null;
  store: StoreResponse | null;
  /** Derived from the fork — which side of the app is on screen. */
  mode: Mode;

  /** What the app should show next. */
  onboardingStep: OnboardingStep;
  /** Which path they picked at the fork, before the server knows about it. */
  roleIntent: Mode | null;
  chooseRole: (role: Mode) => Promise<void>;
  /** Return to the role fork — used by back from an onboarding step. */
  clearRoleIntent: () => Promise<void>;

  refresh: () => Promise<void>;
  signIn: (email: string, otp: string) => Promise<boolean>;
  signOut: () => Promise<void>;

  // Kept for screens that still read these.
  merchantProfile: { merchantId: string; bharosaScore: number } | null;
  refreshMerchantState: () => Promise<void>;
};

const AuthContext = createContext<AuthContextValue | null>(null);

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error("useAuth must be used within AuthProvider");
  return ctx;
}

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [booting, setBooting] = useState(true);
  const [profile, setProfile] = useState<UserProfile | null>(null);
  const [store, setStore] = useState<StoreResponse | null>(null);
  const [roleIntent, setRoleIntent] = useState<Mode | null>(null);

  const load = useCallback(async () => {
    const me = await AuthApi.me();
    setProfile(me);

    // A merchant's store details are only needed once they have one.
    if (me.hasStore) {
      try {
        setStore(await StoreApi.me());
      } catch {
        setStore(null);
      }
    } else {
      setStore(null);
    }

    return me;
  }, []);

  useEffect(() => {
    (async () => {
      const token = await getAccessToken();
      if (token) {
        try {
          await load();
          setRoleIntent((await AsyncStorage.getItem(ROLE_INTENT_KEY)) as Mode | null);
        } catch {
          await clearTokens();
          setProfile(null);
        }
      }
      setBooting(false);
    })();
  }, [load]);


  /**
   * The fork straight after sign-in. Remembered on the device because the
   * server has no concept of "intends to be a merchant" until they actually
   * register one — without this, closing the app mid-setup would drop them
   * back at the fork with no memory of what they chose.
   */
  const chooseRole = useCallback(async (role: Mode) => {
    await AsyncStorage.setItem(ROLE_INTENT_KEY, role);
    setRoleIntent(role);
  }, []);

  const clearRoleIntent = useCallback(async () => {
    await AsyncStorage.removeItem(ROLE_INTENT_KEY);
    setRoleIntent(null);
  }, []);

  const signIn = useCallback(
    async (emailInput: string, otp: string) => {
      const res = await AuthApi.verifyOtp(emailInput, otp);
      await setTokens(res.accessToken, res.refreshToken);
      const me = await load();
      // Each sign-in starts without an answer, so the fork asks once and the
      // rest of the app never has to ask again. The exception is a shopkeeper
      // who hasn't finished opening their shop — they already answered, and
      // re-asking would strand them halfway through setup.
      if (me.isMerchant && !me.hasStore) {
        await AsyncStorage.setItem(ROLE_INTENT_KEY, "merchant");
        setRoleIntent("merchant");
      } else {
        await AsyncStorage.removeItem(ROLE_INTENT_KEY);
        setRoleIntent(null);
      }
      return res.newUser;
    },
    [load]
  );

  const signOut = useCallback(async () => {
    await clearTokens();
    await AsyncStorage.removeItem(ROLE_INTENT_KEY);
    setProfile(null);
    setStore(null);
    setRoleIntent(null);
  }, []);

  useEffect(() => {
    setSessionExpiredHandler(() => {
      setProfile(null);
      setStore(null);
      setRoleIntent(null);
    });
    return () => setSessionExpiredHandler(null);
  }, []);

  /**
   * One place decides what comes next, so no screen has to reason about
   * onboarding order itself.
   */
  const onboardingStep: OnboardingStep = useMemo(() => {
    if (!profile) return "choose-role";

    // Identity before anything else — a shopkeeper needs to reach a person, and
    // a shopper needs to be reachable when their order is ready. The fork comes
    // first only because its answer picks the wording on that screen.
    if (!profile.profileComplete) {
      return roleIntent || profile.isMerchant ? "complete-profile" : "choose-role";
    }

    // The fork is the login question, and the only place we ask it. Someone who
    // runs a shop and also shops answers it here rather than hunting for a
    // setting later.
    if (!roleIntent) return "choose-role";

    if (roleIntent === "merchant" && !profile.hasStore) return "setup-shop";
    return "done";
  }, [profile, roleIntent]);

  // Which tabs you see follows straight from the fork. Picking "shopper" as a
  // shopkeeper is allowed; claiming the shop side without a shop is not.
  const mode: Mode = useMemo(
    () =>
      roleIntent === "merchant" && profile?.isMerchant && profile?.hasStore
        ? "merchant"
        : "customer",
    [roleIntent, profile]
  );

  const value = useMemo<AuthContextValue>(
    () => ({
      booting,
      signedIn: !!profile,
      profile,
      email: profile?.email ?? null,
      store,
      mode,
      onboardingStep,
      roleIntent,
      chooseRole,
      clearRoleIntent,
      refresh: async () => {
        await load();
      },
      signIn,
      signOut,
      merchantProfile:
        profile?.merchantId != null
          ? { merchantId: profile.merchantId, bharosaScore: profile.bharosaScore ?? 0 }
          : null,
      refreshMerchantState: async () => {
        await load();
      },
    }),
    [booting, profile, store, mode, onboardingStep, roleIntent, chooseRole, clearRoleIntent, load, signIn, signOut]
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}
