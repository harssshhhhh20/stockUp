import { StyleSheet } from "react-native";
import { layout } from "./tokens";

/**
 * Applied to every screen's scroll content and app bar so the app reads as one
 * centred column on tablets and wide browser windows.
 */
export const contentWidth = StyleSheet.create({
  column: {
    width: "100%",
    maxWidth: layout.maxContentWidth,
    alignSelf: "center",
  },
});
