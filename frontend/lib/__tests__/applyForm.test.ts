import { parseCustomFormSchemaJson } from "../applyForm";

describe("parseCustomFormSchemaJson", () => {
  it("returns empty array for null or undefined", () => {
    expect(parseCustomFormSchemaJson(null)).toEqual([]);
    expect(parseCustomFormSchemaJson(undefined)).toEqual([]);
  });

  it("returns empty array for invalid JSON", () => {
    expect(parseCustomFormSchemaJson("not json")).toEqual([]);
    expect(parseCustomFormSchemaJson("{")).toEqual([]);
  });

  it("returns empty array for non-array JSON", () => {
    expect(parseCustomFormSchemaJson("{}")).toEqual([]);
    expect(parseCustomFormSchemaJson("true")).toEqual([]);
  });

  it("parses valid array of field schemas", () => {
    const json = JSON.stringify([
      { key: "portfolio", label: "Portfolio URL", type: "url" },
      { key: "years", label: "Years of experience", type: "number", required: true },
    ]);
    expect(parseCustomFormSchemaJson(json)).toHaveLength(2);
    expect(parseCustomFormSchemaJson(json)[0]).toEqual({
      key: "portfolio",
      label: "Portfolio URL",
      type: "url",
    });
    expect(parseCustomFormSchemaJson(json)[1].key).toBe("years");
    expect(parseCustomFormSchemaJson(json)[1].required).toBe(true);
  });

  it("filters out items without key", () => {
    const json = JSON.stringify([{ label: "No key" }, { key: "ok" }]);
    expect(parseCustomFormSchemaJson(json)).toHaveLength(1);
    expect(parseCustomFormSchemaJson(json)[0].key).toBe("ok");
  });
});
