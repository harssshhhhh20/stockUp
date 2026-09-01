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
        ) : baskets.length === 0 ? (
          <EmptyState
            emoji="🧺"
            title="No lists yet"
            body="Make a list of what you need. Nearby shops will tell you what they've got in stock."
          />
        ) : (
          baskets.map((b) => {
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
  loader: {
    marginTop: spacing.xxl,
  },
});
