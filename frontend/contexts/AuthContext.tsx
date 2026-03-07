"use client";

import React, { createContext, useCallback, useContext, useEffect, useMemo, useState } from "react";
import { getAccessToken, setTokens, clearTokens, getRefreshToken, hasStoredRefreshToken } from "@/lib/auth";
import { setApiTokenGetter, api, ApiError } from "@/lib/api";
import { decodeJwtPayload } from "@/lib/jwt";
import type { AuthResponse } from "@/lib/types";

const COMPANY_ID_KEY = "companyId";

export type UserType = "recruiter" | "candidate" | "platform_admin";

export interface User {
  id?: string;
  email: string;
  userType: UserType;
  fullName?: string | null;
  sub?: string;
  defaultCompanyId?: string | null;
}

interface AuthState {
  user: User | null;
  companyId: string | null;
  isLoading: boolean;
  isAuthenticated: boolean;
}

interface AuthContextValue extends AuthState {
  login: (email: string, password: string) => Promise<User>;
  signupSuperAdmin: (data: { email: string; password: string; fullName: string; companyName: string }) => Promise<void>;
  acceptInvite: (token: string, password?: string, fullName?: string) => Promise<User>;
  logout: () => Promise<void>;
  refresh: () => Promise<void>;
  setCompanyId: (id: string | null) => void;
  getAccessToken: () => Promise<string | null>;
}

const AuthContext = createContext<AuthContextValue | null>(null);

function userFromToken(accessToken: string): User | null {
  const payload = decodeJwtPayload(accessToken);
  if (!payload) return null;
  const userType = (payload.userType ?? payload.role ?? "candidate") as UserType;
  const email = (payload.email ?? payload.sub ?? "") as string;
  if (!email) return null;
  return { email, userType, sub: payload.sub as string };
}

function userFromAuthResponse(data: AuthResponse): User | null {
  if (data.user) {
    const rawId = data.user.defaultCompanyId;
    const defaultCompanyId =
      rawId == null ? null : typeof rawId === "string" ? rawId : String(rawId);
    return {
      id: data.user.id,
      email: data.user.email,
      userType: data.user.userType as UserType,
      fullName: data.user.fullName ?? null,
      sub: data.user.id,
      defaultCompanyId: defaultCompanyId || null,
    };
  }
  return null;
}

function applyAuthResponse(data: AuthResponse): { user: User | null; companyId: string | null } {
  const user = userFromAuthResponse(data) ?? (data.accessToken ? userFromToken(data.accessToken) : null);
  const isRecruiter = user?.userType === "recruiter" || user?.userType === "platform_admin";
  const companyId =
    (user?.defaultCompanyId ? String(user.defaultCompanyId) : null) ??
    (isRecruiter ? getStoredCompanyId() : null);
  if (companyId) setStoredCompanyId(companyId);
  return { user, companyId };
}

function getStoredCompanyId(): string | null {
  if (typeof window === "undefined") return null;
  return localStorage.getItem(COMPANY_ID_KEY);
}

