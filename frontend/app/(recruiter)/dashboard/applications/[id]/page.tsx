"use client";

import { useEffect, useState } from "react";
import { Link } from "@/components/link";
import { useParams } from "next/navigation";
import { api, apiBlob } from "@/lib/api";
import { formatUtcToLocal } from "@/lib/date";
import { Button } from "@/components/ui/button";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";

interface Application {
  id: string;
  jobId: string;
  jobTitle?: string;
  companyName?: string;
  candidateId?: string;
  candidateName?: string | null;
  candidateEmail?: string | null;
  resumeId?: string | null;
  resumeOriginalFilename?: string | null;
  status?: string;
  appliedAt?: string;
}

interface StageProgressItem {
  id?: string;
  stageId: string;
  stageName?: string;
  status?: string;
  startedAt?: string;
  completedAt?: string;
}

export default function ApplicationDetailPage() {
  const params = useParams();
  const id = params.id as string;
  const [application, setApplication] = useState<Application | null>(null);
  const [stage, setStage] = useState<StageProgressItem[] | null>(null);
  const [resumeLoading, setResumeLoading] = useState(false);

  async function handlePreviewResume() {
    if (!application?.resumeId) return;
    setResumeLoading(true);
    const win = window.open("", "_blank");
    try {
      const blob = await apiBlob(`/applications/${id}/resume`);
      const url = URL.createObjectURL(blob);
      if (win) {
        win.location.href = url;
      } else {
        window.open(url, "_blank");
      }
      setTimeout(() => URL.revokeObjectURL(url), 60000);
    } catch {
      if (win) win.close();
    } finally {
      setResumeLoading(false);
    }
  }

  useEffect(() => {
    let cancelled = false;
    Promise.all([
      api<Application>(`/applications/${id}`),
      api<StageProgressItem[]>(`/applications/${id}/stage`).catch(() => null),
    ]).then(([app, st]) => {
      if (!cancelled) {
        setApplication(app);
        setStage(Array.isArray(st) ? st : null);
      }
    });
    return () => {
      cancelled = true;
    };
  }, [id]);

  if (!application) return <div className="container mx-auto px-4 py-8">Loading…</div>;

  return (
    <div className="space-y-6 max-w-2xl">
        <div className="flex gap-2">
          <Link href="/dashboard/jobs">
            <Button variant="ghost" size="sm">← Jobs</Button>
          </Link>
          {application.jobId && (
            <Link href={`/dashboard/jobs/${application.jobId}`}>
              <Button variant="ghost" size="sm">Job</Button>
            </Link>
          )}
        </div>
        <h1 className="text-2xl font-semibold">Application</h1>
        <Card>
          <CardHeader>
            <CardTitle>Application #{application.id.slice(0, 8)}</CardTitle>
            <CardDescription>
              {application.jobTitle && (
                <span>{application.jobTitle}{application.companyName ? ` · ${application.companyName}` : ""}</span>
              )}
            </CardDescription>
          </CardHeader>
          <CardContent className="space-y-3">
            <div className="grid gap-2 text-sm">
              <p><span className="text-muted-foreground">Candidate:</span> {application.candidateName ?? "—"}</p>
              <p><span className="text-muted-foreground">Email:</span>{" "}
                <a href={`mailto:${application.candidateEmail ?? ""}`} className="text-primary hover:underline">
                  {application.candidateEmail ?? "—"}
                </a>
              </p>
              <p><span className="text-muted-foreground">Status:</span> {application.status ?? "—"}</p>
              <p><span className="text-muted-foreground">Applied:</span> {formatUtcToLocal(application.appliedAt ?? "")}</p>
            </div>
            {application.resumeId && (
              <Button
                variant="outline"
                size="sm"
                disabled={resumeLoading}
                onClick={handlePreviewResume}
              >
                {resumeLoading ? "Loading…" : "View resume (PDF)"}
              </Button>
            )}
          </CardContent>
        </Card>
        {stage && stage.length > 0 && (
          <Card>
            <CardHeader>
              <CardTitle>Stage progress</CardTitle>
            </CardHeader>
            <CardContent>
              <ul className="space-y-2">
                {stage.map((s) => (
                  <li key={s.stageId} className="flex justify-between">
                    <span>{s.stageName ?? s.stageId}</span>
                    <span className="text-muted-foreground">{s.status ?? "—"}</span>
                  </li>
                ))}
              </ul>
            </CardContent>
          </Card>
        )}
    </div>
  );
}
