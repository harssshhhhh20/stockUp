import React, { useState } from "react";
import { KeyboardAvoidingView, Platform, StyleSheet, View } from "react-native";
import { Screen } from "../../components/Screen";
import { Text } from "../../components/Text";
import { Button } from "../../components/Button";
import { TextField } from "../../components/TextField";
import { OtpInput } from "../../components/OtpInput";
import { Logo } from "../../components/Logo";
import { AuthApi } from "../../api/endpoints";
import { useAuth } from "../../state/AuthContext";
import { useToast } from "../../components/Toast";
import { color, spacing } from "../../theme/tokens";
import { ApiError } from "../../api/client";

export function SignInScreen() {
  const [step, setStep] = useState<"email" | "otp">("email");
  const [email, setEmail] = useState("");
  const [otp, setOtp] = useState("");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const { signIn } = useAuth();
  const toast = useToast();

  const emailValid = /^[^@\s]+@[^@\s]+\.[^@\s]+$/.test(email.trim());

  async function sendOtp() {
    setLoading(true);
    setError(null);
    try {
      await AuthApi.requestOtp(email.trim());
      setStep("otp");
      toast("Code sent — it can take a few minutes", "info");
    } catch (e) {
      setError(e instanceof ApiError ? e.message : "Couldn't send the code. Try again.");
    } finally {
      setLoading(false);
    }
  }

  async function verify() {
    setLoading(true);
    setError(null);
    try {
      await signIn(email.trim(), otp);
    } catch (e) {
      setError(
        e instanceof ApiError
          ? "That code didn't work. Check it and try again."
          : "Something went wrong. Try again."
      );
      setLoading(false);
    }
  }

  return (
    <Screen scroll={false} padded={false}>
      <KeyboardAvoidingView
        behavior={Platform.OS === "ios" ? "padding" : undefined}
        style={styles.flex}
      >
        <View style={styles.content}>
          <View style={styles.header}>
            <Logo size={54} />
            <Text variant="hero" style={styles.title}>
              {step === "email" ? "Your street, stocked." : "Check your email"}
            </Text>
            <Text variant="body" color={color.neutral.inkMuted} style={styles.sub}>
              {step === "email"
                ? "Ask nearby shops what's in stock — before you walk over."
                : `We sent a 6-digit code to ${email.trim()}`}
            </Text>
          </View>

          {step === "email" ? (
            <View style={styles.form}>
              <TextField
                label="Email"
                placeholder="you@example.com"
                value={email}
                onChangeText={setEmail}
                autoCapitalize="none"
                keyboardType="email-address"
                autoComplete="email"
                error={error ?? undefined}
                onSubmitEditing={() => emailValid && sendOtp()}
              />
              <Button
                label="Send code"
                onPress={sendOtp}
                loading={loading}
                disabled={!emailValid}
              />
            </View>
          ) : (
            <View style={styles.form}>
              <OtpInput value={otp} onChange={setOtp} />
              {error ? (
                <Text variant="bodySm" color={color.status.urgent.strong}>
                  {error}
                </Text>
              ) : null}
              {/* Delivery on the free email tier can genuinely take minutes.
                  Saying so beats letting someone stare at an empty inbox and
                  conclude the app is broken. */}
              <Text variant="bodySm" color={color.neutral.inkFaint} style={styles.deliveryNote}>
                It can take a few minutes to arrive — check spam too. The code
                stays valid for 20 minutes.
              </Text>

              <Button
                label="Verify & continue"
                onPress={verify}
                loading={loading}
                disabled={otp.length !== 6}
              />
              <Button
                label="Use a different email"
                variant="ghost"
                onPress={() => {
                  setStep("email");
                  setOtp("");
                  setError(null);
                }}
              />
            </View>
          )}
        </View>
      </KeyboardAvoidingView>
    </Screen>
  );
}

const styles = StyleSheet.create({
  deliveryNote: { textAlign: "center", marginTop: -2 },
  flex: { flex: 1 },
  content: {
    flex: 1,
    justifyContent: "center",
    padding: spacing.xl,
    gap: spacing.xxl,
  },
  header: {
    gap: spacing.xs,
    alignItems: "flex-start",
  },
  title: {
    marginTop: spacing.md,
  },
  sub: {
    maxWidth: 300,
  },
  form: {
    gap: spacing.md,
  },
});
