import React, { useCallback, useState } from "react";
import { ActivityIndicator, RefreshControl, ScrollView, StyleSheet, View } from "react-native";
import { useFocusEffect, useNavigation } from "@react-navigation/native";
import { AppBar } from "../../components/AppBar";
import { Card } from "../../components/Card";
import { Text } from "../../components/Text";
import { Button } from "../../components/Button";
import { StatusPill } from "../../components/StatusPill";
import { LiveTimer } from "../../components/LiveTimer";
import { EmptyState } from "../../components/EmptyState";
import { BasketApi } from "../../api/endpoints";
import { BasketHistoryItem } from "../../api/types";
import { basketStatus } from "../../theme/statusMap";
import { color, spacing } from "../../theme/tokens";
import { contentWidth } from "../../theme/layoutStyles";

export function BasketsScreen() {
  const nav = useNavigation<any>();
  const [baskets, setBaskets] = useState<BasketHistoryItem[] | null>(null);
  const [refreshing, setRefreshing] = useState(false);

  const load = useCallback(async () => {
    try {
      const data = await BasketApi.history();
      setBaskets(data);
    } catch {
      setBaskets([]);
    }
  }, []);

  useFocusEffect(
    useCallback(() => {
      load();
    }, [load])
  );

  /**
   * A reserved list has stopped being a question and become an order, so it
   * moves to the Orders tab rather than sitting in both. Showing it here too
   * meant the same shopping trip appeared twice, under two different status
   * vocabularies — which read as a bug because it was one.
   *
   * A reservation that falls through reopens the list, and it reappears here.
   */
  const open = (baskets ?? []).filter((b) => b.status !== "RESERVED");
  const reservedCount = (baskets ?? []).length - open.length;

  const onRefresh = async () => {
    setRefreshing(true);
    await load();
    setRefreshing(false);
  };

  return (
    <View style={styles.flex}>
      <AppBar title="Your lists" subtitle="Ask nearby shops what's in stock" />
      <ScrollView
        contentContainerStyle={[styles.content, contentWidth.column]}
        refreshControl={<RefreshControl refreshing={refreshing} onRefresh={onRefresh} />}
        showsVerticalScrollIndicator={false}
      >
        <Button label="+  New list" onPress={() => nav.navigate("CreateBasket")} />

        {baskets === null ? (
          <ActivityIndicator style={styles.loader} color={color.brand[500]} />
        ) : open.length === 0 ? (
          <EmptyState
            emoji="🧺"
            title={reservedCount > 0 ? "Nothing open right now" : "No lists yet"}
            body={
              reservedCount > 0
                ? `Your ${reservedCount === 1 ? "reserved list is" : `${reservedCount} reserved lists are`} waiting under Orders. Start a new list any time.`
                : "Make a list of what you need. Nearby shops will tell you what they've got in stock."
            }
          />
        ) : (
          open.map((b) => {
            const s = basketStatus(b.status);
            const live = b.status === "ACTIVE" || b.status === "PENDING_BROADCAST";
            return (
              <Card
                key={b.basketId}
                elevated
                onPress={() => nav.navigate("BasketDetail", { basketId: b.basketId })}
              >
                <View style={styles.cardTop}>
                  <StatusPill status={s.status} label={s.label} />
                  <Text variant="bodySm" color={color.neutral.inkFaint}>
                    {b.totalItems} item{b.totalItems === 1 ? "" : "s"}
                  </Text>
                </View>
                {live ? (
                  <LiveTimer expiresAt={b.expiresAt} label="open" />
                ) : (
                  <Text variant="bodySm" color={color.neutral.inkMuted}>
                    {new Date(b.createdAt).toLocaleDateString(undefined, {
                      day: "numeric",
                      month: "short",
                    })}
                  </Text>
                )}
              </Card>
            );
          })
        )}

        {/* Say where the missing ones went, rather than silently dropping them. */}
        {open.length > 0 && reservedCount > 0 ? (
          <Text variant="bodySm" color={color.neutral.inkFaint} style={styles.footnote}>
            {reservedCount === 1 ? "1 reserved list is" : `${reservedCount} reserved lists are`}{" "}
            under Orders.
          </Text>
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
    gap: spacing.sm,
    paddingBottom: spacing.xxxl,
  },
  cardTop: {
    flexDirection: "row",
    alignItems: "center",
    justifyContent: "space-between",
  },
  footnote: { textAlign: "center", marginTop: spacing.xs },
  loader: {
    marginTop: spacing.xxl,
  },
});
