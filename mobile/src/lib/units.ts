import { BasketCategory, BasketItemUnit } from "../api/types";

/** Units a shopper can pick, in the order they're most often needed. */
export const UNITS_FOR: Record<BasketCategory, BasketItemUnit[]> = {
  GROCERY: ["PIECE", "PACKET", "KG", "GRAM", "LITRE", "ML", "BOX", "BOTTLE", "DOZEN", "STRIP", "CAN"],
  FOOD: ["PLATE", "HALF_PLATE", "PIECE", "BOWL", "GLASS"],
};

const LABEL: Record<BasketItemUnit, string> = {
  PIECE: "pc",
  PACKET: "packet",
  KG: "kg",
  GRAM: "g",
  LITRE: "L",
  ML: "ml",
  BOX: "box",
  BOTTLE: "bottle",
  DOZEN: "dozen",
  STRIP: "strip",
  CAN: "can",
  PLATE: "plate",
  HALF_PLATE: "half plate",
  BOWL: "bowl",
  GLASS: "glass",
};

export function unitLabel(unit: BasketItemUnit) {
  return LABEL[unit] ?? unit.toLowerCase();
}
