import React from "react";
import { ScrollView, StyleSheet, View } from "react-native";
import { useNavigation } from "@react-navigation/native";
import { AppBar } from "../../components/AppBar";
import { Card } from "../../components/Card";
import { Text } from "../../components/Text";
import { Button } from "../../components/Button";
import { BharosaRing } from "../../components/BharosaRing";
import { Logo } from "../../components/Logo";
import { useAuth } from "../../state/AuthContext";
import { color, spacing } from "../../theme/tokens";
import { contentWidth } from "../../theme/layoutStyles";

function bharosaCopy(score: number) {
  if (score >= 71) return "Customers can count on you. Keep replying and completing pickups.";
  if (score >= 41) return "You're building trust. Reply to requests and complete pickups to climb.";
  return "Your score is low. Reply to requests and don't cancel confirmed orders.";
}

export function ProfileScreen() {
  const nav = useNavigation<any>();
  const { email, mode, setMode, merchantProfile, store, signOut } = useAuth();
  const isMerchant = !!merchantProfile && !!store;

  return (
    <View style={styles.flex}>
      <AppBar title="You" subtitle={email ?? undefined} />
      <ScrollView contentContainerStyle={[styles.content, contentWidth.column]} showsVerticalScrollIndicator={false}>
        {isMerchant && merchantProfile ? (
          <Card elevated>
            <View style={styles.scoreRow}>
              <BharosaRing score={merchantProfile.bharosaScore} size="lg" />
              <View style={styles.scoreCopy}>
                <Text variant="h2">Bharosa Score</Text>
                <Text variant="bodySm" color={color.neutral.inkMuted}>
                  {bharosaCopy(merchantProfile.bharosaScore)}
                </Text>
              </View>
            </View>

            <View style={styles.rules}>
              <ScoreRule delta="+5" text="Complete a pickup" tone="positive" />
              <ScoreRule delta="+2" text="Reply to a request" tone="positive" />
              <ScoreRule delta="−2" text="Never open a request" tone="attention" />
              <ScoreRule delta="−8" text="Open it, then ignore it" tone="urgent" />
              <ScoreRule delta="−10" text="Cancel a confirmed order" tone="urgent" />
              <ScoreRule delta="−25" text="Let an order expire" tone="urgent" />
            </View>
          </Card>
        ) : null}

        {isMerchant && store ? (
          <Card elevated>
            <Text variant="caption" color={color.neutral.inkMuted}>
              Your shop
            </Text>
            <Text variant="h2">{store.name}</Text>
            <Text variant="bodySm" color={color.neutral.inkMuted}>
              {store.addressLine1}, {store.city}, {store.state} {store.postalCode}
            </Text>
          </Card>
        ) : null}

        {isMerchant ? (
          <Card elevated>
            <Text variant="caption" color={color.neutral.inkMuted}>
              Viewing StockUp as
            </Text>
            <View style={styles.modeRow}>
              <Button
                label="Shopper"
                variant={mode === "customer" ? "primary" : "secondary"}
                onPress={() => setMode("customer")}
                fullWidth={false}
                style={styles.modeBtn}
              />
              <Button
                label="Shopkeeper"
                variant={mode === "merchant" ? "primary" : "secondary"}
                onPress={() => setMode("merchant")}
                fullWidth={false}
                style={styles.modeBtn}
              />
            </View>
          </Card>
        ) : (
          <Card elevated style={styles.pitch}>
            <Logo size={40} />
            <Text variant="h2">Run a shop?</Text>
            <Text variant="body" color={color.neutral.inkMuted}>
              Get requests from customers nearby and tell them what you have in stock.
            </Text>
            <Button
              label="Set up my shop"
              onPress={() => nav.navigate("BecomeMerchant")}
              style={styles.pitchBtn}
            />
          </Card>
        )}

        <Button label="Sign out" variant="ghost" onPress={signOut} />
      </ScrollView>
    </View>
  );
}

function ScoreRule({
  delta,
  text,
  tone,
}: {
  delta: string;
  text: string;
  tone: "positive" | "attention" | "urgent";
}) {
  const c = color.status[tone];
  return (
    <View style={styles.ruleRow}>
      <View style={[styles.ruleBadge, { backgroundColor: c.soft }]}>
        <Text variant="mono" color={c.strong} style={styles.ruleDelta}>
          {delta}
        </Text>
      </View>
      <Text variant="bodySm" color={color.neutral.inkMuted} style={styles.ruleText}>
        {text}
      </Text>
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
  scoreRow: {
    flexDirection: "row",
    alignItems: "center",
    gap: spacing.md,
  },
  scoreCopy: { flex: 1, gap: 2 },
  rules: {
    marginTop: spacing.md,
    gap: 6,
  },
  ruleRow: {
    flexDirection: "row",
    alignItems: "center",
    gap: spacing.xs,
  },
  ruleBadge: {
    minWidth: 44,
    paddingHorizontal: 6,
    paddingVertical: 3,
    borderRadius: 6,
    alignItems: "center",
  },
  ruleDelta: { fontSize: 12 },
  ruleText: { flex: 1 },
  modeRow: {
    flexDirection: "row",
    gap: spacing.xs,
    marginTop: 6,
  },
  modeBtn: { flex: 1 },
  pitch: { alignItems: "flex-start", gap: 6 },
  pitchBtn: { marginTop: spacing.sm, alignSelf: "stretch" },
});
