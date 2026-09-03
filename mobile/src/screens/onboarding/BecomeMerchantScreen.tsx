import React, { useState } from "react";
import { Pressable, ScrollView, StyleSheet, View } from "react-native";
import { useNavigation } from "@react-navigation/native";
import { AppBar } from "../../components/AppBar";
import { Card } from "../../components/Card";
import { Text } from "../../components/Text";
import { Button } from "../../components/Button";
import { TextField } from "../../components/TextField";
import { useToast } from "../../components/Toast";
import { MerchantApi, StoreApi } from "../../api/endpoints";
import { BusinessType } from "../../api/types";
import { useAuth } from "../../state/AuthContext";
import { color, font, radius, spacing } from "../../theme/tokens";
import { contentWidth } from "../../theme/layoutStyles";
import { useLocation } from "../../state/useLocation";
import { ApiError } from "../../api/client";

/**
 * StockUp is a kirana app first, on purpose. Every other category needs its own
 * vocabulary before it works — a pharmacy needs prescriptions and controlled
 * substances, stationery needs brand-and-size matching. Shipping them as
 * selectable options would mean shipping a worse version of each.
 *
 * They are shown rather than hidden so a shopkeeper can see their category is
 * coming, and so we learn which ones people reach for.
 */
const TYPES: { key: BusinessType; label: string; emoji: string; live: boolean }[] = [
  { key: "KIRANA", label: "Kirana", emoji: "🏪", live: true },
  { key: "GENERAL_STORE", label: "General", emoji: "📦", live: false },
  { key: "SUPERMARKET", label: "Supermarket", emoji: "🛒", live: false },
  { key: "PHARMACY", label: "Pharmacy", emoji: "💊", live: false },
  { key: "STATIONERY", label: "Stationery", emoji: "✏️", live: false },
];

