import React, { useRef, useState } from "react";
import { Pressable, ScrollView, StyleSheet, View } from "react-native";
import { useNavigation } from "@react-navigation/native";
import * as Location from "expo-location";
import { AppBar } from "../../components/AppBar";
import { Card } from "../../components/Card";
import { Text } from "../../components/Text";
import { Button } from "../../components/Button";
import { TextField } from "../../components/TextField";
import { useToast } from "../../components/Toast";
import { MenuPhoto, MenuPhotoPicker } from "../../components/MenuPhotoPicker";
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
 * Restaurants are the exception: what a customer needs from one is its menu,
 * and a photo of the printed card answers that with nothing to build.
 *
 * The rest are shown rather than hidden so a shopkeeper can see their category
 * is coming, and so we learn which ones people reach for.
 */
const TYPES: { key: BusinessType; label: string; emoji: string; live: boolean }[] = [
  { key: "KIRANA", label: "Kirana", emoji: "🏪", live: true },
  { key: "RESTAURANT", label: "Restaurant", emoji: "🍽️", live: true },
  { key: "GENERAL_STORE", label: "General", emoji: "📦", live: false },
  { key: "SUPERMARKET", label: "Supermarket", emoji: "🛒", live: false },
  { key: "PHARMACY", label: "Pharmacy", emoji: "💊", live: false },
  { key: "STATIONERY", label: "Stationery", emoji: "✏️", live: false },
];

/** "12, MG Road, Camp" from whichever parts the geocoder actually returned. */
function streetLine(a: Location.LocationGeocodedAddress) {
  const parts = [a.streetNumber, a.street, a.district].filter(
    (p, i, all): p is string => !!p && all.indexOf(p) === i
  );
  return parts.length ? parts.join(", ") : a.name ?? "";
}

