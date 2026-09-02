import AsyncStorage from "@react-native-async-storage/async-storage";
import { ApiResponse } from "./types";

// Web preview & iOS Simulator can both reach the Mac's own localhost directly.
// Testing on a physical phone via Expo Go needs your Mac's LAN IP instead —
// set EXPO_PUBLIC_API_URL="http://<lan-ip>:8080" in mobile/.env for that.
export const API_BASE_URL = process.env.EXPO_PUBLIC_API_URL ?? "http://localhost:8080";

const ACCESS_TOKEN_KEY = "stockup.accessToken";
const REFRESH_TOKEN_KEY = "stockup.refreshToken";

export async function getAccessToken() {
  return AsyncStorage.getItem(ACCESS_TOKEN_KEY);
}

export async function setTokens(accessToken: string, refreshToken: string) {
  await AsyncStorage.multiSet([
    [ACCESS_TOKEN_KEY, accessToken],
    [REFRESH_TOKEN_KEY, refreshToken],
  ]);
}

export async function getRefreshToken() {
  return AsyncStorage.getItem(REFRESH_TOKEN_KEY);
}

export async function clearTokens() {
  await AsyncStorage.multiRemove([ACCESS_TOKEN_KEY, REFRESH_TOKEN_KEY]);
}

export class ApiError extends Error {
  status: number;
  fieldErrors: { field: string | null; message: string }[];

  constructor(message: string, status: number, fieldErrors: { field: string | null; message: string }[] = []) {
    super(message);
    this.status = status;
    this.fieldErrors = fieldErrors;
  }
}

type RequestOptions = {
  method?: "GET" | "POST" | "PATCH" | "PUT" | "DELETE";
  body?: unknown;
  auth?: boolean;
  query?: Record<string, string | number | undefined>;
};

/**
 * Access tokens are short-lived (15 min), so any authenticated call can come
 * back 401 mid-session. We refresh once, transparently, and replay the request.
 * Concurrent 401s share a single refresh so we don't stampede the endpoint.
 */
let refreshInFlight: Promise<string | null> | null = null;

async function refreshAccessToken(): Promise<string | null> {
  if (!refreshInFlight) {
    refreshInFlight = (async () => {
      try {
        const stored = await getRefreshToken();
        if (!stored) return null;

        const response = await fetch(`${API_BASE_URL}/api/v1/auth/refresh`, {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify({ refreshToken: stored }),
        });

        if (!response.ok) return null;

        const payload: ApiResponse<string> = await response.json();
        if (!payload?.success || !payload.data) return null;

        await AsyncStorage.setItem(ACCESS_TOKEN_KEY, payload.data);
        return payload.data;
      } catch {
        return null;
      } finally {
        // Let the next 401 start a fresh attempt.
        setTimeout(() => {
          refreshInFlight = null;
        }, 0);
      }
    })();
  }
  return refreshInFlight;
}

/** Notifies the app when the session is truly gone so it can show sign-in. */
let onSessionExpired: (() => void) | null = null;
export function setSessionExpiredHandler(handler: (() => void) | null) {
  onSessionExpired = handler;
}

async function request<T>(path: string, options: RequestOptions = {}): Promise<T> {
  const { method = "GET", body, auth = true, query } = options;

  let url = `${API_BASE_URL}${path}`;
  if (query) {
    const params = Object.entries(query)
      .filter(([, v]) => v !== undefined)
      .map(([k, v]) => `${encodeURIComponent(k)}=${encodeURIComponent(String(v))}`)
      .join("&");
    if (params) url += `?${params}`;
  }

  async function send(token: string | null) {
    const headers: Record<string, string> = { "Content-Type": "application/json" };
    if (token) headers.Authorization = `Bearer ${token}`;

    return fetch(url, {
      method,
      headers,
      body: body !== undefined ? JSON.stringify(body) : undefined,
    });
  }

  let token = auth ? await getAccessToken() : null;
  let response = await send(token);

  if (response.status === 401 && auth) {
    const fresh = await refreshAccessToken();

    if (fresh) {
      response = await send(fresh);
    } else {
      await clearTokens();
      onSessionExpired?.();
    }
  }

  let payload: ApiResponse<T> | null = null;
  try {
    payload = await response.json();
  } catch {
    // no body
  }

  if (!response.ok || !payload?.success) {
    const message = payload?.message ?? `Request failed (${response.status})`;
    throw new ApiError(message, response.status, payload?.errors ?? []);
  }

  return payload.data;
}

export const api = {
  get: <T>(path: string, query?: RequestOptions["query"], auth = true) =>
    request<T>(path, { method: "GET", query, auth }),
  post: <T>(path: string, body?: unknown, auth = true) =>
    request<T>(path, { method: "POST", body, auth }),
  patch: <T>(path: string, body?: unknown, auth = true) =>
    request<T>(path, { method: "PATCH", body, auth }),
};
