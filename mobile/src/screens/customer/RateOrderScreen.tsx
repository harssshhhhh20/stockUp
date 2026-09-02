import React, { useState } from "react";
import { Pressable, ScrollView, StyleSheet, View } from "react-native";
import { useNavigation, useRoute } from "@react-navigation/native";
import { AppBar } from "../../components/AppBar";
import { Card } from "../../components/Card";
import { Text } from "../../components/Text";
import { Button } from "../../components/Button";
import { TextField } from "../../components/TextField";
import { useToast } from "../../components/Toast";
import { FeedbackApi } from "../../api/endpoints";
import { ApiError } from "../../api/client";
import { color, font, radius, spacing } from "../../theme/tokens";
import { contentWidth } from "../../theme/layoutStyles";

type Chip = { key: "repliedFast" | "readyOnTime" | "stockAccurate"; icon: string; label: string };

/**
 * Each chip is a second opinion on one Bharosa pillar — which is what lets
 * feedback corroborate observed behaviour instead of just decorating it. Chips
 * are three-state on purpose: yes, no, or unanswered. Forcing a verdict on
 * something the shopper didn't notice would manufacture data.
 */
const CHIPS: Chip[] = [
  { key: "repliedFast", icon: "⚡", label: "Replied quickly" },
  { key: "readyOnTime", icon: "📦", label: "Ready on time" },
  { key: "stockAccurate", icon: "✅", label: "Stock was accurate" },
];

export function RateOrderScreen() {
  const nav = useNavigation<any>();
  const route = useRoute<any>();
  const { reservationId, storeName } = route.params as {
    reservationId: string;
    storeName?: string;
  };
  const toast = useToast();

  const [stars, setStars] = useState(0);
  const [chips, setChips] = useState<Record<string, boolean | null>>({
    repliedFast: null,
    readyOnTime: null,
    stockAccurate: null,
  });
  const [comment, setComment] = useState("");
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState<string | null>(null);

  function cycle(key: string) {
    setChips((prev) => ({
      ...prev,
      // unanswered → yes → no → unanswered
      [key]: prev[key] === null ? true : prev[key] === true ? false : null,
    }));
  }

  async function submit() {
    if (stars === 0) return;
    setBusy(true);
    setError(null);
    try {
      await FeedbackApi.submit(reservationId, {
        stars,
        repliedFast: chips.repliedFast,
        readyOnTime: chips.readyOnTime,
        stockAccurate: chips.stockAccurate,
        comment: comment.trim() || null,
      });
      toast("Thanks — that helps other shoppers 🙏", "positive");
      nav.goBack();
    } catch (e) {
      setError(e instanceof ApiError ? e.message : "Couldn't send your rating.");
      setBusy(false);
    }
  }

  return (
    <View style={styles.flex}>
      <AppBar
        title="Rate this order"
        subtitle={storeName ?? undefined}
        onBack={() => nav.goBack()}
      />
      <ScrollView
        contentContainerStyle={[styles.content, contentWidth.column]}
        showsVerticalScrollIndicator={false}
        keyboardShouldPersistTaps="handled"
      >
        <Card elevated>
          <Text variant="h3">How did it go?</Text>

          <View style={styles.stars}>
            {[1, 2, 3, 4, 5].map((n) => (
              <Pressable
                key={n}
                onPress={() => setStars(n)}
                hitSlop={6}
                accessibilityRole="button"
                accessibilityLabel={`${n} star${n === 1 ? "" : "s"}`}
                style={styles.starHit}
              >
                <Text style={[styles.star, n <= stars && styles.starOn]}>★</Text>
              </Pressable>
            ))}
          </View>

          {stars > 0 ? (
            <Text variant="bodySm" color={color.neutral.inkMuted}>
              {["", "Poor", "Not great", "Fine", "Good", "Excellent"][stars]}
            </Text>
          ) : null}
        </Card>

        <Card elevated>
          <Text variant="h3">What went well?</Text>
          <Text variant="bodySm" color={color.neutral.inkMuted}>
            Tap once for yes, twice for no. Skip anything you didn't notice.
          </Text>

          <View style={styles.chips}>
            {CHIPS.map((c) => {
              const state = chips[c.key];
              return (
                <Pressable
                  key={c.key}
                  onPress={() => cycle(c.key)}
                  accessibilityRole="button"
                  accessibilityState={{ selected: state === true }}
                  accessibilityLabel={`${c.label}: ${
                    state === true ? "yes" : state === false ? "no" : "not answered"
                  }`}
                  style={[
                    styles.chip,
                    state === true && styles.chipYes,
                    state === false && styles.chipNo,
                  ]}
                >
                  <Text style={styles.chipIcon}>
                    {state === false ? "✕" : c.icon}
                  </Text>
                  <Text
                    variant="bodySm"
                    weight="semibold"
                    color={
                      state === true
                        ? color.status.positive.strong
                        : state === false
                        ? color.status.urgent.strong
                        : color.neutral.inkMuted
                    }
                  >
                    {c.label}
                  </Text>
                </Pressable>
              );
            })}
          </View>
        </Card>

        <Card elevated>
          <Text variant="h3">Anything to add?</Text>
          <TextField
            placeholder="e.g. Had it packed before I arrived"
            value={comment}
            onChangeText={setComment}
            multiline
            style={styles.comment}
            maxLength={1000}
          />
        </Card>

        {error ? (
          <Text variant="bodySm" color={color.status.urgent.strong}>
            {error}
          </Text>
        ) : null}

        <Button
          label="Send rating"
          onPress={submit}
          loading={busy}
          disabled={stars === 0}
        />
        <Text variant="bodySm" color={color.neutral.inkFaint} style={styles.footnote}>
          Only shoppers who actually collected an order can rate it.
        </Text>
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
  stars: { flexDirection: "row", gap: spacing.xs, marginTop: spacing.xs },
  starHit: { padding: 2 },
  star: { fontSize: 36, color: color.neutral.border, lineHeight: 42 },
  starOn: { color: color.status.attention.base },
  chips: { gap: spacing.xs, marginTop: spacing.xs },
  chip: {
    flexDirection: "row",
    alignItems: "center",
    gap: spacing.xs,
    paddingHorizontal: spacing.md,
    minHeight: 46,
    borderRadius: radius.md,
    borderWidth: 1.5,
    borderColor: color.neutral.border,
    backgroundColor: color.neutral.surface,
  },
  chipYes: {
    borderColor: color.status.positive.base,
    backgroundColor: color.status.positive.soft,
  },
  chipNo: {
    borderColor: color.status.urgent.base,
    backgroundColor: color.status.urgent.soft,
  },
  chipIcon: { fontSize: 15 },
  comment: {
    height: 92,
    paddingTop: spacing.sm,
    textAlignVertical: "top",
  },
  footnote: { textAlign: "center" },
});
