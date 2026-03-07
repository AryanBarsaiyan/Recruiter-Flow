"use client";

import { useEffect, useState } from "react";
import { Link } from "@/components/link";
import { useAuth } from "@/contexts/AuthContext";
import { api } from "@/lib/api";
import type { RecruiterDashboard as DashboardData } from "@/lib/types";
import { Button } from "@/components/ui/button";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";

export default function DashboardPage() {
  const { companyId, refresh, logout } = useAuth();
  const [dashboard, setDashboard] = useState<DashboardData | null>(null);
  const [loading, setLoading] = useState(true);
  const [retried, setRetried] = useState(false);

  useEffect(() => {
    if (!companyId) {
      setLoading(false);
      return;
    }
    let cancelled = false;
    api<DashboardData>(`/dashboards/recruiter?companyId=${encodeURIComponent(companyId)}`)
      .then((data) => {
        if (!cancelled) setDashboard(data);
      })
      .catch(() => {
        if (!cancelled) setDashboard(null);
      })
      .finally(() => {
        if (!cancelled) setLoading(false);
      });
    return () => {
      cancelled = true;
    };
  }, [companyId]);

  async function handleRetry() {
    setRetried(true);
    await refresh();
  }

  return (
    <div className="space-y-6">
        {loading ? (
          <p className="text-muted-foreground">Loading…</p>
        ) : !companyId ? (
          <Card className="max-w-md">
            <CardHeader>
              <CardTitle>Company not loaded</CardTitle>
              <CardDescription>
                Your company is usually set automatically when you sign in. Try refreshing your session below, or log out and sign in again.
              </CardDescription>
            </CardHeader>
            <CardContent className="flex flex-col gap-3">
              <Button onClick={handleRetry} disabled={retried}>
                {retried ? "Refreshing…" : "Refresh session"}
              </Button>
              <Button variant="outline" onClick={() => logout()}>
                Log out and sign in again
              </Button>
            </CardContent>
          </Card>
        ) : (
          <div className="space-y-6">
            <h1 className="text-2xl font-semibold">Dashboard</h1>
            <div className="grid gap-4 md:grid-cols-2">
              <Card>
                <CardHeader>
                  <CardTitle className="text-lg">Jobs</CardTitle>
                  <CardDescription>Total open jobs</CardDescription>
                </CardHeader>
                <CardContent>
                  <p className="text-3xl font-semibold">{dashboard?.totalJobs ?? "—"}</p>
                </CardContent>
              </Card>
              <Card>
                <CardHeader>
                  <CardTitle className="text-lg">Applications</CardTitle>
                  <CardDescription>Total applications</CardDescription>
                </CardHeader>
                <CardContent>
                  <p className="text-3xl font-semibold">{dashboard?.totalApplications ?? "—"}</p>
                </CardContent>
              </Card>
            </div>
          </div>
        )}
    </div>
  );
}
