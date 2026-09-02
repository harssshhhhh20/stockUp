import React from "react";
import { StyleSheet, View } from "react-native";
import { MapProps, MapPlaceholder, mapStyles, pinTone } from "./NearbyMap.shared";
import { Pulse } from "./Motion";
import { color } from "../theme/tokens";
import { Text } from "./Text";

/**
 * Default (web) map: a schematic stand-in.
 *
 * react-native-maps has no web implementation, and Metro pulls it into the
 * graph even from inside a `Platform.OS` check — so the split has to happen at
 * the file level. `NearbyMap.native.tsx` holds the real map and wins on device;
 * this version keeps the browser preview useful for layout work.
 *
 * Same information, no cartography.
 */
export function NearbyMap({ coords, stores, height = 190 }: MapProps) {
  if (!coords) return <MapPlaceholder height={height} />;

  const visible = stores.slice(0, 6);
  const spread = Math.max(...stores.map((s) => s.distanceKm), 1);

  return (
    <View style={[mapStyles.wrap, mapStyles.schematic, { height }]}>
      <View style={styles.grid} pointerEvents="none">
        {Array.from({ length: 5 }).map((_, i) => (
          <View key={`h${i}`} style={[styles.gridLine, { top: `${(i + 1) * 16}%` }]} />
        ))}
        {Array.from({ length: 5 }).map((_, i) => (
          <View key={`v${i}`} style={[styles.gridLineV, { left: `${(i + 1) * 16}%` }]} />
        ))}
      </View>

      <Pulse style={styles.meWrap}>
        <View style={styles.meHalo} />
        <View style={styles.me} />
      </Pulse>

      {visible.map((s, i) => {
        const angle = (i / Math.max(1, visible.length)) * 2 * Math.PI;
        const r = 12 + (s.distanceKm / spread) * 28;
        return (
          <View
            key={s.storeId}
            style={[
              styles.schematicPin,
              pinTone(s.band),
              { left: `${50 + r * Math.cos(angle)}%`, top: `${50 + r * Math.sin(angle)}%` },
            ]}
          >
            <Text style={mapStyles.pinText}>{s.bharosa}</Text>
          </View>
        );
      })}
    </View>
  );
}

const styles = StyleSheet.create({
  grid: { ...StyleSheet.absoluteFillObject },
  gridLine: {
    position: "absolute",
    left: 0,
    right: 0,
    height: 1,
    backgroundColor: color.neutral.border,
    opacity: 0.5,
  },
  gridLineV: {
    position: "absolute",
    top: 0,
    bottom: 0,
    width: 1,
    backgroundColor: color.neutral.border,
    opacity: 0.5,
  },
  meWrap: {
    position: "absolute",
    left: "50%",
    top: "50%",
    marginLeft: -18,
    marginTop: -18,
    width: 36,
    height: 36,
    alignItems: "center",
    justifyContent: "center",
  },
  meHalo: {
    position: "absolute",
    width: 36,
    height: 36,
    borderRadius: 18,
    backgroundColor: color.brand[500],
    opacity: 0.18,
  },
  me: {
    width: 14,
    height: 14,
    borderRadius: 7,
    backgroundColor: color.brand[500],
    borderWidth: 2.5,
    borderColor: "#fff",
  },
  schematicPin: {
    position: "absolute",
    minWidth: 30,
    paddingHorizontal: 6,
    height: 24,
    borderRadius: 12,
    alignItems: "center",
    justifyContent: "center",
    borderWidth: 2,
    borderColor: "#fff",
    marginLeft: -15,
    marginTop: -12,
  },
});
