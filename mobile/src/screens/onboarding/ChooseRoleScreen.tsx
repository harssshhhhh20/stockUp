import React from "react";
import { StyleSheet, View } from "react-native";
import { useSafeAreaInsets } from "react-native-safe-area-context";
import { Text } from "../../components/Text";
import { Card } from "../../components/Card";
import { FadeIn, PressableScale } from "../../components/Motion";
import { useAuth } from "../../state/AuthContext";
import { color, font, radius, spacing } from "../../theme/tokens";
import { contentWidth } from "../../theme/layoutStyles";

/**
 * The fork. Two doors, equal weight — neither is the "real" app with the other
 * bolted on, because a shopkeeper is also somebody's customer.
 *
 * Copy is written from each side's own words: a shopper thinks "find things",
 * a shopkeeper thinks "sell what I have".
 */
export function ChooseRoleScreen() {
  const { chooseRole } = useAuth();
  const insets = useSafeAreaInsets();

  return (
    <View style={[styles.flex, { paddingTop: insets.top + spacing.xxl }]}>
      <View style={[styles.inner, contentWidth.column]}>
        <FadeIn>
          <Text variant="hero" style={styles.title}>
            How will you{"\n"}use StockUp?
          </Text>
          <Text variant="body" color={color.neutral.inkMuted} style={styles.sub}>
            We'll ask again next time you sign in — plenty of people do both.
          </Text>
        </FadeIn>

        <FadeIn index={1}>
          <PressableScale onPress={() => chooseRole("customer")} accessibilityLabel="I'm shopping">
            <Card elevated style={styles.card}>
              <View style={[styles.iconWrap, { backgroundColor: color.brand[50] }]}>
                <Text style={styles.icon}>🧺</Text>
              </View>
              <View style={styles.copy}>
                <Text variant="h2">I'm shopping</Text>
                <Text variant="bodySm" color={color.neutral.inkMuted}>
                  Ask nearby shops what's in stock before walking over.
                </Text>
              </View>
              <Text style={styles.chev}>›</Text>
            </Card>
          </PressableScale>
        </FadeIn>

        <FadeIn index={2}>
          <PressableScale onPress={() => chooseRole("merchant")} accessibilityLabel="I run a shop">
            <Card elevated style={styles.card}>
              <View style={[styles.iconWrap, { backgroundColor: color.marigold[100] }]}>
                <Text style={styles.icon}>🏪</Text>
              </View>
              <View style={styles.copy}>
                <Text variant="h2">I run a shop</Text>
                <Text variant="bodySm" color={color.neutral.inkMuted}>
                  Get requests from people nearby and reply with what you have.
                </Text>
              </View>
              <Text style={styles.chev}>›</Text>
            </Card>
          </PressableScale>
        </FadeIn>

        <FadeIn index={3}>
          <Text variant="bodySm" color={color.neutral.inkFaint} style={styles.footnote}>
            Shops are ranked on how reliably they actually behave — not on ads or ratings alone.
          </Text>
        </FadeIn>
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  flex: { flex: 1, backgroundColor: color.neutral.background },
  inner: { paddingHorizontal: spacing.lg, gap: spacing.sm },
  title: { marginBottom: spacing.xs },
  sub: { marginBottom: spacing.lg },
  card: {
    flexDirection: "row",
    alignItems: "center",
    gap: spacing.md,
    paddingVertical: spacing.lg,
  },
  iconWrap: {
    width: 54,
    height: 54,
    borderRadius: radius.md,
    alignItems: "center",
    justifyContent: "center",
  },
  // Emoji sit taller than their font box; without an explicit lineHeight the
  // variant default (22) crops them on iOS.
  icon: { fontSize: 26, lineHeight: 34 },
  copy: { flex: 1, gap: 2 },
  chev: {
    fontFamily: font.display.bold,
    fontSize: 26,
    lineHeight: 32,
    color: color.neutral.inkFaint,
  },
  footnote: { marginTop: spacing.lg, textAlign: "center" },
});
