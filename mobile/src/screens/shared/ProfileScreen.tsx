import React, { useCallback, useState } from "react";
import { ScrollView, StyleSheet, View } from "react-native";
import { useFocusEffect, useNavigation } from "@react-navigation/native";
import { AppBar } from "../../components/AppBar";
import { Card } from "../../components/Card";
import { Text } from "../../components/Text";
import { Button } from "../../components/Button";
import { BharosaRing } from "../../components/BharosaRing";
import { OrderApi } from "../../api/endpoints";
import { MerchantStats } from "../../api/types";
import { Logo } from "../../components/Logo";
import { useAuth } from "../../state/AuthContext";
import { color, spacing, font } from "../../theme/tokens";
import { contentWidth } from "../../theme/layoutStyles";

/**
 * Thresholds mirror the engine's own bands (trusted 75+, mixed 45+), so the
 * encouragement a shopkeeper reads never contradicts the colour and wording a
 * shopper sees for the same score.
 */
function bharosaCopy(score: number) {
  if (score >= 75) return "Customers can count on you. Keep replying and completing pickups.";
  if (score >= 45) return "You're building trust. Reply faster and complete every pickup to climb.";
  return "Your score is low. Reply to requests and don't cancel confirmed orders.";
}

export function ProfileScreen() {
  const nav = useNavigation<any>();
  const { email, profile, mode, setMode, merchantProfile, store, signOut } = useAuth();
  const isMerchant = !!merchantProfile && !!store;
  const [stats, setStats] = useState<MerchantStats | null>(null);

  useFocusEffect(
    useCallback(() => {
      if (!isMerchant) return;
      OrderApi.merchantStats(30)
        .then(setStats)
        .catch(() => setStats(null));
    }, [isMerchant])
  );

  return (
    <View style={styles.flex}>
      <AppBar title="You" subtitle={email ?? undefined} />
      <ScrollView contentContainerStyle={[styles.content, contentWidth.column]} showsVerticalScrollIndicator={false}>
        {/* Identity — who StockUp thinks you are, and how shops reach you. */}
        <Card elevated>
          <View style={styles.identityRow}>
            <View style={styles.avatar}>
              <Text style={styles.avatarText}>
                {(profile?.firstName?.[0] ?? email?.[0] ?? "?").toUpperCase()}
              </Text>
            </View>
            <View style={styles.identityCopy}>
              <Text variant="h2" numberOfLines={1}>
                {[profile?.firstName, profile?.lastName].filter(Boolean).join(" ") || "Add your name"}
              </Text>
              <Text variant="bodySm" color={color.neutral.inkMuted} numberOfLines={1}>
                {email}
              </Text>
            </View>
          </View>

          <View style={styles.detailRows}>
            <DetailRow icon="📱" label="Phone" value={profile?.phone ?? "Not added"} />
            <DetailRow
              icon="🪪"
              label="Account"
              value={profile?.isMerchant ? "Shopper + shopkeeper" : "Shopper"}
            />
            {profile?.hasStore ? (
              <DetailRow icon="🏪" label="Shop" value={profile.storeName ?? "—"} />
            ) : null}
          </View>
        </Card>

        {isMerchant && merchantProfile ? (
          <Card elevated>
            <View style={styles.scoreRow}>
              <BharosaRing score={stats?.bharosaScore ?? merchantProfile.bharosaScore} size="lg" />
              <View style={styles.scoreCopy}>
                <Text variant="h2">Bharosa Score</Text>
                <Text variant="bodySm" color={color.neutral.inkMuted}>
                  {bharosaCopy(stats?.bharosaScore ?? merchantProfile.bharosaScore)}
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

        {isMerchant && stats ? (
          <Card elevated>
            <Text variant="caption" color={color.neutral.inkMuted}>
              Last 30 days
            </Text>
            <Text variant="bodySm" color={color.neutral.inkMuted} style={styles.statsIntro}>
              These are the numbers your Bharosa is built from.
            </Text>

            <View style={styles.statGrid}>
              <Stat label="Orders completed" value={String(stats.ordersCompleted)} />
              <Stat
                label="Average reply"
                value={
                  stats.averageResponseSeconds == null
                    ? "—"
                    : stats.averageResponseSeconds < 60
                    ? `${stats.averageResponseSeconds}s`
                    : `${Math.floor(stats.averageResponseSeconds / 60)}m ${
                        stats.averageResponseSeconds % 60
                      }s`
                }
              />
              <Stat label="Requests answered" value={pct(stats.answeredRate)} />
              <Stat label="Completion rate" value={pct(stats.completionRate)} />
              <Stat label="Cancellations" value={pct(stats.cancellationRate)} />
              <Stat label="Different shoppers" value={String(stats.distinctCustomers)} />
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

/** One labelled fact. Kept flat and scannable rather than boxed. */
function DetailRow({ icon, label, value }: { icon: string; label: string; value: string }) {
  return (
    <View style={styles.detailRow}>
      <Text style={styles.detailIcon}>{icon}</Text>
      <Text variant="bodySm" color={color.neutral.inkMuted} style={styles.detailLabel}>
        {label}
      </Text>
      <Text variant="bodySm" weight="semibold" style={styles.detailValue} numberOfLines={1}>
        {value}
      </Text>
    </View>
  );
}

function pct(v: number | null) {
  return v == null ? "—" : `${Math.round(v * 100)}%`;
}

/** One measured figure. Deliberately plain — this is a dashboard, not a poster. */
function Stat({ label, value }: { label: string; value: string }) {
  return (
    <View style={styles.stat}>
      <Text style={styles.statValue}>{value}</Text>
      <Text variant="bodySm" color={color.neutral.inkMuted}>
        {label}
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
  identityRow: { flexDirection: "row", alignItems: "center", gap: spacing.md },
  avatar: {
    width: 52,
    height: 52,
    borderRadius: 26,
    backgroundColor: color.brand[50],
    alignItems: "center",
    justifyContent: "center",
  },
  avatarText: {
    fontFamily: font.display.bold,
    fontSize: 22,
    color: color.brand[600],
  },
  identityCopy: { flex: 1, gap: 1 },
  detailRows: {
    marginTop: spacing.md,
    gap: spacing.xs,
    borderTopWidth: 1,
    borderTopColor: color.neutral.border,
    paddingTop: spacing.sm,
  },
  detailRow: { flexDirection: "row", alignItems: "center", gap: spacing.xs },
  detailIcon: { fontSize: 14, width: 20 },
  detailLabel: { width: 74 },
  detailValue: { flex: 1, textAlign: "right" },
  statsIntro: { marginTop: -2, marginBottom: spacing.xs },
  statGrid: {
    flexDirection: "row",
    flexWrap: "wrap",
    gap: spacing.sm,
    marginTop: spacing.xs,
  },
  stat: {
    minWidth: "30%",
    flexGrow: 1,
    gap: 1,
  },
  statValue: {
    fontFamily: font.mono.bold,
    fontSize: 20,
    color: color.neutral.ink,
  },
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
