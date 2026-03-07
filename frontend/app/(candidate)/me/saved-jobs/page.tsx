"use client";

import { useEffect, useState } from "react";
import { Link } from "@/components/link";
import { useAuth } from "@/contexts/AuthContext";
import { api } from "@/lib/api";
import { formatUtcToLocalDate } from "@/lib/date";
import { Button } from "@/components/ui/button";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";

interface SavedJob {
  id: string;
  jobId: string;
  jobTitle?: string;
  savedAt?: string;
}

export default function SavedJobsPage() {
  const { user, logout } = useAuth();
  const [saved, setSaved] = useState<SavedJob[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    let cancelled = false;
    api<SavedJob[]>("/me/saved-jobs")
      .then((data) => {
        if (!cancelled) setSaved(Array.isArray(data) ? data : []);
      })
      .catch(() => {
        if (!cancelled) setSaved([]);
      })
      .finally(() => {
        if (!cancelled) setLoading(false);
      });
    return () => {
      cancelled = true;
    };
  }, []);

  async function handleRemove(id: string) {
    try {
      await api(`/me/saved-jobs/${id}`, { method: "DELETE" });
      setSaved((prev) => prev.filter((s) => s.id !== id));
    } catch {
      // ignore
    }
  }

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
            <Link href="/me/saved-jobs">
              <Button variant="ghost" size="sm">Saved jobs</Button>
            </Link>
            <Link href="/jobs">
              <Button variant="ghost" size="sm">Jobs</Button>
            </Link>
            <span className="text-sm text-muted-foreground">{user?.email}</span>
            <Button variant="outline" size="sm" onClick={() => logout()}>
              Log out
            </Button>
          </nav>
        </div>
      </header>
      <main className="flex-1 container mx-auto px-4 py-8">
        <h1 className="text-2xl font-semibold mb-6">Saved jobs</h1>
        {loading ? (
          <p className="text-muted-foreground">Loading…</p>
        ) : saved.length === 0 ? (
          <Card>
            <CardContent className="py-8 text-center text-muted-foreground">
              No saved jobs. <Link href="/jobs" className="text-primary underline">Browse jobs</Link> and save ones you like.
            </CardContent>
          </Card>
        ) : (
          <div className="space-y-2">
            {saved.map((s) => (
              <Card key={s.id}>
                <CardHeader className="flex flex-row items-center justify-between py-4">
                  <div>
                    <CardTitle className="text-lg">
                      <Link href={`/jobs/${s.jobId}/apply`} className="hover:underline">
                        {s.jobTitle ?? `Job ${s.jobId.slice(0, 8)}`}
                      </Link>
                    </CardTitle>
                    <CardDescription>{s.savedAt ? `Saved ${formatUtcToLocalDate(s.savedAt)}` : ""}</CardDescription>
                  </div>
                  <div className="flex gap-2">
                    <Link href={`/jobs/${s.jobId}/apply`}>
                      <Button size="sm">Apply</Button>
                    </Link>
                    <Button
                      variant="outline"
                      size="sm"
                      onClick={() => handleRemove(s.id)}
                    >
                      Remove
                    </Button>
                  </div>
                </CardHeader>
              </Card>
            ))}
          </div>
        )}
      </main>
    </div>
  );
}
