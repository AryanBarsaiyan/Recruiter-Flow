"use client";

import { useEffect, useState } from "react";
import { Link } from "@/components/link";
import { useParams } from "next/navigation";
import { useAuth } from "@/contexts/AuthContext";
import { api } from "@/lib/api";
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
  notes?: string;
}

export default function CandidateApplicationDetailPage() {
  const params = useParams();
  const id = params.id as string;
  const { user, logout } = useAuth();
  const [application, setApplication] = useState<Application | null>(null);
  const [stage, setStage] = useState<StageProgressItem[] | null>(null);

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
    <div className="min-h-screen flex flex-col">
      <header className="border-b bg-card/50">
        <div className="container mx-auto flex h-14 items-center justify-between px-4">
          <Link href="/me" className="font-semibold text-lg">
            Future Scope
          </Link>
          <nav className="flex items-center gap-4">
            <Link href="/me/applications">
              <Button variant="ghost" size="sm">My applications</Button>
            </Link>
            <span className="text-sm text-muted-foreground">{user?.email}</span>
            <Button variant="outline" size="sm" onClick={() => logout()}>
              Log out
            </Button>
          </nav>
        </div>
      </header>
      <main className="flex-1 container mx-auto px-4 py-8 max-w-2xl space-y-6">
        <h1 className="text-2xl font-semibold">Application</h1>
        <Card>
          <CardHeader>
            <CardTitle>Application #{application.id.slice(0, 8)}</CardTitle>
            <CardDescription>
              Status: {application.status ?? "—"} · Applied: {formatUtcToLocal(application.appliedAt)}
            </CardDescription>
          </CardHeader>
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
      </main>
    </div>
  );
}
