export type ApiResponse<T> = {
  success: boolean;
  message: string;
  data: T;
  errors: { field: string | null; message: string }[];
  timestamp: string;
};

export type Page<T> = {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
};

export type AuthResponse = {
  accessToken: string;
  refreshToken: string;
  newUser: boolean;
};

export type BusinessType =
  | "KIRANA"
  | "SUPERMARKET"
  | "PHARMACY"
  | "STATIONERY"
  | "GENERAL_STORE";

export type BasketItemUnit =
  | "PIECE"
  | "PACKET"
  | "BOX"
  | "BOTTLE"
  | "CAN"
  | "STRIP"
  | "DOZEN"
  | "KG"
  | "GRAM"
  | "LITRE"
  | "ML";

export type BasketStatus = "ACTIVE" | "EXPIRED" | "RESERVED" | "PENDING_BROADCAST" | "CANCELLED";
export type BasketTargetMode = "NEARBY" | "SELECTED_STORES";

export type BasketItem = {
  basketItemId: string;
  productName: string;
  quantity: number;
  unit: BasketItemUnit;
  brand: string | null;
  notes: string | null;
};

export type BasketHistoryItem = {
  basketId: string;
  status: BasketStatus;
  createdAt: string;
  expiresAt: string;
  totalItems: number;
  targetMode: BasketTargetMode;
};

export type BasketDetails = {
  basketId: string;
  status: BasketStatus;
  targetMode: BasketTargetMode;
  searchRadius: number | null;
  basketLatitude: number;
  basketLongitude: number;
  createdAt: string;
  expiresAt: string;
  items: BasketItem[];
};

export type BroadcastRecipientStatus = "PENDING" | "VIEWED" | "RESPONDED" | "EXPIRED";

export type BroadcastRecipientSummary = {
  broadcastRecipientId: string;
  basketId: string;
  status: BroadcastRecipientStatus;
  createdAt: string;
  viewedAt: string | null;
  basketExpiresAt: string;
  items: {
    basketItemId: string;
    productName: string;
    quantity: number;
    unit: BasketItemUnit;
    brand: string | null;
    notes: string | null;
  }[];
};

export type MerchantProfileResponse = {
  merchantId: string;
  bharosaScore: number;
};

export type StoreResponse = {
  storeId: string;
  name: string;
  businessType: BusinessType;
  addressLine1: string;
  addressLine2: string | null;
  city: string;
  state: string;
  postalCode: string;
  country: string;
  latitude: number | null;
  longitude: number | null;
};

export type MerchantOfferItemStatus = "AVAILABLE" | "PARTIAL" | "NOT_AVAILABLE";
export type MerchantOfferStatus = "SUBMITTED" | "RESERVED" | "NOT_SELECTED";

export type MerchantOfferSummary = {
  merchantOfferId: string;
  broadcastRecipientId: string;
  storeId: string;
  storeName: string;
  status: MerchantOfferStatus;
  submittedAt: string;
  items: {
    basketItemId: string;
    productName: string;
    status: MerchantOfferItemStatus;
    availableQuantity: number | null;
  }[];
};

export type ReservationStatus =
  | "PENDING_NOTIFICATION"
  | "ACTIVE"
  | "MERCHANT_CANCELLED"
  | "CUSTOMER_CANCELLED"
  | "EXPIRED"
  | "COMPLETED";

export type Reservation = {
  id: string;
  basketId: string;
  merchantOfferId: string;
  customerId: string;
  merchantId: string;
  storeId: string;
  status: ReservationStatus;
  reservedAt: string;
  activeAt: string | null;
  notificationSentAt: string | null;
  viewedAt: string | null;
};

export type NotificationType =
  | "BASKET_BROADCASTED"
  | "OFFER_SUBMITTED"
  | "RESERVATION_CREATED"
  | "RESERVATION_ACTIVATED"
  | "RESERVATION_CANCELLED"
  | "RESERVATION_COMPLETED"
  | "RESERVATION_EXPIRED";

export type AppNotification = {
  id: string;
  type: NotificationType;
  title: string;
  message: string;
  referenceId: string | null;
  read: boolean;
  readAt: string | null;
  createdAt: string;
};

export type MerchantSummary = {
  merchantId: string;
  email: string;
  bharosaScore: number;
  createdAt: string;
};
