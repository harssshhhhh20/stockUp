import { useCallback, useEffect, useState } from "react";
import * as Location from "expo-location";

export type Coords = { latitude: number; longitude: number };

/**
 * "Nearby" only means anything if we know where the user actually is, so the
 * app asks for location rather than assuming a city. Permission can be denied
 * — callers get `null` coords and are expected to say so in the UI instead of
 * silently searching the wrong place.
 */
export type LocationState = {
  coords: Coords | null;
  status: "idle" | "loading" | "granted" | "denied" | "error";
  request: () => Promise<Coords | null>;
};

export function useLocation(autoRequest = true): LocationState {
  const [coords, setCoords] = useState<Coords | null>(null);
  const [status, setStatus] = useState<LocationState["status"]>("idle");

  const request = useCallback(async () => {
    setStatus("loading");
    try {
      const { status: perm } = await Location.requestForegroundPermissionsAsync();

      if (perm !== "granted") {
        setStatus("denied");
        return null;
      }

      const position = await Location.getCurrentPositionAsync({
        accuracy: Location.Accuracy.Balanced,
      });

      const next = {
        latitude: position.coords.latitude,
        longitude: position.coords.longitude,
      };

      setCoords(next);
      setStatus("granted");
      return next;
    } catch {
      setStatus("error");
      return null;
    }
  }, []);

  useEffect(() => {
    if (autoRequest) request();
  }, [autoRequest, request]);

  return { coords, status, request };
}
