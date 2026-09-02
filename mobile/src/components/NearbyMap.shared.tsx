import React from "react";
import { StyleSheet, View } from "react-native";
import { NearbyStore } from "../api/types";
import { color, font, radius } from "../theme/tokens";
import { Text } from "./Text";

export type MapProps = {
  coords: { latitude: number; longitude: number } | null;
  stores: NearbyStore[];
  height?: number;
};

/** Pins carry the same semantic colours as everything else in StockUp. */
export function pinTone(band: NearbyStore["band"]) {
  switch (band) {
    case "trusted":
      return { backgroundColor: color.status.positive.base };
    case "risky":
      return { backgroundColor: color.status.urgent.base };
    case "new":
      return { backgroundColor: color.status.info.base };
    default:
      return { backgroundColor: color.status.attention.base };
  }
}

export function MapPlaceholder({ height }: { height: number }) {
  return (
    <View style={[mapStyles.wrap, mapStyles.schematic, mapStyles.placeholder, { height }]}>
      <Text variant="bodySm" color={color.neutral.inkFaint}>
        Turn on location to see shops around you
      </Text>
    </View>
  );
}

export const mapStyles = StyleSheet.create({
  wrap: {
    borderRadius: radius.lg,
    overflow: "hidden",
    borderWidth: 1,
    borderColor: color.neutral.border,
    backgroundColor: color.neutral.surfaceMuted,
  },
  schematic: { backgroundColor: color.brand[50] },
  placeholder: { alignItems: "center", justifyContent: "center" },
  pin: {
    minWidth: 30,
    paddingHorizontal: 6,
    height: 24,
    borderRadius: 12,
    alignItems: "center",
    justifyContent: "center",
    borderWidth: 2,
    borderColor: "#fff",
  },
  pinText: { fontFamily: font.mono.bold, fontSize: 11, color: "#fff" },
});
