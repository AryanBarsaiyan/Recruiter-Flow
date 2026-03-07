import {
  getAccessToken,
  getRefreshToken,
  setTokens,
  clearTokens,
  hasStoredRefreshToken,
} from "../auth";

describe("auth", () => {
  const originalLocalStorage = typeof window !== "undefined" ? window.localStorage : undefined;
  let localStorageMock: Record<string, string>;

  beforeEach(() => {
    localStorageMock = {};
    if (typeof window !== "undefined") {
      (window as unknown as { __accessToken?: string }).__accessToken = undefined;
      Object.defineProperty(window, "localStorage", {
        value: {
          getItem: (key: string) => localStorageMock[key] ?? null,
          setItem: (key: string, value: string) => {
            localStorageMock[key] = value;
          },
          removeItem: (key: string) => {
            delete localStorageMock[key];
          },
        },
        writable: true,
      });
    }
  });

  afterEach(() => {
    if (typeof window !== "undefined" && originalLocalStorage) {
      Object.defineProperty(window, "localStorage", { value: originalLocalStorage, writable: true });
    }
  });

  describe("setTokens and getAccessToken", () => {
    it("stores access token in memory and refresh in localStorage", () => {
      setTokens("access-abc", "refresh-xyz");
      expect(getAccessToken()).toBe("access-abc");
      expect(getRefreshToken()).toBe("refresh-xyz");
    });
  });

  describe("getRefreshToken", () => {
    it("returns null when no refresh token stored", () => {
      expect(getRefreshToken()).toBeNull();
    });
    it("returns stored refresh token after setTokens", () => {
      setTokens("a", "r");
      expect(getRefreshToken()).toBe("r");
    });
  });

  describe("clearTokens", () => {
    it("clears access and refresh tokens", () => {
      setTokens("access-1", "refresh-1");
      clearTokens();
      expect(getAccessToken()).toBeNull();
      expect(getRefreshToken()).toBeNull();
    });
  });

  describe("hasStoredRefreshToken", () => {
    it("returns false when no refresh token", () => {
      clearTokens();
      expect(hasStoredRefreshToken()).toBe(false);
    });
    it("returns true after setTokens", () => {
      setTokens("a", "r");
      expect(hasStoredRefreshToken()).toBe(true);
    });
  });
});