export function BecomeMerchantScreen() {
  const nav = useNavigation<any>();
  const toast = useToast();
  const { merchantProfile, refreshMerchantState, clearRoleIntent } = useAuth();
  const { coords, status: locationStatus, request: requestLocation } = useLocation();

  const [name, setName] = useState("");
  const [businessType, setBusinessType] = useState<BusinessType>("KIRANA");
  const [addressLine1, setAddressLine1] = useState("");
  const [city, setCity] = useState("");
  const [stateName, setStateName] = useState("");
  const [postalCode, setPostalCode] = useState("");
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const detailsFilled =
    name.trim() && addressLine1.trim() && city.trim() && stateName.trim() && postalCode.trim();
  const valid = !!detailsFilled && !!coords;

  async function submit() {
    if (!coords) return;
    setBusy(true);
    setError(null);
    try {
      if (!merchantProfile) {
        await MerchantApi.register();
      }
      await StoreApi.create({
        name: name.trim(),
        businessType,
        addressLine1: addressLine1.trim(),
        addressLine2: null,
        city: city.trim(),
        state: stateName.trim(),
        postalCode: postalCode.trim(),
        country: "India",
        latitude: coords.latitude,
        longitude: coords.longitude,
      });
      await refreshMerchantState();
      toast("Your shop is live 🎉", "positive");
      nav.goBack();
    } catch (e) {
      setError(e instanceof ApiError ? e.message : "Couldn't set up your shop.");
      setBusy(false);
    }
  }

  return (
    <View style={styles.flex}>
      <AppBar
        title="Set up your shop"
        subtitle="Start getting requests from nearby customers"
        // During onboarding this is the only screen in the stack, so goBack()
        // would silently do nothing — a dead control is worse than none. Fall
        // back to returning to the role fork.
        onBack={() => (nav.canGoBack() ? nav.goBack() : clearRoleIntent())}
      />
      <ScrollView contentContainerStyle={[styles.content, contentWidth.column]} showsVerticalScrollIndicator={false}>
        <Card elevated>
          <TextField
            label="Shop name"
            placeholder="e.g. Sharma Kirana Store"
            value={name}
            onChangeText={setName}
          />

          <Text variant="caption" color={color.neutral.inkMuted} style={styles.typeLabel}>
            What kind of shop?
          </Text>
          <Text variant="bodySm" color={color.neutral.inkFaint} style={styles.typeNote}>
            We're starting with kirana shops and doing them properly. Other kinds
            are on the way.
          </Text>
          <View style={styles.types}>
            {TYPES.map((t) => {
              const active = businessType === t.key;
              return (
                <Pressable
                  key={t.key}
                  onPress={() =>
                    t.live
                      ? setBusinessType(t.key)
                      : toast(`${t.label} shops are coming soon.`, "info")
                  }
                  accessibilityRole="button"
                  accessibilityState={{ selected: active, disabled: !t.live }}
                  accessibilityLabel={
                    t.live ? t.label : `${t.label} — coming soon, not yet available`
                  }
                  style={[
                    styles.typeChip,
                    active && styles.typeChipActive,
                    !t.live && styles.typeChipSoon,
                  ]}
                >
                  <Text style={[styles.typeEmoji, !t.live && styles.dim]}>{t.emoji}</Text>
                  <Text
                    variant="bodySm"
                    weight="semibold"
                    color={
                      active ? "#fff" : t.live ? color.neutral.inkMuted : color.neutral.inkFaint
                    }
                  >
                    {t.label}
                  </Text>
                  {!t.live ? <Text style={styles.soonTag}>SOON</Text> : null}
                </Pressable>
              );
            })}
          </View>
        </Card>

        <Card elevated>
          <TextField
            label="Street address"
            placeholder="12 MG Road"
            value={addressLine1}
            onChangeText={setAddressLine1}
          />
          <TextField label="City" placeholder="Pune" value={city} onChangeText={setCity} />
          <View style={styles.row}>
            <View style={styles.flexHalf}>
              <TextField
                label="State"
                placeholder="Maharashtra"
                value={stateName}
                onChangeText={setStateName}
              />
            </View>
            <View style={styles.flexHalf}>
              <TextField
                label="PIN code"
                placeholder="411001"
                value={postalCode}
                onChangeText={(t) => setPostalCode(t.replace(/[^0-9]/g, ""))}
                keyboardType="number-pad"
              />
            </View>
          </View>
        </Card>

        {locationStatus === "denied" || locationStatus === "error" ? (
          <Card elevated style={styles.locationCard}>
            <Text variant="h3">Stand in your shop</Text>
            <Text variant="bodySm" color={color.neutral.inkMuted}>
              We pin your shop to where you are right now, so nearby customers can
              find you. Allow location while you're at the shop.
            </Text>
            <Button
              label="Allow location"
              variant="secondary"
              onPress={requestLocation}
              style={styles.locationBtn}
            />
          </Card>
        ) : null}

        {error ? (
          <Text variant="bodySm" color={color.status.urgent.strong}>
            {error}
          </Text>
        ) : null}

        <Button
          label={locationStatus === "loading" ? "Finding your shop…" : "Open my shop"}
          onPress={submit}
          loading={busy}
          disabled={!valid}
        />
      </ScrollView>
    </View>
  );
}

const styles = StyleSheet.create({
  flex: { flex: 1, backgroundColor: color.neutral.background },
  content: {
    padding: spacing.lg,
    paddingTop: 0,
    gap: spacing.sm,
    paddingBottom: spacing.xxxl,
  },
  typeLabel: { marginTop: spacing.xs },
  types: {
    flexDirection: "row",
    flexWrap: "wrap",
    gap: spacing.xs,
    marginTop: 6,
  },
  typeChip: {
    flexDirection: "row",
    alignItems: "center",
    gap: 6,
    paddingHorizontal: spacing.md,
    height: 44,
    borderRadius: radius.pill,
    borderWidth: 1,
    borderColor: color.neutral.border,
    backgroundColor: color.neutral.surface,
  },
  typeChipActive: {
    backgroundColor: color.brand[500],
    borderColor: color.brand[500],
  },
  typeChipSoon: {
    borderStyle: "dashed",
    backgroundColor: "transparent",
  },
  dim: { opacity: 0.45 },
  soonTag: {
    fontFamily: font.body.bold,
    fontSize: 9,
    letterSpacing: 0.6,
    lineHeight: 12,
    color: color.neutral.inkFaint,
    borderWidth: 1,
    borderColor: color.neutral.border,
    borderRadius: radius.pill,
    paddingHorizontal: 5,
    paddingVertical: 2,
  },
  typeNote: { marginTop: 2 },
  typeEmoji: { fontSize: 15, lineHeight: 21 },
  row: { flexDirection: "row", gap: spacing.xs },
  flexHalf: { flex: 1 },
  locationCard: {
    borderColor: color.status.attention.base,
    borderWidth: 1.5,
  },
  locationBtn: { marginTop: spacing.xs },
});
