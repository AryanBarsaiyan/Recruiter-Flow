"use client";

import { useState, useEffect } from "react";
import { Link } from "@/components/link";
import { useParams } from "next/navigation";
import { useAuth } from "@/contexts/AuthContext";
import { api, ApiError } from "@/lib/api";
import type { Job, UploadResumeResponse } from "@/lib/types";
import { parseCustomFormSchemaJson } from "@/lib/applyForm";
import { parseBrandingConfigJson } from "@/lib/branding";
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

type Step = "upload" | "form" | "done";

export default function ApplyPage() {
  const params = useParams();
  const jobId = params.id as string;
  const { user } = useAuth();
  const [job, setJob] = useState<Job | null>(null);
  const [jobLoadError, setJobLoadError] = useState<"not_found" | "error" | null>(null);
  const [step, setStep] = useState<Step>("upload");
  const [uploadResult, setUploadResult] = useState<UploadResumeResponse | null>(null);
  const [email, setEmail] = useState("");
  const [fullName, setFullName] = useState("");
  const [phone, setPhone] = useState("");
  const [customAnswers, setCustomAnswers] = useState<Record<string, string | number>>({});
  const [error, setError] = useState("");
  const [uploadLoading, setUploadLoading] = useState(false);
  const [submitLoading, setSubmitLoading] = useState(false);

  const customFields = parseCustomFormSchemaJson(job?.customFormSchemaJson);
  const branding = parseBrandingConfigJson(job?.brandingConfigJson);

  useEffect(() => {
    setJobLoadError(null);
    api<Job>(`/jobs/public/${jobId}`, { skipAuth: true })
      .then((data) => {
        setJob(data);
        setJobLoadError(null);
      })
      .catch((err) => {
        setJob(null);
        setJobLoadError(err instanceof ApiError && err.status === 404 ? "not_found" : "error");
      });
  }, [jobId]);

  useEffect(() => {
    if (user?.email) setEmail(user.email);
  }, [user?.email]);

  useEffect(() => {
    if (uploadResult) {
      if (uploadResult.extractedEmail) setEmail((e) => e || (uploadResult.extractedEmail ?? ""));
      if (uploadResult.extractedFullName) setFullName(uploadResult.extractedFullName);
      if (uploadResult.extractedPhone) setPhone(uploadResult.extractedPhone ?? "");
    }
  }, [uploadResult]);

  async function handleUpload(e: React.FormEvent<HTMLFormElement>) {
    e.preventDefault();
    setError("");
    const form = e.currentTarget;
    const fileInput = form.querySelector('input[type="file"]') as HTMLInputElement;
    const file = fileInput?.files?.[0];
    if (!file) {
      setError("Please select a PDF or TXT file.");
      return;
    }
    if (!file.name.toLowerCase().endsWith(".pdf") && !file.name.toLowerCase().endsWith(".txt")) {
      setError("Resume must be PDF or TXT.");
      return;
    }
    setUploadLoading(true);
    try {
      const formData = new FormData();
      formData.append("file", file);
      const base = process.env.NEXT_PUBLIC_API_URL?.replace(/\/$/, "") ?? "";
      const res = await fetch(`${base}/jobs/${jobId}/upload-resume`, {
        method: "POST",
        body: formData,
      });
      if (!res.ok) {
        const err = await res.json().catch(() => ({}));
        throw new ApiError(res.status, err.code ?? "ERROR", err.message ?? "Upload failed", err.details);
      }
      const data = (await res.json()) as UploadResumeResponse;
      setUploadResult(data);
      setStep("form");
    } catch (err) {
      if (err instanceof ApiError) setError(err.isRateLimit ? "Too many requests. Please try again later." : (err.details?.join(" ") || err.message));
      else setError("Failed to upload resume");
    } finally {
      setUploadLoading(false);
    }
  }

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    if (!uploadResult) return;
    setError("");
    setSubmitLoading(true);
    try {
      await api("/jobs/" + jobId + "/apply", {
        method: "POST",
        skipAuth: true,
        body: JSON.stringify({
          email,
          fullName,
          phone: phone || undefined,
          resumeStoragePath: uploadResult.storagePath,
          resumeOriginalFilename: uploadResult.originalFilename,
          answers: Object.keys(customAnswers).length ? customAnswers : undefined,
        }),
      });
      setStep("done");
    } catch (err) {
      if (err instanceof ApiError) setError(err.isRateLimit ? "Too many requests. Please try again later." : (err.details?.join(" ") || err.message));
      else setError("Failed to submit application");
    } finally {
      setSubmitLoading(false);
    }
  }

  if (!job && !jobLoadError) return <div className="container mx-auto px-4 py-8">Loading…</div>;
  if (jobLoadError === "not_found")
    return (
      <div className="container mx-auto px-4 py-8 space-y-4">
        <p>Job not found.</p>
        <Link href="/jobs">
          <Button variant="outline">Back to jobs</Button>
        </Link>
      </div>
    );
  if (jobLoadError === "error" || !job)
    return (
      <div className="container mx-auto px-4 py-8 space-y-4">
        <p>Something went wrong loading this job.</p>
        <Link href="/jobs">
          <Button variant="outline">Back to jobs</Button>
        </Link>
      </div>
    );

  if (step === "done") {
    return (
      <div className="min-h-screen flex flex-col">
        <header
          className="border-b bg-card/50"
          style={
            branding?.primaryColor
              ? { borderBottomColor: branding.primaryColor, borderBottomWidth: "2px" }
              : undefined
          }
        >
          <div className="container mx-auto flex h-14 items-center justify-between px-4">
            <Link href="/" className="font-semibold text-lg flex items-center gap-2">
              {branding?.logoUrl ? (
                <img
                  src={branding.logoUrl}
                  alt={job.companyName ?? "Company logo"}
                  className="h-8 w-auto object-contain"
                />
              ) : null}
              <span style={branding?.primaryColor ? { color: branding.primaryColor } : undefined}>
                {job.companyName ?? "Future Scope"}
              </span>
            </Link>
            <Link href="/jobs">
              <Button variant="ghost" size="sm" style={branding?.primaryColor ? { color: branding.primaryColor } : undefined}>
                Back to jobs
              </Button>
            </Link>
          </div>
        </header>
        <div className="flex-1 flex items-center justify-center p-4 bg-muted/30">
          <Card className="w-full max-w-md">
            <CardHeader>
              <CardTitle>Application submitted</CardTitle>
              <CardDescription>
                Thanks for applying to {job.title}. We&apos;ll be in touch.
              </CardDescription>
            </CardHeader>
            <CardFooter>
              <Link href="/jobs">
                <Button variant="outline">Browse more jobs</Button>
              </Link>
            </CardFooter>
          </Card>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen flex flex-col">
      <header
        className="border-b bg-card/50"
        style={
          branding?.primaryColor
            ? { borderBottomColor: branding.primaryColor, borderBottomWidth: "2px" }
            : undefined
        }
      >
        <div className="container mx-auto flex h-14 items-center justify-between px-4">
          <Link href="/" className="font-semibold text-lg flex items-center gap-2">
            {branding?.logoUrl ? (
              <img
                src={branding.logoUrl}
                alt={job.companyName ?? "Company logo"}
                className="h-8 w-auto object-contain"
              />
            ) : null}
            <span style={branding?.primaryColor ? { color: branding.primaryColor } : undefined}>
              {job.companyName ?? "Future Scope"}
            </span>
          </Link>
          <Link href="/jobs">
            <Button
              variant="ghost"
              size="sm"
              style={
                branding?.primaryColor
                  ? { color: branding.primaryColor }
                  : undefined
              }
            >
              Back to jobs
            </Button>
          </Link>
        </div>
      </header>
      <main className="flex-1 container mx-auto px-4 py-8 max-w-lg">
        <Card>
          <CardHeader>
            <CardTitle>Apply to {job.title}</CardTitle>
            <CardDescription>
              {step === "upload"
                ? "Upload your resume (PDF or TXT). We’ll use it to pre-fill the form."
                : "Review and edit your details, then submit."}
            </CardDescription>
          </CardHeader>

          {step === "upload" && (
            <form onSubmit={handleUpload}>
              <CardContent className="space-y-4">
                {error && <p className="text-sm text-destructive">{error}</p>}
                <div className="space-y-2">
                  <Label htmlFor="resume">Resume (PDF or TXT)</Label>
                  <Input id="resume" type="file" accept=".pdf,.txt" required disabled={uploadLoading} />
                </div>
              </CardContent>
              <CardFooter>
                <Button type="submit" disabled={uploadLoading}>
                  {uploadLoading ? "Uploading…" : "Upload & continue"}
                </Button>
              </CardFooter>
            </form>
          )}

          {step === "form" && (
            <form onSubmit={handleSubmit}>
              <CardContent className="space-y-4">
                {error && <p className="text-sm text-destructive">{error}</p>}
                <div className="space-y-2">
                  <Label htmlFor="fullName">Full name</Label>
                  <Input
                    id="fullName"
                    value={fullName}
                    onChange={(e) => setFullName(e.target.value)}
                    required
                    disabled={submitLoading}
                  />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="email">Email</Label>
                  <Input
                    id="email"
                    type="email"
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                    required
                    disabled={submitLoading}
                  />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="phone">Phone</Label>
                  <Input
                    id="phone"
                    type="tel"
                    value={phone}
                    onChange={(e) => setPhone(e.target.value)}
                    disabled={submitLoading}
                  />
                </div>
                {customFields.map((field) => (
                  <div key={field.key} className="space-y-2">
                    <Label htmlFor={`custom-${field.key}`}>
                      {field.label ?? field.key}
                      {field.required && " *"}
                    </Label>
                    {field.type === "textarea" ? (
                      <textarea
                        id={`custom-${field.key}`}
                        className="flex min-h-[80px] w-full rounded-lg border border-input bg-transparent px-2.5 py-2 text-sm ring-offset-background placeholder:text-muted-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring"
                        value={(customAnswers[field.key] as string) ?? ""}
                        onChange={(e) =>
                          setCustomAnswers((prev) => ({ ...prev, [field.key]: e.target.value }))
                        }
                        required={field.required}
                        disabled={submitLoading}
                      />
                    ) : (
                      <Input
                        id={`custom-${field.key}`}
                        type={field.type === "number" ? "number" : field.type ?? "text"}
                        value={(customAnswers[field.key] as string) ?? ""}
                        onChange={(e) =>
                          setCustomAnswers((prev) => ({
                            ...prev,
                            [field.key]: field.type === "number" ? Number(e.target.value) : e.target.value,
                          }))
                        }
                        required={field.required}
                        disabled={submitLoading}
                      />
                    )}
                  </div>
                ))}
              </CardContent>
              <CardFooter className="flex gap-2">
                <Button type="button" variant="outline" onClick={() => setStep("upload")} disabled={submitLoading}>
                  Change resume
                </Button>
                <Button type="submit" disabled={submitLoading}>
                  {submitLoading ? "Submitting…" : "Submit application"}
                </Button>
              </CardFooter>
            </form>
          )}
        </Card>
      </main>
    </div>
  );
}
