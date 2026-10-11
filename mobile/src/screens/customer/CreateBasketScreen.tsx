import React, { useState } from "react";
import { Pressable, ScrollView, StyleSheet, View } from "react-native";
import { useNavigation, useRoute } from "@react-navigation/native";
import { AppBar } from "../../components/AppBar";
import { Card } from "../../components/Card";
import { Text } from "../../components/Text";
import { Button } from "../../components/Button";
import { TextField } from "../../components/TextField";
import { useToast } from "../../components/Toast";
import { BasketApi } from "../../api/endpoints";
import { BasketCategory, BasketItemUnit } from "../../api/types";
import { ApiError } from "../../api/client";
import { color, radius, spacing } from "../../theme/tokens";
import { contentWidth } from "../../theme/layoutStyles";
import { useLocation } from "../../state/useLocation";
import { UNITS_FOR, unitLabel } from "../../lib/units";

type Draft = {
  productName: string;
  quantity: string;
  unit: BasketItemUnit;
  brand: string;
  notes: string;
};

/**
 * A grocery list and a food order ask different questions: "which brand, how
 * many grams" versus "how many plates, how spicy". Each gets its own words,
 * units and audience — food goes only to restaurants, groceries never do.
 */
const COPY: Record<
  BasketCategory,
  {
    tab: string;
    title: string;
    subtitle: string;
    placeholder: string;
    notesPlaceholder: string | null;
    add: string;
    who: string;
    send: string;
    sent: string;
    footnote: string;
    footnoteTargeted: string;
  }
> = {
  GROCERY: {
    tab: "🛒  Groceries",
    title: "New list",
    subtitle: "What do you need?",
    placeholder: "e.g. Amul milk",
    notesPlaceholder: null,
    add: "+  Add another item",
    who: "shops",
    send: "Send to nearby shops",
    sent: "List sent to nearby shops 🎉",
    footnote: "Shops near you get 15 minutes to reply with what they have.",
    footnoteTargeted: "The shops you picked get 15 minutes to reply.",
  },
  FOOD: {
    tab: "🍽️  Food",
    title: "Order food",
    subtitle: "What would you like to eat?",
    placeholder: "e.g. Paneer butter masala",
    notesPlaceholder: "Any instructions? e.g. less spicy, no onion",
    add: "+  Add another dish",
    who: "restaurants",
    send: "Send to nearby restaurants",
    sent: "Order sent to nearby restaurants 🎉",
    footnote: "Restaurants near you get 15 minutes to say what they can make.",
    footnoteTargeted: "The restaurants you picked get 15 minutes to reply.",
  },
};

function emptyDraft(category: BasketCategory): Draft {
  return { productName: "", quantity: "1", unit: UNITS_FOR[category][0], brand: "", notes: "" };
}

