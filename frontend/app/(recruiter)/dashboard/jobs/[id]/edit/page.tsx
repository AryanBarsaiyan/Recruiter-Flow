"use client";

import { useEffect, useState } from "react";
import { Link } from "@/components/link";
import { useRouter, useParams } from "next/navigation";
import { useAuth } from "@/contexts/AuthContext";
import { useCompanyRole } from "@/hooks/useCompanyRole";
import { api, ApiError } from "@/lib/api";
import type { Job, Pipeline } from "@/lib/types";
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

export default function EditJobPage() {
  const params = useParams();
  const id = params.id as string;
  const { companyId } = useAuth();
  const { canEdit, loading: roleLoading } = useCompanyRole(companyId);
  const router = useRouter();

  useEffect(() => {
    if (!roleLoading && companyId && !canEdit) {
      router.replace(`/dashboard/jobs/${id}`);
    }
  }, [roleLoading, companyId, canEdit, router, id]);
  const [job, setJob] = useState<Job | null>(null);
  const [title, setTitle] = useState("");
  const [description, setDescription] = useState("");
  const [location, setLocation] = useState("");
  const [employmentType, setEmploymentType] = useState("");
  const [published, setPublished] = useState(false);
  const [pipelineId, setPipelineId] = useState<string>("");
  const [pipelines, setPipelines] = useState<Pipeline[]>([]);
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    let cancelled = false;
    api<Job>(`/jobs/${id}`)
      .then((data) => {
        if (!cancelled) {
          setJob(data);
          setTitle(data.title ?? "");
          setDescription(data.description ?? "");
          setLocation(data.location ?? "");
          setEmploymentType(data.employmentType ?? "");
          setPublished(data.published ?? false);
          setPipelineId(data.pipelineId ?? "");
        }
      })
      .catch(() => {
        if (!cancelled) setJob(null);
      });
    return () => {
      cancelled = true;
    };
  }, [id]);

  useEffect(() => {
    if (!companyId) return;
    api<Pipeline[]>(`/companies/${companyId}/pipelines`)
      .then((data) => setPipelines(Array.isArray(data) ? data : []))
      .catch(() => setPipelines([]));
  }, [companyId]);

  useEffect(() => {
    if (job && pipelines.length > 0 && !job.pipelineId && !pipelineId) {
      const defaultPipeline = pipelines.find((p) => p.isDefault) ?? pipelines[0];
      setPipelineId(defaultPipeline.id);
    }
  }, [job, pipelines, pipelineId]);

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    setError("");
    setLoading(true);
    try {
      await api(`/jobs/${id}`, {
        method: "PUT",
        body: JSON.stringify({
          companyId,
          title: title || "Untitled Job",
          description: description || undefined,
          location: location || undefined,
          employmentType: employmentType || undefined,
          published,
          pipelineId: pipelineId,
        }),
      });
      router.replace(`/dashboard/jobs/${id}`);
    } catch (err) {
      if (err instanceof ApiError) setError(err.details?.join(" ") || err.message);
      else setError("Failed to update job");
    } finally {
      setLoading(false);
    }
  }

  if (!job) return <div className="container mx-auto px-4 py-8">Loading…</div>;
  if (roleLoading || (companyId && !canEdit)) return <div className="container mx-auto px-4 py-8">Loading…</div>;

  if (pipelines.length === 0 && canEdit) {
    return (
      <div className="space-y-6 max-w-lg">
        <Link href={`/dashboard/jobs/${id}`}>
          <Button variant="ghost" size="sm">← Cancel</Button>
        </Link>
        <Card>
          <CardHeader>
            <CardTitle>Pipeline required</CardTitle>
            <CardDescription>
              Every job must have a pipeline. Create a pipeline first to continue editing this job.
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

  return (
    <div className="space-y-6 max-w-lg">
        <Link href={`/dashboard/jobs/${id}`}>
          <Button variant="ghost" size="sm">← Cancel</Button>
        </Link>
        <Card>
          <CardHeader>
            <CardTitle>Edit job</CardTitle>
            <CardDescription>Update job details</CardDescription>
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
                  disabled={loading}
                />
              </div>
              <div className="space-y-2">
                <Label htmlFor="location">Location</Label>
                <Input
                  id="location"
                  value={location}
                  onChange={(e) => setLocation(e.target.value)}
                  disabled={loading}
                />
              </div>
              <div className="space-y-2">
                <Label htmlFor="employmentType">Employment type</Label>
                <Input
                  id="employmentType"
                  value={employmentType}
                  onChange={(e) => setEmploymentType(e.target.value)}
                  disabled={loading}
                />
              </div>
              <div className="space-y-2">
                <Label htmlFor="pipeline">Pipeline (required)</Label>
                <select
                  id="pipeline"
                  value={pipelineId}
                  onChange={(e) => setPipelineId(e.target.value)}
                  disabled={loading || pipelines.length === 0}
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
              </div>
              <div className="flex items-center gap-2">
                <input
                  type="checkbox"
                  id="published"
                  checked={published}
                  onChange={(e) => setPublished(e.target.checked)}
                  disabled={loading}
                  className="h-4 w-4 rounded border-input"
                />
                <Label htmlFor="published" className="cursor-pointer">
                  Published (visible on public job board)
                </Label>
              </div>
            </CardContent>
            <CardFooter>
              <Button type="submit" disabled={loading || !pipelineId}>
                {loading ? "Saving…" : "Save"}
              </Button>
            </CardFooter>
          </form>
        </Card>
    </div>
  );
}
