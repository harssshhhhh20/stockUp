import React, { useCallback, useState } from "react";
import { ActivityIndicator, RefreshControl, ScrollView, StyleSheet, View } from "react-native";
import { useFocusEffect, useNavigation } from "@react-navigation/native";
import { AppBar } from "../../components/AppBar";
import { Card } from "../../components/Card";
import { Text } from "../../components/Text";
import { StatusPill } from "../../components/StatusPill";
import { LiveTimer } from "../../components/LiveTimer";
import { EmptyState } from "../../components/EmptyState";
import { BharosaRing } from "../../components/BharosaRing";
import { BroadcastApi } from "../../api/endpoints";
import { BroadcastRecipientSummary } from "../../api/types";
import { useAuth } from "../../state/AuthContext";
import { color, spacing } from "../../theme/tokens";
import { contentWidth } from "../../theme/layoutStyles";

export function RequestsScreen() {
  const nav = useNavigation<any>();
  const { merchantProfile, store, refreshMerchantState } = useAuth();
  const [rows, setRows] = useState<BroadcastRecipientSummary[] | null>(null);
  const [refreshing, setRefreshing] = useState(false);

  const load = useCallback(async () => {
    try {
      const data = await BroadcastApi.mine();
      setRows(data);
    } catch {
      setRows([]);
    }
    // The score changes as a side effect of replying/expiring, so re-read it
    // here rather than letting the header drift out of date.
    refreshMerchantState();
  }, [refreshMerchantState]);

  useFocusEffect(
    useCallback(() => {
      load();
    }, [load])
  );

  return (
    <View style={styles.flex}>
      <AppBar
        title={store?.name ?? "Your shop"}
        subtitle="Customers looking for stock nearby"
        right={
          merchantProfile ? <BharosaRing score={merchantProfile.bharosaScore} /> : undefined
        }
      />
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
      >
        {rows === null ? (
          <ActivityIndicator style={styles.loader} color={color.brand[500]} />
        ) : rows.length === 0 ? (
          <EmptyState
            emoji="🛒"
            title="No requests right now"
            body="When someone nearby needs something, their list lands here. Reply fast to build your Bharosa Score."
          />
        ) : (
          rows.map((r) => {
            const isNew = r.status === "PENDING";
            return (
              <Card
                key={r.broadcastRecipientId}
                elevated
                onPress={() =>
                  nav.navigate("RespondToRequest", { recipientId: r.broadcastRecipientId })
                }
                style={isNew ? styles.newCard : undefined}
              >
                <View style={styles.rowBetween}>
                  <StatusPill
                    status={isNew ? "attention" : "info"}
                    label={isNew ? "New request" : "Opened"}
                  />
                  <LiveTimer expiresAt={r.basketExpiresAt} label="closes in" />
                </View>

                <View style={styles.items}>
                  {r.items.slice(0, 3).map((item, i) => (
                    <View key={i} style={styles.itemRow}>
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
  newCard: {
    borderColor: color.status.attention.base,
    borderWidth: 1.5,
  },
  rowBetween: {
    flexDirection: "row",
    alignItems: "center",
    justifyContent: "space-between",
    gap: spacing.xs,
  },
  items: {
    marginTop: spacing.xs,
    gap: 6,
  },
  itemRow: {
    flexDirection: "row",
    alignItems: "center",
    justifyContent: "space-between",
    gap: spacing.xs,
  },
  itemName: { flex: 1 },
  loader: { marginTop: spacing.xxl },
});
