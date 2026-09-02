import React, { useCallback, useState } from "react";
import { ActivityIndicator, RefreshControl, ScrollView, StyleSheet, View } from "react-native";
import { useFocusEffect, useNavigation } from "@react-navigation/native";
import { useSafeAreaInsets } from "react-native-safe-area-context";
import { Text } from "../../components/Text";
import { Card } from "../../components/Card";
import { StatusPill } from "../../components/StatusPill";
import { LiveTimer } from "../../components/LiveTimer";
import { BharosaRing } from "../../components/BharosaRing";
import { EmptyState } from "../../components/EmptyState";
import { FadeIn, PressableScale, Pulse, useCountUp } from "../../components/Motion";
import { BroadcastApi, OrderApi, ReservationApi } from "../../api/endpoints";
import { BroadcastRecipientSummary, MerchantStats, Reservation } from "../../api/types";
import { useAuth } from "../../state/AuthContext";
import { color, font, radius, spacing } from "../../theme/tokens";
import { contentWidth } from "../../theme/layoutStyles";

/**
 * The shopkeeper's home.
 *
 * Ordered by what costs them money if missed: live requests first (they expire),
 * then orders waiting for collection, then the numbers. A dashboard that leads
 * with statistics is a dashboard that lets requests lapse.
 */
export function MerchantHomeScreen() {
  const nav = useNavigation<any>();
  const insets = useSafeAreaInsets();
  const { store, profile } = useAuth();

  const [requests, setRequests] = useState<BroadcastRecipientSummary[] | null>(null);
  const [pickups, setPickups] = useState<Reservation[] | null>(null);
  const [stats, setStats] = useState<MerchantStats | null>(null);
  const [refreshing, setRefreshing] = useState(false);

  const load = useCallback(async () => {
    const [r, p, s] = await Promise.allSettled([
      BroadcastApi.mine(),
      ReservationApi.list("ACTIVE", 0, 5),
      OrderApi.merchantStats(30),
    ]);
    setRequests(r.status === "fulfilled" ? r.value : []);
    setPickups(p.status === "fulfilled" ? p.value.content : []);
    setStats(s.status === "fulfilled" ? s.value : null);
  }, []);

  useFocusEffect(
    useCallback(() => {
      load();
    }, [load])
  );

  const score = useCountUp(stats?.bharosaScore ?? profile?.bharosaScore ?? 0);
  const openRequests = (requests ?? []).filter((r) => r.status === "PENDING");

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
          <View style={styles.header}>
            <View style={styles.headerCopy}>
              <Text variant="h1" numberOfLines={1}>
                {store?.name ?? "Your shop"}
              </Text>
              <Text variant="bodySm" color={color.neutral.inkMuted}>
                {openRequests.length > 0
                  ? `${openRequests.length} request${openRequests.length === 1 ? "" : "s"} waiting on you`
                  : "You're all caught up"}
              </Text>
            </View>
            <PressableScale onPress={() => nav.navigate("You")} accessibilityLabel="Your Bharosa score">
              <BharosaRing score={score} size="lg" />
            </PressableScale>
          </View>
        </FadeIn>

        {/* What expires if ignored comes first. */}
        <FadeIn index={1}>
          <View style={styles.sectionHead}>
            <Text variant="h2">Requests</Text>
            {openRequests.length > 0 ? (
              <Pulse>
                <View style={styles.liveDot} />
              </Pulse>
            ) : null}
          </View>
        </FadeIn>

        {requests === null ? (
          <ActivityIndicator style={styles.loader} color={color.brand[500]} />
        ) : requests.length === 0 ? (
          <EmptyState
            emoji="🛒"
            title="No requests right now"
            body="When someone nearby needs something, their list lands here. Replying quickly is the fastest way to lift your Bharosa."
          />
        ) : (
          requests.slice(0, 4).map((r, i) => {
            const isNew = r.status === "PENDING";
            return (
              <FadeIn key={r.broadcastRecipientId} index={2 + i}>
                <PressableScale
                  onPress={() =>
                    nav.navigate("RespondToRequest", { recipientId: r.broadcastRecipientId })
                  }
                  accessibilityLabel={`Request with ${r.items.length} items`}
                >
                  <Card elevated style={isNew ? styles.newCard : undefined}>
                    <View style={styles.rowBetween}>
                      <StatusPill
                        status={isNew ? "attention" : "info"}
                        label={isNew ? "New request" : "Opened"}
                      />
                      <LiveTimer expiresAt={r.basketExpiresAt} label="closes in" />
                    </View>
                    <View style={styles.items}>
                      {r.items.slice(0, 3).map((item, k) => (
                        <View key={k} style={styles.itemRow}>
                          <Text variant="body" weight="semibold" style={styles.itemName}>
                            {item.productName}
                          </Text>
                          <Text variant="mono" color={color.neutral.inkMuted}>
                            {item.quantity} {item.unit.toLowerCase()}
                          </Text>
                        </View>
                      ))}
                      {r.items.length > 3 ? (
                        <Text variant="bodySm" color={color.neutral.inkFaint}>
                          +{r.items.length - 3} more
                        </Text>
                      ) : null}
                    </View>
                  </Card>
                </PressableScale>
              </FadeIn>
            );
          })
        )}

        {pickups && pickups.length > 0 ? (
          <>
            <FadeIn index={6}>
              <Text variant="h2" style={styles.sectionTitle}>
                Waiting for collection
              </Text>
            </FadeIn>
            {pickups.map((p, i) => (
              <FadeIn key={p.id} index={7 + i}>
                <PressableScale
                  onPress={() =>
                    nav.navigate("MerchantReservationDetail", { reservationId: p.id })
                  }
                  accessibilityLabel="Order awaiting collection"
                >
                  <Card elevated>
                    <View style={styles.rowBetween}>
                      <StatusPill status="positive" label="Ready to collect" />
                      <Text variant="bodySm" color={color.neutral.inkFaint}>
                        {new Date(p.reservedAt).toLocaleTimeString(undefined, {
                          hour: "numeric",
                          minute: "2-digit",
                        })}
                      </Text>
                    </View>
                    <Text variant="bodySm" color={color.neutral.inkMuted}>
                      Ask the customer for their 6-digit code.
                    </Text>
                  </Card>
                </PressableScale>
              </FadeIn>
            ))}
          </>
        ) : null}

        {stats ? (
          <FadeIn index={10}>
            <Text variant="h2" style={styles.sectionTitle}>
              Last 30 days
            </Text>
            <Card elevated>
              <View style={styles.statGrid}>
                <Stat label="Completed" value={String(stats.ordersCompleted)} />
                <Stat
                  label="Avg reply"
                  value={
                    stats.averageResponseSeconds == null
                      ? "—"
                      : stats.averageResponseSeconds < 60
                      ? `${stats.averageResponseSeconds}s`
                      : `${Math.floor(stats.averageResponseSeconds / 60)}m`
                  }
                />
                <Stat label="Answered" value={pct(stats.answeredRate)} />
                <Stat label="Completion" value={pct(stats.completionRate)} />
              </View>
              <Text variant="bodySm" color={color.neutral.inkFaint} style={styles.statsNote}>
                These are the numbers your Bharosa is built from.
              </Text>
            </Card>
          </FadeIn>
        ) : null}
      </ScrollView>
    </View>
  );
}

