"use client";

import { useEffect, useState } from "react";
import { useAuth } from "@/contexts/AuthContext";
import { api } from "@/lib/api";

const ROLES_CAN_EDIT = ["SuperAdmin", "Admin"];

/** Returns whether the current user can create/update/delete in the company. */
export function useCompanyRole(companyId: string | null) {
  const { user } = useAuth();
  const [roleName, setRoleName] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!companyId || !user) {
      setRoleName(null);
      setLoading(false);
      return;
    }
    let cancelled = false;
    setLoading(true);
    api<{ roleName?: string }>(`/companies/${companyId}/members/me`)
      .then((data) => {
        if (!cancelled) setRoleName(data.roleName ?? null);
      })
      .catch(() => {
        if (!cancelled) setRoleName(null);
      })
      .finally(() => {
        if (!cancelled) setLoading(false);
      });
    return () => {
      cancelled = true;
    };
  }, [companyId, user?.id]);

  const canEdit = roleName != null && ROLES_CAN_EDIT.includes(roleName);
  return { roleName, canEdit, loading };
}
