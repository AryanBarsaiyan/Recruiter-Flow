"use client";

import { Suspense, useState, useEffect } from "react";
import { Link } from "@/components/link";
import { useSearchParams } from "next/navigation";
import { useRouter } from "next/navigation";
import { api, ApiError } from "@/lib/api";
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

interface StartResponse {
  interviewId: string;
  firstQuestionId?: string;
  firstQuestionTitle?: string;
  firstQuestionDescription?: string;
  firstQuestionStarterCode?: string;
}

const FIRST_QUESTION_KEY = "interview_first_question";

function InterviewStartForm() {
  const router = useRouter();
  const searchParams = useSearchParams();
  const [invitationToken, setInvitationToken] = useState("");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    const t = searchParams.get("token");
    if (t) setInvitationToken(t);
  }, [searchParams]);

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    setError("");
    setLoading(true);
    try {
      const data = await api<StartResponse>("/interviews/start", {
        method: "POST",
        skipAuth: true,
        body: JSON.stringify({ invitationToken: invitationToken.trim() }),
      });
      if (data.interviewId && data.firstQuestionId) {
        sessionStorage.setItem(
          `${FIRST_QUESTION_KEY}_${data.interviewId}`,
          JSON.stringify({
            questionId: data.firstQuestionId,
            title: data.firstQuestionTitle ?? "",
            description: data.firstQuestionDescription ?? "",
            starterCode: data.firstQuestionStarterCode ?? "",
          })
        );
      }
      router.replace(`/interview/${data.interviewId}`);
    } catch (err) {
      if (err instanceof ApiError) setError(err.isRateLimit ? "Too many requests. Please try again later." : (err.message || "Failed to start interview"));
      else setError("Something went wrong");
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="min-h-screen flex items-center justify-center p-4 bg-muted/30">
      <Card className="w-full max-w-md">
        <CardHeader>
          <CardTitle>Start interview</CardTitle>
          <CardDescription>
            Enter your invitation token to begin the interview.
          </CardDescription>
        </CardHeader>
        <form onSubmit={handleSubmit}>
          <CardContent className="space-y-4">
            {error && <p className="text-sm text-destructive">{error}</p>}
            <div className="space-y-2">
              <Label htmlFor="token">Invitation token</Label>
              <Input
                id="token"
                value={invitationToken}
                onChange={(e) => setInvitationToken(e.target.value)}
                placeholder="Paste token from email"
                required
                disabled={loading}
              />
            </div>
          </CardContent>
          <CardFooter>
            <Button type="submit" disabled={loading}>
              {loading ? "Starting…" : "Start interview"}
            </Button>
          </CardFooter>
        </form>
      </Card>
    </div>
  );
}

export default function InterviewStartPage() {
  return (
    <Suspense fallback={<div className="min-h-screen flex items-center justify-center p-4">Loading…</div>}>
      <InterviewStartForm />
    </Suspense>
  );
}
