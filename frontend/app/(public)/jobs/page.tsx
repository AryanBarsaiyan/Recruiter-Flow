"use client";

import { useEffect, useState } from "react";
import { Link } from "@/components/link";
import { useAuth } from "@/contexts/AuthContext";
import { api } from "@/lib/api";
import type { Job } from "@/lib/types";
import { parseBrandingConfigJson } from "@/lib/branding";
import { Button } from "@/components/ui/button";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";

export default function PublicJobsPage() {
  const { user } = useAuth();
  const [jobs, setJobs] = useState<Job[]>([]);
  const [loading, setLoading] = useState(true);
  const [savedIds, setSavedIds] = useState<Set<string>>(new Set());
  const [savingId, setSavingId] = useState<string | null>(null);

  useEffect(() => {
    let cancelled = false;
    api<Job[]>("/jobs/public", { skipAuth: true })
      .then((data) => {
        if (!cancelled) setJobs(Array.isArray(data) ? data : []);
      })
      .catch(() => {
        if (!cancelled) setJobs([]);
      })
      .finally(() => {
        if (!cancelled) setLoading(false);
      });
    return () => {
      cancelled = true;
    };
  }, []);

  useEffect(() => {
    if (user?.userType !== "candidate") return;
    let cancelled = false;
    api<{ jobId: string }[]>("/me/saved-jobs")
      .then((data) => {
        if (!cancelled && Array.isArray(data))
          setSavedIds(new Set(data.map((s) => s.jobId)));
      })
      .catch(() => {});
    return () => {
      cancelled = true;
    };
  }, [user?.userType]);

  async function toggleSave(jobId: string) {
    if (savingId) return;
    const isSaved = savedIds.has(jobId);
    setSavingId(jobId);
    try {
      if (isSaved) {
        const list = await api<{ id: string; jobId: string }[]>("/me/saved-jobs");
        const entry = list.find((s) => s.jobId === jobId);
        if (entry) await api(`/me/saved-jobs/${entry.id}`, { method: "DELETE" });
        setSavedIds((prev) => {
          const next = new Set(prev);
          next.delete(jobId);
          return next;
        });
      } else {
        await api("/me/saved-jobs", {
          method: "POST",
          body: JSON.stringify({ jobId }),
        });
        setSavedIds((prev) => new Set(prev).add(jobId));
      }
    } catch {
      // ignore
    } finally {
      setSavingId(null);
    }
  }

  return (
    <div className="min-h-screen flex flex-col">
      <header className="border-b bg-card/50">
        <div className="container mx-auto flex h-14 items-center justify-between px-4">
          <Link href="/" className="font-semibold text-lg">
            Future Scope
          </Link>
          <nav className="flex items-center gap-4">
            <Link href="/login">
              <Button variant="ghost" size="sm">
                Sign in
              </Button>
            </Link>
            <Link href="/signup">
              <Button size="sm">
                Get started
              </Button>
            </Link>
          </nav>
        </div>
      </header>
      <main className="flex-1 container mx-auto px-4 py-8">
        <h1 className="text-2xl font-semibold mb-6">Job board</h1>
        {loading ? (
          <p className="text-muted-foreground">Loading…</p>
        ) : jobs.length === 0 ? (
          <Card>
            <CardContent className="py-8 text-center text-muted-foreground">
              No open positions at the moment. Check back later.
            </CardContent>
          </Card>
        ) : (
          <div className="space-y-4">
            {jobs.map((job) => {
              const branding = parseBrandingConfigJson(job.brandingConfigJson);
              return (
              <Card key={job.id}>
                <CardHeader className="flex flex-row items-center justify-between py-4">
                  <div className="flex items-start gap-3">
                    {branding?.logoUrl && (
                      <img
                        src={branding.logoUrl}
                        alt={job.companyName ?? ""}
                        className="h-10 w-10 rounded object-contain bg-muted flex-shrink-0"
                      />
                    )}
                    <div>
                      <CardTitle className="text-lg">{job.title}</CardTitle>
                      <CardDescription>
                        {job.companyName && <span className="font-medium text-foreground/80">{job.companyName}</span>}
                        {(job.companyName && (job.location || job.employmentType)) && " · "}
                        {job.location && `${job.location}`}
                        {job.location && job.employmentType && " · "}
                        {job.employmentType ?? "—"}
                      </CardDescription>
                    </div>
                  </div>
                  <div className="flex items-center gap-2">
                  <Link href={`/jobs/${job.id}/apply`}>
                    <Button size="sm">Apply</Button>
                  </Link>
                  {user?.userType === "candidate" && (
                    <Button
                      variant="outline"
                      size="sm"
                      disabled={!!savingId}
                      onClick={() => toggleSave(job.id)}
                    >
                      {savedIds.has(job.id) ? "Saved" : "Save"}
                    </Button>
                  )}
                </div>
                </CardHeader>
                {job.description && (
                  <CardContent className="pt-0">
                    <p className="text-sm text-muted-foreground line-clamp-2">
                      {job.description}
                    </p>
                  </CardContent>
                )}
              </Card>
            );
            })}
          </div>
        )}
      </main>
    </div>
  );
}
