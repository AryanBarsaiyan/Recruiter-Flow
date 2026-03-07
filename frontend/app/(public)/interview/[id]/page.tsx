"use client";

import { useEffect, useState, useCallback } from "react";
import { Link } from "@/components/link";
import { useParams } from "next/navigation";
import { useAuth } from "@/contexts/AuthContext";
import { api, ApiError } from "@/lib/api";
import { Button } from "@/components/ui/button";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";

const FIRST_QUESTION_KEY = "interview_first_question";

interface Interview {
  id: string;
  status?: string;
  applicationId?: string;
}

interface QuestionData {
  questionId: string;
  title: string;
  description: string;
  starterCode: string;
}

interface SubmitCodeResponse {
  status?: string;
  interviewQuestionId?: string;
}

interface FollowupResponse {
  status?: string;
  followupQuestionId?: string;
}

export default function InterviewRoomPage() {
  const params = useParams();
  const id = params.id as string;
  const { user, isAuthenticated } = useAuth();
  const [interview, setInterview] = useState<Interview | null>(null);
  const [question, setQuestion] = useState<QuestionData | null>(null);
  const [code, setCode] = useState("");
  const [language] = useState("javascript");
  const [submitStatus, setSubmitStatus] = useState<string | null>(null);
  const [followupQuestionId, setFollowupQuestionId] = useState<string | null>(null);
  const [followupAnswer, setFollowupAnswer] = useState("");
  const [followupStatus, setFollowupStatus] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);
  const [submitLoading, setSubmitLoading] = useState(false);
  const [followupLoading, setFollowupLoading] = useState(false);
  const [authError, setAuthError] = useState(false);
  const [proctoringSessionId, setProctoringSessionId] = useState<string | null>(null);

  const loadQuestion = useCallback(() => {
    if (typeof window === "undefined") return;
    try {
      const raw = sessionStorage.getItem(`${FIRST_QUESTION_KEY}_${id}`);
      if (raw) {
        const q = JSON.parse(raw) as QuestionData;
        setQuestion(q);
        setCode(q.starterCode ?? "");
      }
    } catch {
      // ignore
    }
  }, [id]);

  useEffect(() => {
    let cancelled = false;
    if (!isAuthenticated) {
      setLoading(false);
      setAuthError(false);
      return;
    }
    setAuthError(false);
    api<Interview>(`/interviews/${id}`)
      .then((data) => {
        if (!cancelled) {
          setInterview(data);
          loadQuestion();
        }
      })
      .catch((err) => {
        if (!cancelled) {
          setInterview(null);
          if (err instanceof ApiError && err.status === 401) setAuthError(true);
        }
      })
      .finally(() => {
        if (!cancelled) setLoading(false);
      });
    return () => {
      cancelled = true;
    };
  }, [id, isAuthenticated, loadQuestion]);

  useEffect(() => {
    if (isAuthenticated) return;
    loadQuestion();
  }, [isAuthenticated, loadQuestion]);

  async function handleSubmitCode(e: React.FormEvent) {
    e.preventDefault();
    if (!question || !isAuthenticated) return;
    setSubmitStatus(null);
    setSubmitLoading(true);
    try {
      const res = await api<SubmitCodeResponse>(
        `/interviews/${id}/questions/${question.questionId}/submit-code`,
        {
          method: "POST",
          body: JSON.stringify({ language, code }),
        }
      );
      setSubmitStatus(res.status ?? "Submitted");
    } catch (err) {
      if (err instanceof ApiError) setSubmitStatus(err.isRateLimit ? "Too many requests. Please try again later." : err.message);
      else setSubmitStatus("Submit failed");
    } finally {
      setSubmitLoading(false);
    }
  }

  async function handleSubmitFollowup(e: React.FormEvent) {
    e.preventDefault();
    if (!followupQuestionId || !isAuthenticated) return;
    setFollowupStatus(null);
    setFollowupLoading(true);
    try {
      const res = await api<FollowupResponse>(
        `/interviews/${id}/followups/${followupQuestionId}/answer`,
        {
          method: "POST",
          body: JSON.stringify({ answerText: followupAnswer }),
        }
      );
      setFollowupStatus(res.status ?? "Submitted");
    } catch (err) {
      if (err instanceof ApiError) setFollowupStatus(err.isRateLimit ? "Too many requests. Please try again later." : err.message);
      else setFollowupStatus("Submit failed");
    } finally {
      setFollowupLoading(false);
    }
  }

  async function handleStartProctoring() {
    try {
      const res = await api<{ id: string }>("/proctoring/sessions", {
        method: "POST",
        body: JSON.stringify({ interviewId: id }),
      });
      setProctoringSessionId(res.id);
    } catch {
      // ignore
    }
  }

  async function handleEndProctoring() {
    if (!proctoringSessionId) return;
    try {
      await api(`/proctoring/sessions/${proctoringSessionId}/end`, {
        method: "POST",
      });
      setProctoringSessionId(null);
    } catch {
      // ignore
    }
  }

  useEffect(() => {
    if (!proctoringSessionId) return;
    function sendEvent(eventType: string, details?: Record<string, unknown>, weight?: number) {
      api(`/proctoring/sessions/${proctoringSessionId}/events`, {
        method: "POST",
        body: JSON.stringify({
          eventType,
          detailsJson: details ? JSON.stringify(details) : undefined,
          weight: weight ?? 1,
        }),
      }).catch(() => { /* ignore */ });
    }
    function onVisibilityChange() {
      if (document.hidden) sendEvent("tab_switch", { hidden: true }, 1);
    }
    function onFullscreenChange() {
      if (!document.fullscreenElement) sendEvent("fullscreen_exit", { exited: true }, 1);
    }
    document.addEventListener("visibilitychange", onVisibilityChange);
    document.addEventListener("fullscreenchange", onFullscreenChange);
    return () => {
      document.removeEventListener("visibilitychange", onVisibilityChange);
      document.removeEventListener("fullscreenchange", onFullscreenChange);
    };
  }, [proctoringSessionId]);

  if (!isAuthenticated) {
    return (
      <div className="min-h-screen flex flex-col items-center justify-center p-4 bg-muted/30">
        <Card className="w-full max-w-md">
          <CardHeader>
            <CardTitle>Sign in to continue</CardTitle>
            <CardDescription>
              You need to be signed in as a candidate to take this interview.
            </CardDescription>
          </CardHeader>
          <CardContent>
            <Link href={`/login?redirect=${encodeURIComponent(`/interview/${id}`)}`}>
              <Button>Sign in</Button>
            </Link>
          </CardContent>
        </Card>
      </div>
    );
  }

  if (loading && !question) return <div className="container mx-auto px-4 py-8">Loading…</div>;

  if (authError) {
    return (
      <div className="min-h-screen flex flex-col items-center justify-center p-4">
        <Card className="max-w-md">
          <CardHeader>
            <CardTitle>Unable to load interview</CardTitle>
            <CardDescription>You may not have access. Try signing in again.</CardDescription>
          </CardHeader>
          <CardContent>
            <Link href="/login">
              <Button>Sign in</Button>
            </Link>
          </CardContent>
        </Card>
      </div>
    );
  }

  if (!question && !interview) {
    return (
      <div className="min-h-screen flex flex-col items-center justify-center p-4">
        <Card className="max-w-md">
          <CardHeader>
            <CardTitle>Interview</CardTitle>
            <CardDescription>
              Start this interview from your invitation link (book a slot first, then use &quot;Start interview&quot;).
            </CardDescription>
          </CardHeader>
          <CardContent>
            <Link href="/interview/start">
              <Button variant="outline">Go to start</Button>
            </Link>
          </CardContent>
        </Card>
      </div>
    );
  }

  return (
    <div className="min-h-screen flex flex-col p-4 max-w-4xl mx-auto">
      <div className="flex items-center justify-between mb-4">
        <h1 className="text-xl font-semibold">Interview {id.slice(0, 8)}</h1>
        <div className="flex gap-2">
          {!proctoringSessionId ? (
            <Button variant="outline" size="sm" onClick={handleStartProctoring}>
              Start proctoring
            </Button>
          ) : (
            <Button variant="outline" size="sm" onClick={handleEndProctoring}>
              End proctoring
            </Button>
          )}
        </div>
      </div>

      {question && (
        <>
          <Card className="mb-4">
            <CardHeader>
              <CardTitle>{question.title || "Coding question"}</CardTitle>
              <CardDescription className="whitespace-pre-wrap">
                {question.description || "No description."}
              </CardDescription>
            </CardHeader>
          </Card>

          <Card className="mb-4">
            <CardHeader>
              <CardTitle className="text-base">Your code</CardTitle>
            </CardHeader>
            <CardContent className="space-y-3">
              <textarea
                className="flex min-h-[200px] w-full rounded-lg border border-input bg-muted/30 px-3 py-2 font-mono text-sm ring-offset-background placeholder:text-muted-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring"
                value={code}
                onChange={(e) => setCode(e.target.value)}
                placeholder="Write your solution..."
                spellCheck={false}
              />
              <Button onClick={handleSubmitCode} disabled={submitLoading}>
                {submitLoading ? "Submitting…" : "Submit code"}
              </Button>
              {submitStatus && (
                <p className="text-sm text-muted-foreground">{submitStatus}</p>
              )}
            </CardContent>
          </Card>

          <Card>
            <CardHeader>
              <CardTitle className="text-base">Follow-up (optional)</CardTitle>
              <CardDescription>If you received a follow-up question, answer here.</CardDescription>
            </CardHeader>
            <CardContent className="space-y-3">
              <input
                type="text"
                className="flex h-9 w-full rounded-lg border border-input bg-transparent px-3 py-1 text-sm"
                placeholder="Follow-up question ID (if provided)"
                value={followupQuestionId ?? ""}
                onChange={(e) => setFollowupQuestionId(e.target.value || null)}
              />
              <textarea
                className="flex min-h-[80px] w-full rounded-lg border border-input bg-muted/30 px-3 py-2 text-sm"
                value={followupAnswer}
                onChange={(e) => setFollowupAnswer(e.target.value)}
                placeholder="Your answer..."
              />
              <Button
                variant="secondary"
                onClick={handleSubmitFollowup}
                disabled={followupLoading || !followupQuestionId}
              >
                {followupLoading ? "Submitting…" : "Submit follow-up"}
              </Button>
              {followupStatus && (
                <p className="text-sm text-muted-foreground">{followupStatus}</p>
              )}
            </CardContent>
          </Card>
        </>
      )}
    </div>
  );
}
