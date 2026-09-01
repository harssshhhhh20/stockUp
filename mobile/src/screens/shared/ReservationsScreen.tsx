import React, { useCallback, useState } from "react";
import { ActivityIndicator, Pressable, RefreshControl, ScrollView, StyleSheet, View } from "react-native";
import { useFocusEffect, useNavigation } from "@react-navigation/native";
import { AppBar } from "../../components/AppBar";
import { Card } from "../../components/Card";
import { Text } from "../../components/Text";
import { StatusPill } from "../../components/StatusPill";
import { EmptyState } from "../../components/EmptyState";
import { ReservationApi } from "../../api/endpoints";
import { Reservation, ReservationStatus } from "../../api/types";
import { reservationStatus } from "../../theme/statusMap";
import { useAuth } from "../../state/AuthContext";
import { color, radius, spacing } from "../../theme/tokens";
import { contentWidth } from "../../theme/layoutStyles";

const PAGE_SIZE = 20;

const FILTERS: { key: ReservationStatus; label: string }[] = [
  { key: "ACTIVE", label: "Active" },
  { key: "PENDING_NOTIFICATION", label: "Confirming" },
  { key: "COMPLETED", label: "Done" },
  { key: "EXPIRED", label: "Expired" },
];

export function ReservationsScreen() {
  const nav = useNavigation<any>();
  const { mode } = useAuth();
  const [filter, setFilter] = useState<ReservationStatus>("ACTIVE");
  const [rows, setRows] = useState<Reservation[] | null>(null);
  const [refreshing, setRefreshing] = useState(false);
  const [page, setPage] = useState(0);
  const [hasMore, setHasMore] = useState(false);
  const [loadingMore, setLoadingMore] = useState(false);

  const load = useCallback(async (status: ReservationStatus) => {
    try {
      const result = await ReservationApi.list(status, 0, PAGE_SIZE);
      setRows(result.content);
      setPage(0);
      setHasMore(result.totalPages > 1);
    } catch {
      setRows([]);
      setHasMore(false);
    }
  }, []);

  const loadMore = useCallback(async () => {
    if (loadingMore || !hasMore) return;
    setLoadingMore(true);
    try {
      const next = page + 1;
      const result = await ReservationApi.list(filter, next, PAGE_SIZE);
      setRows((prev) => [...(prev ?? []), ...result.content]);
      setPage(next);
      setHasMore(next + 1 < result.totalPages);
    } catch {
      setHasMore(false);
    } finally {
      setLoadingMore(false);
    }
  }, [loadingMore, hasMore, page, filter]);

  useFocusEffect(
    useCallback(() => {
      load(filter);
    }, [load, filter])
  );

  const isMerchant = mode === "merchant";

  return (
    <View style={styles.flex}>
      <AppBar
        title={isMerchant ? "Pickups" : "Your orders"}
        subtitle={isMerchant ? "Orders waiting for collection" : "Things you've reserved"}
      />

      <ScrollView
        horizontal
        showsHorizontalScrollIndicator={false}
        contentContainerStyle={[styles.filters, contentWidth.column]}
        style={styles.filterBar}
      >
        {FILTERS.map((f) => {
          const active = filter === f.key;
          return (
            <Pressable
              key={f.key}
              onPress={() => {
                setFilter(f.key);
                setRows(null);
              }}
              style={[styles.chip, active && styles.chipActive]}
            >
              <Text
                variant="bodySm"
                weight="semibold"
                color={active ? "#fff" : color.neutral.inkMuted}
              >
                {f.label}
              </Text>
            </Pressable>
          );
        })}
      </ScrollView>

      <ScrollView
        contentContainerStyle={[styles.content, contentWidth.column]}
        refreshControl={
          <RefreshControl
            refreshing={refreshing}
            onRefresh={async () => {
              setRefreshing(true);
              await load(filter);
              setRefreshing(false);
            }}
          />
        }
        showsVerticalScrollIndicator={false}
        onScroll={({ nativeEvent }) => {
          const { layoutMeasurement, contentOffset, contentSize } = nativeEvent;
          const nearBottom =
            layoutMeasurement.height + contentOffset.y >= contentSize.height - 240;
          if (nearBottom) loadMore();
        }}
        scrollEventThrottle={200}
      >
        {rows === null ? (
          <ActivityIndicator style={styles.loader} color={color.brand[500]} />
        ) : rows.length === 0 ? (
          <EmptyState
            emoji="🧾"
            title="Nothing here"
            body={
              isMerchant
                ? "Orders customers reserve at your shop will show up here."
                : "When you reserve something from a shop, it'll appear here."
            }
          />
        ) : (
          rows.map((r) => {
            const s = reservationStatus(r.status);
            return (
              <Card
                key={r.id}
                elevated
                onPress={() =>
                  nav.navigate(
                    isMerchant ? "MerchantReservationDetail" : "ReservationDetail",
                    { reservationId: r.id }
                  )
                }
              >
                <View style={styles.rowBetween}>
                  <StatusPill status={s.status} label={s.label} />
                  <Text variant="bodySm" color={color.neutral.inkFaint}>
                    {new Date(r.reservedAt).toLocaleDateString(undefined, {
                      day: "numeric",
                      month: "short",
                    })}
                  </Text>
                </View>
                <Text variant="bodySm" color={color.neutral.inkMuted}>
                  Reserved {new Date(r.reservedAt).toLocaleTimeString(undefined, {
                    hour: "numeric",
                    minute: "2-digit",
                  })}
                </Text>
              </Card>
            );
          })
        )}

        {loadingMore ? (
          <ActivityIndicator color={color.brand[500]} style={styles.moreLoader} />
        ) : null}
      </ScrollView>
    </View>
  );
}

const styles = StyleSheet.create({
  flex: { flex: 1, backgroundColor: color.neutral.background },
  filterBar: {
    flexGrow: 0,
    maxHeight: 58,
  },
  filters: {
    paddingHorizontal: spacing.lg,
    gap: spacing.xs,
    paddingBottom: spacing.sm,
  },
  chip: {
    paddingHorizontal: spacing.md,
    height: 40,
    borderRadius: radius.pill,
    borderWidth: 1,
    borderColor: color.neutral.border,
    backgroundColor: color.neutral.surface,
    alignItems: "center",
    justifyContent: "center",
  },
  chipActive: {
    backgroundColor: color.brand[500],
    borderColor: color.brand[500],
  },
  content: {
    padding: spacing.lg,
    paddingTop: spacing.xs,
    gap: spacing.sm,
    paddingBottom: spacing.xxxl,
  },
  rowBetween: {
    flexDirection: "row",
    alignItems: "center",
    justifyContent: "space-between",
  },
  loader: { marginTop: spacing.xxl },
  moreLoader: { marginVertical: spacing.md },
});