function pct(v: number | null) {
  return v == null ? "—" : `${Math.round(v * 100)}%`;
}

function Stat({ label, value }: { label: string; value: string }) {
  return (
    <View style={styles.stat}>
      <Text style={styles.statValue}>{value}</Text>
      <Text variant="bodySm" color={color.neutral.inkMuted}>
        {label}
      </Text>
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
  header: { flexDirection: "row", alignItems: "center", gap: spacing.md },
  headerCopy: { flex: 1, gap: 2 },
  sectionHead: {
    flexDirection: "row",
    alignItems: "center",
    gap: spacing.xs,
    marginTop: spacing.sm,
  },
  sectionTitle: { marginTop: spacing.sm },
  liveDot: {
    width: 8,
    height: 8,
    borderRadius: 4,
    backgroundColor: color.status.attention.base,
  },
  newCard: { borderColor: color.status.attention.base, borderWidth: 1.5 },
  rowBetween: {
    flexDirection: "row",
    alignItems: "center",
    justifyContent: "space-between",
    gap: spacing.xs,
  },
  items: { marginTop: spacing.xs, gap: 6 },
  itemRow: {
    flexDirection: "row",
    alignItems: "center",
    justifyContent: "space-between",
    gap: spacing.xs,
  },
  itemName: { flex: 1 },
  statGrid: { flexDirection: "row", flexWrap: "wrap", gap: spacing.sm },
  stat: { minWidth: "40%", flexGrow: 1, gap: 1 },
  statValue: { fontFamily: font.mono.bold, fontSize: 20, color: color.neutral.ink },
  statsNote: { marginTop: spacing.xs },
  loader: { marginTop: spacing.xl },
});
