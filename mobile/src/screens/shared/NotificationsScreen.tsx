import React, { useCallback, useState } from "react";
import { ActivityIndicator, RefreshControl, ScrollView, StyleSheet, View } from "react-native";
import { useFocusEffect } from "@react-navigation/native";
import { AppBar } from "../../components/AppBar";
import { Card } from "../../components/Card";
import { Text } from "../../components/Text";
import { EmptyState } from "../../components/EmptyState";
import { NotificationApi } from "../../api/endpoints";
import { AppNotification } from "../../api/types";
import { notificationEmoji, notificationTone } from "../../theme/statusMap";
import { color, radius, spacing } from "../../theme/tokens";
import { contentWidth } from "../../theme/layoutStyles";

function relativeTime(iso: string) {
  const diff = Date.now() - new Date(iso).getTime();
  const mins = Math.floor(diff / 60000);
  if (mins < 1) return "just now";
  if (mins < 60) return `${mins}m ago`;
  const hours = Math.floor(mins / 60);
  if (hours < 24) return `${hours}h ago`;
  const days = Math.floor(hours / 24);
  return `${days}d ago`;
}

const PAGE_SIZE = 20;

export function NotificationsScreen() {
  const [rows, setRows] = useState<AppNotification[] | null>(null);
  const [refreshing, setRefreshing] = useState(false);
  const [page, setPage] = useState(0);
  const [hasMore, setHasMore] = useState(false);
  const [loadingMore, setLoadingMore] = useState(false);

  const load = useCallback(async () => {
    try {
      const result = await NotificationApi.list(0, PAGE_SIZE);
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
      const result = await NotificationApi.list(next, PAGE_SIZE);
      setRows((prev) => [...(prev ?? []), ...result.content]);
      setPage(next);
      setHasMore(next + 1 < result.totalPages);
    } catch {
      setHasMore(false);
    } finally {
      setLoadingMore(false);
    }
  }, [loadingMore, hasMore, page]);

  useFocusEffect(
    useCallback(() => {
      load();
    }, [load])
  );

  async function markRead(n: AppNotification) {
    if (n.read) return;
    setRows((prev) =>
      prev ? prev.map((r) => (r.id === n.id ? { ...r, read: true } : r)) : prev
    );
    try {
      await NotificationApi.markRead(n.id);
    } catch {
      load();
    }
  }

  return (
    <View style={styles.flex}>
      <AppBar title="Updates" subtitle="What's happening with your orders" />
      <ScrollView
        contentContainerStyle={[styles.content, contentWidth.column]}
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
            emoji="🔔"
            title="All quiet"
            body="Updates about your lists, offers and pickups will land here."
          />
        ) : (
          rows.map((n) => {
            const tone = color.status[notificationTone(n.type)];
            return (
              <Card key={n.id} elevated onPress={() => markRead(n)} style={styles.row}>
                <View style={[styles.iconWrap, { backgroundColor: tone.soft }]}>
                  <Text style={styles.emoji}>{notificationEmoji(n.type)}</Text>
                </View>
                <View style={styles.body}>
                  <View style={styles.titleRow}>
                    <Text variant="h3" style={styles.title} numberOfLines={1}>
                      {n.title}
                    </Text>
                    {!n.read ? <View style={[styles.unread, { backgroundColor: tone.base }]} /> : null}
                  </View>
                  <Text variant="bodySm" color={color.neutral.inkMuted}>
                    {n.message}
                  </Text>
                  <Text variant="caption" color={color.neutral.inkFaint}>
                    {relativeTime(n.createdAt)}
                  </Text>
                </View>
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
  content: {
    padding: spacing.lg,
    paddingTop: 0,
    gap: spacing.xs,
    paddingBottom: spacing.xxxl,
  },
  row: {
    flexDirection: "row",
    gap: spacing.sm,
    alignItems: "flex-start",
  },
  iconWrap: {
    width: 40,
    height: 40,
    borderRadius: radius.md,
    alignItems: "center",
    justifyContent: "center",
  },
  emoji: { fontSize: 18 },
  body: { flex: 1, gap: 3 },
  titleRow: {
    flexDirection: "row",
    alignItems: "center",
    gap: spacing.xs,
  },
  title: { flex: 1 },
  unread: {
    width: 8,
    height: 8,
    borderRadius: 4,
  },
  loader: { marginTop: spacing.xxl },
  moreLoader: { marginVertical: spacing.md },
});
