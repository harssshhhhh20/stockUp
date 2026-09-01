import React, { useCallback, useEffect, useState } from "react";
import { ActivityIndicator, Pressable, ScrollView, StyleSheet, View } from "react-native";
import { useNavigation, useRoute } from "@react-navigation/native";
import { AppBar } from "../../components/AppBar";
import { Card } from "../../components/Card";
import { Text } from "../../components/Text";
import { Button } from "../../components/Button";
import { TextField } from "../../components/TextField";
import { LiveTimer } from "../../components/LiveTimer";
import { useToast } from "../../components/Toast";
import { BroadcastApi, MerchantOfferApi } from "../../api/endpoints";
import { BroadcastRecipientSummary, MerchantOfferItemStatus } from "../../api/types";
import { color, radius, spacing } from "../../theme/tokens";
import { contentWidth } from "../../theme/layoutStyles";
import { ApiError } from "../../api/client";

type Answer = { status: MerchantOfferItemStatus; availableQuantity: string };

const CHOICES: { key: MerchantOfferItemStatus; label: string; tone: "positive" | "attention" | "urgent" }[] = [
  { key: "AVAILABLE", label: "Have it", tone: "positive" },
  { key: "PARTIAL", label: "Some", tone: "attention" },
  { key: "NOT_AVAILABLE", label: "None", tone: "urgent" },
];

export function RespondToRequestScreen() {
  const nav = useNavigation<any>();
  const route = useRoute<any>();
  const { recipientId } = route.params as { recipientId: string };
  const toast = useToast();

  const [request, setRequest] = useState<BroadcastRecipientSummary | null>(null);
  const [answers, setAnswers] = useState<Record<string, Answer>>({});
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const load = useCallback(async () => {
    const all = await BroadcastApi.mine();
    const found = all.find((r) => r.broadcastRecipientId === recipientId) ?? null;
    setRequest(found);
    if (found) {
      setAnswers(
        Object.fromEntries(
          found.items.map((i) => [
            i.basketItemId,
            { status: "AVAILABLE" as MerchantOfferItemStatus, availableQuantity: "" },
          ])
        )
      );
      // Opening the request is what "viewing" means — mark it server-side so
      // the merchant can submit an offer (the API requires VIEWED first).
      if (found.status === "PENDING") {
        try {
          await BroadcastApi.markViewed(recipientId);
        } catch {
          // non-fatal; submit will surface a clear error if it mattered
        }
      }
    }
  }, [recipientId]);

  useEffect(() => {
    load();
  }, [load]);

  async function submit() {
    if (!request) return;
    setSubmitting(true);
    setError(null);
    try {
      await MerchantOfferApi.submit({
        broadcastRecipientId: recipientId,
        responses: request.items.map((item) => {
          const a = answers[item.basketItemId];
          return a.status === "PARTIAL"
            ? {
                basketItemId: item.basketItemId,
                status: a.status,
                availableQuantity: Number(a.availableQuantity),
              }
            : { basketItemId: item.basketItemId, status: a.status };
        }),
      });
      toast("Reply sent to the customer 🎉", "positive");
      nav.goBack();
    } catch (e) {
      setError(e instanceof ApiError ? e.message : "Couldn't send your reply.");
      setSubmitting(false);
    }
  }

  const partialsValid = request
    ? request.items.every((item) => {
        const a = answers[item.basketItemId];
        if (!a) return false;
        if (a.status !== "PARTIAL") return true;
        const q = Number(a.availableQuantity);
        return q > 0 && q <= item.quantity;
      })
    : false;

  return (
    <View style={styles.flex}>
      <AppBar
        title="What can you supply?"
        subtitle="Tap for each item, then send"
        onBack={() => nav.goBack()}
      />
      <ScrollView contentContainerStyle={[styles.content, contentWidth.column]} showsVerticalScrollIndicator={false}>
        {!request ? (
          <ActivityIndicator style={styles.loader} color={color.brand[500]} />
        ) : (
          <>
            <View style={styles.timerRow}>
              <LiveTimer expiresAt={request.basketExpiresAt} label="closes in" />
            </View>

            {request.items.map((item) => {
              const a = answers[item.basketItemId];
              if (!a) return null;
              return (
                <Card key={item.basketItemId} elevated>
                  <View style={styles.itemHead}>
                    <Text variant="h3" style={styles.itemName}>
                      {item.productName}
                    </Text>
                    <Text variant="mono" color={color.neutral.inkMuted}>
                      {item.quantity} {item.unit.toLowerCase()}
                    </Text>
                  </View>
                  {item.brand ? (
                    <Text variant="bodySm" color={color.neutral.inkFaint}>
                      Prefers {item.brand}
                    </Text>
                  ) : null}

                  <View style={styles.choices}>
                    {CHOICES.map((c) => {
                      const active = a.status === c.key;
                      const tone = color.status[c.tone];
                      return (
                        <Pressable
                          key={c.key}
                          onPress={() =>
                            setAnswers((prev) => ({
                              ...prev,
                              [item.basketItemId]: { ...prev[item.basketItemId], status: c.key },
                            }))
                          }
                          style={[
                            styles.choice,
                            active && { backgroundColor: tone.base, borderColor: tone.base },
                          ]}
                        >
                          <Text
                            variant="bodySm"
                            weight="bold"
                            color={active ? "#fff" : color.neutral.inkMuted}
                          >
                            {c.label}
                          </Text>
                        </Pressable>
                      );
                    })}
                  </View>

                  {a.status === "PARTIAL" ? (
                    <View style={styles.partialRow}>
                      <Text variant="bodySm" color={color.neutral.inkMuted}>
                        How many can you give?
                      </Text>
                      <View style={styles.partialField}>
                        <TextField
                          placeholder="0"
                          value={a.availableQuantity}
                          keyboardType="decimal-pad"
                          onChangeText={(t) =>
                            setAnswers((prev) => ({
                              ...prev,
                              [item.basketItemId]: {
                                ...prev[item.basketItemId],
                                availableQuantity: t.replace(/[^0-9.]/g, ""),
                              },
                            }))
                          }
                        />
                      </View>
                    </View>
                  ) : null}
                </Card>
              );
            })}

            {error ? (
              <Text variant="bodySm" color={color.status.urgent.strong}>
                {error}
              </Text>
            ) : null}

            <Button
              label="Send my reply"
              onPress={submit}
              loading={submitting}
              disabled={!partialsValid}
            />
            <Text variant="bodySm" color={color.neutral.inkFaint} style={styles.footnote}>
              Replying quickly raises your Bharosa Score.
            </Text>
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
  timerRow: {
    marginBottom: spacing.xxs,
  },
  itemHead: {
    flexDirection: "row",
    alignItems: "center",
    justifyContent: "space-between",
    gap: spacing.xs,
  },
  itemName: { flex: 1 },
  choices: {
    flexDirection: "row",
    gap: spacing.xs,
    marginTop: spacing.xs,
  },
  choice: {
    flex: 1,
    height: 44,
    borderRadius: radius.md,
    borderWidth: 1.5,
    borderColor: color.neutral.border,
    backgroundColor: color.neutral.surface,
    alignItems: "center",
    justifyContent: "center",
  },
  partialRow: {
    flexDirection: "row",
    alignItems: "center",
    justifyContent: "space-between",
    gap: spacing.sm,
    marginTop: spacing.xs,
  },
  partialField: { width: 100 },
  footnote: { textAlign: "center" },
  loader: { marginTop: spacing.xxl },
});
