import React from "react";
import { View, Text, StyleSheet } from "react-native";
import { color, font, radius, spacing, StatusKey } from "../theme/tokens";

type Props = {
  status: StatusKey;
  label: string;
  dot?: boolean;
};

/**
 * The single source of truth for "what does this color mean" across StockUp.
 * Every status surface in the app — basket state, order state, merchant
 * standing, notification type — renders through this component so the same
 * five colors always carry the same meaning.
 */
export function StatusPill({ status, label, dot = true }: Props) {
  const c = color.status[status];

  return (
    <View
      style={[styles.pill, { backgroundColor: c.soft }]}
      accessibilityRole="text"
      accessibilityLabel={`Status: ${label}`}
    >
      {dot ? <View style={[styles.dot, { backgroundColor: c.base }]} /> : null}
      <Text style={[styles.label, { color: c.strong }]} numberOfLines={1}>
        {label}
      </Text>
    </View>
  );
}

const styles = StyleSheet.create({
  pill: {
    flexDirection: "row",
    alignItems: "center",
    alignSelf: "flex-start",
    paddingVertical: 5,
    paddingHorizontal: spacing.xs,
    borderRadius: radius.pill,
    gap: 6,
  },
  dot: {
    width: 6,
    height: 6,
    borderRadius: 3,
  },
  label: {
    fontFamily: font.body.semibold,
    fontSize: 12,
    letterSpacing: 0.1,
  },
});
