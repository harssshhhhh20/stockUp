import React, { useState } from "react";
import { Pressable, ScrollView, StyleSheet, View } from "react-native";
import { useNavigation } from "@react-navigation/native";
import { AppBar } from "../../components/AppBar";
import { Card } from "../../components/Card";
import { Text } from "../../components/Text";
import { Button } from "../../components/Button";
import { TextField } from "../../components/TextField";
import { useToast } from "../../components/Toast";
import { BasketApi } from "../../api/endpoints";
import { BasketItemUnit } from "../../api/types";
import { ApiError } from "../../api/client";
import { color, radius, spacing } from "../../theme/tokens";
import { contentWidth } from "../../theme/layoutStyles";
import { useLocation } from "../../state/useLocation";

const UNITS: BasketItemUnit[] = [
  "PIECE",
  "PACKET",
  "KG",
  "GRAM",
  "LITRE",
  "ML",
  "BOX",
  "BOTTLE",
  "DOZEN",
  "STRIP",
  "CAN",
];

const UNIT_LABEL: Record<BasketItemUnit, string> = {
  PIECE: "pc",
  PACKET: "packet",
  KG: "kg",
  GRAM: "g",
  LITRE: "L",
  ML: "ml",
  BOX: "box",
  BOTTLE: "bottle",
  DOZEN: "dozen",
  STRIP: "strip",
  CAN: "can",
};

type Draft = {
  productName: string;
  quantity: string;
  unit: BasketItemUnit;
  brand: string;
};

const EMPTY: Draft = { productName: "", quantity: "1", unit: "PIECE", brand: "" };

