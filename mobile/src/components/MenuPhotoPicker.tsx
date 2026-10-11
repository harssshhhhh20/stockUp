import React, { useState } from "react";
import { ActivityIndicator, Image, Pressable, StyleSheet, View } from "react-native";
import { Text } from "./Text";
import { useToast } from "./Toast";
import { MAX_MENU_PAGES, pickMenuPhotos, takeMenuPhoto } from "../lib/menuPhotos";
import { color, font, radius, spacing } from "../theme/tokens";

export type MenuPhoto = { key: string; uri: string };

type Props = {
  photos: MenuPhoto[];
  /** Receives local, already-resized uris. */
  onAdd: (uris: string[]) => void | Promise<void>;
  onRemove: (key: string) => void;
  /** True while the parent is uploading or deleting. */
  busy?: boolean;
};

/**
 * Menu card pages as a grid of thumbnails, plus camera and gallery buttons.
 *
 * It only reports what was picked or removed. Shop setup holds the photos
 * locally until the shop exists; the shop screen uploads each one straight
 * away. Both use this same grid.
 */
export function MenuPhotoPicker({ photos, onAdd, onRemove, busy = false }: Props) {
  const toast = useToast();
  const [working, setWorking] = useState(false);
  const room = MAX_MENU_PAGES - photos.length;
  const disabled = busy || working;

  async function run(get: () => Promise<string[]>) {
    setWorking(true);
    try {
      const uris = await get();
      if (uris.length) await onAdd(uris);
    } catch {
      toast("Couldn't add that photo. Try again.", "urgent");
    } finally {
      setWorking(false);
    }
  }

  const camera = () =>
    run(async () => {
      const r = await takeMenuPhoto();
      if (r === "denied") {
        toast("Allow camera access in Settings to photograph your menu.", "info");
        return [];
      }
      return r ? [r] : [];
    });

  const gallery = () => run(() => pickMenuPhotos(room));

  return (
    <View style={styles.wrap}>
      {photos.length ? (
        <View style={styles.grid}>
          {photos.map((p, i) => (
            <View key={p.key} style={styles.thumbWrap}>
              <Image source={{ uri: p.uri }} style={styles.thumb} resizeMode="cover" />
              <Text style={styles.pageNo}>{i + 1}</Text>
              <Pressable
                onPress={() => onRemove(p.key)}
                disabled={disabled}
                hitSlop={8}
                accessibilityRole="button"
                accessibilityLabel={`Remove menu page ${i + 1}`}
                style={styles.remove}
              >
                <Text style={styles.removeText}>×</Text>
              </Pressable>
            </View>
          ))}
        </View>
      ) : null}

      {room > 0 ? (
        <View style={styles.actions}>
          <AddButton label="📷  Take photo" onPress={camera} disabled={disabled} />
          <AddButton label="🖼️  From gallery" onPress={gallery} disabled={disabled} />
        </View>
      ) : (
        <Text variant="bodySm" color={color.neutral.inkFaint}>
          That's the most pages a menu can have ({MAX_MENU_PAGES}).
        </Text>
      )}

      {disabled ? <ActivityIndicator color={color.brand[500]} /> : null}
    </View>
  );
}

function AddButton({
  label,
  onPress,
  disabled,
}: {
  label: string;
  onPress: () => void;
  disabled: boolean;
}) {
  return (
    <Pressable
      onPress={onPress}
      disabled={disabled}
      accessibilityRole="button"
      style={({ pressed }) => [styles.add, (pressed || disabled) && styles.addDim]}
    >
      <Text variant="bodySm" weight="semibold" color={color.brand[600]}>
        {label}
      </Text>
    </Pressable>
  );
}

const THUMB = 92;

const styles = StyleSheet.create({
  wrap: { gap: spacing.sm, marginTop: spacing.xs },
  grid: { flexDirection: "row", flexWrap: "wrap", gap: spacing.xs },
  thumbWrap: {
    width: THUMB,
    height: THUMB * 1.3,
    borderRadius: radius.md,
    overflow: "hidden",
    backgroundColor: color.neutral.surfaceMuted,
    borderWidth: 1,
    borderColor: color.neutral.border,
  },
  thumb: { width: "100%", height: "100%" },
  pageNo: {
    position: "absolute",
    left: 6,
    bottom: 6,
    fontFamily: font.body.bold,
    fontSize: 11,
    lineHeight: 14,
    color: "#fff",
    backgroundColor: "rgba(20,25,20,0.6)",
    borderRadius: radius.pill,
    paddingHorizontal: 6,
    paddingVertical: 1,
    overflow: "hidden",
  },
  remove: {
    position: "absolute",
    top: 4,
    right: 4,
    width: 24,
    height: 24,
    borderRadius: 12,
    backgroundColor: "rgba(20,25,20,0.6)",
    alignItems: "center",
    justifyContent: "center",
  },
  removeText: { color: "#fff", fontSize: 16, lineHeight: 18, fontFamily: font.body.bold },
  actions: { flexDirection: "row", gap: spacing.xs },
  add: {
    flex: 1,
    height: 44,
    borderRadius: radius.pill,
    borderWidth: 1,
    borderStyle: "dashed",
    borderColor: color.brand[300],
    backgroundColor: color.brand[50],
    alignItems: "center",
    justifyContent: "center",
  },
  addDim: { opacity: 0.6 },
});
