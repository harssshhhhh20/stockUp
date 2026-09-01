import React from "react";
import { Text as RNText, TextProps, StyleSheet } from "react-native";
import { color, font, type } from "../theme/tokens";

type Variant =
  | "hero"
  | "h1"
  | "h2"
  | "h3"
  | "body"
  | "bodySm"
  | "caption"
  | "mono"
  | "monoLg";

type Props = TextProps & {
  variant?: Variant;
  color?: string;
  weight?: "regular" | "medium" | "semibold" | "bold";
};

const variantFont: Record<Variant, string> = {
  hero: font.display.extrabold,
  h1: font.display.bold,
  h2: font.display.semibold,
  h3: font.body.bold,
  body: font.body.regular,
  bodySm: font.body.regular,
  caption: font.body.semibold,
  mono: font.mono.medium,
  monoLg: font.mono.bold,
};

const variantSize: Record<Variant, { fontSize: number; lineHeight: number }> = {
  hero: type.hero,
  h1: type.h1,
  h2: type.h2,
  h3: type.h3,
  body: type.body,
  bodySm: type.bodySm,
  caption: type.caption,
  mono: type.body,
  monoLg: type.h2,
};

export function Text({ variant = "body", color: c, weight, style, ...rest }: Props) {
  return (
    <RNText
      style={[
        styles.base,
        { fontFamily: weight ? weightFont(weight) : variantFont[variant] },
        variantSize[variant],
        { color: c ?? color.neutral.ink },
        variant === "caption" && styles.caption,
        style,
      ]}
      {...rest}
    />
  );
}

function weightFont(weight: NonNullable<Props["weight"]>) {
  return font.body[weight];
}

const styles = StyleSheet.create({
  base: {
    includeFontPadding: false,
  },
  caption: {
    textTransform: "uppercase",
    letterSpacing: 0.5,
  },
});
