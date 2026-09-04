import React, { useState } from "react";
import { ScrollView, StyleSheet, View } from "react-native";
import { useSafeAreaInsets } from "react-native-safe-area-context";
import { Text } from "../../components/Text";
import { Card } from "../../components/Card";
import { Button } from "../../components/Button";
import { TextField } from "../../components/TextField";
import { FadeIn, Pulse } from "../../components/Motion";
import { AuthApi } from "../../api/endpoints";
import { ApiError } from "../../api/client";
import { useAuth } from "../../state/AuthContext";
import { useLocation } from "../../state/useLocation";
import { color, font, radius, spacing } from "../../theme/tokens";
import { contentWidth } from "../../theme/layoutStyles";

/**
 * Two steps in one screen: where you are, then who you are.
 *
 * Location is asked first and on its own, with the reason stated before the
 * system dialog appears — a permission prompt with no explanation in front of
 * it is the most common way to get denied permanently.
 */
export function CompleteProfileScreen() {
  const insets = useSafeAreaInsets();
  const { profile, refresh } = useAuth();
  const { coords, status: locationStatus, request: requestLocation } = useLocation(false);

  const [firstName, setFirstName] = useState(profile?.firstName ?? "");
  const [lastName, setLastName] = useState(profile?.lastName ?? "");
  const [phone, setPhone] = useState(profile?.phone ?? "");
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const locationDone = locationStatus === "granted" && !!coords;
  const detailsValid = firstName.trim().length > 0 && phone.trim().length >= 7;

  async function save() {
    setBusy(true);
    setError(null);
    try {
      await AuthApi.updateProfile({
        firstName: firstName.trim(),
        lastName: lastName.trim() || null,
        phone: phone.trim(),
      });
      await refresh();
    } catch (e) {
      setError(e instanceof ApiError ? e.message : "Couldn't save that. Try again.");
      setBusy(false);
    }
  }

  return (
    <View style={[styles.flex, { paddingTop: insets.top + spacing.lg }]}>
      <ScrollView
        contentContainerStyle={[styles.content, contentWidth.column]}
        keyboardShouldPersistTaps="handled"
        showsVerticalScrollIndicator={false}
      >
        <FadeIn>

          <Text variant="h1">
            {profile?.role === "MERCHANT" ? "Let's set you up" : "Nearly there"}
          </Text>
          <Text variant="body" color={color.neutral.inkMuted} style={styles.sub}>
            Two quick things, then you're in.
          </Text>
        </FadeIn>

        {/* Step 1 — location */}
        <FadeIn index={1}>
          <Card elevated style={locationDone ? styles.stepDone : undefined}>
            <View style={styles.stepHead}>
              <View style={[styles.stepDot, locationDone && styles.stepDotDone]}>
                <Text style={styles.stepDotText}>{locationDone ? "✓" : "1"}</Text>
              </View>
              <Text variant="h3" style={styles.stepTitle}>
                {locationDone ? "Location shared" : "Share your location"}
              </Text>
            </View>

            {locationDone ? (
              <Text variant="bodySm" color={color.neutral.inkMuted}>
                We'll show shops near you and how far each one is.
              </Text>
            ) : (
              <>
                <Text variant="bodySm" color={color.neutral.inkMuted}>
                  {profile?.role === "MERCHANT"
                    ? "So customers nearby can find your shop. Do this while you're at the shop."
                    : "So we can ask the shops closest to you — nothing is shared with them beyond the search area."}
                </Text>

                {locationStatus === "denied" ? (
                  <Text variant="bodySm" color={color.status.attention.strong}>
                    Permission was declined. You can enable it in Settings, then tap again.
                  </Text>
                ) : null}

                <Button
                  label={locationStatus === "loading" ? "Finding you…" : "Allow location"}
                  variant="secondary"
                  onPress={requestLocation}
                  loading={locationStatus === "loading"}
                  style={styles.stepBtn}
                />
              </>
            )}
          </Card>
        </FadeIn>

        {/* Step 2 — details */}
        <FadeIn index={2}>
          <Card elevated>
            <View style={styles.stepHead}>
              <View style={[styles.stepDot, detailsValid && styles.stepDotDone]}>
                <Text style={styles.stepDotText}>{detailsValid ? "✓" : "2"}</Text>
              </View>
              <Text variant="h3" style={styles.stepTitle}>
                Who are you?
              </Text>
            </View>
            <Text variant="bodySm" color={color.neutral.inkMuted}>
              {profile?.role === "MERCHANT"
                ? "Customers see your name when they reserve with you."
                : "The shop needs a name and number to hand your order to."}
            </Text>

            <View style={styles.nameRow}>
              <View style={styles.grow}>
                <TextField
                  label="First name"
                  placeholder="Harsh"
                  value={firstName}
                  onChangeText={setFirstName}
                  autoCapitalize="words"
                />
              </View>
              <View style={styles.grow}>
                <TextField
                  label="Last name"
                  placeholder="optional"
                  value={lastName}
                  onChangeText={setLastName}
                  autoCapitalize="words"
                />
              </View>
            </View>

            <TextField
              label="Phone"
              placeholder="+91 98765 43210"
              value={phone}
              onChangeText={setPhone}
              keyboardType="phone-pad"
            />
          </Card>
        </FadeIn>

        {error ? (
          <Text variant="bodySm" color={color.status.urgent.strong}>
            {error}
          </Text>
        ) : null}

        <FadeIn index={3}>
          <Button
            label={profile?.role === "MERCHANT" ? "Next: your shop" : "Start shopping"}
            onPress={save}
            loading={busy}
            disabled={!detailsValid}
          />
          {!locationDone ? (
            <Pulse style={styles.hintWrap}>
              <Text variant="bodySm" color={color.neutral.inkFaint} style={styles.hint}>
                You can skip location for now, but "nearby" won't work without it.
              </Text>
            </Pulse>
          ) : null}
        </FadeIn>
      </ScrollView>
    </View>
  );
}

const styles = StyleSheet.create({
  flex: { flex: 1, backgroundColor: color.neutral.background },
  content: {
    paddingHorizontal: spacing.lg,
    paddingBottom: spacing.xxxl,
    gap: spacing.sm,
  },
  sub: { marginBottom: spacing.sm },
  stepHead: { flexDirection: "row", alignItems: "center", gap: spacing.xs },
  stepTitle: { flex: 1 },
  stepDot: {
    width: 24,
    height: 24,
    borderRadius: 12,
    backgroundColor: color.neutral.surfaceMuted,
    alignItems: "center",
    justifyContent: "center",
  },
  stepDotDone: { backgroundColor: color.status.positive.base },
  stepDotText: {
    fontFamily: font.body.bold,
    fontSize: 12,
    color: color.neutral.surface,
  },
  stepDone: { borderColor: color.status.positive.base },
  stepBtn: { marginTop: spacing.xs },
  nameRow: { flexDirection: "row", gap: spacing.xs, marginTop: spacing.xs },
  grow: { flex: 1 },
  hintWrap: { marginTop: spacing.sm },
  hint: { textAlign: "center" },
});
