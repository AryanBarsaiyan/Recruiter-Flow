"use client";

import { useEffect, useState } from "react";
import { Link } from "@/components/link";
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

interface BulkImportBatch {
  id: string;
  companyId: string;
  status: string;
  sourceFilePath?: string;
  createdAt: string;
}

export default function BulkImportPage() {
  const { companyId } = useAuth();
  const [batches, setBatches] = useState<BulkImportBatch[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!companyId) {
      setLoading(false);
      return;
    }
    let cancelled = false;
    api<BulkImportBatch[]>(`/bulk-import/batches?companyId=${encodeURIComponent(companyId)}`)
      .then((data) => {
        if (!cancelled) setBatches(Array.isArray(data) ? data : []);
      })
      .catch(() => {
        if (!cancelled) setBatches([]);
      })
      .finally(() => {
        if (!cancelled) setLoading(false);
      });
    return () => {
      cancelled = true;
    };
  }, [companyId]);

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

  return (
    <div className="space-y-6">
        <h1 className="text-2xl font-semibold">Bulk import</h1>
        <Card>
          <CardHeader>
            <CardTitle>Import batches</CardTitle>
            <CardDescription>History of bulk candidate imports</CardDescription>
          </CardHeader>
          <CardContent>
            {batches.length === 0 ? (
              <p className="text-muted-foreground py-4">No import batches yet.</p>
            ) : (
              <ul className="space-y-2">
                {batches.map((batch) => (
                  <li key={batch.id}>
                    <Link
                      href={`/dashboard/bulk-import/${batch.id}`}
                      className="flex items-center justify-between rounded-lg border p-3 hover:bg-muted/50"
                    >
                      <span className="font-medium">{batch.id.slice(0, 8)}…</span>
                      <span className="text-sm text-muted-foreground">{batch.status}</span>
                      <span className="text-sm text-muted-foreground">
                        {formatUtcToLocal(batch.createdAt)}
                      </span>
                    </Link>
                  </li>
                ))}
              </ul>
            )}
          </CardContent>
        </Card>
    </div>
  );
}
