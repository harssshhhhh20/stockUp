import * as ImagePicker from "expo-image-picker";
import { ImageManipulator, SaveFormat } from "expo-image-manipulator";

/** Mirrors the server's cap (StoreMenuServiceImpl.MAX_PAGES). */
export const MAX_MENU_PAGES = 10;

/**
 * Wide enough that the small print on a menu card stays legible when a
 * customer zooms, small enough that a page uploads in a second or two on a
 * shop's mobile data. A raw phone photo is 4000px and several MB.
 */
const MAX_WIDTH = 1600;

async function shrink(uri: string, width: number): Promise<string> {
  const context = ImageManipulator.manipulate(uri);
  if (!width || width > MAX_WIDTH) context.resize({ width: MAX_WIDTH });
  const image = await context.renderAsync();
  const saved = await image.saveAsync({ compress: 0.7, format: SaveFormat.JPEG });
  return saved.uri;
}

/** Resolves to local, resized JPEG uris — empty if the user backed out. */
export async function pickMenuPhotos(limit: number): Promise<string[]> {
  if (limit <= 0) return [];
  const result = await ImagePicker.launchImageLibraryAsync({
    mediaTypes: ["images"],
    allowsMultipleSelection: true,
    selectionLimit: limit,
    quality: 1,
  });
  if (result.canceled) return [];
  return Promise.all(result.assets.slice(0, limit).map((a) => shrink(a.uri, a.width)));
}

/**
 * The menu card is usually right there on the counter, so the camera is often
 * quicker than finding an old photo. Returns null on cancel or no permission.
 */
export async function takeMenuPhoto(): Promise<string | null | "denied"> {
  const { status } = await ImagePicker.requestCameraPermissionsAsync();
  if (status !== "granted") return "denied";
  const result = await ImagePicker.launchCameraAsync({ mediaTypes: ["images"], quality: 1 });
  if (result.canceled || !result.assets[0]) return null;
  return shrink(result.assets[0].uri, result.assets[0].width);
}
