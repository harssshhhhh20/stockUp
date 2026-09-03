import React, { useRef } from "react";
import { Platform, StyleSheet, TextInput, View } from "react-native";
import { color, font, radius } from "../theme/tokens";

type Props = {
  value: string;
  onChange: (value: string) => void;
  length?: number;
};

export function OtpInput({ value, onChange, length = 6 }: Props) {
  const inputRef = useRef<TextInput>(null);
  const digits = value.split("");

  return (
    <View style={styles.wrap}>
      <TextInput
        ref={inputRef}
        value={value}
        onChangeText={(t) => onChange(t.replace(/[^0-9]/g, "").slice(0, length))}
        keyboardType="number-pad"
        maxLength={length}
        style={styles.hiddenInput}
        autoFocus
      />
      <View pointerEvents="none" style={styles.boxes}>
        {Array.from({ length }).map((_, i) => {
          const filled = i < digits.length;
          const active = i === digits.length;
          return (
            <View
              key={i}
              style={[
                styles.box,
                filled && styles.boxFilled,
                active && styles.boxActive,
              ]}
            >
              <TextInput
                editable={false}
                value={digits[i] ?? ""}
                style={styles.boxText}
              />
            </View>
          );
        })}
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  wrap: {
    position: "relative",
  },
  hiddenInput: {
    position: "absolute",
    opacity: 0,
    width: "100%",
    height: 56,
    zIndex: 1,
    ...(Platform.OS === "web" ? { outlineStyle: "none" as never } : null),
  },
  boxes: {
    flexDirection: "row",
    gap: 8,
    justifyContent: "space-between",
  },
  box: {
    flex: 1,
    height: 56,
    borderRadius: radius.md,
    borderWidth: 1.5,
    borderColor: color.neutral.border,
    backgroundColor: color.neutral.surface,
    alignItems: "center",
    justifyContent: "center",
  },
  boxFilled: {
    borderColor: color.brand[500],
    backgroundColor: color.brand[50],
  },
  boxActive: {
    borderColor: color.brand[500],
  },
  boxText: {
    fontFamily: font.mono.bold,
    fontSize: 22,
    lineHeight: 28,
    color: color.neutral.ink,
    textAlign: "center",
  },
});
