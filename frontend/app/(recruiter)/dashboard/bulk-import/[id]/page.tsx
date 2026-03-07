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

interface BatchDetail {
  id: string;
  companyId: string;
  status: string;
  sourceFilePath?: string;
  createdAt: string;
  candidateCount: number;
}

export default function BulkImportBatchPage() {
  const params = useParams();
  const id = params.id as string;
  const { companyId } = useAuth();
  const [batch, setBatch] = useState<BatchDetail | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!id) {
      setLoading(false);
      return;
    }
    let cancelled = false;
    api<BatchDetail>(`/bulk-import/batches/${id}`)
      .then((data) => {
        if (!cancelled) setBatch(data);
      })
      .catch(() => {
        if (!cancelled) setBatch(null);
      })
      .finally(() => {
        if (!cancelled) setLoading(false);
      });
    return () => {
      cancelled = true;
    };
  }, [id]);

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
  if (!batch) return <div className="container mx-auto px-4 py-8">Batch not found.</div>;

  return (
    <div className="space-y-6 max-w-2xl">
        <Link href="/dashboard/bulk-import">
          <Button variant="ghost" size="sm">← Bulk import</Button>
        </Link>
        <h1 className="text-2xl font-semibold">Import batch</h1>
        <Card>
          <CardHeader>
            <CardTitle>Batch {batch.id.slice(0, 8)}…</CardTitle>
            <CardDescription>Status: {batch.status}</CardDescription>
          </CardHeader>
          <CardContent className="space-y-2">
            <p className="text-sm">
              <span className="text-muted-foreground">Created:</span>{" "}
              {formatUtcToLocal(batch.createdAt)}
            </p>
            <p className="text-sm">
              <span className="text-muted-foreground">Candidates:</span> {batch.candidateCount}
            </p>
            {batch.sourceFilePath && (
              <p className="text-sm">
                <span className="text-muted-foreground">Source:</span> {batch.sourceFilePath}
              </p>
            )}
          </CardContent>
        </Card>
        <Link href="/dashboard/bulk-import">
          <Button variant="outline">Back to batches</Button>
        </Link>
    </div>
  );
}
