import React from "react";
import { View } from "react-native";
import Svg, { Rect } from "react-native-svg";
import { color } from "../theme/tokens";

/**
 * StockUp's mark: three crates rising left-to-right — a shelf filling up.
 * The tallest crate is marigold so the mark carries both brand colors and
 * reads clearly even at tab-bar size.
 */
export function Logo({ size = 40 }: { size?: number }) {
  return (
    <View style={{ width: size, height: size }}>
      <Svg width={size} height={size} viewBox="0 0 48 48">
        <Rect x="4" y="28" width="12" height="16" rx="3" fill={color.brand[300]} />
        <Rect x="18" y="19" width="12" height="25" rx="3" fill={color.brand[500]} />
        <Rect x="32" y="8" width="12" height="36" rx="3" fill={color.marigold[400]} />
      </Svg>
    </View>
  );
}
