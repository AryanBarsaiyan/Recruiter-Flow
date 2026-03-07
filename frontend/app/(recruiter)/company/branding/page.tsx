"use client";

import { useEffect, useState } from "react";
import { useAuth } from "@/contexts/AuthContext";
import { useCompanyRole } from "@/hooks/useCompanyRole";
import { api, ApiError } from "@/lib/api";
import type { Company } from "@/lib/types";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";

export default function CompanyBrandingPage() {
  const { companyId } = useAuth();
  const { canEdit } = useCompanyRole(companyId);
  const [company, setCompany] = useState<Company | null>(null);
  const [loading, setLoading] = useState(true);
  const [brandingLogoUrl, setBrandingLogoUrl] = useState("");
  const [brandingPrimary, setBrandingPrimary] = useState("");
  const [brandingSecondary, setBrandingSecondary] = useState("");
  const [brandingAccent, setBrandingAccent] = useState("");
  const [brandingLoading, setBrandingLoading] = useState(false);
  const [brandingError, setBrandingError] = useState("");

  useEffect(() => {
    if (!companyId) {
      setLoading(false);
      return;
    }
    let cancelled = false;
    api<Company>(`/companies/${companyId}`)
      .then((data) => {
        if (!cancelled) setCompany(data);
      })
      .catch(() => {
        if (!cancelled) setCompany(null);
      })
      .finally(() => {
        if (!cancelled) setLoading(false);
      });
    return () => {
      cancelled = true;
    };
  }, [companyId]);

  useEffect(() => {
    if (!company?.brandingConfigJson) {
      setBrandingLogoUrl("");
      setBrandingPrimary("");
      setBrandingSecondary("");
      setBrandingAccent("");
      return;
    }
    try {
      const cfg = JSON.parse(company.brandingConfigJson) as {
        logoUrl?: string;
        primary?: string;
        primaryColor?: string;
        secondary?: string;
        secondaryColor?: string;
        accent?: string;
        accentColor?: string;
      };
      setBrandingLogoUrl(cfg.logoUrl ?? "");
      setBrandingPrimary(cfg.primaryColor ?? cfg.primary ?? "");
      setBrandingSecondary(cfg.secondaryColor ?? cfg.secondary ?? "");
      setBrandingAccent(cfg.accentColor ?? cfg.accent ?? "");
    } catch {
      setBrandingLogoUrl("");
      setBrandingPrimary("");
      setBrandingSecondary("");
      setBrandingAccent("");
    }
  }, [company?.brandingConfigJson]);

  async function handleBrandingSave(e: React.FormEvent) {
    e.preventDefault();
    if (!companyId) return;
    setBrandingError("");
    setBrandingLoading(true);
    try {
      const cfg: Record<string, string> = {};
      if (brandingLogoUrl.trim()) cfg.logoUrl = brandingLogoUrl.trim();
      if (brandingPrimary.trim()) cfg.primaryColor = brandingPrimary.trim();
      if (brandingSecondary.trim()) cfg.secondaryColor = brandingSecondary.trim();
      if (brandingAccent.trim()) cfg.accentColor = brandingAccent.trim();
      const updated = await api<Company>(`/companies/${companyId}/branding`, {
        method: "PATCH",
        body: JSON.stringify({ brandingConfigJson: JSON.stringify(cfg) }),
      });
      setCompany(updated);
    } catch (err) {
      if (err instanceof ApiError) setBrandingError(err.message || "Update failed");
      else setBrandingError("Something went wrong");
    } finally {
      setBrandingLoading(false);
    }
  }

  if (!companyId) {
    return (
      <div className="space-y-4">
        <p className="text-muted-foreground">Set your company on the dashboard first.</p>
      </div>
    );
  }

  if (loading) return <div className="text-muted-foreground">Loading…</div>;

  return (
    <div className="space-y-6">
      <h1 className="text-2xl font-semibold">Branding & design</h1>
      <Card>
        <CardHeader>
          <CardTitle>Customize public pages</CardTitle>
          <CardDescription>
            Logo and colors applied to job listings and apply pages.
          </CardDescription>
        </CardHeader>
        <CardContent>
          <form onSubmit={handleBrandingSave} className="space-y-4 max-w-md">
            {brandingError && <p className="text-sm text-destructive">{brandingError}</p>}
            {!canEdit && (
              <p className="text-sm text-muted-foreground">
                You have read-only access. Only Admin and SuperAdmin can edit branding.
              </p>
            )}
            <div className="space-y-2">
              <Label htmlFor="branding-logo">Logo URL</Label>
              <Input
                id="branding-logo"
                type="url"
                value={brandingLogoUrl}
                onChange={(e) => setBrandingLogoUrl(e.target.value)}
                placeholder="https://example.com/logo.png"
                disabled={brandingLoading || !canEdit}
              />
            </div>
            <div className="space-y-2">
              <Label htmlFor="branding-primary">Primary color</Label>
              <Input
                id="branding-primary"
                type="text"
                value={brandingPrimary}
                onChange={(e) => setBrandingPrimary(e.target.value)}
                placeholder="#3b82f6"
                disabled={brandingLoading || !canEdit}
              />
            </div>
            <div className="space-y-2">
              <Label htmlFor="branding-secondary">Secondary color</Label>
              <Input
                id="branding-secondary"
                type="text"
                value={brandingSecondary}
                onChange={(e) => setBrandingSecondary(e.target.value)}
                placeholder="#64748b"
                disabled={brandingLoading || !canEdit}
              />
            </div>
            <div className="space-y-2">
              <Label htmlFor="branding-accent">Accent color</Label>
              <Input
                id="branding-accent"
                type="text"
                value={brandingAccent}
                onChange={(e) => setBrandingAccent(e.target.value)}
                placeholder="#0ea5e9"
                disabled={brandingLoading || !canEdit}
              />
            </div>
            {canEdit && (
            <Button type="submit" disabled={brandingLoading}>
              {brandingLoading ? "Saving…" : "Save branding"}
            </Button>
            )}
          </form>
        </CardContent>
      </Card>
    </div>
  );
}
