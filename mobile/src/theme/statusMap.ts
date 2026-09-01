import { StatusKey } from "./tokens";
import {
  BasketStatus,
  MerchantOfferItemStatus,
  NotificationType,
  ReservationStatus,
} from "../api/types";

type Mapped = { status: StatusKey; label: string };

/**
 * Single place where backend enums become StockUp's five semantic colors.
 * Keeping this centralised is what makes color meaning consistent across
 * every screen — no screen invents its own mapping.
 */

export function basketStatus(s: BasketStatus): Mapped {
  switch (s) {
    case "PENDING_BROADCAST":
      return { status: "info", label: "Sending out" };
    case "ACTIVE":
      return { status: "attention", label: "Waiting for replies" };
    case "RESERVED":
      return { status: "positive", label: "Reserved" };
    case "EXPIRED":
      return { status: "urgent", label: "Expired" };
    case "CANCELLED":
      return { status: "urgent", label: "Cancelled" };
  }
}

export function reservationStatus(s: ReservationStatus): Mapped {
  switch (s) {
    case "PENDING_NOTIFICATION":
      return { status: "info", label: "Confirming" };
    case "ACTIVE":
      return { status: "positive", label: "Ready to collect" };
    case "COMPLETED":
      return { status: "positive", label: "Collected" };
    case "MERCHANT_CANCELLED":
      return { status: "urgent", label: "Shop cancelled" };
    case "CUSTOMER_CANCELLED":
      return { status: "urgent", label: "You cancelled" };
    case "EXPIRED":
      return { status: "urgent", label: "Expired" };
  }
}

export function offerItemStatus(s: MerchantOfferItemStatus): Mapped {
  switch (s) {
    case "AVAILABLE":
      return { status: "positive", label: "In stock" };
    case "PARTIAL":
      return { status: "attention", label: "Partial" };
    case "NOT_AVAILABLE":
      return { status: "urgent", label: "Out of stock" };
  }
}

export function notificationTone(t: NotificationType): StatusKey {
  switch (t) {
    case "BASKET_BROADCASTED":
      return "info";
    case "OFFER_SUBMITTED":
      return "special";
    case "RESERVATION_CREATED":
      return "info";
    case "RESERVATION_ACTIVATED":
      return "positive";
    case "RESERVATION_COMPLETED":
      return "positive";
    case "RESERVATION_CANCELLED":
      return "urgent";
    case "RESERVATION_EXPIRED":
      return "urgent";
  }
}

export function notificationEmoji(t: NotificationType): string {
  switch (t) {
    case "BASKET_BROADCASTED":
      return "📢";
    case "OFFER_SUBMITTED":
      return "🏪";
    case "RESERVATION_CREATED":
      return "📝";
    case "RESERVATION_ACTIVATED":
      return "✅";
    case "RESERVATION_COMPLETED":
      return "🎉";
    case "RESERVATION_CANCELLED":
      return "🚫";
    case "RESERVATION_EXPIRED":
      return "⌛";
  }
}
