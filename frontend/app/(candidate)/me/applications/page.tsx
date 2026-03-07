"use client";

import { useEffect, useState } from "react";
import { Link } from "@/components/link";
import { useAuth } from "@/contexts/AuthContext";
import { api } from "@/lib/api";
import type { Application } from "@/lib/types";
import { formatUtcToLocal } from "@/lib/date";
import { Button } from "@/components/ui/button";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";

export default function MyApplicationsPage() {
  const { user, logout } = useAuth();
  const [applications, setApplications] = useState<Application[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    let cancelled = false;
    api<Application[]>("/me/applications")
      .then((data) => {
        if (!cancelled) setApplications(Array.isArray(data) ? data : []);
      })
      .catch(() => {
        if (!cancelled) setApplications([]);
      })
      .finally(() => {
        if (!cancelled) setLoading(false);
      });
    return () => {
      cancelled = true;
    };
  }, []);

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
        <h1 className="text-2xl font-semibold mb-6">My applications</h1>
        {loading ? (
          <p className="text-muted-foreground">Loading…</p>
        ) : applications.length === 0 ? (
          <Card>
            <CardContent className="py-8 text-center text-muted-foreground">
              No applications yet. <Link href="/jobs" className="text-primary underline">Browse jobs</Link>.
            </CardContent>
          </Card>
        ) : (
          <div className="space-y-2">
            {applications.map((app) => (
              <Card key={app.id}>
                <CardHeader className="flex flex-row items-center justify-between py-4">
                  <div>
                    <CardTitle className="text-lg">
                      <Link href={`/applications/${app.id}`} className="hover:underline">
                        {app.jobTitle ? `Applied to ${app.jobTitle}` : `Application #${app.id.slice(0, 8)}`}
                      </Link>
                    </CardTitle>
                    <CardDescription>
                      {app.companyName && <span>{app.companyName} · </span>}
                      Status: {app.status ?? "—"}
                      {app.appliedAt && ` · ${formatUtcToLocal(app.appliedAt)}`}
                    </CardDescription>
                  </div>
                  <Link href={`/applications/${app.id}`}>
                    <Button variant="outline" size="sm">View</Button>
                  </Link>
                </CardHeader>
              </Card>
            ))}
          </div>
        )}
      </main>
    </div>
  );
}
