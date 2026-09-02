import React, { useEffect, useRef } from "react";
import { Animated, Easing, Pressable, ViewStyle, StyleProp } from "react-native";

/**
 * Shared motion, so the app moves in one voice rather than each screen
 * inventing its own timing.
 *
 * The rules: nothing takes longer than ~400ms, everything decelerates rather
 * than bouncing, and motion only ever explains where something came from.
 * Decorative animation on a shopping app just delays the shopping.
 */

const EASE = Easing.bezier(0.22, 1, 0.36, 1); // decelerate, no overshoot

/** Fades and lifts content in. `index` staggers a list without a library. */
export function FadeIn({
  children,
  index = 0,
  from = 12,
  duration = 320,
  style,
}: {
  children: React.ReactNode;
  index?: number;
  from?: number;
  duration?: number;
  style?: StyleProp<ViewStyle>;
}) {
  const t = useRef(new Animated.Value(0)).current;

  useEffect(() => {
    const id = setTimeout(() => {
      Animated.timing(t, {
        toValue: 1,
        duration,
        easing: EASE,
        useNativeDriver: true,
      }).start();
      // Cap the stagger: past a handful of rows the delay stops reading as
      // rhythm and starts reading as lag.
    }, Math.min(index, 6) * 55);
    return () => clearTimeout(id);
  }, [t, index, duration]);

  return (
    <Animated.View
      style={[
        style,
        {
          opacity: t,
          transform: [
            { translateY: t.interpolate({ inputRange: [0, 1], outputRange: [from, 0] }) },
          ],
        },
      ]}
    >
      {children}
    </Animated.View>
  );
}

/**
 * A pressable that dips slightly under the finger.
 *
 * Worth the code: on a list of shops, the press feedback is what tells someone
 * the tap registered before the next screen has loaded.
 */
export function PressableScale({
  children,
  onPress,
  disabled,
  style,
  scaleTo = 0.97,
  accessibilityLabel,
}: {
  children: React.ReactNode;
  onPress?: () => void;
  disabled?: boolean;
  style?: StyleProp<ViewStyle>;
  scaleTo?: number;
  accessibilityLabel?: string;
}) {
  const scale = useRef(new Animated.Value(1)).current;

  const to = (v: number) =>
    Animated.spring(scale, {
      toValue: v,
      useNativeDriver: true,
      speed: 40,
      bounciness: 0,
    }).start();

  return (
    <Pressable
      onPress={onPress}
      disabled={disabled}
      onPressIn={() => to(scaleTo)}
      onPressOut={() => to(1)}
      accessibilityRole="button"
      accessibilityLabel={accessibilityLabel}
    >
      <Animated.View style={[style, { transform: [{ scale }] }]}>{children}</Animated.View>
    </Pressable>
  );
}

/** A soft looping pulse — used for "we're looking / listening" states. */
export function Pulse({
  children,
  style,
}: {
  children: React.ReactNode;
  style?: StyleProp<ViewStyle>;
}) {
  const t = useRef(new Animated.Value(0)).current;

  useEffect(() => {
    const loop = Animated.loop(
      Animated.sequence([
        Animated.timing(t, { toValue: 1, duration: 1400, easing: Easing.inOut(Easing.ease), useNativeDriver: true }),
        Animated.timing(t, { toValue: 0, duration: 1400, easing: Easing.inOut(Easing.ease), useNativeDriver: true }),
      ])
    );
    loop.start();
    return () => loop.stop();
  }, [t]);

  return (
    <Animated.View
      style={[
        style,
        {
          opacity: t.interpolate({ inputRange: [0, 1], outputRange: [0.55, 1] }),
          transform: [{ scale: t.interpolate({ inputRange: [0, 1], outputRange: [1, 1.06] }) }],
        },
      ]}
    >
      {children}
    </Animated.View>
  );
}

/** Counts up to a number. Used for the Bharosa score so it lands, not blinks. */
export function useCountUp(target: number, duration = 700) {
  const [value, setValue] = React.useState(0);
  const t = useRef(new Animated.Value(0)).current;

  useEffect(() => {
    t.setValue(0);
    const id = t.addListener(({ value: v }) => setValue(Math.round(v * target)));
    Animated.timing(t, { toValue: 1, duration, easing: EASE, useNativeDriver: false }).start();
    return () => t.removeListener(id);
  }, [target, duration, t]);

  return value;
}
