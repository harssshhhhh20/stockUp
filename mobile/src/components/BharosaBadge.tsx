import React, { useEffect, useRef } from "react";
import { Animated, Easing, Pressable, StyleSheet, View } from "react-native";
import Svg, { Circle } from "react-native-svg";
import { color, font, radius, spacing } from "../theme/tokens";
import { Text } from "./Text";

type Band = "new" | "trusted" | "mixed" | "risky";

type Props = {
  score: number;
  band: Band;
  onPress?: () => void;
  size?: "sm" | "md";
};

/**
 * StockUp's signature trust mark: a shield-tinted chip with a thin arc that
 * fills to the score.
 *
 * The arc is the point — it turns an abstract number into a quantity you can
 * read without reading, and it uses the app's semantic colours so the meaning
 * carries without a legend. A new shop is deliberately *info* blue, never
 * amber: being new is a fact, not a warning.
 */
export function BharosaBadge({ score, band, onPress, size = "sm" }: Props) {
  const tone = toneFor(band);
  const dim = size === "md" ? 30 : 22;
  const stroke = size === "md" ? 3 : 2.5;
  const r = (dim - stroke) / 2;
  const circumference = 2 * Math.PI * r;

  const progress = useRef(new Animated.Value(0)).current;

  useEffect(() => {
    // A short sweep on appear: the badge draws itself, which makes the number
    // feel earned rather than stamped on.
    Animated.timing(progress, {
      toValue: Math.max(0, Math.min(100, score)) / 100,
      duration: 650,
      easing: Easing.out(Easing.cubic),
      useNativeDriver: false,
    }).start();
  }, [score, progress]);

  const [offset, setOffset] = React.useState(circumference);
  useEffect(() => {
    const id = progress.addListener(({ value }) => {
      setOffset(circumference * (1 - value));
    });
    return () => progress.removeListener(id);
  }, [progress, circumference]);

  const body = (
    <View style={[styles.chip, { backgroundColor: tone.soft, borderColor: tone.border }]}>
      <View style={{ width: dim, height: dim }}>
        <Svg width={dim} height={dim}>
          <Circle
            cx={dim / 2}
            cy={dim / 2}
            r={r}
            stroke={tone.track}
            strokeWidth={stroke}
            fill="none"
          />
          <Circle
            cx={dim / 2}
            cy={dim / 2}
            r={r}
            stroke={tone.base}
            strokeWidth={stroke}
            strokeDasharray={`${circumference} ${circumference}`}
            strokeDashoffset={offset}
            strokeLinecap="round"
            fill="none"
            rotation={-90}
            origin={`${dim / 2}, ${dim / 2}`}
          />
        </Svg>
      </View>
      <View style={styles.labelWrap}>
        <Text style={[styles.word, { color: tone.strong }]}>Bharosa</Text>
        <Text style={[styles.score, { color: tone.strong }]}>{score}</Text>
      </View>
    </View>
  );

  if (!onPress) return body;

  return (
    <Pressable
      onPress={onPress}
      accessibilityRole="button"
      accessibilityLabel={`Bharosa score ${score} out of 100. Tap to see why.`}
      style={({ pressed }) => [pressed && styles.pressed]}
    >
      {body}
    </Pressable>
  );
}

function toneFor(band: Band) {
  switch (band) {
    case "trusted":
      return {
        base: color.status.positive.base,
        strong: color.status.positive.strong,
        soft: color.status.positive.soft,
        track: "rgba(46,158,91,0.18)",
        border: "rgba(46,158,91,0.28)",
      };
    case "mixed":
      return {
        base: color.status.attention.base,
        strong: color.status.attention.strong,
        soft: color.status.attention.soft,
        track: "rgba(217,119,6,0.18)",
        border: "rgba(217,119,6,0.28)",
      };
    case "risky":
      return {
        base: color.status.urgent.base,
        strong: color.status.urgent.strong,
        soft: color.status.urgent.soft,
        track: "rgba(214,80,63,0.18)",
        border: "rgba(214,80,63,0.28)",
      };
    default:
      return {
        base: color.status.info.base,
        strong: color.status.info.strong,
        soft: color.status.info.soft,
        track: "rgba(59,110,165,0.18)",
        border: "rgba(59,110,165,0.28)",
      };
  }
}

const styles = StyleSheet.create({
  chip: {
    flexDirection: "row",
    alignItems: "center",
    alignSelf: "flex-start",
    gap: 7,
    paddingVertical: 4,
    paddingHorizontal: 8,
    borderRadius: radius.pill,
    borderWidth: 1,
  },
  labelWrap: {
    flexDirection: "row",
    alignItems: "baseline",
    gap: 4,
  },
  word: {
    fontFamily: font.body.bold,
    fontSize: 11,
    letterSpacing: 0.3,
  },
  score: {
    fontFamily: font.mono.bold,
    fontSize: 13,
  },
  pressed: { opacity: 0.75 },
});
