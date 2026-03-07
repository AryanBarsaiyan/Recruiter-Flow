"use client";

import { useEffect, useState } from "react";
import { Link } from "@/components/link";
import { useAuth } from "@/contexts/AuthContext";
import { useCompanyRole } from "@/hooks/useCompanyRole";
import { api } from "@/lib/api";
import type { Job, SpringPage } from "@/lib/types";
import { Button } from "@/components/ui/button";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";

const PAGE_SIZE = 10;

export default function RecruiterJobsPage() {
  const { companyId } = useAuth();
  const { canEdit } = useCompanyRole(companyId);
  const [page, setPage] = useState<SpringPage<Job> | null>(null);
  const [pageNumber, setPageNumber] = useState(0);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!companyId) {
      setLoading(false);
      return;
    }
    let cancelled = false;
    setLoading(true);
    api<SpringPage<Job>>(
      `/jobs?companyId=${encodeURIComponent(companyId)}&page=${pageNumber}&size=${PAGE_SIZE}`
    )
      .then((data) => {
        if (!cancelled) setPage(data);
      })
      .catch(() => {
        if (!cancelled) setPage(null);
      })
      .finally(() => {
        if (!cancelled) setLoading(false);
      });
    return () => {
      cancelled = true;
    };
  }, [companyId, pageNumber]);

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

  const jobs = page?.content ?? [];
  const totalPages = page?.totalPages ?? 0;
  const currentNumber = page?.number ?? 0;

  return (
    <div className="space-y-6">
        <div className="flex items-center justify-between">
          <h1 className="text-2xl font-semibold">Jobs</h1>
          {canEdit && (
            <Link href="/dashboard/jobs/new">
              <Button>New job</Button>
            </Link>
          )}
        </div>
        {jobs.length === 0 ? (
          <Card>
            <CardContent className="py-8 text-center text-muted-foreground">
              {canEdit
                ? "No jobs yet. Create your first job to get started."
                : "No jobs yet."}
            </CardContent>
          </Card>
        ) : (
          <>
            <div className="space-y-2">
              {jobs.map((job) => (
                <Card key={job.id}>
                  <CardHeader className="flex flex-row items-center justify-between py-4">
                    <div>
                      <CardTitle className="text-lg">
                        <Link href={`/dashboard/jobs/${job.id}`} className="hover:underline">
                          {job.title}
                        </Link>
                      </CardTitle>
                      <CardDescription>
                        <span className={job.published ? "text-green-600" : "text-muted-foreground"}>
                          {job.published ? "Published" : "Draft"}
                        </span>
                        {" · "}
                        {job.location && `${job.location} · `}
                        {job.employmentType ?? "—"}
                      </CardDescription>
                    </div>
                    <Link href={`/dashboard/jobs/${job.id}`}>
                      <Button variant="outline" size="sm">View</Button>
                    </Link>
                  </CardHeader>
                </Card>
              ))}
            </div>
            {totalPages > 1 && (
              <div className="flex items-center justify-center gap-2">
                <Button
                  variant="outline"
                  size="sm"
                  disabled={currentNumber <= 0}
                  onClick={() => setPageNumber((n) => n - 1)}
                >
                  Previous
                </Button>
                <span className="text-sm text-muted-foreground">
                  Page {currentNumber + 1} of {totalPages}
                </span>
                <Button
                  variant="outline"
                  size="sm"
                  disabled={currentNumber >= totalPages - 1}
                  onClick={() => setPageNumber((n) => n + 1)}
                >
                  Next
                </Button>
              </div>
            )}
          </>
        )}
    </div>
  );
}
