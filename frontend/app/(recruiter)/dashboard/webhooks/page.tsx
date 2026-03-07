"use client";

import { useEffect, useState } from "react";
import { Link } from "@/components/link";
import { useAuth } from "@/contexts/AuthContext";
import { useCompanyRole } from "@/hooks/useCompanyRole";
import { api, ApiError } from "@/lib/api";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import {
  Card,
  CardContent,
  CardDescription,
  CardFooter,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";

interface WebhookConfig {
  companyId: string;
  endpoints: string[];
}

export default function WebhooksPage() {
  const { companyId } = useAuth();
  const { canEdit } = useCompanyRole(companyId);
  const [config, setConfig] = useState<WebhookConfig | null>(null);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState("");
  const [saveMessage, setSaveMessage] = useState("");
  const [notifLoading, setNotifLoading] = useState(false);
  const [notifMessage, setNotifMessage] = useState("");

  useEffect(() => {
    if (!companyId) {
      setLoading(false);
      return;
    }
    let cancelled = false;
    api<WebhookConfig>(`/webhooks/config?companyId=${encodeURIComponent(companyId)}`)
      .then((data) => {
        if (!cancelled) setConfig(data);
      })
      .catch(() => {
        if (!cancelled) setConfig(null);
      })
      .finally(() => {
        if (!cancelled) setLoading(false);
      });
    return () => {
      cancelled = true;
    };
  }, [companyId]);

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    if (!companyId) return;
    setError("");
    setSaveMessage("");
    setSaving(true);
    try {
      await api("/webhooks/config", {
        method: "POST",
        body: JSON.stringify({ companyId, endpoints: config?.endpoints ?? [] }),
      });
      setSaveMessage("Webhook config updated.");
    } catch (err) {
      if (err instanceof ApiError) setError(err.message);
      else setError("Failed to save");
    } finally {
      setSaving(false);
    }
  }

  async function handleTestNotification() {
    setNotifMessage("");
    setNotifLoading(true);
    try {
      const res = await api<{ status?: string; message?: string }>("/notifications/test", {
        method: "POST",
      });
      setNotifMessage(res.message ?? res.status ?? "Test notification sent.");
    } catch (err) {
      if (err instanceof ApiError) setNotifMessage(err.message || "Failed");
      else setNotifMessage("Failed to send test");
    } finally {
      setNotifLoading(false);
    }
  }

  if (!companyId) {
    return (
      <div className="container mx-auto px-4 py-8">
        <p className="text-muted-foreground">Set your company on the dashboard first.</p>
        <Link href="/dashboard">
          <Button className="mt-4">Go to dashboard</Button>
        </Link>
      </div>
    );
  }

  if (loading) return <div className="container mx-auto px-4 py-8">Loading…</div>;

  const endpoints = config?.endpoints ?? [];

  return (
    <div className="space-y-6 max-w-2xl">
        <h1 className="text-2xl font-semibold">Webhooks</h1>
        <Card>
          <CardHeader>
            <CardTitle>Webhook config</CardTitle>
            <CardDescription>
              Configure webhook endpoints for your company. The API may support adding URLs in a future release.
            </CardDescription>
          </CardHeader>
          <form onSubmit={handleSubmit}>
            <CardContent className="space-y-4">
              {error && <p className="text-sm text-destructive">{error}</p>}
              {saveMessage && <p className="text-sm text-green-600">{saveMessage}</p>}
              {!canEdit && (
                <p className="text-sm text-muted-foreground">
                  You have read-only access. Only Admin and SuperAdmin can edit webhooks.
                </p>
              )}
              {endpoints.length > 0 ? (
                <ul className="list-disc list-inside text-sm text-muted-foreground">
                  {endpoints.map((url, i) => (
                    <li key={i}>{url}</li>
                  ))}
                </ul>
              ) : (
                <p className="text-sm text-muted-foreground">No webhook endpoints configured.</p>
              )}
              <div className="space-y-2">
                <Label htmlFor="placeholder">Config (stub)</Label>
                <Input
                  id="placeholder"
                  type="text"
                  placeholder="Webhook config is read-only for now"
                  disabled
                  className="max-w-md"
                />
              </div>
            </CardContent>
            <CardFooter>
              {canEdit && (
                <Button type="submit" disabled={saving}>
                  {saving ? "Saving…" : "Save (stub)"}
                </Button>
              )}
            </CardFooter>
          </form>
        </Card>
        <Card>
          <CardHeader>
            <CardTitle>Test notification</CardTitle>
            <CardDescription>
              Send a test notification to verify your notification setup.
            </CardDescription>
          </CardHeader>
          <CardContent>
            <Button onClick={handleTestNotification} disabled={notifLoading}>
              {notifLoading ? "Sending…" : "Send test notification"}
            </Button>
            {notifMessage && (
              <p className="mt-2 text-sm text-muted-foreground">{notifMessage}</p>
            )}
          </CardContent>
        </Card>
    </div>
  );
}
