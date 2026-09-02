import React, { useCallback, useState } from "react";
import { ActivityIndicator, RefreshControl, ScrollView, StyleSheet, View } from "react-native";
import { useFocusEffect, useNavigation, useRoute } from "@react-navigation/native";
import { AppBar } from "../../components/AppBar";
import { Card } from "../../components/Card";
import { Text } from "../../components/Text";
import { Button } from "../../components/Button";
import { StatusPill } from "../../components/StatusPill";
import { LiveTimer } from "../../components/LiveTimer";
import { EmptyState } from "../../components/EmptyState";
import { useToast } from "../../components/Toast";
import { BasketApi, BharosaApi, MerchantOfferApi, ReservationApi } from "../../api/endpoints";
import { BasketDetails, BharosaResponse, MerchantOfferSummary } from "../../api/types";
import { BharosaBadge } from "../../components/BharosaBadge";
import { ReputationBanner } from "../../components/ReputationBanner";
import { BharosaSheet } from "../../components/BharosaSheet";
import { basketStatus, offerItemStatus } from "../../theme/statusMap";
import { color, radius, spacing } from "../../theme/tokens";
import { contentWidth } from "../../theme/layoutStyles";
import { ApiError } from "../../api/client";

export function BasketDetailScreen() {
  const nav = useNavigation<any>();
  const route = useRoute<any>();
  const { basketId } = route.params as { basketId: string };
  const toast = useToast();

  const [basket, setBasket] = useState<BasketDetails | null>(null);
  const [offers, setOffers] = useState<MerchantOfferSummary[] | null>(null);
  const [refreshing, setRefreshing] = useState(false);
  const [reservingId, setReservingId] = useState<string | null>(null);
  // Trust, keyed by store, so each reply can carry its own reputation.
  const [bharosa, setBharosa] = useState<Record<string, BharosaResponse>>({});
  const [openSheet, setOpenSheet] = useState<BharosaResponse | null>(null);

  const load = useCallback(async () => {
    const [b, o] = await Promise.allSettled([
      BasketApi.detail(basketId),
      MerchantOfferApi.forBasket(basketId),
    ]);
    if (b.status === "fulfilled") setBasket(b.value);
    const list = o.status === "fulfilled" ? o.value : [];
    setOffers(list);

    // Reputation is fetched per replying shop. Failures are silent: a missing
    // score should never stop someone seeing that a shop has their milk.
    const results = await Promise.allSettled(
      list.map((offer) => BharosaApi.forStore(offer.storeId))
    );
    const next: Record<string, BharosaResponse> = {};
    results.forEach((r) => {
      if (r.status === "fulfilled" && r.value?.storeId) next[r.value.storeId] = r.value;
    });
    setBharosa(next);
  }, [basketId]);

  useFocusEffect(
    useCallback(() => {
      load();
    }, [load])
  );

  async function reserve(offerId: string) {
    setReservingId(offerId);
    try {
      const res = await ReservationApi.reserve(offerId);
      toast("Reserved! The shop is confirming 🎉", "positive");
      nav.navigate("ReservationDetail", { reservationId: res.id });
    } catch (e) {
      toast(e instanceof ApiError ? e.message : "Couldn't reserve", "urgent");
    } finally {
      setReservingId(null);
    }
  }

  const s = basket ? basketStatus(basket.status) : null;
  const live = basket?.status === "ACTIVE" || basket?.status === "PENDING_BROADCAST";

  return (
    <View style={styles.flex}>
      <AppBar title="Your list" onBack={() => nav.goBack()} />
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
        {!basket ? (
          <ActivityIndicator style={styles.loader} color={color.brand[500]} />
        ) : (
          <>
            <Card elevated>
              <View style={styles.rowBetween}>
                {s ? <StatusPill status={s.status} label={s.label} /> : null}
                {live ? <LiveTimer expiresAt={basket.expiresAt} label="open" /> : null}
              </View>
              <View style={styles.items}>
                {basket.items.map((item, i) => (
                  <View key={i} style={styles.itemRow}>
                    <Text variant="body" weight="semibold" style={styles.itemName}>
                      {item.productName}
                    </Text>
                    <Text variant="mono" color={color.neutral.inkMuted}>
                      {item.quantity} {item.unit.toLowerCase()}
                    </Text>
                  </View>
                ))}
              </View>
            </Card>

            <Text variant="h2" style={styles.sectionTitle}>
              Replies from shops
            </Text>

            {offers === null ? (
              <ActivityIndicator color={color.brand[500]} />
            ) : offers.length === 0 ? (
              <EmptyState
                emoji={live ? "📡" : "🤷"}
                title={live ? "Waiting for shops" : "No replies came in"}
                body={
                  live
                    ? "Nearby shops are looking at your list right now. Replies show up here."
                    : "No shop replied before this list expired. Try again with a wider distance."
                }
              />
            ) : (
              offers.map((offer) => {
                const canReserve = offer.status === "SUBMITTED" && basket.status === "ACTIVE";
                return (
                  <Card key={offer.merchantOfferId} elevated>
                    {bharosa[offer.storeId]?.tags?.length ? (
                      <ReputationBanner tags={bharosa[offer.storeId].tags} />
                    ) : null}

                    <View style={styles.rowBetween}>
                      <Text variant="h3">{offer.storeName}</Text>
                      {offer.status === "RESERVED" ? (
                        <StatusPill status="positive" label="Reserved" />
                      ) : null}
                    </View>

                    {bharosa[offer.storeId] ? (
                      <BharosaBadge
                        score={bharosa[offer.storeId].score}
                        band={bharosa[offer.storeId].band}
                        onPress={() => setOpenSheet(bharosa[offer.storeId])}
                      />
                    ) : null}

                    <View style={styles.offerItems}>
                      {offer.items.map((item) => {
                        const st = offerItemStatus(item.status);
                        const requested = basket.items.find(
                          (bi) => bi.basketItemId === item.basketItemId
                        );
                        return (
                          <View key={item.basketItemId} style={styles.itemRow}>
                            <Text variant="body" style={styles.itemName}>
                              {item.productName}
                            </Text>
                            <View style={styles.offerRight}>
                              {item.availableQuantity != null && requested ? (
                                <Text variant="mono" color={color.status.attention.strong}>
                                  {item.availableQuantity} of {requested.quantity}{" "}
                                  {requested.unit.toLowerCase()}
                                </Text>
                              ) : null}
                              <StatusPill status={st.status} label={st.label} dot={false} />
                            </View>
                          </View>
                        );
                      })}
                    </View>

                    {canReserve ? (
                      <Button
                        label="Reserve at this shop"
                        onPress={() => reserve(offer.merchantOfferId)}
                        loading={reservingId === offer.merchantOfferId}
                        style={styles.reserveBtn}
                      />
                    ) : null}
                  </Card>
                );
              })
            )}
          </>
        )}
      </ScrollView>

      <BharosaSheet
        visible={!!openSheet}
        data={openSheet}
        onClose={() => setOpenSheet(null)}
      />
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
  items: {
    marginTop: spacing.xs,
    gap: 6,
  },
  offerItems: {
    marginTop: spacing.xxs,
    gap: 8,
  },
  itemRow: {
    flexDirection: "row",
    alignItems: "center",
    justifyContent: "space-between",
    gap: spacing.xs,
  },
  itemName: {
    flex: 1,
  },
  offerRight: {
    flexDirection: "row",
    alignItems: "center",
    gap: 6,
  },
  sectionTitle: {
    marginTop: spacing.md,
  },
  reserveBtn: {
    marginTop: spacing.sm,
  },
  loader: {
    marginTop: spacing.xxl,
  },
});
