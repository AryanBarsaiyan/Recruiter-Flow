/**
 * Client-side JWT payload decode (no verification).
 * Used to read userType, sub, email from access token for UI.
 */

export interface JwtPayload {
  sub?: string;
  email?: string;
  userType?: string;
  exp?: number;
  [key: string]: unknown;
}

export function decodeJwtPayload(token: string): JwtPayload | null {
  try {
    const parts = token.split(".");
    if (parts.length !== 3) return null;
    const base64 = parts[1].replace(/-/g, "+").replace(/_/g, "/");
    const json = atob(base64);
    return JSON.parse(json) as JwtPayload;
  } catch {
    return null;
  }
}
