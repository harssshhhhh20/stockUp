import React, { useCallback, useState } from "react";
import { ActivityIndicator, ScrollView, StyleSheet, View } from "react-native";
import { useFocusEffect, useNavigation, useRoute } from "@react-navigation/native";
import { AppBar } from "../../components/AppBar";
import { Card } from "../../components/Card";
import { Text } from "../../components/Text";
import { Button } from "../../components/Button";
import { StatusPill } from "../../components/StatusPill";
import { OrderApi } from "../../api/endpoints";
import { OrderDetail } from "../../api/types";
import { reservationStatus } from "../../theme/statusMap";
import { color, font, radius, spacing } from "../../theme/tokens";
import { contentWidth } from "../../theme/layoutStyles";

/** Seconds → "3m 12s", because "192" tells a shopkeeper nothing. */
function duration(seconds: number | null) {
  if (seconds == null) return null;
  if (seconds < 60) return `${seconds}s`;
  const m = Math.floor(seconds / 60);
  const s = seconds % 60;
  return s === 0 ? `${m}m` : `${m}m ${s}s`;
}

function clockTime(iso: string) {
  return new Date(iso).toLocaleTimeString(undefined, {
    hour: "2-digit",
    minute: "2-digit",
  });
}

export function OrderDetailScreen() {
  const nav = useNavigation<any>();
  const route = useRoute<any>();
  const { reservationId } = route.params as { reservationId: string };

  const [order, setOrder] = useState<OrderDetail | null>(null);
  const [missing, setMissing] = useState(false);

  const load = useCallback(async () => {
    try {
      setOrder(await OrderApi.detail(reservationId));
    } catch {
      setMissing(true);
    }
  }, [reservationId]);

  useFocusEffect(
    useCallback(() => {
      load();
    }, [load])
  );

  const s = order ? reservationStatus(order.status) : null;

  return (
    <View style={styles.flex}>
      <AppBar title="Order" subtitle={order?.storeName} onBack={() => nav.goBack()} />
      <ScrollView
        contentContainerStyle={[styles.content, contentWidth.column]}
        showsVerticalScrollIndicator={false}
      >
        {missing ? (
          <Card elevated>
            <Text variant="h3">Can't show this order</Text>
            <Text variant="bodySm" color={color.neutral.inkMuted}>
              It either doesn't exist, or it isn't yours.
            </Text>
          </Card>
        ) : !order ? (
          <ActivityIndicator style={styles.loader} color={color.brand[500]} />
        ) : (
          <>
            <Card elevated>
              <View style={styles.rowBetween}>
                {s ? <StatusPill status={s.status} label={s.label} /> : null}
                <Text variant="bodySm" color={color.neutral.inkFaint}>
                  {new Date(order.reservedAt).toLocaleDateString(undefined, {
                    day: "numeric",
                    month: "short",
                  })}
                </Text>
              </View>

              <View style={styles.items}>
                {order.items.map((line, i) => (
                  <Text key={i} variant="body">
                    {line}
                  </Text>
                ))}
              </View>

              {order.cancellationReason ? (
                <Text variant="bodySm" color={color.status.urgent.strong}>
                  Reason: {order.cancellationReason}
                </Text>
              ) : null}
            </Card>

            {/* The two numbers that feed Bharosa, shown plainly. */}
            {(order.responseSeconds != null || order.fulfilmentSeconds != null) && (
              <View style={styles.metrics}>
                {order.responseSeconds != null ? (
                  <Card elevated style={styles.metric}>
                    <Text variant="caption" color={color.neutral.inkMuted}>
                      Shop replied in
                    </Text>
                    <Text style={styles.metricValue}>{duration(order.responseSeconds)}</Text>
                  </Card>
                ) : null}
                {order.fulfilmentSeconds != null ? (
                  <Card elevated style={styles.metric}>
                    <Text variant="caption" color={color.neutral.inkMuted}>
                      Ready in
                    </Text>
                    <Text style={styles.metricValue}>{duration(order.fulfilmentSeconds)}</Text>
                  </Card>
                ) : null}
              </View>
            )}

            <Text variant="h2" style={styles.sectionTitle}>
              What happened
            </Text>

            <Card elevated>
              <View style={styles.timeline}>
                {order.timeline.map((entry, i) => {
                  const last = i === order.timeline.length - 1;
                  return (
                    <View key={i} style={styles.tlRow}>
                      <View style={styles.tlGutter}>
                        <View
                          style={[
                            styles.tlDot,
                            last && styles.tlDotLast,
                          ]}
                        />
                        {!last ? <View style={styles.tlLine} /> : null}
                      </View>
                      <View style={styles.tlBody}>
                        <Text variant="mono" color={color.neutral.inkFaint} style={styles.tlTime}>
                          {clockTime(entry.occurredAt)}
                        </Text>
                        <Text variant="body" weight={last ? "bold" : "medium"}>
                          {entry.label}
                        </Text>
                      </View>
                    </View>
                  );
                })}
              </View>
            </Card>

            {order.feedback ? (
              <Card elevated>
                <View style={styles.rowBetween}>
                  <Text variant="h3">You rated this</Text>
                  <StatusPill status="positive" label="✓ Verified" dot={false} />
                </View>
                <Text style={styles.ratedStars}>
                  {"★".repeat(order.feedback.stars)}
                  <Text style={styles.ratedStarsOff}>
                    {"★".repeat(5 - order.feedback.stars)}
                  </Text>
                </Text>
                {order.feedback.comment ? (
                  <Text variant="bodySm" color={color.neutral.inkMuted}>
                    "{order.feedback.comment}"
                  </Text>
                ) : null}
              </Card>
            ) : order.canRate ? (
              <Button
                label="Rate this order"
                onPress={() =>
                  nav.navigate("RateOrder", {
                    reservationId: order.reservationId,
                    storeName: order.storeName,
                  })
                }
              />
            ) : null}
          </>
        )}
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
  rowBetween: {
    flexDirection: "row",
    alignItems: "center",
    justifyContent: "space-between",
  },
  items: { marginTop: spacing.xs, gap: 3 },
  metrics: { flexDirection: "row", gap: spacing.xs },
  metric: { flex: 1, gap: 2 },
  metricValue: {
    fontFamily: font.mono.bold,
    fontSize: 19,
    color: color.brand[600],
  },
  sectionTitle: { marginTop: spacing.sm },
  timeline: { gap: 0 },
  tlRow: { flexDirection: "row", gap: spacing.sm },
  tlGutter: { width: 14, alignItems: "center" },
  tlDot: {
    width: 9,
    height: 9,
    borderRadius: 5,
    marginTop: 6,
    backgroundColor: color.neutral.borderStrong,
  },
  tlDotLast: { backgroundColor: color.brand[500] },
  tlLine: {
    flex: 1,
    width: 1.5,
    backgroundColor: color.neutral.border,
    marginVertical: 2,
  },
  tlBody: { flex: 1, paddingBottom: spacing.md, gap: 1 },
  tlTime: { fontSize: 11.5 },
  ratedStars: {
    fontSize: 20,
    lineHeight: 26,
    color: color.status.attention.base,
    letterSpacing: 2,
  },
  ratedStarsOff: { color: color.neutral.border },
  loader: { marginTop: spacing.xxl },
});
