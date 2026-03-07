"use client";

import { useEffect, useState, useRef } from "react";
import { Link } from "@/components/link";
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

interface MeProfile {
  id?: string;
  email?: string;
  fullName?: string;
  phone?: string;
  college?: string;
  graduationYear?: number;
  avatarUrl?: string | null;
}

export default function MePage() {
  const { user, logout } = useAuth();
  const [profile, setProfile] = useState<MeProfile | null>(null);
  const [editing, setEditing] = useState(false);
  const [fullName, setFullName] = useState("");
  const [phone, setPhone] = useState("");
  const [college, setCollege] = useState("");
  const [graduationYear, setGraduationYear] = useState("");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);
  const [avatarLoading, setAvatarLoading] = useState(false);
  const fileInputRef = useRef<HTMLInputElement>(null);

  function loadProfile() {
    let cancelled = false;
    api<MeProfile>("/me")
      .then((data) => {
        if (!cancelled) {
          setProfile(data);
          setFullName(data.fullName ?? "");
          setPhone(data.phone ?? "");
          setCollege(data.college ?? "");
          setGraduationYear(data.graduationYear != null ? String(data.graduationYear) : "");
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
      const updated = await api<MeProfile>("/me", {
        method: "PATCH",
        body: JSON.stringify({
          fullName: fullName.trim() || undefined,
          phone: phone.trim() || undefined,
          college: college.trim() || undefined,
          graduationYear: graduationYear.trim() ? parseInt(graduationYear, 10) : undefined,
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
      const updated = await api<MeProfile>("/me/avatar", {
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
    <div className="min-h-screen flex flex-col">
      <header className="border-b bg-card/50">
        <div className="container mx-auto flex h-14 items-center justify-between px-4">
          <Link href="/me" className="font-semibold text-lg">
            Future Scope
          </Link>
          <nav className="flex items-center gap-4">
            <Link href="/me/applications">
              <Button variant="ghost" size="sm">
                My applications
              </Button>
            </Link>
            <Link href="/me/saved-jobs">
              <Button variant="ghost" size="sm">
                Saved jobs
              </Button>
            </Link>
            <Link href="/jobs">
              <Button variant="ghost" size="sm">
                Jobs
              </Button>
            </Link>
            <span className="text-sm text-muted-foreground">{user?.email}</span>
            <Button variant="outline" size="sm" onClick={() => logout()}>
              Log out
            </Button>
          </nav>
        </div>
      </header>
      <main className="flex-1 container mx-auto px-4 py-8 max-w-2xl">
        <h1 className="text-2xl font-semibold mb-6">My profile</h1>
        <Card>
          <CardHeader>
            <CardTitle>Profile</CardTitle>
            <CardDescription>Your candidate profile. Email cannot be changed.</CardDescription>
          </CardHeader>
          <CardContent className="space-y-6">
            <div className="flex items-start gap-6">
              <div className="relative group">
                <ProfileAvatar
                  avatarUrl={profile?.avatarUrl}
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
                      <p className="text-xs text-muted-foreground">Email cannot be changed</p>
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
                    <div className="space-y-2">
                      <Label htmlFor="phone">Phone</Label>
                      <Input
                        id="phone"
                        type="tel"
                        value={phone}
                        onChange={(e) => setPhone(e.target.value)}
                        placeholder="+1 234 567 8900"
                        disabled={loading}
                      />
                    </div>
                    <div className="space-y-2">
                      <Label htmlFor="college">College</Label>
                      <Input
                        id="college"
                        value={college}
                        onChange={(e) => setCollege(e.target.value)}
                        placeholder="Your college or university"
                        disabled={loading}
                      />
                    </div>
                    <div className="space-y-2">
                      <Label htmlFor="graduationYear">Graduation year</Label>
                      <Input
                        id="graduationYear"
                        type="number"
                        min="1990"
                        max="2030"
                        value={graduationYear}
                        onChange={(e) => setGraduationYear(e.target.value)}
                        placeholder="2025"
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
                    <p><span className="text-muted-foreground">Email:</span> {profile?.email ?? user?.email ?? "—"}</p>
                    <p><span className="text-muted-foreground">Name:</span> {profile?.fullName ?? "—"}</p>
                    <p><span className="text-muted-foreground">Phone:</span> {profile?.phone ?? "—"}</p>
                    <p><span className="text-muted-foreground">College:</span> {profile?.college ?? "—"}</p>
                    <p><span className="text-muted-foreground">Graduation year:</span> {profile?.graduationYear ?? "—"}</p>
                    <Button variant="outline" size="sm" onClick={() => setEditing(true)} className="mt-4">
                      Edit profile
                    </Button>
                  </div>
                )}
              </div>
            </div>
          </CardContent>
        </Card>
      </main>
    </div>
  );
}
