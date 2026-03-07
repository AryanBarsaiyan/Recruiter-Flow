"use client";

import { useEffect, useState, useRef } from "react";
import { useAuth } from "@/contexts/AuthContext";
import { api, ApiError } from "@/lib/api";
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
import { ProfileAvatar } from "@/components/ProfileAvatar";

interface RecruiterProfile {
  email?: string;
  fullName?: string;
  avatarUrl?: string | null;
}

export default function RecruiterProfilePage() {
  const { user } = useAuth();
  const [profile, setProfile] = useState<RecruiterProfile | null>(null);
  const [editing, setEditing] = useState(false);
  const [fullName, setFullName] = useState("");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);
  const [avatarLoading, setAvatarLoading] = useState(false);
  const fileInputRef = useRef<HTMLInputElement>(null);

  function loadProfile() {
    let cancelled = false;
    api<RecruiterProfile>("/recruiter/me")
      .then((data) => {
        if (!cancelled) {
          setProfile(data);
          setFullName(data.fullName ?? "");
        }
      })
      .catch(() => {
        if (!cancelled) setProfile(null);
      });
    return () => {
      cancelled = true;
    };
  }

  useEffect(loadProfile, []);

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    setError("");
    setLoading(true);
    try {
      const updated = await api<RecruiterProfile>("/recruiter/me", {
        method: "PATCH",
        body: JSON.stringify({
          fullName: fullName.trim() || undefined,
        }),
      });
      setProfile(updated);
      setEditing(false);
    } catch (err) {
      if (err instanceof ApiError) setError(err.message || "Update failed");
      else setError("Failed to update profile");
    } finally {
      setLoading(false);
    }
  }

  async function handleAvatarChange(e: React.ChangeEvent<HTMLInputElement>) {
    const file = e.target.files?.[0];
    if (!file) return;
    setError("");
    setAvatarLoading(true);
    try {
      const formData = new FormData();
      formData.append("file", file);
      const updated = await api<RecruiterProfile>("/recruiter/me/avatar", {
        method: "POST",
        body: formData,
      });
      setProfile(updated);
    } catch (err) {
      if (err instanceof ApiError) setError(err.message || "Avatar upload failed");
      else setError("Failed to upload avatar");
    } finally {
      setAvatarLoading(false);
      e.target.value = "";
    }
  }

  if (!profile && !user) return null;

  return (
    <div className="space-y-6">
      <h1 className="text-2xl font-semibold">Profile</h1>
      <Card>
        <CardHeader>
          <CardTitle>Your profile</CardTitle>
          <CardDescription>
            Your recruiter profile. Email cannot be changed.
          </CardDescription>
        </CardHeader>
        <CardContent className="space-y-6">
          <div className="flex items-start gap-6">
            <div className="relative group">
              <ProfileAvatar
                avatarUrl={profile?.avatarUrl}
                avatarEndpoint="/recruiter/me/avatar"
                alt={profile?.fullName ?? user?.email ?? "Profile"}
                size="lg"
              />
              <input
                ref={fileInputRef}
                type="file"
                accept="image/jpeg,image/png,image/webp,image/gif"
                className="hidden"
                onChange={handleAvatarChange}
              />
              <button
                type="button"
                onClick={() => fileInputRef.current?.click()}
                disabled={avatarLoading}
                className="absolute inset-0 flex items-center justify-center rounded-full bg-black/50 opacity-0 group-hover:opacity-100 transition-opacity text-white text-sm font-medium"
              >
                {avatarLoading ? "Uploading…" : "Change photo"}
              </button>
            </div>
            <div className="flex-1 space-y-4">
              {editing ? (
                <form onSubmit={handleSubmit} className="space-y-4">
                  {error && <p className="text-sm text-destructive">{error}</p>}
                  <div className="space-y-2">
                    <Label htmlFor="email">Email</Label>
                    <Input
                      id="email"
                      value={profile?.email ?? user?.email ?? ""}
                      disabled
                      className="bg-muted"
                    />
                    <p className="text-xs text-muted-foreground">
                      Email cannot be changed
                    </p>
                  </div>
                  <div className="space-y-2">
                    <Label htmlFor="fullName">Full name</Label>
                    <Input
                      id="fullName"
                      value={fullName}
                      onChange={(e) => setFullName(e.target.value)}
                      placeholder="Your name"
                      disabled={loading}
                    />
                  </div>
                  <div className="flex gap-2">
                    <Button type="submit" disabled={loading}>
                      {loading ? "Saving…" : "Save"}
                    </Button>
                    <Button
                      type="button"
                      variant="outline"
                      onClick={() => setEditing(false)}
                      disabled={loading}
                    >
                      Cancel
                    </Button>
                  </div>
                </form>
              ) : (
                <div className="space-y-2">
                  <p>
                    <span className="text-muted-foreground">Email:</span>{" "}
                    {profile?.email ?? user?.email ?? "—"}
                  </p>
                  <p>
                    <span className="text-muted-foreground">Name:</span>{" "}
                    {profile?.fullName ?? "—"}
                  </p>
                  <Button
                    variant="outline"
                    size="sm"
                    onClick={() => setEditing(true)}
                    className="mt-4"
                  >
                    Edit profile
                  </Button>
                </div>
              )}
            </div>
          </div>
        </CardContent>
      </Card>
    </div>
  );
}
