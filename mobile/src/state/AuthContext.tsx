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
  /** Answer the one-time fork. Permanent; the server rejects a second call. */
  chooseRole: (role: Mode) => Promise<void>;

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
    await AuthApi.chooseRole(role === "merchant" ? "MERCHANT" : "CUSTOMER");
    await load();
  }, [load]);


  const signIn = useCallback(
    async (emailInput: string, otp: string) => {
      const res = await AuthApi.verifyOtp(emailInput, otp);
      await setTokens(res.accessToken, res.refreshToken);
      const me = await load();
      return res.newUser;
    },
    [load]
  );

  const signOut = useCallback(async () => {
    await clearTokens();
    setProfile(null);
    setStore(null);
  }, []);

  useEffect(() => {
    setSessionExpiredHandler(() => {
      setProfile(null);
      setStore(null);
    });
    return () => setSessionExpiredHandler(null);
  }, []);

  /**
   * One place decides what comes next, so no screen has to reason about
   * onboarding order itself.
   */
  /**
   * One place decides what comes next. The role is answered once and lives on
   * the server, so there is nothing local that can disagree with it.
   */
  const onboardingStep: OnboardingStep = useMemo(() => {
    if (!profile) return "choose-role";
    if (!profile.role) return "choose-role";
    if (!profile.profileComplete) return "complete-profile";
    if (profile.role === "MERCHANT" && !profile.hasStore) return "setup-shop";
    return "done";
  }, [profile]);

  // Which side of the app you see follows straight from the account's role.
  const mode: Mode = useMemo(
    () => (profile?.role === "MERCHANT" && profile?.hasStore ? "merchant" : "customer"),
    [profile]
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
      chooseRole,
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
    [booting, profile, store, mode, onboardingStep, chooseRole, load, signIn, signOut]
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}
