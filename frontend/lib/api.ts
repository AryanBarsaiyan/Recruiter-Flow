/**
 * API client for Future Scope backend.
 * - Bearer token from getToken()
 * - 401 → refresh then retry once
 * - ApiError with status, code, message, details
 * - 429 handling (rate limit)
 */

const REFRESH_ENDPOINT = "/auth/refresh";

export interface ApiErrorBody {
  timestamp?: string;
  status: number;
  code?: string;
  message?: string;
  details?: string[];
  error?: string;
}

export class ApiError extends Error {
  constructor(
    public status: number,
    public code: string,
    message: string,
    public details?: string[]
  ) {
    super(message);
    this.name = "ApiError";
  }

  static fromResponse(status: number, body: ApiErrorBody): ApiError {
    const code = body.code ?? "ERROR";
    const message =
      body.message ?? body.error ?? `Request failed with status ${status}`;
    return new ApiError(status, code, message, body.details);
  }

  get isRateLimit(): boolean {
    return this.status === 429;
  }

  get isUnauthorized(): boolean {
    return this.status === 401;
  }
}

export type TokenGetter = () => Promise<string | null>;

let tokenGetter: TokenGetter | null = null;

export function setApiTokenGetter(getter: TokenGetter) {
  tokenGetter = getter;
}

function getBaseUrl(): string {
  const url = process.env.NEXT_PUBLIC_API_URL;
  if (!url) throw new Error("NEXT_PUBLIC_API_URL is not set");
  return url.replace(/\/$/, "");
}

async function getAuthHeader(): Promise<string | null> {
  if (!tokenGetter) return null;
  const token = await tokenGetter();
  if (!token) return null;
  return `Bearer ${token}`;
}

async function doRefresh(): Promise<boolean> {
  const { getRefreshToken, setTokens } = await import("./auth");
  const refreshToken = getRefreshToken();
  if (!refreshToken) return false;
  const base = getBaseUrl();
  const res = await fetch(`${base}${REFRESH_ENDPOINT}`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ refreshToken }),
  });
  if (!res.ok) return false;
  const data = (await res.json()) as {
    accessToken?: string;
    refreshToken?: string;
  };
  if (data.accessToken && data.refreshToken) {
    setTokens(data.accessToken, data.refreshToken);
    return true;
  }
  return false;
}

export interface RequestConfig extends RequestInit {
  skipAuth?: boolean;
  skipRefreshRetry?: boolean;
}

export async function api<T = unknown>(
  path: string,
  config: RequestConfig = {}
): Promise<T> {
  const { skipAuth = false, skipRefreshRetry = false, ...init } = config;
  const base = getBaseUrl();
  const url = path.startsWith("http") ? path : `${base}${path.startsWith("/") ? "" : "/"}${path}`;

  const headers = new Headers(init.headers as HeadersInit);
  const bodyIsFormData = init.body instanceof FormData;
  if (!headers.has("Content-Type")) {
    if (!bodyIsFormData) headers.set("Content-Type", "application/json");
  }
  if (!skipAuth) {
    const auth = await getAuthHeader();
    if (auth) headers.set("Authorization", auth);
  }

  let res = await fetch(url, { ...init, headers });

  if (res.status === 401 && !skipAuth && !skipRefreshRetry) {
    const refreshed = await doRefresh();
    if (refreshed) {
      const auth = await getAuthHeader();
      if (auth) headers.set("Authorization", auth);
      res = await fetch(url, { ...init, headers });
    }
  }

  if (res.status === 429) {
    const text = await res.text();
    let body: ApiErrorBody = { status: 429, message: "Rate limit exceeded. Please try again later." };
    try {
      body = { ...body, ...JSON.parse(text) };
    } catch {
      // use default message
    }
    throw ApiError.fromResponse(429, body);
  }

  if (!res.ok) {
    const text = await res.text();
    let body: ApiErrorBody = { status: res.status, message: res.statusText };
    try {
      body = { ...body, ...JSON.parse(text) };
    } catch {
      // use statusText
    }
    throw ApiError.fromResponse(res.status, body);
  }

  if (res.status === 204) return undefined as T;
  return res.json() as Promise<T>;
}

/** Fetch binary (e.g. PDF) with auth. Returns blob for preview/download. */
export async function apiBlob(path: string): Promise<Blob> {
  const base = getBaseUrl();
  const url = path.startsWith("http") ? path : `${base}${path.startsWith("/") ? "" : "/"}${path}`;

  const headers = new Headers();
  const auth = await getAuthHeader();
  if (auth) headers.set("Authorization", auth);

  let res = await fetch(url, { headers });

  if (res.status === 401) {
    const refreshed = await doRefresh();
    if (refreshed) {
      const auth2 = await getAuthHeader();
      if (auth2) headers.set("Authorization", auth2);
      res = await fetch(url, { headers });
    }
  }

  if (!res.ok) {
    const text = await res.text();
    let body: ApiErrorBody = { status: res.status, message: res.statusText };
    try {
      body = { ...body, ...JSON.parse(text) };
    } catch {
      // ignore
    }
    throw ApiError.fromResponse(res.status, body);
  }

  return res.blob();
}
