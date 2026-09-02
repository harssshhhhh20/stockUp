import React from "react";
import { StyleSheet, View } from "react-native";
import { color, font, radius } from "../theme/tokens";
import { BharosaTag, BharosaTone } from "../api/types";
import { Text } from "./Text";

type Props = {
  tags: BharosaTag[];
};

/**
 * The compact reputation strip that sits directly above a store name.
 *
 * Deliberately small: it is a caption, not a hero. Two tags at most, generated
 * server-side from the pillar values — never written by hand — and coloured
 * from the same five semantics used everywhere else in StockUp, so shoppers
 * learn the language once and read it everywhere.
 */
export function ReputationBanner({ tags }: Props) {
  if (!tags?.length) return null;

  return (
    <View style={styles.row} accessibilityRole="text">
      {tags.map((tag, i) => {
        const tone = toneColors(tag.tone);
        return (
          <React.Fragment key={`${tag.label}-${i}`}>
            {i > 0 ? <Text style={styles.sep}>·</Text> : null}
            <View style={[styles.tag, { backgroundColor: tone.soft }]}>
              <Text style={styles.icon}>{tag.icon}</Text>
              <Text style={[styles.label, { color: tone.strong }]} numberOfLines={1}>
                {tag.label}
              </Text>
            </View>
          </React.Fragment>
        );
      })}
    </View>
  );
}

function toneColors(tone: BharosaTone) {
  switch (tone) {
    case "positive":
      return color.status.positive;
    case "attention":
      return color.status.attention;
    case "urgent":
      return color.status.urgent;
    case "special":
      return color.status.special;
    default:
      return color.status.info;
  }
}

const styles = StyleSheet.create({
  row: {
    flexDirection: "row",
    alignItems: "center",
    flexWrap: "wrap",
    gap: 4,
  },
  tag: {
    flexDirection: "row",
    alignItems: "center",
    gap: 4,
    paddingVertical: 3,
    paddingHorizontal: 7,
    borderRadius: radius.pill,
  },
  icon: { fontSize: 11 },
  label: {
    fontFamily: font.body.bold,
    fontSize: 11.5,
    letterSpacing: 0.1,
  },
  sep: {
    color: color.neutral.inkFaint,
    fontSize: 11,
    marginHorizontal: 1,
  },
});
