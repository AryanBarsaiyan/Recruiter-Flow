/**
 * Token storage: in-memory accessToken + localStorage refreshToken.
 * Used by API client and AuthContext.
 */

const MEMORY_KEY = "__accessToken";

function getMemoryAccessToken(): string | null {
  if (typeof window === "undefined") return null;
  return (window as unknown as { __accessToken?: string }).__accessToken ?? null;
}

function setMemoryAccessToken(token: string | null): void {
  if (typeof window === "undefined") return;
  (window as unknown as { __accessToken?: string }).__accessToken = token ?? undefined;
}

export function getAccessToken(): string | null {
  return getMemoryAccessToken();
}

export function getRefreshToken(): string | null {
  if (typeof window === "undefined") return null;
  return localStorage.getItem("refreshToken");
}

export function setTokens(accessToken: string, refreshToken: string): void {
  setMemoryAccessToken(accessToken);
  if (typeof window !== "undefined") {
    localStorage.setItem("refreshToken", refreshToken);
  }
}

export function clearTokens(): void {
  setMemoryAccessToken(null);
  if (typeof window !== "undefined") {
    localStorage.removeItem("refreshToken");
  }
}

export function hasStoredRefreshToken(): boolean {
  return !!getRefreshToken();
}
