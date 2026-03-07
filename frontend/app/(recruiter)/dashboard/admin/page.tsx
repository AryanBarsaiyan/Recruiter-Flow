"use client";

import { useEffect, useState } from "react";
import { Link } from "@/components/link";
import { useAuth } from "@/contexts/AuthContext";
import { api, ApiError } from "@/lib/api";
import { Button } from "@/components/ui/button";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";

interface PlatformAdminDashboard {
  totalCompanies: number;
  totalUsers: number;
}

export default function PlatformAdminPage() {
  const { user } = useAuth();
  const [dashboard, setDashboard] = useState<PlatformAdminDashboard | null>(null);
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(true);

  const isPlatformAdmin = user?.userType === "platform_admin";

  useEffect(() => {
    if (!isPlatformAdmin) {
      setLoading(false);
      return;
    }
    let cancelled = false;
    api<PlatformAdminDashboard>("/dashboards/platform-admin")
      .then((data) => {
        if (!cancelled) setDashboard(data);
      })
      .catch((err) => {
        if (!cancelled) {
          if (err instanceof ApiError) setError(err.message);
          else setError("Failed to load dashboard");
        }
      })
      .finally(() => {
        if (!cancelled) setLoading(false);
      });
    return () => {
      cancelled = true;
    };
  }, [isPlatformAdmin]);

  if (!user) return null;
  if (!isPlatformAdmin) {
    return (
      <div className="space-y-6 max-w-md">
          <Card>
            <CardHeader>
              <CardTitle>Access required</CardTitle>
              <CardDescription>
                Platform admin access is required to view this page.
              </CardDescription>
            </CardHeader>
            <CardContent>
              <Link href="/dashboard">
                <Button variant="outline">Go to dashboard</Button>
              </Link>
            </CardContent>
          </Card>
      </div>
    );
  }

  if (loading) return <div className="container mx-auto px-4 py-8">Loading…</div>;
  if (error) {
    return (
      <div className="space-y-6">
        <p className="text-destructive">{error}</p>
        <Link href="/dashboard">
          <Button variant="outline">Back to dashboard</Button>
        </Link>
      </div>
    );
  }

  return (
    <div className="space-y-6">
        <h1 className="text-2xl font-semibold">Platform admin</h1>
        <div className="grid gap-4 md:grid-cols-2">
          <Card>
            <CardHeader>
              <CardTitle className="text-lg">Companies</CardTitle>
              <CardDescription>Total companies</CardDescription>
            </CardHeader>
            <CardContent>
              <p className="text-3xl font-semibold">{dashboard?.totalCompanies ?? "—"}</p>
            </CardContent>
          </Card>
          <Card>
            <CardHeader>
              <CardTitle className="text-lg">Users</CardTitle>
              <CardDescription>Total users</CardDescription>
            </CardHeader>
            <CardContent>
              <p className="text-3xl font-semibold">{dashboard?.totalUsers ?? "—"}</p>
            </CardContent>
          </Card>
        </div>
    </div>
  );
}
