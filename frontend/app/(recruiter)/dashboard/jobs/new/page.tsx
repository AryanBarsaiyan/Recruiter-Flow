"use client";

import { useEffect, useState } from "react";
import { Link } from "@/components/link";
import { useRouter } from "next/navigation";
import { useAuth } from "@/contexts/AuthContext";
import { useCompanyRole } from "@/hooks/useCompanyRole";
import { api, ApiError } from "@/lib/api";
import type { Pipeline } from "@/lib/types";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import {
  Card,
  CardContent,
  CardDescription,
  CardFooter,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";

export default function NewJobPage() {
  const { companyId } = useAuth();
  const { canEdit, loading: roleLoading } = useCompanyRole(companyId);
  const router = useRouter();

  useEffect(() => {
    if (!roleLoading && companyId && !canEdit) {
      router.replace("/dashboard/jobs");
    }
  }, [roleLoading, companyId, canEdit, router]);
  const [title, setTitle] = useState("");
  const [description, setDescription] = useState("");
  const [location, setLocation] = useState("");
  const [employmentType, setEmploymentType] = useState("");
  const [pipelineId, setPipelineId] = useState<string>("");
  const [pipelines, setPipelines] = useState<Pipeline[]>([]);
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (!companyId) return;
    api<Pipeline[]>(`/companies/${companyId}/pipelines`)
      .then((data) => {
        const list = Array.isArray(data) ? data : [];
        setPipelines(list);
        if (list.length > 0 && !pipelineId) {
          const defaultPipeline = list.find((p) => p.isDefault) ?? list[0];
          setPipelineId(defaultPipeline.id);
        }
      })
      .catch(() => setPipelines([]));
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

  if (roleLoading || !canEdit) {
    return <div className="container mx-auto px-4 py-8">Loading…</div>;
  }

  if (pipelines.length === 0) {
    return (
      <div className="space-y-6 max-w-lg">
        <Link href="/dashboard/jobs">
          <Button variant="ghost" size="sm">← Back to jobs</Button>
        </Link>
        <Card>
          <CardHeader>
            <CardTitle>Pipeline required</CardTitle>
            <CardDescription>
              Every job must have a pipeline. Create a pipeline first to define hiring stages (Resume Screening → AI Interview → Offer).
            </CardDescription>
          </CardHeader>
          <CardContent>
            <Link href="/company">
              <Button>Create pipeline</Button>
            </Link>
          </CardContent>
        </Card>
      </div>
    );
  }

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    setError("");
    setLoading(true);
    try {
      const job = await api<{ id: string }>("/jobs", {
        method: "POST",
        body: JSON.stringify({
          companyId,
          title: title || "Untitled Job",
          description: description || undefined,
          location: location || undefined,
          employmentType: employmentType || undefined,
          pipelineId: pipelineId,
        }),
      });
      router.replace(`/dashboard/jobs/${job.id}`);
    } catch (err) {
      if (err instanceof ApiError) setError(err.details?.join(" ") || err.message);
      else setError("Failed to create job");
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="space-y-6 max-w-lg">
        <Link href="/dashboard/jobs">
          <Button variant="ghost" size="sm">← Back to jobs</Button>
        </Link>
        <Card>
          <CardHeader>
            <CardTitle>New job</CardTitle>
            <CardDescription>Create a new job posting</CardDescription>
          </CardHeader>
          <form onSubmit={handleSubmit}>
            <CardContent className="space-y-4">
              {error && (
                <p className="text-sm text-destructive">{error}</p>
              )}
              <div className="space-y-2">
                <Label htmlFor="title">Title</Label>
                <Input
                  id="title"
                  value={title}
                  onChange={(e) => setTitle(e.target.value)}
                  placeholder="Job title"
                  required
                  disabled={loading}
                />
              </div>
              <div className="space-y-2">
                <Label htmlFor="description">Description</Label>
                <textarea
                  id="description"
                  className="flex min-h-[100px] w-full rounded-lg border border-input bg-transparent px-2.5 py-2 text-sm ring-offset-background placeholder:text-muted-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring"
                  value={description}
                  onChange={(e) => setDescription(e.target.value)}
                  placeholder="Description"
                  disabled={loading}
                />
              </div>
              <div className="space-y-2">
                <Label htmlFor="location">Location</Label>
                <Input
                  id="location"
                  value={location}
                  onChange={(e) => setLocation(e.target.value)}
                  placeholder="e.g. Remote"
                  disabled={loading}
                />
              </div>
              <div className="space-y-2">
                <Label htmlFor="employmentType">Employment type</Label>
                <Input
                  id="employmentType"
                  value={employmentType}
                  onChange={(e) => setEmploymentType(e.target.value)}
                  placeholder="e.g. Full-time"
                  disabled={loading}
                />
              </div>
              <div className="space-y-2">
                <Label htmlFor="pipeline">Pipeline (required)</Label>
                <select
                  id="pipeline"
                  value={pipelineId}
                  onChange={(e) => setPipelineId(e.target.value)}
                  disabled={loading}
                  required
                  className="flex h-8 w-full rounded-lg border border-input bg-transparent px-2.5 py-2 text-sm ring-offset-background focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring"
                >
                  {pipelines.map((p) => (
                    <option key={p.id} value={p.id}>
                      {p.name ?? p.id}
                      {p.isDefault ? " (default)" : ""}
                    </option>
                  ))}
                </select>
                <p className="text-xs text-muted-foreground">
                  Pipeline defines hiring stages (Resume Screening → AI Interview → Offer).
                </p>
              </div>
            </CardContent>
            <CardFooter>
              <Button type="submit" disabled={loading || !pipelineId}>
                {loading ? "Creating…" : "Create job"}
              </Button>
            </CardFooter>
          </form>
        </Card>
    </div>
  );
}
