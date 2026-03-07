"use client";

import { useEffect, useState } from "react";
import { Link } from "@/components/link";
import { useAuth } from "@/contexts/AuthContext";
import { useCompanyRole } from "@/hooks/useCompanyRole";
import { api, ApiError } from "@/lib/api";
import type { Company, Pipeline, PipelineStage } from "@/lib/types";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";

export default function CompanyOverviewPage() {
  const { companyId } = useAuth();
  const { canEdit } = useCompanyRole(companyId);
  const [company, setCompany] = useState<Company | null>(null);
  const [pipelines, setPipelines] = useState<Pipeline[]>([]);
  const [loading, setLoading] = useState(true);
  const [showCreate, setShowCreate] = useState(false);
  const [newPipelineName, setNewPipelineName] = useState("");
  const [newPipelineDefault, setNewPipelineDefault] = useState(false);
  const [createLoading, setCreateLoading] = useState(false);
  const [createError, setCreateError] = useState("");
  const [expandedPipeline, setExpandedPipeline] = useState<string | null>(null);
  const [stagesByPipeline, setStagesByPipeline] = useState<Record<string, PipelineStage[]>>({});
  const [showAddStage, setShowAddStage] = useState<string | null>(null);
  const [newStageName, setNewStageName] = useState("");
  const [newStageType, setNewStageType] = useState("manual_interview");
  const [addStageLoading, setAddStageLoading] = useState(false);
  const [reorderLoading, setReorderLoading] = useState<string | null>(null);

  useEffect(() => {
    if (!companyId) {
      setLoading(false);
      return;
    }
    let cancelled = false;
    Promise.all([
      api<Company>(`/companies/${companyId}`),
      api<Pipeline[]>(`/companies/${companyId}/pipelines`).catch(() => []),
    ]).then(([c, p]) => {
      if (!cancelled) {
        setCompany(c);
        setPipelines(Array.isArray(p) ? p : []);
      }
    }).finally(() => {
      if (!cancelled) setLoading(false);
    });
    return () => {
      cancelled = true;
    };
  }, [companyId]);

  async function handleCreatePipeline(e: React.FormEvent) {
    e.preventDefault();
    if (!companyId || !newPipelineName.trim()) return;
    setCreateError("");
    setCreateLoading(true);
    try {
      const created = await api<Pipeline>(`/companies/${companyId}/pipelines`, {
        method: "POST",
        body: JSON.stringify({
          companyId,
          name: newPipelineName.trim(),
          isDefault: newPipelineDefault,
        }),
      });
      setPipelines((prev) => [created, ...prev]);
      setShowCreate(false);
      setNewPipelineName("");
      setNewPipelineDefault(false);
    } catch (err) {
      setCreateError(err instanceof ApiError ? err.message : "Failed to create pipeline");
    } finally {
      setCreateLoading(false);
    }
  }

  async function loadStages(pipelineId: string) {
    try {
      const stages = await api<PipelineStage[]>(`/pipelines/${pipelineId}/stages`);
      setStagesByPipeline((prev) => ({ ...prev, [pipelineId]: Array.isArray(stages) ? stages : [] }));
    } catch {
      setStagesByPipeline((prev) => ({ ...prev, [pipelineId]: [] }));
    }
  }

  function togglePipeline(pipelineId: string) {
    const next = expandedPipeline === pipelineId ? null : pipelineId;
    setExpandedPipeline(next);
    if (next && !stagesByPipeline[next]) {
      loadStages(next);
    }
  }

  async function handleAddStage(e: React.FormEvent, pipelineId: string) {
    e.preventDefault();
    if (!newStageName.trim()) return;
    setAddStageLoading(true);
    try {
      const created = await api<PipelineStage>(`/pipelines/${pipelineId}/stages`, {
        method: "POST",
        body: JSON.stringify({
          name: newStageName.trim(),
          type: newStageType,
        }),
      });
      setStagesByPipeline((prev) => ({
        ...prev,
        [pipelineId]: [...(prev[pipelineId] ?? []), created].sort((a, b) => (a.orderIndex ?? 0) - (b.orderIndex ?? 0)),
      }));
      setShowAddStage(null);
      setNewStageName("");
      setNewStageType("manual_interview");
    } catch {
      // ignore
    } finally {
      setAddStageLoading(false);
    }
  }

  async function handleMoveStage(pipelineId: string, stageId: string, direction: "up" | "down") {
    const stages = stagesByPipeline[pipelineId] ?? [];
    const idx = stages.findIndex((s) => s.id === stageId);
    if (idx < 0) return;
    const newIdx = direction === "up" ? idx - 1 : idx + 1;
    if (newIdx < 0 || newIdx >= stages.length) return;
    const reordered = [...stages];
    [reordered[idx], reordered[newIdx]] = [reordered[newIdx], reordered[idx]];
    const stageIds = reordered.map((s) => s.id);
    setReorderLoading(pipelineId);
    try {
      const updated = await api<PipelineStage[]>(`/pipelines/${pipelineId}/stages/reorder`, {
        method: "PUT",
        body: JSON.stringify({ stageIds }),
      });
      setStagesByPipeline((prev) => ({ ...prev, [pipelineId]: Array.isArray(updated) ? updated : reordered }));
    } catch {
      // ignore
    } finally {
      setReorderLoading(null);
    }
  }

  if (!companyId) {
    return (
      <div className="space-y-4">
        <p className="text-muted-foreground">Set your company on the dashboard first.</p>
        <Link href="/dashboard">
          <Button>Go to dashboard</Button>
        </Link>
      </div>
    );
  }

  if (loading) return <div className="text-muted-foreground">Loading…</div>;

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-semibold">Company overview</h1>
        <div className="flex gap-2">
          <Link href="/company/members">
            <Button variant="outline" size="sm">Members</Button>
          </Link>
          <Link href="/company/branding">
            <Button variant="outline" size="sm">Branding</Button>
          </Link>
        </div>
      </div>
      <Card>
        <CardHeader>
          <CardTitle>{company?.name ?? "—"}</CardTitle>
          <CardDescription>Slug: {company?.slug ?? "—"}</CardDescription>
        </CardHeader>
      </Card>
      <Card>
        <CardHeader>
          <div className="flex items-center justify-between">
            <div>
              <CardTitle>Pipelines</CardTitle>
              <CardDescription>Hiring pipelines for this company. Jobs use pipelines to track applications through stages (Resume Screening → AI Interview → Offer).</CardDescription>
            </div>
            {canEdit && (
              <Button
                variant="outline"
                size="sm"
                onClick={() => setShowCreate(!showCreate)}
              >
                {showCreate ? "Cancel" : "Create pipeline"}
              </Button>
            )}
          </div>
        </CardHeader>
        <CardContent className="space-y-4">
          {showCreate && canEdit && (
            <form onSubmit={handleCreatePipeline} className="rounded-lg border border-border p-4 space-y-3">
              <div className="space-y-2">
                <Label htmlFor="pipelineName">Name</Label>
                <Input
                  id="pipelineName"
                  value={newPipelineName}
                  onChange={(e) => setNewPipelineName(e.target.value)}
                  placeholder="e.g. Default Hiring"
                  required
                  disabled={createLoading}
                />
              </div>
              <div className="flex items-center gap-2">
                <input
                  type="checkbox"
                  id="pipelineDefault"
                  checked={newPipelineDefault}
                  onChange={(e) => setNewPipelineDefault(e.target.checked)}
                  disabled={createLoading}
                  className="h-4 w-4 rounded border-input"
                />
                <Label htmlFor="pipelineDefault" className="cursor-pointer">Use as default for new jobs</Label>
              </div>
              {createError && <p className="text-sm text-destructive">{createError}</p>}
              <Button type="submit" disabled={createLoading}>
                {createLoading ? "Creating…" : "Create"}
              </Button>
            </form>
          )}
          {pipelines.length === 0 ? (
            <p className="text-muted-foreground">No pipelines. Create one to assign hiring stages to jobs.</p>
          ) : (
            <ul className="divide-y">
              {pipelines.map((p) => (
                <li key={p.id} className="py-2">
                  <button
                    type="button"
                    onClick={() => togglePipeline(p.id)}
                    className="flex w-full items-center justify-between text-left hover:bg-muted/50 rounded px-2 py-1 -mx-2"
                  >
                    <span>{p.name ?? p.id}{p.isDefault ? " (default)" : ""}</span>
                    <span className="text-muted-foreground text-sm">
                      {expandedPipeline === p.id ? "▼" : "▶"}
                    </span>
                  </button>
                  {expandedPipeline === p.id && (
                    <div className="mt-2 ml-4 space-y-2">
                      <p className="text-xs text-muted-foreground font-medium">Stages — drag order with ↑↓</p>
                      <ul className="text-sm space-y-1">
                        {(stagesByPipeline[p.id] ?? []).map((s, idx) => (
                          <li key={s.id} className="flex items-center gap-2 group">
                            <span className="text-muted-foreground w-5">{(s.orderIndex ?? idx) + 1}.</span>
                            <span className="flex-1">{s.name ?? s.id}</span>
                            <span className="text-xs text-muted-foreground">({s.type})</span>
                            {canEdit && (
                              <span className="flex gap-0.5 opacity-0 group-hover:opacity-100 transition-opacity">
                                <Button
                                  variant="ghost"
                                  size="icon-xs"
                                  className="h-6 w-6"
                                  disabled={reorderLoading === p.id || idx === 0}
                                  onClick={() => handleMoveStage(p.id, s.id, "up")}
                                  title="Move up"
                                >
                                  ↑
                                </Button>
                                <Button
                                  variant="ghost"
                                  size="icon-xs"
                                  className="h-6 w-6"
                                  disabled={reorderLoading === p.id || idx === (stagesByPipeline[p.id]?.length ?? 0) - 1}
                                  onClick={() => handleMoveStage(p.id, s.id, "down")}
                                  title="Move down"
                                >
                                  ↓
                                </Button>
                              </span>
                            )}
                          </li>
                        ))}
                      </ul>
                      {canEdit && (
                        <div className="pt-2">
                          {showAddStage === p.id ? (
                            <form onSubmit={(e) => handleAddStage(e, p.id)} className="flex flex-wrap gap-2 items-end">
                              <div>
                                <Label htmlFor="stageName" className="text-xs">Stage name</Label>
                                <Input
                                  id="stageName"
                                  value={newStageName}
                                  onChange={(e) => setNewStageName(e.target.value)}
                                  placeholder="e.g. Phone Screen"
                                  className="h-8 text-sm"
                                  disabled={addStageLoading}
                                />
                              </div>
                              <div>
                                <Label htmlFor="stageType" className="text-xs">Type</Label>
                                <select
                                  id="stageType"
                                  value={newStageType}
                                  onChange={(e) => setNewStageType(e.target.value)}
                                  disabled={addStageLoading}
                                  className="h-8 text-sm rounded border border-input bg-transparent px-2"
                                >
                                  <option value="manual_interview">manual_interview</option>
                                  <option value="mcq">mcq</option>
                                  <option value="resume_screening">resume_screening</option>
                                  <option value="ai_interview">ai_interview</option>
                                  <option value="offer">offer</option>
                                </select>
                              </div>
                              <Button type="submit" size="sm" disabled={addStageLoading || !newStageName.trim()}>
                                {addStageLoading ? "Adding…" : "Add"}
                              </Button>
                              <Button type="button" variant="ghost" size="sm" onClick={() => setShowAddStage(null)}>
                                Cancel
                              </Button>
                            </form>
                          ) : (
                            <Button variant="outline" size="xs" onClick={() => setShowAddStage(p.id)}>
                              + Add stage
                            </Button>
                          )}
                        </div>
                      )}
                    </div>
                  )}
                </li>
              ))}
            </ul>
          )}
        </CardContent>
      </Card>
    </div>
  );
}
