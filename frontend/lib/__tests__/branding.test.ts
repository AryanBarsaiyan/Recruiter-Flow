import { parseBrandingConfigJson } from "../branding";

describe("parseBrandingConfigJson", () => {
  it("returns null for empty or invalid input", () => {
    expect(parseBrandingConfigJson(null)).toBeNull();
    expect(parseBrandingConfigJson(undefined)).toBeNull();
    expect(parseBrandingConfigJson("")).toBeNull();
    expect(parseBrandingConfigJson("  ")).toBeNull();
    expect(parseBrandingConfigJson("invalid")).toBeNull();
  });

  it("parses logoUrl and primaryColor", () => {
    const json = JSON.stringify({ logoUrl: "https://example.com/logo.png", primaryColor: "#3b82f6" });
    const result = parseBrandingConfigJson(json);
    expect(result).toEqual({ logoUrl: "https://example.com/logo.png", primaryColor: "#3b82f6" });
  });

  it("parses legacy primary key", () => {
    const json = JSON.stringify({ primary: "#3b82f6" });
    const result = parseBrandingConfigJson(json);
    expect(result?.primaryColor).toBe("#3b82f6");
  });

  it("parses full color scheme", () => {
    const json = JSON.stringify({
      logoUrl: "https://acme.com/logo.png",
      primaryColor: "#3b82f6",
      secondaryColor: "#64748b",
      accentColor: "#0ea5e9",
    });
    const result = parseBrandingConfigJson(json);
    expect(result).toEqual({
      logoUrl: "https://acme.com/logo.png",
      primaryColor: "#3b82f6",
      secondaryColor: "#64748b",
      accentColor: "#0ea5e9",
    });
  });
});
