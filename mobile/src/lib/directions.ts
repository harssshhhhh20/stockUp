import { Linking, Platform } from "react-native";

type Destination = {
  name: string;
  address?: string | null;
  latitude?: number | null;
  longitude?: number | null;
};

/**
 * Hand the shopper off to whatever maps app they actually use.
 *
 * Coordinates win when we have them — a kirana shop is often a doorway on a
 * lane that geocoding puts on the wrong side of the street, and the shopkeeper
 * pinned it themselves at signup. The written address is the fallback, and the
 * name is passed alongside the pin so the destination card reads "Sharma Kirana
 * Store" rather than a pair of decimals.
 */
export function directionsUrl(dest: Destination): string {
  const hasPin =
    typeof dest.latitude === "number" && typeof dest.longitude === "number";
  const label = encodeURIComponent(dest.name);

  if (hasPin) {
    const pin = `${dest.latitude},${dest.longitude}`;
    return Platform.OS === "ios"
      ? `http://maps.apple.com/?daddr=${pin}&q=${label}&dirflg=d`
      : `https://www.google.com/maps/dir/?api=1&destination=${pin}&travelmode=driving`;
  }

  const query = encodeURIComponent(dest.address?.trim() || dest.name);
  return Platform.OS === "ios"
    ? `http://maps.apple.com/?daddr=${query}&dirflg=d`
    : `https://www.google.com/maps/dir/?api=1&destination=${query}&travelmode=driving`;
}

/** Returns false when there is nothing to navigate to, or no maps app to open. */
export async function openDirections(dest: Destination): Promise<boolean> {
  const navigable =
    (typeof dest.latitude === "number" && typeof dest.longitude === "number") ||
    !!dest.address?.trim();
  if (!navigable) return false;

  try {
    await Linking.openURL(directionsUrl(dest));
    return true;
  } catch {
    return false;
  }
}