export function BecomeMerchantScreen() {
  const nav = useNavigation<any>();
  const toast = useToast();
  const { merchantProfile, refreshMerchantState } = useAuth();
  // Not asked on arrival: location is one optional button on this page, so
  // nobody meets a permission dialog before they know why it's there.
  const { coords, status: locationStatus, request: requestLocation } = useLocation(false);

  const [name, setName] = useState("");
  const [businessType, setBusinessType] = useState<BusinessType>("KIRANA");
  const [addressLine1, setAddressLine1] = useState("");
  const [city, setCity] = useState("");
  const [stateName, setStateName] = useState("");
  const [postalCode, setPostalCode] = useState("");
  const [menuPhotos, setMenuPhotos] = useState<MenuPhoto[]>([]);
  const [filling, setFilling] = useState(false);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState<string | null>(null);
  // Registering as a merchant can succeed while creating the shop then fails
  // (an address the map can't find); a retry must not register twice.
  const registered = useRef(!!merchantProfile);

  const isRestaurant = businessType === "RESTAURANT";
  // Coordinates are optional: without them the server finds the typed address.
  const valid =
    !!name.trim() && !!addressLine1.trim() && !!city.trim() && !!stateName.trim() && !!postalCode.trim();

  async function pinCurrentLocation() {
    const here = await requestLocation();
    if (!here) return;

    setFilling(true);
    try {
      const [found] = await Location.reverseGeocodeAsync(here);
      if (!found) throw new Error("no address");
      const street = streetLine(found);
      if (street) setAddressLine1(street);
      if (found.city ?? found.subregion) setCity((found.city ?? found.subregion)!);
      if (found.region) setStateName(found.region);
      if (found.postalCode) setPostalCode(found.postalCode.replace(/[^0-9]/g, ""));
    } catch {
      // The pin still counts; only the auto-filled text is missing.
      toast("Pinned your location. Type the address below.", "info");
    } finally {
      setFilling(false);
    }
  }

  function addPhotos(uris: string[]) {
    setMenuPhotos((prev) => [
      ...prev,
      ...uris.map((uri, i) => ({ key: `${Date.now()}-${i}`, uri })),
    ]);
  }

  async function submit() {
    setBusy(true);
    setError(null);
    try {
      if (!registered.current) {
        await MerchantApi.register();
        registered.current = true;
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
        latitude: coords?.latitude ?? null,
        longitude: coords?.longitude ?? null,
      });

      // The shop exists now, so menu pages have somewhere to go. One failed
      // page shouldn't undo the shop: report it and let the Shop tab retry.
      let failed = 0;
      if (isRestaurant) {
        for (const photo of menuPhotos) {
          try {
            await StoreApi.uploadMenuImage(photo.uri);
          } catch {
            failed++;
          }
        }
      }

      await refreshMerchantState();
      if (failed) {
        toast(
          `Your shop is live, but ${failed} menu page${failed === 1 ? "" : "s"} didn't upload. Add ${failed === 1 ? "it" : "them"} from the Shop tab.`,
          "attention"
        );
      } else {
        toast("Your shop is live 🎉", "positive");
      }
      // During onboarding this is the only screen: refreshing the merchant
      // state above swaps the navigator to the shop's tabs by itself.
      if (nav.canGoBack()) nav.goBack();
    } catch (e) {
      setError(
        e instanceof ApiError
          ? e.fieldErrors[0]?.message ?? e.message
          : "Couldn't set up your shop."
      );
      setBusy(false);
    }
  }

  const pinned = locationStatus === "granted" && !!coords;

  return (
    <View style={styles.flex}>
      <AppBar
        title="Set up your shop"
        subtitle="Start getting requests from nearby customers"
      />
      <ScrollView
        contentContainerStyle={[styles.content, contentWidth.column]}
        keyboardShouldPersistTaps="handled"
        showsVerticalScrollIndicator={false}
      >
        <Card elevated>
          <TextField
            label="Shop name"
            placeholder={isRestaurant ? "e.g. Sharma Bhojnalaya" : "e.g. Sharma Kirana Store"}
            value={name}
            onChangeText={setName}
          />

          <Text variant="caption" color={color.neutral.inkMuted} style={styles.typeLabel}>
            What kind of shop?
          </Text>
          <Text variant="bodySm" color={color.neutral.inkFaint} style={styles.typeNote}>
            We're starting with kirana shops and restaurants, and doing them
            properly. Other kinds are on the way.
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

          {isRestaurant ? (
            <View style={styles.menuSection}>
              <Text variant="caption" color={color.neutral.inkMuted}>
                Menu card
              </Text>
              <Text variant="bodySm" color={color.neutral.inkFaint}>
                Add a photo of each page. Customers see these when they tap your
                restaurant. Optional — you can add them later from the Shop tab.
              </Text>
              <MenuPhotoPicker
                photos={menuPhotos}
                onAdd={addPhotos}
                onRemove={(key) => setMenuPhotos((prev) => prev.filter((p) => p.key !== key))}
                busy={busy}
              />
            </View>
          ) : null}
        </Card>

        <Card elevated>
          <Text variant="caption" color={color.neutral.inkMuted}>
            Where is your shop?
          </Text>

          {pinned ? (
            <View style={styles.pinnedRow}>
              <Text variant="bodySm" color={color.status.positive.strong} style={styles.grow}>
                ✓ Pinned to where you're standing. Check the address below.
              </Text>
              <Pressable
                onPress={pinCurrentLocation}
                hitSlop={8}
                accessibilityRole="button"
                disabled={filling}
              >
                <Text variant="bodySm" weight="semibold" color={color.brand[600]}>
                  Re-pin
                </Text>
              </Pressable>
            </View>
          ) : (
            <>
              <Text variant="bodySm" color={color.neutral.inkFaint}>
                {locationStatus === "denied" || locationStatus === "error"
                  ? "No problem — type your address below and we'll find it on the map."
                  : "Standing in your shop? Tap below and we'll fill in the address. Or just type it."}
              </Text>
              <Button
                label="📍  Use my current location"
                variant="secondary"
                onPress={pinCurrentLocation}
                loading={locationStatus === "loading" || filling}
                style={styles.locationBtn}
              />
            </>
          )}

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

        {error ? (
          <Text variant="bodySm" color={color.status.urgent.strong}>
            {error}
          </Text>
        ) : null}

        <Button
          label={busy && isRestaurant && menuPhotos.length ? "Opening shop, uploading menu…" : "Open my shop"}
          onPress={submit}
          loading={busy}
          disabled={!valid || filling}
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
  menuSection: { marginTop: spacing.md, gap: 2 },
  row: { flexDirection: "row", gap: spacing.xs },
  flexHalf: { flex: 1 },
  grow: { flex: 1 },
  pinnedRow: { flexDirection: "row", alignItems: "center", gap: spacing.sm },
  locationBtn: { marginTop: spacing.xs, marginBottom: spacing.xs },
});
