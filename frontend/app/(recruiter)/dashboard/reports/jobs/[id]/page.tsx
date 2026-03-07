"use client";

import { useEffect, useState } from "react";
import { Link } from "@/components/link";
import { useParams } from "next/navigation";
import { api } from "@/lib/api";
import { Button } from "@/components/ui/button";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";

export default function JobReportPage() {
  const params = useParams();
  const id = params.id as string;
  const [report, setReport] = useState<Record<string, unknown> | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    let cancelled = false;
    api<Record<string, unknown>>(`/reports/jobs/${id}`)
      .then((data) => {
        if (!cancelled) setReport(data);
      })
      .catch(() => {
        if (!cancelled) setReport(null);
      })
      .finally(() => {
        if (!cancelled) setLoading(false);
      });
    return () => {
      cancelled = true;
    };
  }, [id]);

  if (loading) return <div className="container mx-auto px-4 py-8">Loading…</div>;
  if (!report) return <div className="container mx-auto px-4 py-8">Report not found.</div>;

  return (
    <div className="space-y-6 max-w-2xl">
        <Link href="/dashboard/reports">
          <Button variant="ghost" size="sm">← Reports</Button>
        </Link>
        <h1 className="text-2xl font-semibold">Job report</h1>
        <Card>
          <CardHeader>
            <CardTitle>Report data</CardTitle>
            <CardDescription>Job ID: {id}</CardDescription>
          </CardHeader>
          <CardContent>
            <pre className="text-sm overflow-auto rounded bg-muted p-4">
              {JSON.stringify(report, null, 2)}
            </pre>
          </CardContent>
        </Card>
    </div>
  );
}
