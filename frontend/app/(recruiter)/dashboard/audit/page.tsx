"use client";

import { useEffect, useState } from "react";
import { Link } from "@/components/link";
import { useAuth } from "@/contexts/AuthContext";
import { api } from "@/lib/api";
import type { SpringPage } from "@/lib/types";
import { formatUtcToLocal } from "@/lib/date";
import { Button } from "@/components/ui/button";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";

interface AuditLogEntry {
  id: string;
  action: string;
  entityType: string;
  entityId: string;
  actorRole: string;
  createdAt: string;
  metadata?: string;
}

const PAGE_SIZE = 20;

export default function AuditLogPage() {
  const { companyId } = useAuth();
  const [page, setPage] = useState<SpringPage<AuditLogEntry> | null>(null);
  const [pageNumber, setPageNumber] = useState(0);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!companyId) {
      setLoading(false);
      return;
    }
    let cancelled = false;
    setLoading(true);
    api<SpringPage<AuditLogEntry>>(
      `/audit-logs?companyId=${encodeURIComponent(companyId)}&page=${pageNumber}&size=${PAGE_SIZE}`
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

  const entries = page?.content ?? [];
  const totalPages = page?.totalPages ?? 0;
  const currentNumber = page?.number ?? 0;

  return (
    <div className="space-y-6">
        <h1 className="text-2xl font-semibold">Audit logs</h1>
        <Card>
          <CardHeader>
            <CardTitle>Activity</CardTitle>
            <CardDescription>Recent actions in your company</CardDescription>
          </CardHeader>
          <CardContent>
            {entries.length === 0 ? (
              <p className="text-muted-foreground py-4">No audit entries yet.</p>
            ) : (
              <>
                <div className="overflow-x-auto">
                  <table className="w-full text-sm">
                    <thead>
                      <tr className="border-b text-left">
                        <th className="py-2 pr-4 font-medium">Time</th>
                        <th className="py-2 pr-4 font-medium">Action</th>
                        <th className="py-2 pr-4 font-medium">Entity</th>
                        <th className="py-2 pr-4 font-medium">Role</th>
                      </tr>
                    </thead>
                    <tbody>
                      {entries.map((entry) => (
                        <tr key={entry.id} className="border-b">
                          <td className="py-2 pr-4 text-muted-foreground whitespace-nowrap">
                            {formatUtcToLocal(entry.createdAt)}
                          </td>
                          <td className="py-2 pr-4">{entry.action}</td>
                          <td className="py-2 pr-4">
                            {entry.entityType}
                            {entry.entityId ? ` · ${entry.entityId.slice(0, 8)}…` : ""}
                          </td>
                          <td className="py-2 pr-4 text-muted-foreground">{entry.actorRole || "—"}</td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
                {totalPages > 1 && (
                  <div className="flex justify-center gap-2 mt-4">
                    <Button
                      variant="outline"
                      size="sm"
                      disabled={currentNumber <= 0}
                      onClick={() => setPageNumber((n) => n - 1)}
                    >
                      Previous
                    </Button>
                    <span className="text-sm text-muted-foreground flex items-center">
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
          </CardContent>
        </Card>
    </div>
  );
}
