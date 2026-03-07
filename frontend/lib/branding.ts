import type { BrandingConfig } from "./types";

/**
 * Parse branding config from JSON string (company or job brandingConfigJson).
 * Supports both legacy keys (primary) and new keys (primaryColor, logoUrl, etc.).
 */
export function parseBrandingConfigJson(json: string | null | undefined): BrandingConfig | null {
  if (!json?.trim()) return null;
  try {
    const raw = JSON.parse(json) as Record<string, string>;
    const cfg: BrandingConfig = {};
    if (raw.logoUrl) cfg.logoUrl = raw.logoUrl;
    cfg.primaryColor = raw.primaryColor ?? raw.primary;
    cfg.secondaryColor = raw.secondaryColor ?? raw.secondary;
    cfg.accentColor = raw.accentColor ?? raw.accent;
    return Object.keys(cfg).length ? cfg : null;
  } catch {
    return null;
  }
}