function setStoredCompanyId(id: string | null): void {
  if (typeof window === "undefined") return;
  if (id) localStorage.setItem(COMPANY_ID_KEY, id);
  else localStorage.removeItem(COMPANY_ID_KEY);
}

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [state, setState] = useState<AuthState>({
    user: null,
    companyId: getStoredCompanyId(),
    isLoading: true,
    isAuthenticated: false,
  });

  const getToken = useCallback(async (): Promise<string | null> => {
    return getAccessToken();
  }, []);

  useEffect(() => {
    setApiTokenGetter(getToken);
  }, [getToken]);

  const loadUserFromStorage = useCallback(async () => {
    const refresh = getRefreshToken();
    if (!refresh) {
      setState((s) => ({ ...s, user: null, isAuthenticated: false, isLoading: false }));
      return;
    }
    const base = process.env.NEXT_PUBLIC_API_URL?.replace(/\/$/, "") ?? "";
    const res = await fetch(`${base}/auth/refresh`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ refreshToken: refresh }),
    });
    if (!res.ok) {
      clearTokens();
      setState((s) => ({ ...s, user: null, isAuthenticated: false, isLoading: false }));
      return;
    }
    const data = (await res.json()) as AuthResponse;
    if (data.accessToken && data.refreshToken) {
      setTokens(data.accessToken, data.refreshToken);
      const { user, companyId } = applyAuthResponse(data);
      setState((s) => ({
        ...s,
        user,
        companyId: companyId ?? getStoredCompanyId(),
        isAuthenticated: !!user,
        isLoading: false,
      }));
    } else {
      setState((s) => ({ ...s, user: null, isAuthenticated: false, isLoading: false }));
    }
  }, []);

  useEffect(() => {
    if (!hasStoredRefreshToken()) {
      setState((s) => ({ ...s, isLoading: false }));
      return;
    }
    loadUserFromStorage();
  }, [loadUserFromStorage]);

  const login = useCallback(async (email: string, password: string): Promise<User> => {
    const data = await api<AuthResponse>("/auth/login", {
      method: "POST",
      skipAuth: true,
      body: JSON.stringify({ email, password }),
    });
    if (!data.accessToken || !data.refreshToken) throw new ApiError(500, "ERROR", "Invalid login response");
    setTokens(data.accessToken, data.refreshToken);
    const { user, companyId } = applyAuthResponse(data);
    const resolvedUser = user ?? userFromToken(data.accessToken);
    if (!resolvedUser) throw new ApiError(500, "ERROR", "Invalid token");
    setState((s) => ({
      ...s,
      user: resolvedUser,
      isAuthenticated: true,
      companyId: companyId ?? getStoredCompanyId(),
    }));
    return resolvedUser;
  }, []);

  const signupSuperAdmin = useCallback(
    async (body: { email: string; password: string; fullName: string; companyName: string }) => {
      const data = await api<AuthResponse>("/auth/signup-super-admin", {
        method: "POST",
        skipAuth: true,
        body: JSON.stringify(body),
      });
      if (!data.accessToken || !data.refreshToken) throw new ApiError(500, "ERROR", "Invalid signup response");
      setTokens(data.accessToken, data.refreshToken);
      const { user, companyId } = applyAuthResponse(data);
      const resolvedUser = user ?? userFromToken(data.accessToken);
      setState((s) => ({
        ...s,
        user: resolvedUser,
        isAuthenticated: !!resolvedUser,
        companyId: companyId ?? getStoredCompanyId(),
      }));
    },
    []
  );

  const acceptInvite = useCallback(async (token: string, password?: string, fullName?: string): Promise<User> => {
    const data = await api<AuthResponse>("/auth/accept-invite", {
      method: "POST",
      skipAuth: true,
      body: JSON.stringify({ token: token.trim(), password: password ?? "", fullName: fullName?.trim() ?? "" }),
    });
    if (!data.accessToken || !data.refreshToken) throw new ApiError(500, "ERROR", "Invalid response");
    setTokens(data.accessToken, data.refreshToken);
    const { user, companyId } = applyAuthResponse(data);
    const resolvedUser = user ?? userFromToken(data.accessToken);
    if (!resolvedUser) throw new ApiError(500, "ERROR", "Invalid token");
    setState((s) => ({
      ...s,
      user: resolvedUser,
      isAuthenticated: true,
      companyId: companyId ?? getStoredCompanyId(),
    }));
    return resolvedUser;
  }, []);

  const logout = useCallback(async () => {
    try {
      await api("/auth/logout", { method: "POST", skipRefreshRetry: true });
    } catch {
      // ignore
    }
    clearTokens();
    setState({ user: null, companyId: getStoredCompanyId(), isLoading: false, isAuthenticated: false });
  }, []);

  const refresh = useCallback(async () => {
    await loadUserFromStorage();
  }, [loadUserFromStorage]);

  const setCompanyId = useCallback((id: string | null) => {
    setStoredCompanyId(id);
    setState((s) => ({ ...s, companyId: id }));
  }, []);

  const value = useMemo<AuthContextValue>(
    () => ({
      ...state,
      login,
      signupSuperAdmin,
      acceptInvite,
      logout,
      refresh,
      setCompanyId,
      getAccessToken: getToken,
    }),
    [state, login, signupSuperAdmin, acceptInvite, logout, refresh, setCompanyId, getToken]
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth(): AuthContextValue {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error("useAuth must be used within AuthProvider");
  return ctx;
}
