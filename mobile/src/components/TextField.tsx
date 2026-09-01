import React, { useState } from "react";
import { Platform, StyleSheet, TextInput, TextInputProps, View } from "react-native";
import { color, font, radius, spacing } from "../theme/tokens";
import { Text } from "./Text";

type Props = TextInputProps & {
  label?: string;
  error?: string;
};

export function TextField({ label, error, style, onFocus, onBlur, ...rest }: Props) {
  const [focused, setFocused] = useState(false);

  return (
    <View style={styles.wrap}>
      {label ? (
        <Text variant="caption" color={color.neutral.inkMuted}>
          {label}
        </Text>
      ) : null}
      <TextInput
        placeholderTextColor={color.neutral.inkFaint}
        style={[
          styles.input,
          focused && styles.inputFocused,
          error && styles.inputError,
          style,
        ]}
        onFocus={(e) => {
          setFocused(true);
          onFocus?.(e);
        }}
        onBlur={(e) => {
          setFocused(false);
          onBlur?.(e);
        }}
        {...rest}
      />
      {error ? (
        <Text variant="bodySm" color={color.status.urgent.strong}>
          {error}
        </Text>
      ) : null}
    </View>
  );
}

const styles = StyleSheet.create({
  wrap: {
    gap: 6,
  },
  input: {
    height: 50,
    borderRadius: radius.md,
    borderWidth: 1.5,
    borderColor: color.neutral.border,
    backgroundColor: color.neutral.surface,
    paddingHorizontal: spacing.md,
    fontFamily: font.body.medium,
    fontSize: 15,
    color: color.neutral.ink,
    // The focused border below is the visible focus affordance; suppress the
    // browser's own ring on web so focus reads in brand teal, not system amber.
    ...(Platform.OS === "web" ? { outlineStyle: "none" as never } : null),
  },
  inputFocused: {
    borderColor: color.brand[500],
  },
  inputError: {
    borderColor: color.status.urgent.base,
  },
});
