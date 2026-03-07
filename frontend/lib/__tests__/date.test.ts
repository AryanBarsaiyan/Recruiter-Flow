import { formatUtcToLocal, formatUtcToLocalDate, toUtcIsoString } from "../date";

describe("date", () => {
  describe("formatUtcToLocal", () => {
    it("formats ISO UTC string to local date and time", () => {
      const result = formatUtcToLocal("2025-03-10T14:00:00.000Z");
      expect(result).not.toBe("—");
      expect(typeof result).toBe("string");
      expect(result.length).toBeGreaterThan(0);
    });

    it("returns — for null or undefined", () => {
      expect(formatUtcToLocal(null)).toBe("—");
      expect(formatUtcToLocal(undefined)).toBe("—");
    });

    it("returns — for invalid date string", () => {
      expect(formatUtcToLocal("not-a-date")).toBe("—");
    });
  });

  describe("formatUtcToLocalDate", () => {
    it("formats ISO UTC string to local date only", () => {
      const result = formatUtcToLocalDate("2025-03-10T14:00:00.000Z");
      expect(result).not.toBe("—");
      expect(typeof result).toBe("string");
    });

    it("returns — for null or undefined", () => {
      expect(formatUtcToLocalDate(null)).toBe("—");
      expect(formatUtcToLocalDate(undefined)).toBe("—");
    });
  });

  describe("toUtcIsoString", () => {
    it("converts Date to ISO string", () => {
      const d = new Date("2025-03-10T14:00:00.000Z");
      expect(toUtcIsoString(d)).toBe("2025-03-10T14:00:00.000Z");
    });
  });
});
