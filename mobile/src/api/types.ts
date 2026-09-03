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
  storeName: string;
  storeAddress: string;
  storeLatitude: number | null;
  storeLongitude: number | null;
  status: ReservationStatus;
  reservedAt: string;
  activeAt: string | null;
  notificationSentAt: string | null;
  viewedAt: string | null;
  /** Pickup code. Only ever populated for the customer holding the order. */
  otp: string | null;
  /** When the customer's cancel window closes. Null once it has. */
  cancellableUntil: string | null;
  /** When a held order is released back to the shop. Null unless active. */
  expiresAt: string | null;
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

// ---- Bharosa ----
export type BharosaTone = "positive" | "attention" | "urgent" | "info" | "special";

export type BharosaTag = {
  icon: string;
  label: string;
  tone: BharosaTone;
};

export type BharosaResponse = {
  merchantId: string;
  storeId: string | null;
  storeName: string | null;
  score: number;
  band: "new" | "trusted" | "mixed" | "risky";
  unproven: boolean;
  tags: BharosaTag[];
  reasons: string[];
  basedOnInteractions: number;
  distinctCustomers: number;
};

// ---- Feedback ----
export type FeedbackResponse = {
  id: string;
  reservationId: string;
  stars: number;
  repliedFast: boolean | null;
  readyOnTime: boolean | null;
  stockAccurate: boolean | null;
  comment: string | null;
  verifiedPurchase: boolean;
  reviewerName: string;
  createdAt: string;
};

export type StoreFeedbackSummary = {
  averageStars: number | null;
  totalReviews: number;
  commonlySaid: string[];
  recent: FeedbackResponse[];
};

// ---- Order history ----
export type OrderTimelineEntry = {
  eventType: string;
  label: string;
  actor: "CUSTOMER" | "MERCHANT" | "SYSTEM";
  occurredAt: string;
};

export type OrderDetail = {
  reservationId: string;
  basketId: string;
  storeId: string;
  storeName: string;
  status: ReservationStatus;
  reservedAt: string;
  activeAt: string | null;
  items: string[];
  responseSeconds: number | null;
  fulfilmentSeconds: number | null;
  cancellationReason: string | null;
  timeline: OrderTimelineEntry[];
  feedback: FeedbackResponse | null;
  canRate: boolean;
};

export type MerchantStats = {
  ordersCompleted: number;
  averageResponseSeconds: number | null;
  answeredRate: number | null;
  completionRate: number | null;
  cancellationRate: number | null;
  repeatCustomerRate: number | null;
  distinctCustomers: number;
  bharosaScore: number;
};

export type NearbyStore = {
  knownFor: string;
  averageStars: number | null;
  reviewCount: number;
  storeId: string;
  name: string;
  businessType: BusinessType;
  city: string;
  distanceKm: number;
  bharosa: number;
  band: "new" | "trusted" | "mixed" | "risky";
  tags: BharosaTag[];
  rankScore: number;
};

export type UserProfile = {
  id: string;
  email: string;
  firstName: string | null;
  lastName: string | null;
  phone: string | null;
  roles: string[];
  profileComplete: boolean;
  isMerchant: boolean;
  hasStore: boolean;
  merchantId: string | null;
  bharosaScore: number | null;
  storeId: string | null;
  storeName: string | null;
};
