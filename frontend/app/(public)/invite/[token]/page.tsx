"use client";

import { useEffect, useState } from "react";
import { Link } from "@/components/link";
import { useParams } from "next/navigation";
import { api, ApiError } from "@/lib/api";
import { formatUtcToLocal, toUtcIsoString } from "@/lib/date";
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

interface Invitation {
  invitationId?: string;
  applicationId?: string;
  jobId?: string;
  jobTitle?: string;
  interviewType?: string;
  expiresAt?: string;
  status?: string;
}

export default function InvitePage() {
  const params = useParams();
  const token = params.token as string;
  const [invitation, setInvitation] = useState<Invitation | null>(null);
  const [scheduledStartAt, setScheduledStartAt] = useState("");
  const [scheduledEndAt, setScheduledEndAt] = useState("");
  const [error, setError] = useState("");
  const [success, setSuccess] = useState(false);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    let cancelled = false;
    api<Invitation>(`/interview-invitations/${token}`, { skipAuth: true })
      .then((data) => {
        if (!cancelled) setInvitation(data);
      })
      .catch(() => {
        if (!cancelled) setInvitation(null);
      });
    return () => {
      cancelled = true;
    };
  }, [token]);

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    setError("");
    setLoading(true);
    try {
      const startUtc = scheduledStartAt ? toUtcIsoString(new Date(scheduledStartAt)) : undefined;
      const endUtc = scheduledEndAt ? toUtcIsoString(new Date(scheduledEndAt)) : undefined;
      await api(`/interview-invitations/${token}/slots`, {
        method: "POST",
        skipAuth: true,
        body: JSON.stringify({
          scheduledStartAt: startUtc,
          scheduledEndAt: endUtc,
        }),
      });
      setSuccess(true);
    } catch (err) {
      if (err instanceof ApiError) setError(err.details?.join(" ") || err.message);
      else setError("Failed to book slot");
    } finally {
      setLoading(false);
    }
  }

  if (!invitation && !error) return <div className="container mx-auto px-4 py-8">Loading…</div>;
  if (!invitation) return <div className="container mx-auto px-4 py-8">Invalid or expired invitation.</div>;

  if (success) {
    return (
      <div className="min-h-screen flex items-center justify-center p-4 bg-muted/30">
        <Card className="w-full max-w-md">
          <CardHeader>
            <CardTitle>Slot booked</CardTitle>
            <CardDescription>
              Your interview slot has been confirmed. Sign in as a candidate, then start the interview when the time comes.
            </CardDescription>
          </CardHeader>
          <CardFooter className="flex flex-col gap-2">
            <Link href={`/interview/start?token=${encodeURIComponent(token)}`}>
              <Button className="w-full">Start interview</Button>
            </Link>
            <Link href="/login">
              <Button variant="outline" className="w-full">Sign in first</Button>
            </Link>
          </CardFooter>
        </Card>
      </div>
    );
  }

  return (
    <div className="min-h-screen flex items-center justify-center p-4 bg-muted/30">
      <Card className="w-full max-w-md">
        <CardHeader>
          <CardTitle>Interview invitation</CardTitle>
          <CardDescription className="space-y-1">
            {invitation.jobTitle && <span className="block font-medium text-foreground">Role: {invitation.jobTitle}</span>}
            {invitation.interviewType && <span className="block">Type: {invitation.interviewType}</span>}
            {invitation.expiresAt && <span className="block text-muted-foreground">Expires: {formatUtcToLocal(invitation.expiresAt)}</span>}
            <span className="block pt-1">Book your interview slot below.</span>
          </CardDescription>
        </CardHeader>
        <form onSubmit={handleSubmit}>
          <CardContent className="space-y-4">
            {error && <p className="text-sm text-destructive">{error}</p>}
            <div className="space-y-2">
              <Label htmlFor="scheduledStartAt">Start (your local time)</Label>
              <Input
                id="scheduledStartAt"
                type="datetime-local"
                value={scheduledStartAt}
                onChange={(e) => setScheduledStartAt(e.target.value)}
                disabled={loading}
              />
            </div>
            <div className="space-y-2">
              <Label htmlFor="scheduledEndAt">End (your local time)</Label>
              <Input
                id="scheduledEndAt"
                type="datetime-local"
                value={scheduledEndAt}
                onChange={(e) => setScheduledEndAt(e.target.value)}
                disabled={loading}
              />
            </div>
          </CardContent>
          <CardFooter>
            <Button type="submit" disabled={loading}>
              {loading ? "Booking…" : "Book slot"}
            </Button>
          </CardFooter>
        </form>
      </Card>
    </div>
  );
}
