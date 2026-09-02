import React, { useCallback, useState } from "react";
import { ActivityIndicator, Pressable, ScrollView, StyleSheet, View } from "react-native";
import { useNavigation, useRoute } from "@react-navigation/native";
import { AppBar } from "../../components/AppBar";
import { Card } from "../../components/Card";
import { Text } from "../../components/Text";
import { Button } from "../../components/Button";
import { EmptyState } from "../../components/EmptyState";
import { BharosaBadge } from "../../components/BharosaBadge";
import { ReputationBanner } from "../../components/ReputationBanner";
import { DiscoveryApi } from "../../api/endpoints";
import { NearbyStore } from "../../api/types";
import { useLocation } from "../../state/useLocation";
import { color, radius, spacing } from "../../theme/tokens";
import { contentWidth } from "../../theme/layoutStyles";

/**
 * Choose specific shops instead of broadcasting to everyone nearby.
 *
 * The order here is the ranking blend, not raw distance — so the shops a
 * shopper sees first are the ones most likely to actually come through, which
 * is the whole point of computing Bharosa in the first place.
 */
export function PickShopsScreen() {
  const nav = useNavigation<any>();
  const route = useRoute<any>();
  const { selected: initial = [] } = (route.params ?? {}) as { selected?: string[] };

  const { coords, status: locationStatus, request: requestLocation } = useLocation();
  const [stores, setStores] = useState<NearbyStore[] | null>(null);
  const [selected, setSelected] = useState<string[]>(initial);

  const load = useCallback(async () => {
    if (!coords) return;
    try {
      setStores(await DiscoveryApi.nearby(coords.latitude, coords.longitude, 5000));
    } catch {
      setStores([]);
    }
  }, [coords]);

  React.useEffect(() => {
    load();
  }, [load]);

  function toggle(storeId: string) {
    setSelected((prev) =>
      prev.includes(storeId) ? prev.filter((id) => id !== storeId) : [...prev, storeId]
    );
  }

  return (
    <View style={styles.flex}>
      <AppBar
        title="Choose shops"
        subtitle="Ranked by how reliably they deliver"
        onBack={() => nav.goBack()}
      />
      <ScrollView
        contentContainerStyle={[styles.content, contentWidth.column]}
        showsVerticalScrollIndicator={false}
      >
        {locationStatus === "denied" || locationStatus === "error" ? (
          <Card elevated style={styles.locationCard}>
            <Text variant="h3">We need your location</Text>
            <Text variant="bodySm" color={color.neutral.inkMuted}>
              To show shops near you and how far each one is.
            </Text>
            <Button label="Allow location" variant="secondary" onPress={requestLocation} />
          </Card>
        ) : stores === null ? (
          <ActivityIndicator style={styles.loader} color={color.brand[500]} />
        ) : stores.length === 0 ? (
          <EmptyState
            emoji="🏪"
            title="No shops nearby yet"
            body="StockUp works best once a few shops in your area have joined."
          />
        ) : (
          stores.map((s) => {
            const on = selected.includes(s.storeId);
            return (
              <Card
                key={s.storeId}
                elevated
                onPress={() => toggle(s.storeId)}
                style={on ? styles.picked : undefined}
              >
                {s.tags?.length ? <ReputationBanner tags={s.tags} /> : null}

                <View style={styles.rowBetween}>
                  <Text variant="h3" style={styles.name}>
                    {s.name}
                  </Text>
                  <View style={[styles.check, on && styles.checkOn]}>
                    {on ? <Text style={styles.tick}>✓</Text> : null}
                  </View>
                </View>

                <Text variant="bodySm" color={color.neutral.inkMuted}>
                  {s.businessType.replace("_", " ").toLowerCase()} · {s.distanceKm} km away
                </Text>

                <BharosaBadge score={s.bharosa} band={s.band} />
              </Card>
            );
          })
        )}
      </ScrollView>

      {stores && stores.length > 0 ? (
        <View style={styles.footer}>
          <Button
            label={
              selected.length === 0
                ? "Pick at least one shop"
                : `Ask ${selected.length} shop${selected.length === 1 ? "" : "s"}`
            }
            disabled={selected.length === 0}
            onPress={() => nav.navigate("CreateBasket", { storeIds: selected })}
          />
        </View>
      ) : null}
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
  rowBetween: {
    flexDirection: "row",
    alignItems: "center",
    justifyContent: "space-between",
    gap: spacing.xs,
  },
  name: { flex: 1 },
  picked: {
    borderColor: color.brand[500],
    borderWidth: 1.5,
  },
  check: {
    width: 24,
    height: 24,
    borderRadius: 12,
    borderWidth: 1.5,
    borderColor: color.neutral.border,
    alignItems: "center",
    justifyContent: "center",
  },
  checkOn: {
    backgroundColor: color.brand[500],
    borderColor: color.brand[500],
  },
  tick: { color: "#fff", fontSize: 13, fontWeight: "700" },
  locationCard: {
    borderColor: color.status.attention.base,
    borderWidth: 1.5,
    gap: spacing.xs,
  },
  footer: {
    padding: spacing.lg,
    paddingTop: spacing.sm,
    borderTopWidth: 1,
    borderTopColor: color.neutral.border,
    backgroundColor: color.neutral.background,
  },
  loader: { marginTop: spacing.xxl },
});
