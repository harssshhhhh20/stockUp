import React from "react";
import { View, Text, StyleSheet } from "react-native";
import Svg, { Circle } from "react-native-svg";
import { color, font } from "../theme/tokens";

type Props = {
  score: number; // 0-100
  size?: "sm" | "lg";
};

function bandFor(score: number) {
  if (score >= 71) return color.status.positive;
  if (score >= 41) return color.status.attention;
  return color.status.urgent;
}

/**
 * StockUp's signature merchant-trust indicator. Appears wherever a merchant
 * is represented — store cards, offer cards, reservation detail, the
 * merchant's own profile — so a shopper (or the merchant themselves) reads
 * trustworthiness at a glance without needing a legend. Same three-color
 * banding as every other status in the app: red struggling, amber building,
 * green trusted.
 */
export function BharosaRing({ score, size = "sm" }: Props) {
  const dim = size === "lg" ? 76 : 34;
  const stroke = size === "lg" ? 7 : 3.5;
  const radius = (dim - stroke) / 2;
  const circumference = 2 * Math.PI * radius;
  const clamped = Math.max(0, Math.min(100, score));
  const offset = circumference * (1 - clamped / 100);
  const band = bandFor(clamped);

  return (
    <View
      style={{ width: dim, height: dim }}
      accessibilityRole="image"
      accessibilityLabel={`Bharosa Score ${Math.round(clamped)} out of 100`}
    >
      <Svg width={dim} height={dim}>
        <Circle
          cx={dim / 2}
          cy={dim / 2}
          r={radius}
          stroke={color.neutral.surfaceMuted}
          strokeWidth={stroke}
          fill="none"
        />
        <Circle
          cx={dim / 2}
          cy={dim / 2}
          r={radius}
          stroke={band.base}
          strokeWidth={stroke}
          strokeDasharray={`${circumference} ${circumference}`}
          strokeDashoffset={offset}
          strokeLinecap="round"
          fill="none"
          rotation={-90}
          origin={`${dim / 2}, ${dim / 2}`}
        />
      </Svg>
      <View style={[StyleSheet.absoluteFill, styles.centerFill]}>
        <Text
          style={[
            size === "lg" ? styles.scoreLg : styles.scoreSm,
            { color: band.strong },
          ]}
        >
          {Math.round(clamped)}
        </Text>
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  centerFill: {
    flex: 1,
    alignItems: "center",
    justifyContent: "center",
  },
  scoreSm: {
    fontFamily: font.mono.semibold,
    fontSize: 11,
  },
  scoreLg: {
    fontFamily: font.mono.bold,
    fontSize: 22,
  },
});