export function CreateBasketScreen() {
  const nav = useNavigation<any>();
  const toast = useToast();
  const { coords, status: locationStatus, request: requestLocation } = useLocation();
  const [items, setItems] = useState<Draft[]>([{ ...EMPTY }]);
  const [radiusKm, setRadiusKm] = useState(3);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const hasItems = items.some((i) => i.productName.trim() && Number(i.quantity) > 0);
  const valid = hasItems && !!coords;

  function update(index: number, patch: Partial<Draft>) {
    setItems((prev) => prev.map((it, i) => (i === index ? { ...it, ...patch } : it)));
  }

  async function submit() {
    if (!coords) return;
    setSubmitting(true);
    setError(null);
    const payloadItems = items
      .filter((i) => i.productName.trim() && Number(i.quantity) > 0)
      .map((i) => ({
        productName: i.productName.trim(),
        quantity: Number(i.quantity),
        unit: i.unit,
        brand: i.brand.trim() || null,
        notes: null,
      }));

    try {
      const res = await BasketApi.create({
        targetMode: "NEARBY",
        searchRadiusMeters: radiusKm * 1000,
        basketLatitude: coords.latitude,
        basketLongitude: coords.longitude,
        items: payloadItems,
      });
      toast("List sent to nearby shops 🎉", "positive");
      nav.replace("BasketDetail", { basketId: res.basketId });
    } catch (e) {
      setError(
        e instanceof ApiError ? e.message : "Couldn't send your list. Try again."
      );
      setSubmitting(false);
    }
  }

  return (
    <View style={styles.flex}>
      <AppBar title="New list" subtitle="What do you need?" onBack={() => nav.goBack()} />
      <ScrollView contentContainerStyle={[styles.content, contentWidth.column]} showsVerticalScrollIndicator={false}>
        {items.map((item, index) => (
          <Card key={index} elevated>
            <View style={styles.cardHead}>
              <Text variant="caption" color={color.neutral.inkMuted}>
                Item {index + 1}
              </Text>
              {items.length > 1 ? (
                <Pressable
                  onPress={() => setItems((p) => p.filter((_, i) => i !== index))}
                  hitSlop={8}
                >
                  <Text variant="bodySm" color={color.status.urgent.strong}>
                    Remove
                  </Text>
                </Pressable>
              ) : null}
            </View>

            <TextField
              placeholder="e.g. Amul milk"
              value={item.productName}
              onChangeText={(t) => update(index, { productName: t })}
            />

            <View style={styles.qtyRow}>
              <View style={styles.qtyField}>
                <TextField
                  placeholder="1"
                  value={item.quantity}
                  onChangeText={(t) => update(index, { quantity: t.replace(/[^0-9.]/g, "") })}
                  keyboardType="decimal-pad"
                />
              </View>
              <ScrollView
                horizontal
                showsHorizontalScrollIndicator={false}
                contentContainerStyle={styles.units}
              >
                {UNITS.map((u) => {
                  const active = item.unit === u;
                  return (
                    <Pressable
                      key={u}
                      onPress={() => update(index, { unit: u })}
                      style={[styles.unitChip, active && styles.unitChipActive]}
                    >
                      <Text
                        variant="bodySm"
                        weight="semibold"
                        color={active ? "#fff" : color.neutral.inkMuted}
                      >
                        {UNIT_LABEL[u]}
                      </Text>
                    </Pressable>
                  );
                })}
              </ScrollView>
            </View>
          </Card>
        ))}

        <Button
          label="+  Add another item"
          variant="secondary"
          onPress={() => setItems((p) => [...p, { ...EMPTY }])}
        />

        <Card elevated>
          <Text variant="caption" color={color.neutral.inkMuted}>
            How far to look
          </Text>
          <View style={styles.radiusRow}>
            {[1, 3, 5, 10].map((km) => {
              const active = radiusKm === km;
              return (
                <Pressable
                  key={km}
                  onPress={() => setRadiusKm(km)}
                  style={[styles.radiusChip, active && styles.radiusChipActive]}
                >
                  <Text
                    variant="body"
                    weight="bold"
                    color={active ? "#fff" : color.neutral.inkMuted}
                  >
                    {km} km
                  </Text>
                </Pressable>
              );
            })}
          </View>
        </Card>

        {locationStatus === "denied" || locationStatus === "error" ? (
          <Card elevated style={styles.locationCard}>
            <Text variant="h3">We need your location</Text>
            <Text variant="bodySm" color={color.neutral.inkMuted}>
              StockUp asks shops near you — so it needs to know where "near you" is.
              Nothing is shared with shops except the search area.
            </Text>
            <Button
              label="Allow location"
              variant="secondary"
              onPress={requestLocation}
              style={styles.locationBtn}
            />
          </Card>
        ) : null}

        {error ? (
          <Text variant="bodySm" color={color.status.urgent.strong}>
            {error}
          </Text>
        ) : null}

        <Button
          label={locationStatus === "loading" ? "Finding you…" : "Send to nearby shops"}
          onPress={submit}
          loading={submitting}
          disabled={!valid}
        />
        <Text variant="bodySm" color={color.neutral.inkFaint} style={styles.footnote}>
          Shops near you get 15 minutes to reply with what they have.
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
  cardHead: {
    flexDirection: "row",
    justifyContent: "space-between",
    alignItems: "center",
  },
  qtyRow: {
    flexDirection: "row",
    gap: spacing.xs,
    alignItems: "center",
  },
  qtyField: {
    width: 76,
  },
  units: {
    gap: 6,
    paddingRight: spacing.xs,
  },
  unitChip: {
    paddingHorizontal: spacing.md,
    height: 44,
    borderRadius: radius.pill,
    borderWidth: 1,
    borderColor: color.neutral.border,
    backgroundColor: color.neutral.surface,
    alignItems: "center",
    justifyContent: "center",
  },
  unitChipActive: {
    backgroundColor: color.brand[500],
    borderColor: color.brand[500],
  },
  radiusRow: {
    flexDirection: "row",
    gap: spacing.xs,
    marginTop: 6,
  },
  radiusChip: {
    flex: 1,
    height: 46,
    borderRadius: radius.md,
    borderWidth: 1,
    borderColor: color.neutral.border,
    alignItems: "center",
    justifyContent: "center",
  },
  radiusChipActive: {
    backgroundColor: color.brand[500],
    borderColor: color.brand[500],
  },
  footnote: {
    textAlign: "center",
  },
  locationCard: {
    borderColor: color.status.attention.base,
    borderWidth: 1.5,
  },
  locationBtn: {
    marginTop: spacing.xs,
  },
});
