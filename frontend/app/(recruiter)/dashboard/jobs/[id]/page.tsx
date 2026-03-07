"use client";

import { useEffect, useState } from "react";
import { Link } from "@/components/link";
import { useRouter, useParams } from "next/navigation";
import { useAuth } from "@/contexts/AuthContext";
import { useCompanyRole } from "@/hooks/useCompanyRole";
import { api, apiBlob } from "@/lib/api";
import type { Job, Application } from "@/lib/types";
import { formatUtcToLocal } from "@/lib/date";
import { Button } from "@/components/ui/button";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";

export default function JobDetailPage() {
  const params = useParams();
  const id = params.id as string;
  const { companyId } = useAuth();
  const { canEdit } = useCompanyRole(companyId);
  const router = useRouter();
  const [job, setJob] = useState<Job | null>(null);
  const [applications, setApplications] = useState<Application[]>([]);
  const [loading, setLoading] = useState(true);
  const [deleteLoading, setDeleteLoading] = useState(false);
  const [publishLoading, setPublishLoading] = useState(false);
  const [resumeLoadingId, setResumeLoadingId] = useState<string | null>(null);

  useEffect(() => {
    let cancelled = false;
    api<Job>(`/jobs/${id}`)
      .then((data) => {
        if (!cancelled) setJob(data);
      })
      .catch(() => {
        if (!cancelled) setJob(null);
      })
      .finally(() => {
        if (!cancelled) setLoading(false);
      });
    return () => {
      cancelled = true;
    };
  }, [id]);

  useEffect(() => {
    if (!job) return;
    let cancelled = false;
    api<Application[]>(`/jobs/${id}/applications`)
      .then((data) => {
        if (!cancelled) setApplications(Array.isArray(data) ? data : []);
      })
      .catch(() => {
        if (!cancelled) setApplications([]);
      });
    return () => {
      cancelled = true;
    };
  }, [id, job]);

  async function handleDelete() {
    if (!confirm("Delete this job?")) return;
    setDeleteLoading(true);
    try {
      await api(`/jobs/${id}`, { method: "DELETE" });
      router.replace("/dashboard/jobs");
    } catch {
      setDeleteLoading(false);
    }
  }

  async function handleTogglePublish() {
    if (!job) return;
    setPublishLoading(true);
    try {
      await api(`/jobs/${id}`, {
        method: "PUT",
        body: JSON.stringify({ published: !job.published }),
      });
      setJob({ ...job, published: !job.published });
    } catch {
      setPublishLoading(false);
    } finally {
      setPublishLoading(false);
    }
  }

  async function handlePreviewResume(app: Application) {
    if (!app.resumeId) return;
    setResumeLoadingId(app.id);
    const win = window.open("", "_blank");
    try {
      const blob = await apiBlob(`/applications/${app.id}/resume`);
      const url = URL.createObjectURL(blob);
      if (win) {
        win.location.href = url;
      } else {
        window.open(url, "_blank");
      }
      setTimeout(() => URL.revokeObjectURL(url), 60000);
    } catch (e) {
      if (win) win.close();
      alert("Could not load resume. Check console for details.");
      console.error("Resume preview failed:", e);
    } finally {
      setResumeLoadingId(null);
    }
  }

  if (loading) return <div className="container mx-auto px-4 py-8">Loading…</div>;
  if (!job) return <div className="container mx-auto px-4 py-8">Job not found.</div>;

  return (
    <div className="space-y-6 max-w-4xl">
      <div className="flex items-center justify-between">
          <Link href="/dashboard/jobs">
            <Button variant="ghost" size="sm">← Jobs</Button>
          </Link>
          {canEdit && (
            <div className="flex gap-2">
              <Link href={`/dashboard/jobs/${id}/edit`}>
                <Button variant="outline" size="sm">Edit</Button>
              </Link>
              <Button
                variant="destructive"
                size="sm"
                disabled={deleteLoading}
                onClick={handleDelete}
              >
                {deleteLoading ? "Deleting…" : "Delete"}
              </Button>
            </div>
          )}
      </div>

      <Card>
          <CardHeader>
            <CardTitle>{job.title}</CardTitle>
            <CardDescription>
              {job.location && `${job.location} · `}
              {job.employmentType ?? "—"}
            </CardDescription>
          </CardHeader>
          <CardContent className="space-y-4">
            {job.description && (
              <div>
                <h3 className="font-medium text-sm text-muted-foreground">Description</h3>
                <p className="mt-1 whitespace-pre-wrap">{job.description}</p>
              </div>
            )}
            {job.pipelineName && (
              <p className="text-sm text-muted-foreground">
                Pipeline: <span className="font-medium">{job.pipelineName}</span>
              </p>
            )}
            <div className="space-y-2">
              <p className="text-sm text-muted-foreground">
                Status:{" "}
                <span className={job.published ? "text-green-600 font-medium" : "text-muted-foreground"}>
                  {job.published ? "Published" : "Draft"}
                </span>
                {canEdit && (
                  <>
                    {" · "}
                    <button
                      type="button"
                      onClick={handleTogglePublish}
                      disabled={publishLoading}
                      className="text-primary underline hover:no-underline disabled:opacity-50"
                    >
                      {publishLoading ? "Updating…" : job.published ? "Unpublish" : "Publish"}
                    </button>
                  </>
                )}
              </p>
              <p className="text-sm text-muted-foreground">
                Public apply link:{" "}
                <a
                  href={`${typeof window !== "undefined" ? window.location.origin : ""}/jobs/${id}/apply`}
                  className="text-primary underline"
                >
                  /jobs/{id}/apply
                </a>
                {!job.published && (
                  <span className="ml-1 text-amber-600">(hidden until published)</span>
                )}
              </p>
            </div>
          </CardContent>
        </Card>

      <Card>
        <CardHeader>
          <CardTitle>Applications</CardTitle>
          <CardDescription>
            {applications.length === 0
              ? "No applications yet"
              : `${applications.length} application${applications.length === 1 ? "" : "s"}`}
          </CardDescription>
        </CardHeader>
        <CardContent>
          {applications.length === 0 ? (
            <p className="text-sm text-muted-foreground">Applications will appear here when candidates apply.</p>
          ) : (
            <div className="overflow-x-auto rounded-lg border border-border">
              <table className="w-full text-sm">
                <thead>
                  <tr className="border-b border-border bg-muted/50">
                    <th className="px-4 py-3 text-left font-medium">ID</th>
                    <th className="px-4 py-3 text-left font-medium">Name</th>
                    <th className="px-4 py-3 text-left font-medium">Email</th>
                    <th className="px-4 py-3 text-left font-medium">Status</th>
                    <th className="px-4 py-3 text-left font-medium">Applied</th>
                    <th className="px-4 py-3 text-right font-medium">Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {applications.map((app) => (
                    <tr key={app.id} className="border-b border-border last:border-0 hover:bg-muted/30">
                      <td className="px-4 py-3 font-mono text-xs text-muted-foreground">
                        {app.id.slice(0, 8)}…
                      </td>
                      <td className="px-4 py-3">
                        {app.candidateName ?? "—"}
                      </td>
                      <td className="px-4 py-3">
                        <a
                          href={`mailto:${app.candidateEmail ?? ""}`}
                          className="text-primary hover:underline"
                        >
                          {app.candidateEmail ?? "—"}
                        </a>
                      </td>
                      <td className="px-4 py-3">
                        <span className="rounded-full bg-muted px-2 py-0.5 text-xs capitalize">
                          {app.status}
                        </span>
                      </td>
                      <td className="px-4 py-3 text-muted-foreground">
                        {formatUtcToLocal(app.appliedAt)}
                      </td>
                      <td className="px-4 py-3 text-right">
                        <div className="flex items-center justify-end gap-2">
                          {app.resumeId && (
                            <Button
                              variant="outline"
                              size="xs"
                              disabled={resumeLoadingId === app.id}
                              onClick={() => handlePreviewResume(app)}
                            >
                              {resumeLoadingId === app.id ? "Loading…" : "PDF"}
                            </Button>
                          )}
                          <Link href={`/dashboard/applications/${app.id}`}>
                            <Button variant="ghost" size="xs">View</Button>
                          </Link>
                        </div>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </CardContent>
      </Card>
    </div>
  );
}
