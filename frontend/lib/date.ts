/**
 * API datetimes are in UTC (ISO 8601).
 * Use these helpers to show and edit in the user's local timezone.
 */

/** Format an ISO UTC string for display in local time (date + time). */
export function formatUtcToLocal(isoUtc: string | null | undefined): string {
  if (!isoUtc) return "—";
  try {
    const d = new Date(isoUtc);
    if (Number.isNaN(d.getTime())) return "—";
    return d.toLocaleString(undefined, {
      dateStyle: "short",
      timeStyle: "short",
    });
  } catch {
    return "—";
  }
}

/** Format an ISO UTC string for display in local time (date only). */
export function formatUtcToLocalDate(isoUtc: string | null | undefined): string {
  if (!isoUtc) return "—";
  try {
    const d = new Date(isoUtc);
    if (Number.isNaN(d.getTime())) return "—";
    return d.toLocaleDateString(undefined, { dateStyle: "short" });
  } catch {
    return "—";
  }
}

/** Convert local Date to ISO string (UTC) for sending to API. */
export function toUtcIsoString(date: Date): string {
  return date.toISOString();
}
