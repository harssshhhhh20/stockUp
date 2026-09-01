import React, { useEffect, useMemo, useRef, useState } from "react";
import { Animated, Easing, StyleSheet, Text, View } from "react-native";
import { color, font } from "../theme/tokens";

type Props = {
  expiresAt: string; // ISO timestamp
  label?: string; // e.g. "broadcasting" / "reservation active"
};

function formatRemaining(ms: number) {
  if (ms <= 0) return "expired";
  const totalSeconds = Math.floor(ms / 1000);
  const h = Math.floor(totalSeconds / 3600);
  const m = Math.floor((totalSeconds % 3600) / 60);
  const s = totalSeconds % 60;
  if (h > 0) return `${h}h ${m}m left`;
  if (m > 0) return `${m}m ${s}s left`;
  return `${s}s left`;
}

/**
 * StockUp's signature "this is live" indicator — a softly pulsing dot paired
 * with a live countdown. Baskets broadcast for a fixed window, reservations
 * hold for a fixed window: almost everything in the app is racing a clock,
 * so this motif shows up on basket cards, offer cards and reservation
 * headers alike. The pulse escalates from brand teal to attention amber as
 * time runs low, then to urgent red in the final stretch — reusing the same
 * semantic colors as everywhere else, never a one-off hue.
 */
export function LiveTimer({ expiresAt, label = "live" }: Props) {
  const target = useMemo(() => new Date(expiresAt).getTime(), [expiresAt]);
  const [now, setNow] = useState(() => Date.now());

  useEffect(() => {
    const id = setInterval(() => setNow(Date.now()), 1000);
    return () => clearInterval(id);
  }, []);

  const remaining = target - now;
  const pulse = useRef(new Animated.Value(0)).current;

  useEffect(() => {
    const loop = Animated.loop(
      Animated.sequence([
        Animated.timing(pulse, {
          toValue: 1,
          duration: 900,
          easing: Easing.out(Easing.ease),
          useNativeDriver: true,
        }),
        Animated.timing(pulse, {
          toValue: 0,
          duration: 900,
          easing: Easing.in(Easing.ease),
          useNativeDriver: true,
        }),
      ])
    );
    loop.start();
    return () => loop.stop();
  }, [pulse]);

  if (remaining <= 0) {
    return (
      <View style={styles.row}>
        <View style={[styles.dot, { backgroundColor: color.status.urgent.base }]} />
        <Text style={[styles.text, { color: color.status.urgent.strong }]}>expired</Text>
      </View>
    );
  }

  const band =
    remaining < 60_000
      ? color.status.urgent
      : remaining < 5 * 60_000
      ? color.status.attention
      : color.brand;

  const scale = pulse.interpolate({ inputRange: [0, 1], outputRange: [1, 2.1] });
  const opacity = pulse.interpolate({ inputRange: [0, 1], outputRange: [0.5, 0] });
  const dotColor = "base" in band ? band.base : band[500];

  return (
    <View style={styles.row}>
      <View style={styles.dotWrap}>
        <Animated.View
          style={[
            styles.ping,
            { backgroundColor: dotColor, opacity, transform: [{ scale }] },
          ]}
        />
        <View style={[styles.dot, { backgroundColor: dotColor }]} />
      </View>
      <Text style={[styles.text, { color: "strong" in band ? band.strong : color.brand[700] }]}>
        {label} · {formatRemaining(remaining)}
      </Text>
    </View>
  );
}

const styles = StyleSheet.create({
  row: {
    flexDirection: "row",
    alignItems: "center",
    gap: 7,
  },
  dotWrap: {
    width: 8,
    height: 8,
    alignItems: "center",
    justifyContent: "center",
  },
  dot: {
    width: 7,
    height: 7,
    borderRadius: 4,
  },
  ping: {
    position: "absolute",
    width: 7,
    height: 7,
    borderRadius: 4,
  },
  text: {
    fontFamily: font.mono.medium,
    fontSize: 12,
  },
});
