import { useEffect, useRef } from "react";
import { Platform } from "react-native";
import { isRunningInExpoGo } from "expo";
import * as Device from "expo-device";
import Constants from "expo-constants";
import { api } from "../api/client";

type NotificationsModule = typeof import("expo-notifications");

// Expo Go dropped remote push in SDK 53, and on Android merely *importing*
// expo-notifications throws (its token auto-registration runs at load), which
// takes the whole app down before the first screen. So it is required only
// outside Expo Go; Metro evaluates a module on first require, not at bundle.
const Notifications: NotificationsModule | null = isRunningInExpoGo()
  ? null
  : require("expo-notifications");

/**
 * Registers this device for push once the user is signed in.
 *
 * Push is a courtesy copy of the in-app feed, so every failure here is
 * swallowed: no permission, a simulator, or a network blip must never stop
 * someone using the app. Expo relays to APNs and FCM, so nothing here needs
 * Apple or Google credentials during development.
 */
Notifications?.setNotificationHandler({
  handleNotification: async () => ({
    shouldShowBanner: true,
    shouldShowList: true,
    shouldPlaySound: true,
    shouldSetBadge: false,
  }),
});

/** Loud in development, silent in production — push is a courtesy, not a feature. */
function warn(message: string) {
  if (__DEV__) console.warn(`[push] not registered: ${message}`);
}

export function usePushNotifications(signedIn: boolean) {
  const registered = useRef(false);

  useEffect(() => {
    if (!signedIn || registered.current) return;

    (async () => {
      try {
        // A simulator has no push hardware; asking would only ever fail.
        if (!Device.isDevice) {
          warn("simulators have no push hardware");
          return;
        }

        // On a real phone this is the single most likely reason nothing
        // arrives, and it cannot be worked around from here — it needs a
        // development build.
        if (!Notifications) {
          warn("Expo Go can't receive push since SDK 53 — use a development build");
          return;
        }

        const existing = await Notifications.getPermissionsAsync();
        let status = existing.status;

        if (status !== "granted") {
          status = (await Notifications.requestPermissionsAsync()).status;
        }
        if (status !== "granted") {
          warn("notification permission denied");
          return;
        }

        if (Platform.OS === "android") {
          await Notifications.setNotificationChannelAsync("default", {
            name: "StockUp",
            importance: Notifications.AndroidImportance.DEFAULT,
          });
        }

        // getExpoPushTokenAsync needs an EAS project id. Expo resolves it from
        // the manifest, but a project that has never run `eas init` has none —
        // in which case this throws and, before this was logged, took push down
        // silently for every user.
        const projectId =
          Constants.expoConfig?.extra?.eas?.projectId ??
          (Constants as any).easConfig?.projectId;

        if (!projectId) {
          warn(
            "no EAS projectId — run `eas init`, then rebuild. Push is off until then."
          );
          return;
        }

        const token = (await Notifications.getExpoPushTokenAsync({ projectId })).data;
        if (!token) return;

        await api.post("/api/v1/push/register", { token, platform: Platform.OS });
        registered.current = true;
      } catch (e) {
        // Still non-fatal, but no longer invisible: a swallowed failure here is
        // indistinguishable from "push works and nothing happened".
        warn(e instanceof Error ? e.message : String(e));
      }
    })();
  }, [signedIn]);
}
