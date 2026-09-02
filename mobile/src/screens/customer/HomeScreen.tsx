import React, { useCallback, useState } from "react";
import { ActivityIndicator, RefreshControl, ScrollView, StyleSheet, View } from "react-native";
import { useFocusEffect, useNavigation } from "@react-navigation/native";
import { useSafeAreaInsets } from "react-native-safe-area-context";
import { Text } from "../../components/Text";
import { Card } from "../../components/Card";
import { Button } from "../../components/Button";
import { EmptyState } from "../../components/EmptyState";
import { NearbyMap } from "../../components/NearbyMap";
import { BharosaBadge } from "../../components/BharosaBadge";
import { ReputationBanner } from "../../components/ReputationBanner";
import { BharosaSheet } from "../../components/BharosaSheet";
import { FadeIn, PressableScale } from "../../components/Motion";
import { BharosaApi, DiscoveryApi } from "../../api/endpoints";
import { BharosaResponse, NearbyStore } from "../../api/types";
import { useAuth } from "../../state/AuthContext";
import { useLocation } from "../../state/useLocation";
import { color, font, radius, spacing } from "../../theme/tokens";
import { contentWidth } from "../../theme/layoutStyles";

/**
 * The shopper's home.
 *
 * Order of the page is the order of the decision: where am I → what's around me
 * → why would I pick one → ask them. The map orients; the list decides.
 */
export function HomeScreen() {
  const nav = useNavigation<any>();
  const insets = useSafeAreaInsets();
  const { profile } = useAuth();
  const { coords, status: locationStatus, request: requestLocation } = useLocation();

  const [stores, setStores] = useState<NearbyStore[] | null>(null);
  const [refreshing, setRefreshing] = useState(false);
  const [sheet, setSheet] = useState<BharosaResponse | null>(null);

  const load = useCallback(async () => {
    if (!coords) return;
    try {
      setStores(await DiscoveryApi.nearby(coords.latitude, coords.longitude, 5000));
    } catch {
      setStores([]);
    }
  }, [coords]);

  useFocusEffect(
    useCallback(() => {
      load();
    }, [load])
  );

  async function openBharosa(storeId: string) {
    try {
      setSheet(await BharosaApi.forStore(storeId));
    } catch {
      /* a missing score shouldn't interrupt browsing */
    }
  }

  const firstName = profile?.firstName?.trim();
  const greeting = firstName ? `Hi ${firstName}` : "Hi there";

  return (
    <View style={styles.flex}>
      <ScrollView
        contentContainerStyle={[
          styles.content,
          contentWidth.column,
          { paddingTop: insets.top + spacing.md },
        ]}
        refreshControl={
          <RefreshControl
            refreshing={refreshing}
            onRefresh={async () => {
              setRefreshing(true);
              await load();
              setRefreshing(false);
            }}
          />
        }
        showsVerticalScrollIndicator={false}
      >
        <FadeIn>
          <Text variant="h1">{greeting}</Text>
          <Text variant="body" color={color.neutral.inkMuted}>
            What do you need today?
          </Text>
        </FadeIn>

        <FadeIn index={1}>
          <NearbyMap coords={coords} stores={stores ?? []} />
        </FadeIn>

        <FadeIn index={2}>
          <Button
            label="+  Ask nearby shops"
            onPress={() => nav.navigate("CreateBasket")}
          />
        </FadeIn>

        {/* Only nag when we genuinely have nowhere to search from. */}
        {!coords && (locationStatus === "denied" || locationStatus === "error") ? (
          <FadeIn index={3}>
            <Card elevated style={styles.locationCard}>
              <Text variant="h3">Turn on location</Text>
              <Text variant="bodySm" color={color.neutral.inkMuted}>
                Without it we can't tell which shops are actually near you.
              </Text>
              <Button label="Allow location" variant="secondary" onPress={requestLocation} />
            </Card>
          </FadeIn>
        ) : null}

        <FadeIn index={3}>
          <View style={styles.sectionHead}>
            <Text variant="h2">Shops around you</Text>
            {stores?.length ? (
              <Text variant="bodySm" color={color.neutral.inkFaint}>
                most dependable first
              </Text>
            ) : null}
          </View>
        </FadeIn>

        {stores === null ? (
          <ActivityIndicator style={styles.loader} color={color.brand[500]} />
        ) : stores.length === 0 ? (
          <EmptyState
            emoji="🏪"
            title="No shops nearby yet"
            body="StockUp works once a few shops in your area have joined. You can still post a list — it'll reach them as they sign up."
          />
        ) : (
          stores.map((s, i) => (
            <FadeIn key={s.storeId} index={4 + i}>
              <PressableScale
                onPress={() => nav.navigate("PickShops", { selected: [s.storeId] })}
                accessibilityLabel={`${s.name}, Bharosa ${s.bharosa}`}
              >
                <Card elevated>
                  {s.tags?.length ? <ReputationBanner tags={s.tags} /> : null}

                  <View style={styles.rowBetween}>
                    <Text variant="h3" style={styles.name}>
                      {s.name}
                    </Text>
                    <Text variant="bodySm" color={color.neutral.inkFaint}>
                      {s.distanceKm} km
                    </Text>
                  </View>

                  {/* The 'why', derived from behaviour — never a shop's own claim. */}
                  <Text variant="bodySm" color={color.neutral.inkMuted}>
                    {s.knownFor}
                  </Text>

                  <View style={styles.metaRow}>
                    <BharosaBadge
                      score={s.bharosa}
                      band={s.band}
                      onPress={() => openBharosa(s.storeId)}
                    />
                    {s.reviewCount > 0 && s.averageStars != null ? (
                      <Text variant="bodySm" color={color.neutral.inkFaint}>
                        ★ {s.averageStars} · {s.reviewCount} review
                        {s.reviewCount === 1 ? "" : "s"}
                      </Text>
                    ) : null}
                  </View>
                </Card>
              </PressableScale>
            </FadeIn>
          ))
        )}
      </ScrollView>

      <BharosaSheet visible={!!sheet} data={sheet} onClose={() => setSheet(null)} />
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
  sectionHead: {
    flexDirection: "row",
    alignItems: "baseline",
    justifyContent: "space-between",
    marginTop: spacing.sm,
  },
  rowBetween: {
    flexDirection: "row",
    alignItems: "center",
    justifyContent: "space-between",
    gap: spacing.xs,
  },
  name: { flex: 1 },
  metaRow: {
    flexDirection: "row",
    alignItems: "center",
    gap: spacing.sm,
    marginTop: 2,
  },
  locationCard: {
    borderColor: color.status.attention.base,
    borderWidth: 1.5,
    gap: spacing.xs,
  },
  loader: { marginTop: spacing.xl },
});
