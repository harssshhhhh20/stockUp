import { api } from "./client";
import {
  AppNotification,
  AuthResponse,
  BasketDetails,
  BasketHistoryItem,
  BasketItemUnit,
  BasketTargetMode,
  BroadcastRecipientSummary,
  MerchantOfferItemStatus,
  MerchantOfferSummary,
  MerchantProfileResponse,
  Page,
  Reservation,
  ReservationStatus,
  StoreResponse,
} from "./types";

// ---- Auth ----
export const AuthApi = {
  requestOtp: (email: string) => api.post<string>("/api/v1/auth/request-otp", { email }, false),
  verifyOtp: (email: string, otp: string) =>
    api.post<AuthResponse>("/api/v1/auth/verify-otp", { email, otp }, false),
  me: () => api.get<string>("/api/v1/users/me"),
};

// ---- Merchant / Store ----
export const MerchantApi = {
  register: () => api.post<void>("/api/v1/merchant/register", {}),
  me: () => api.get<MerchantProfileResponse | null>("/api/v1/merchant/me"),
};

export const StoreApi = {
  create: (payload: {
    name: string;
    businessType: string;
    addressLine1: string;
    addressLine2?: string | null;
    city: string;
    state: string;
    postalCode: string;
    country: string;
    latitude: number;
    longitude: number;
  }) => api.post<void>("/api/v1/stores", payload),
  me: () => api.get<StoreResponse | null>("/api/v1/stores/me"),
};

// ---- Baskets ----
export const BasketApi = {
  create: (payload: {
    targetMode: BasketTargetMode;
    searchRadiusMeters?: number;
    basketLatitude: number;
    basketLongitude: number;
    items: {
      productName: string;
      quantity: number;
      unit: BasketItemUnit;
      brand?: string | null;
      notes?: string | null;
    }[];
    storeIds?: string[];
  }) => api.post<{ basketId: string }>("/api/v1/baskets", payload),
  history: () => api.get<BasketHistoryItem[]>("/api/v1/baskets"),
  detail: (basketId: string) => api.get<BasketDetails>(`/api/v1/baskets/${basketId}`),
};

// ---- Broadcasts ----
export const BroadcastApi = {
  markViewed: (broadcastRecipientId: string) =>
    api.post<void>("/api/v1/broadcasts/view", { broadcastRecipientId }),
  mine: () => api.get<BroadcastRecipientSummary[]>("/api/v1/broadcasts/mine"),
};

// ---- Merchant offers ----
export const MerchantOfferApi = {
  submit: (payload: {
    broadcastRecipientId: string;
    responses: {
      basketItemId: string;
      status: MerchantOfferItemStatus;
      availableQuantity?: number;
    }[];
  }) => api.post<{ merchantOfferId: string }>("/api/v1/merchant-offers", payload),
  forBasket: (basketId: string) =>
    api.get<MerchantOfferSummary[]>(`/api/v1/merchant-offers/basket/${basketId}`),
};

// ---- Reservations ----
export const ReservationApi = {
  reserve: (merchantOfferId: string) =>
    api.post<Reservation>(`/api/v1/reservations?merchantOfferId=${merchantOfferId}`),
  list: (status: ReservationStatus, page = 0, size = 20) =>
    api.get<Page<Reservation>>("/api/v1/reservations", { status, page, size }),
  detail: (id: string) => api.get<Reservation>(`/api/v1/reservations/${id}`),
  cancel: (id: string, reason: string) =>
    api.post<Reservation>(`/api/v1/reservations/${id}/cancel`, { reason }),
  complete: (id: string, otp: string) =>
    api.post<Reservation>(`/api/v1/reservations/${id}/complete`, { otp }),
};

// ---- Notifications ----
export const NotificationApi = {
  list: (page = 0, size = 20) =>
    api.get<Page<AppNotification>>("/api/v1/notifications", { page, size }),
  unreadCount: () => api.get<{ unreadCount: number }>("/api/v1/notifications/unread-count"),
  markRead: (id: string) => api.post<AppNotification>(`/api/v1/notifications/${id}/read`),
};
