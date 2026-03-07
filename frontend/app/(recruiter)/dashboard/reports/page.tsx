"use client";

import { useEffect, useState } from "react";
import { Link } from "@/components/link";
import { useAuth } from "@/contexts/AuthContext";
import { api } from "@/lib/api";
import { getAccessToken } from "@/lib/auth";
import type { Job, SpringPage } from "@/lib/types";
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

const PAGE_SIZE = 20;

function getBaseUrl(): string {
  const url = process.env.NEXT_PUBLIC_API_URL;
  if (!url) return "";
  return url.replace(/\/$/, "");
}

async function downloadCsv(path: string, filename: string) {
  const base = getBaseUrl();
  const token = getAccessToken();
  const res = await fetch(`${base}${path}`, {
    headers: token ? { Authorization: `Bearer ${token}` } : {},
  });
  if (!res.ok) throw new Error("Export failed");
  const blob = await res.blob();
  const url = URL.createObjectURL(blob);
  const a = document.createElement("a");
  a.href = url;
  a.download = filename;
  a.click();
  URL.revokeObjectURL(url);
}

export default function ReportsPage() {
  const { user, companyId, logout } = useAuth();
  const [page, setPage] = useState<SpringPage<Job> | null>(null);
  const [exportJobId, setExportJobId] = useState("");
  const [exportAppId, setExportAppId] = useState("");
  const [exportLoading, setExportLoading] = useState(false);
  const [exportError, setExportError] = useState("");

  useEffect(() => {
    if (!companyId) return;
    let cancelled = false;
    api<SpringPage<Job>>(
      `/jobs?companyId=${encodeURIComponent(companyId)}&page=0&size=${PAGE_SIZE}`
    )
      .then((data) => {
        if (!cancelled) setPage(data);
      })
      .catch(() => {
        if (!cancelled) setPage(null);
      });
    return () => {
      cancelled = true;
    };
  }, [companyId]);

  async function handleExportJobCsv(jobId: string) {
    setExportError("");
    setExportLoading(true);
    try {
      await downloadCsv(
        `/reports/export?jobId=${encodeURIComponent(jobId)}`,
        `job-${jobId.slice(0, 8)}-export.csv`
      );
    } catch {
      setExportError("Export failed");
    } finally {
      setExportLoading(false);
    }
  }

  async function handleExportByJobId(e: React.FormEvent) {
    e.preventDefault();
    const id = exportJobId.trim();
    if (!id) return;
    setExportError("");
    setExportLoading(true);
    try {
      await downloadCsv(
        `/reports/export?jobId=${encodeURIComponent(id)}`,
        `job-${id.slice(0, 8)}-export.csv`
      );
    } catch {
      setExportError("Export failed");
    } finally {
      setExportLoading(false);
    }
  }

  async function handleExportByApplicationId(e: React.FormEvent) {
    e.preventDefault();
    const id = exportAppId.trim();
    if (!id) return;
    setExportError("");
    setExportLoading(true);
    try {
      await downloadCsv(
        `/reports/export?applicationId=${encodeURIComponent(id)}`,
        `application-${id.slice(0, 8)}-export.csv`
      );
    } catch {
      setExportError("Export failed");
    } finally {
      setExportLoading(false);
    }
  }

  return (
    <div className="space-y-6">
        <h1 className="text-2xl font-semibold">Reports</h1>
        {exportError && (
          <p className="text-sm text-destructive">{exportError}</p>
        )}

        <Card>
          <CardHeader>
            <CardTitle>Job report & CSV export</CardTitle>
            <CardDescription>
              View report or download CSV for a job.
            </CardDescription>
          </CardHeader>
          <CardContent className="space-y-4">
            {!companyId ? (
              <p className="text-muted-foreground">Set your company to see jobs.</p>
            ) : !page?.content?.length ? (
              <p className="text-muted-foreground">No jobs yet.</p>
            ) : (
              <ul className="space-y-2">
                {page.content.map((job) => (
                  <li
                    key={job.id}
                    className="flex flex-wrap items-center justify-between gap-2 rounded-lg border p-3"
                  >
                    <div>
                      <Link
                        href={`/dashboard/reports/jobs/${job.id}`}
                        className="font-medium hover:underline"
                      >
                        {job.title}
                      </Link>
                      <span className="ml-2 text-sm text-muted-foreground">
                        {job.id}
                      </span>
                    </div>
                    <Button
                      variant="outline"
                      size="sm"
                      disabled={exportLoading}
                      onClick={() => handleExportJobCsv(job.id)}
                    >
                      Export CSV
                    </Button>
                  </li>
                ))}
              </ul>
            )}
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>Export by ID</CardTitle>
            <CardDescription>
              Export CSV by job ID or application ID.
            </CardDescription>
          </CardHeader>
          <CardContent className="space-y-4">
            <form onSubmit={handleExportByJobId} className="flex flex-wrap items-end gap-3">
              <div className="space-y-2">
                <Label htmlFor="jobId">Job ID</Label>
                <Input
                  id="jobId"
                  placeholder="UUID"
                  value={exportJobId}
                  onChange={(e) => setExportJobId(e.target.value)}
                  disabled={exportLoading}
                />
              </div>
              <Button type="submit" disabled={exportLoading}>
                Export job CSV
              </Button>
            </form>
            <form onSubmit={handleExportByApplicationId} className="flex flex-wrap items-end gap-3">
              <div className="space-y-2">
                <Label htmlFor="appId">Application ID</Label>
                <Input
                  id="appId"
                  placeholder="UUID"
                  value={exportAppId}
                  onChange={(e) => setExportAppId(e.target.value)}
                  disabled={exportLoading}
                />
              </div>
              <Button type="submit" disabled={exportLoading}>
                Export application CSV
              </Button>
            </form>
          </CardContent>
        </Card>
    </div>
  );
}
