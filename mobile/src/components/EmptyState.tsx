import React from "react";
import { StyleSheet, View } from "react-native";
import { color, spacing } from "../theme/tokens";
import { Text } from "./Text";

type Props = {
  emoji: string;
  title: string;
  body?: string;
  action?: React.ReactNode;
};

/**
 * A friendly, on-brand empty state — warm marigold badge behind a single
 * emoji, never a generic "nothing here" gray box.
 */
export function EmptyState({ emoji, title, body, action }: Props) {
  return (
    <View style={styles.wrap}>
      <View style={styles.badge}>
        <Text style={styles.emoji}>{emoji}</Text>
      </View>
      <Text variant="h2" style={styles.title}>
        {title}
      </Text>
      {body ? (
        <Text variant="body" color={color.neutral.inkMuted} style={styles.body}>
          {body}
        </Text>
      ) : null}
      {action ? <View style={styles.action}>{action}</View> : null}
    </View>
  );
}

const styles = StyleSheet.create({
  wrap: {
    alignItems: "center",
    paddingVertical: spacing.xxl,
    paddingHorizontal: spacing.xl,
    gap: spacing.xs,
  },
  badge: {
    width: 72,
    height: 72,
    borderRadius: 36,
    backgroundColor: color.marigold[100],
    alignItems: "center",
    justifyContent: "center",
    marginBottom: spacing.xs,
  },
  emoji: {
    fontSize: 32,
    lineHeight: 40,
  },
  title: {
    textAlign: "center",
  },
  body: {
    textAlign: "center",
    maxWidth: 280,
  },
  action: {
    marginTop: spacing.md,
    alignSelf: "stretch",
  },
});
