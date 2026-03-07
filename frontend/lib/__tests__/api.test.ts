import { ApiError } from "../api";

describe("ApiError", () => {
  it("creates error with status, code, message, details", () => {
    const err = new ApiError(400, "BAD_REQUEST", "Invalid input", ["email: required"]);
    expect(err.status).toBe(400);
    expect(err.code).toBe("BAD_REQUEST");
    expect(err.message).toBe("Invalid input");
    expect(err.details).toEqual(["email: required"]);
    expect(err.name).toBe("ApiError");
  });

  it("fromResponse uses body fields and falls back to status", () => {
    const err = ApiError.fromResponse(404, {
      status: 404,
      code: "NOT_FOUND",
      message: "Job not found",
    });
    expect(err.status).toBe(404);
    expect(err.code).toBe("NOT_FOUND");
    expect(err.message).toBe("Job not found");
  });

  it("fromResponse uses error field when message missing", () => {
    const err = ApiError.fromResponse(500, { status: 500, error: "Internal server error" });
    expect(err.message).toBe("Internal server error");
    expect(err.code).toBe("ERROR");
  });

  it("fromResponse uses default message when neither message nor error", () => {
    const err = ApiError.fromResponse(502, { status: 502 });
    expect(err.message).toContain("502");
    expect(err.code).toBe("ERROR");
  });

  it("isRateLimit returns true for 429", () => {
    expect(new ApiError(429, "RATE_LIMIT", "Too many requests").isRateLimit).toBe(true);
    expect(new ApiError(400, "BAD", "Bad").isRateLimit).toBe(false);
  });

  it("isUnauthorized returns true for 401", () => {
    expect(new ApiError(401, "UNAUTHORIZED", "Unauthorized").isUnauthorized).toBe(true);
    expect(new ApiError(403, "FORBIDDEN", "Forbidden").isUnauthorized).toBe(false);
  });
});
