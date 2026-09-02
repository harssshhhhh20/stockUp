import { useEffect, useRef } from "react";
import { Platform } from "react-native";
import * as Notifications from "expo-notifications";
import * as Device from "expo-device";
import { api } from "../api/client";

/**
 * Registers this device for push once the user is signed in.
 *
 * Push is a courtesy copy of the in-app feed, so every failure here is
 * swallowed: no permission, a simulator, or a network blip must never stop
 * someone using the app. Expo relays to APNs and FCM, so nothing here needs
 * Apple or Google credentials during development.
 */
Notifications.setNotificationHandler({
  handleNotification: async () => ({
    shouldShowBanner: true,
    shouldShowList: true,
    shouldPlaySound: true,
    shouldSetBadge: false,
  }),
});

export function usePushNotifications(signedIn: boolean) {
  const registered = useRef(false);

  useEffect(() => {
    if (!signedIn || registered.current) return;

    (async () => {
      try {
        // A simulator has no push hardware; asking would only ever fail.
        if (!Device.isDevice) return;

        const existing = await Notifications.getPermissionsAsync();
        let status = existing.status;

        if (status !== "granted") {
          status = (await Notifications.requestPermissionsAsync()).status;
        }
        if (status !== "granted") return;

        if (Platform.OS === "android") {
          await Notifications.setNotificationChannelAsync("default", {
            name: "StockUp",
            importance: Notifications.AndroidImportance.DEFAULT,
          });
        }

        const token = (await Notifications.getExpoPushTokenAsync()).data;
        if (!token) return;

        await api.post("/api/v1/push/register", { token, platform: Platform.OS });
        registered.current = true;
      } catch {
        // Deliberately silent — see the note above.
      }
    })();
  }, [signedIn]);
}
