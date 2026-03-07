import { decodeJwtPayload } from "../jwt";

function base64UrlEncode(obj: object): string {
  const json = JSON.stringify(obj);
  return Buffer.from(json, "utf-8").toString("base64").replace(/\+/g, "-").replace(/\//g, "_").replace(/=+$/, "");
}

describe("decodeJwtPayload", () => {
  it("decodes valid JWT payload", () => {
    const payload = { sub: "user-123", email: "test@example.com", userType: "recruiter", exp: 9999999999 };
    const token = `header.${base64UrlEncode(payload)}.sig`;
    const result = decodeJwtPayload(token);
    expect(result).toEqual(payload);
    expect(result?.sub).toBe("user-123");
    expect(result?.email).toBe("test@example.com");
    expect(result?.userType).toBe("recruiter");
  });

  it("returns null for token with fewer than 3 parts", () => {
    expect(decodeJwtPayload("only-one-part")).toBeNull();
    expect(decodeJwtPayload("one.two")).toBeNull();
  });

  it("returns null for invalid base64 in payload", () => {
    expect(decodeJwtPayload("a.!!!.c")).toBeNull();
  });

  it("returns null for invalid JSON in payload", () => {
    const badB64 = Buffer.from("not json", "utf-8").toString("base64").replace(/\+/g, "-").replace(/\//g, "_");
    expect(decodeJwtPayload(`a.${badB64}.c`)).toBeNull();
  });

  it("returns null for empty string", () => {
    expect(decodeJwtPayload("")).toBeNull();
  });
});