export function CreateBasketScreen() {
  const nav = useNavigation<any>();
  const route = useRoute<any>();
  const toast = useToast();
  // Set when the shopper came back from picking specific shops.
  const storeIds: string[] | undefined = route.params?.storeIds;
  const targeted = !!storeIds?.length;
  const [category, setCategory] = useState<BasketCategory>(route.params?.category ?? "GROCERY");
  const copy = COPY[category];
  const units = UNITS_FOR[category];
  const { coords, status: locationStatus, request: requestLocation } = useLocation();
  const [items, setItems] = useState<Draft[]>(() => [emptyDraft(route.params?.category ?? "GROCERY")]);
  const [radiusKm, setRadiusKm] = useState(3);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const hasItems = items.some((i) => i.productName.trim() && Number(i.quantity) > 0);
  const valid = hasItems && !!coords;

  function switchCategory(next: BasketCategory) {
    if (next === category) return;
    setCategory(next);
    // Keep what was typed; only the units have to change to ones that fit.
    setItems((prev) =>
      prev.map((it) => (UNITS_FOR[next].includes(it.unit) ? it : { ...it, unit: UNITS_FOR[next][0] }))
    );
  }

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
        notes: i.notes.trim() || null,
      }));

    try {
      const res = await BasketApi.create(
        targeted
          ? {
              category,
              targetMode: "SELECTED_STORES",
              basketLatitude: coords.latitude,
              basketLongitude: coords.longitude,
              items: payloadItems,
              storeIds,
            }
          : {
              category,
              targetMode: "NEARBY",
              searchRadiusMeters: radiusKm * 1000,
              basketLatitude: coords.latitude,
              basketLongitude: coords.longitude,
              items: payloadItems,
            }
      );
      toast(targeted ? `Sent to the ${copy.who} you picked 🎉` : copy.sent, "positive");
      nav.replace("BasketDetail", { basketId: res.basketId });
    } catch (e) {
      setError(
        e instanceof ApiError
          ? e.fieldErrors[0]?.message ?? e.message
          : "Couldn't send your list. Try again."
      );
      setSubmitting(false);
    }
  }

  return (
    <View style={styles.flex}>
      <AppBar title={copy.title} subtitle={copy.subtitle} onBack={() => nav.goBack()} />
      <ScrollView
        contentContainerStyle={[styles.content, contentWidth.column]}
        keyboardShouldPersistTaps="handled"
        showsVerticalScrollIndicator={false}
      >
        {/* Locked once shops are picked: they were picked for this kind. */}
        {!targeted ? (
          <View style={styles.segment} accessibilityRole="tablist">
            {(["GROCERY", "FOOD"] as BasketCategory[]).map((c) => {
              const active = category === c;
              return (
                <Pressable
                  key={c}
                  onPress={() => switchCategory(c)}
                  accessibilityRole="tab"
                  accessibilityState={{ selected: active }}
                  style={[styles.segmentItem, active && styles.segmentItemActive]}
                >
                  <Text
                    variant="body"
                    weight="semibold"
                    color={active ? "#fff" : color.neutral.inkMuted}
                  >
                    {COPY[c].tab}
                  </Text>
                </Pressable>
              );
            })}
          </View>
        ) : null}

        {items.map((item, index) => (
          <Card key={index} elevated>
            <View style={styles.cardHead}>
              <Text variant="caption" color={color.neutral.inkMuted}>
                {category === "FOOD" ? "Dish" : "Item"} {index + 1}
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
              placeholder={copy.placeholder}
              value={item.productName}
              onChangeText={(t) => update(index, { productName: t })}
            />

            <View style={styles.qtyRow}>
              <View style={styles.qtyField}>
                <TextField
                  placeholder="1"
                  value={item.quantity}
                  onChangeText={(t) =>
                    update(index, {
                      // Half a plate is its own unit; "1.5 plates" isn't an order.
                      quantity: t.replace(category === "FOOD" ? /[^0-9]/g : /[^0-9.]/g, ""),
                    })
                  }
                  keyboardType={category === "FOOD" ? "number-pad" : "decimal-pad"}
                />
              </View>
              <ScrollView
                horizontal
                showsHorizontalScrollIndicator={false}
                contentContainerStyle={styles.units}
              >
                {units.map((u) => {
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
                        {unitLabel(u)}
                      </Text>
                    </Pressable>
                  );
                })}
              </ScrollView>
            </View>

            {copy.notesPlaceholder ? (
              <TextField
                placeholder={copy.notesPlaceholder}
                value={item.notes}
                onChangeText={(t) => update(index, { notes: t })}
              />
            ) : null}
          </Card>
        ))}

        <Button
          label={copy.add}
          variant="secondary"
          onPress={() => setItems((p) => [...p, emptyDraft(category)])}
        />

        <Card elevated>
          <View style={styles.cardHead}>
            <Text variant="caption" color={color.neutral.inkMuted}>
              {targeted ? `Asking these ${copy.who}` : "How far to look"}
            </Text>
            <Pressable
              onPress={() =>
                nav.navigate("PickShops", { selected: storeIds ?? [], category })
              }
              hitSlop={8}
            >
              <Text variant="bodySm" weight="semibold" color={color.brand[600]}>
                {targeted ? "Change" : `Pick ${copy.who} instead`}
              </Text>
            </Pressable>
          </View>

          {targeted ? (
            <Text variant="body" color={color.neutral.inkMuted}>
              {storeIds!.length} {storeIds!.length === 1 ? copy.who.slice(0, -1) : copy.who} chosen —
              only they will see this {category === "FOOD" ? "order" : "list"}.
            </Text>
          ) : null}
          <View style={[styles.radiusRow, targeted && styles.hidden]}>
            {(targeted ? [] : [1, 3, 5, 10]).map((km) => {
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
          label={
            locationStatus === "loading"
              ? "Finding you…"
              : targeted
              ? `Send to ${storeIds!.length === 1 ? `this ${copy.who.slice(0, -1)}` : `these ${copy.who}`}`
              : copy.send
          }
          onPress={submit}
          loading={submitting}
          disabled={!valid}
        />
        <Text variant="bodySm" color={color.neutral.inkFaint} style={styles.footnote}>
          {targeted ? copy.footnoteTargeted : copy.footnote}
        </Text>
      </ScrollView>
    </View>
  );
}

const styles = StyleSheet.create({
  flex: { flex: 1, backgroundColor: color.neutral.background },
  segment: {
    flexDirection: "row",
    gap: 4,
    padding: 4,
    borderRadius: radius.pill,
    backgroundColor: color.neutral.surfaceMuted,
  },
  segmentItem: {
    flex: 1,
    height: 44,
    borderRadius: radius.pill,
    alignItems: "center",
    justifyContent: "center",
  },
  segmentItemActive: { backgroundColor: color.brand[500] },
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
  hidden: { display: "none" },
  locationCard: {
    borderColor: color.status.attention.base,
    borderWidth: 1.5,
  },
  locationBtn: {
    marginTop: spacing.xs,
  },
});
