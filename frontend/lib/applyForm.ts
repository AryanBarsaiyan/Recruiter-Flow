import type { CustomFormFieldSchema } from "@/lib/types";

/**
 * Parse job.customFormSchemaJson into an array of field configs for the apply form.
 * Expects JSON array of { key, label?, type?, required? }.
 */
export function parseCustomFormSchemaJson(
  json: string | null | undefined
): CustomFormFieldSchema[] {
  if (!json || typeof json !== "string") return [];
  try {
    const parsed = JSON.parse(json);
    if (!Array.isArray(parsed)) return [];
    return parsed.filter(
      (item): item is CustomFormFieldSchema =>
        item != null && typeof item === "object" && typeof (item as CustomFormFieldSchema).key === "string"
    );
  } catch {
    return [];
  }
}
