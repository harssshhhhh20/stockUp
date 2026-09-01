import React, { createContext, useCallback, useContext, useEffect, useMemo, useState } from "react";
import { AuthApi } from "../api/endpoints";
import {
  api,
  clearTokens,
  getAccessToken,
  setSessionExpiredHandler,
  setTokens,
} from "../api/client";
import { MerchantProfileResponse, StoreResponse } from "../api/types";

type Mode = "customer" | "merchant";

type AuthContextValue = {
  booting: boolean;
  signedIn: boolean;
  email: string | null;
  mode: Mode;
  setMode: (m: Mode) => void;
  merchantProfile: MerchantProfileResponse | null;
  store: StoreResponse | null;
  refreshMerchantState: () => Promise<void>;
  signIn: (email: string, otp: string) => Promise<boolean>;
  signOut: () => Promise<void>;
};

const AuthContext = createContext<AuthContextValue | null>(null);

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error("useAuth must be used within AuthProvider");
  return ctx;
}

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [booting, setBooting] = useState(true);
  const [email, setEmail] = useState<string | null>(null);
  const [mode, setMode] = useState<Mode>("customer");
  const [merchantProfile, setMerchantProfile] = useState<MerchantProfileResponse | null>(null);
  const [store, setStore] = useState<StoreResponse | null>(null);

  const loadMerchantState = useCallback(async () => {
    try {
      const [profile, myStore] = await Promise.all([
        api.get<MerchantProfileResponse | null>("/api/v1/merchant/me"),
        api.get<StoreResponse | null>("/api/v1/stores/me"),
      ]);
      setMerchantProfile(profile);
      setStore(myStore);
      if (profile) setMode("merchant");
    } catch {
      setMerchantProfile(null);
      setStore(null);
    }
  }, []);

  useEffect(() => {
    (async () => {
      const token = await getAccessToken();
      if (token) {
        try {
          const who = await AuthApi.me();
          setEmail(who);
          await loadMerchantState();
        } catch {
          await clearTokens();
        }
      }
      setBooting(false);
    })();
  }, [loadMerchantState]);

  const signIn = useCallback(
    async (emailInput: string, otp: string) => {
      const res = await AuthApi.verifyOtp(emailInput, otp);
      await setTokens(res.accessToken, res.refreshToken);
      setEmail(emailInput);
      await loadMerchantState();
      return res.newUser;
    },
    [loadMerchantState]
  );

  const signOut = useCallback(async () => {
    await clearTokens();
    setEmail(null);
    setMerchantProfile(null);
    setStore(null);
    setMode("customer");
  }, []);

  // When a refresh fails the session is genuinely over — drop back to sign-in
  // rather than leaving the user on a screen whose requests all fail.
  useEffect(() => {
    setSessionExpiredHandler(() => {
      setEmail(null);
      setMerchantProfile(null);
      setStore(null);
      setMode("customer");
    });
    return () => setSessionExpiredHandler(null);
  }, []);

  const value = useMemo<AuthContextValue>(
    () => ({
      booting,
      signedIn: !!email,
      email,
      mode,
      setMode,
      merchantProfile,
      store,
      refreshMerchantState: loadMerchantState,
      signIn,
      signOut,
    }),
    [booting, email, mode, merchantProfile, store, loadMerchantState, signIn, signOut]
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}
