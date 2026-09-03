import React, { useEffect, useRef } from "react";
import { Animated, Easing, Modal, Pressable, StyleSheet, View } from "react-native";
import { useSafeAreaInsets } from "react-native-safe-area-context";
import { BharosaResponse } from "../api/types";
import { color, font, radius, spacing } from "../theme/tokens";
import { Text } from "./Text";
import { ReputationBanner } from "./ReputationBanner";

type Props = {
  visible: boolean;
  onClose: () => void;
  data: BharosaResponse | null;
};

/**
 * "Why this score?" — the explanation, in sentences.
 *
 * No formula is ever shown. The reasons come from the server's explainer, and
 * deliberately include an unflattering line when one applies: a score that only
 * ever explains itself in praise reads as marketing, and shoppers stop
 * believing it.
 */
export function BharosaSheet({ visible, onClose, data }: Props) {
  const insets = useSafeAreaInsets();
  const slide = useRef(new Animated.Value(0)).current;

  useEffect(() => {
    Animated.timing(slide, {
      toValue: visible ? 1 : 0,
      duration: visible ? 260 : 180,
      easing: visible ? Easing.out(Easing.cubic) : Easing.in(Easing.cubic),
      useNativeDriver: true,
    }).start();
  }, [visible, slide]);

  const translateY = slide.interpolate({ inputRange: [0, 1], outputRange: [420, 0] });

  if (!data) return null;

  const bandCopy: Record<string, string> = {
    trusted: "Customers can count on this shop.",
    mixed: "Reasonably dependable, with room to improve.",
    risky: "Has let customers down more than once recently.",
    new: "Too new to judge fairly yet.",
  };

  return (
    <Modal visible={visible} transparent animationType="fade" onRequestClose={onClose}>
      <Pressable style={styles.backdrop} onPress={onClose} accessibilityLabel="Close" />
      <Animated.View
        style={[
          styles.sheet,
          { paddingBottom: insets.bottom + spacing.lg, transform: [{ translateY }] },
        ]}
      >
        <View style={styles.grabber} />

        <View style={styles.header}>
          <Text style={styles.score}>{data.score}</Text>
          <View style={styles.headerText}>
            <Text variant="h2">Bharosa</Text>
            {data.storeName ? (
              <Text variant="bodySm" color={color.neutral.inkMuted}>
                {data.storeName}
              </Text>
            ) : null}
          </View>
        </View>

        <Text variant="body" color={color.neutral.inkMuted} style={styles.band}>
          {bandCopy[data.band] ?? ""}
        </Text>

        {data.tags?.length ? (
          <View style={styles.bannerWrap}>
            <ReputationBanner tags={data.tags} />
          </View>
        ) : null}

        <Text variant="caption" color={color.neutral.inkMuted} style={styles.whyLabel}>
          Why this score
        </Text>

        <View style={styles.reasons}>
          {data.reasons.map((r, i) => (
            <View key={i} style={styles.reasonRow}>
              <Text style={styles.reasonIcon}>{r.slice(0, 2).trim()}</Text>
              <Text variant="bodySm" color={color.neutral.ink} style={styles.reasonText}>
                {r.slice(2).trim()}
              </Text>
            </View>
          ))}
        </View>

        <Text variant="bodySm" color={color.neutral.inkFaint} style={styles.footnote}>
          Based on how this shop has actually behaved
          {data.distinctCustomers > 0
            ? ` with ${data.distinctCustomers} shopper${data.distinctCustomers === 1 ? "" : "s"}`
            : ""}
          {" "}— not on star ratings alone.
        </Text>
      </Animated.View>
    </Modal>
  );
}

const styles = StyleSheet.create({
  backdrop: {
    ...StyleSheet.absoluteFill,
    backgroundColor: "rgba(20,25,20,0.35)",
  },
  sheet: {
    position: "absolute",
    left: 0,
    right: 0,
    bottom: 0,
    backgroundColor: color.neutral.surface,
    borderTopLeftRadius: 22,
    borderTopRightRadius: 22,
    paddingHorizontal: spacing.lg,
    paddingTop: spacing.sm,
    gap: spacing.sm,
  },
  grabber: {
    alignSelf: "center",
    width: 38,
    height: 4,
    borderRadius: 2,
    backgroundColor: color.neutral.border,
    marginBottom: spacing.sm,
  },
  header: {
    flexDirection: "row",
    alignItems: "center",
    gap: spacing.md,
  },
  score: {
    fontFamily: font.mono.bold,
    fontSize: 40,
    color: color.brand[600],
    lineHeight: 44,
  },
  headerText: { flex: 1, gap: 1 },
  band: { marginTop: -2 },
  bannerWrap: { marginTop: 2 },
  whyLabel: { marginTop: spacing.sm },
  reasons: { gap: spacing.xs },
  reasonRow: {
    flexDirection: "row",
    alignItems: "flex-start",
    gap: spacing.xs,
  },
  reasonIcon: { fontSize: 15, lineHeight: 22, width: 22 },
  reasonText: { flex: 1 },
  footnote: { marginTop: spacing.xs },
});
