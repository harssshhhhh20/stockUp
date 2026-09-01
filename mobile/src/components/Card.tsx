import React from "react";
import { Pressable, StyleSheet, View, ViewStyle } from "react-native";
import { color, radius, spacing, shadow } from "../theme/tokens";

type Props = {
  children: React.ReactNode;
  onPress?: () => void;
  style?: ViewStyle;
  elevated?: boolean;
};

export function Card({ children, onPress, style, elevated = false }: Props) {
  const content = (
    <View
      style={[
        styles.card,
        elevated && shadow.card,
        style,
      ]}
    >
      {children}
    </View>
  );

  if (!onPress) return content;

  return (
    <Pressable
      onPress={onPress}
      style={({ pressed }) => [pressed && styles.pressed]}
    >
      {content}
    </Pressable>
  );
}

const styles = StyleSheet.create({
  card: {
    backgroundColor: color.neutral.surface,
    borderRadius: radius.lg,
    borderWidth: 1,
    borderColor: color.neutral.border,
    padding: spacing.md,
    gap: spacing.xs,
  },
  pressed: {
    opacity: 0.92,
  },
});
