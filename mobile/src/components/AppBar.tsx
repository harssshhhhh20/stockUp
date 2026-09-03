import React from "react";
import { StyleSheet, View, Pressable } from "react-native";
import Svg, { Path } from "react-native-svg";
import { useSafeAreaInsets } from "react-native-safe-area-context";
import { color, spacing } from "../theme/tokens";
import { contentWidth } from "../theme/layoutStyles";
import { Text } from "./Text";

type Props = {
  title: string;
  subtitle?: string;
  right?: React.ReactNode;
  onBack?: () => void;
};

export function AppBar({ title, subtitle, right, onBack }: Props) {
  const insets = useSafeAreaInsets();

  return (
    <View style={[styles.wrap, { paddingTop: Math.max(insets.top, spacing.md) + spacing.sm }]}>
      <View style={[styles.row, contentWidth.column]}>
        {onBack ? (
          <Pressable
            onPress={onBack}
            hitSlop={10}
            style={styles.back}
            accessibilityRole="button"
            accessibilityLabel="Go back"
          >
            <Svg width={22} height={22} viewBox="0 0 24 24">
              <Path
                d="M15 5 8 12l7 7"
                stroke={color.brand[600]}
                strokeWidth={2.4}
                strokeLinecap="round"
                strokeLinejoin="round"
                fill="none"
              />
            </Svg>
          </Pressable>
        ) : null}
        <View style={styles.titleWrap}>
          <Text variant="h1">{title}</Text>
          {subtitle ? (
            <Text variant="bodySm" color={color.neutral.inkMuted}>
              {subtitle}
            </Text>
          ) : null}
        </View>
        {right ? <View>{right}</View> : null}
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  wrap: {
    backgroundColor: color.neutral.background,
    paddingHorizontal: spacing.lg,
    paddingBottom: spacing.md,
  },
  row: {
    flexDirection: "row",
    // Top-aligned, not centred: with a subtitle present, centring pushed the
    // chevron halfway down a two-line block and left it floating below the
    // title it belongs to.
    alignItems: "flex-start",
    gap: spacing.sm,
  },
  back: {
    width: 44,
    // Matches the h1 line box so the chevron sits on the title's line. The
    // 44pt touch target is preserved by hitSlop rather than by height.
    height: 30,
    alignItems: "center",
    justifyContent: "center",
    marginLeft: -spacing.sm,
  },
  titleWrap: {
    flex: 1,
    gap: 2,
  },
});
