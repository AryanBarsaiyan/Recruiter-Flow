"use client";

import { useEffect, useState } from "react";
import { useAuth } from "@/contexts/AuthContext";
import { api, ApiError } from "@/lib/api";
import type { CompanyMember } from "@/lib/types";
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

const ROLES_CAN_INVITE = ["SuperAdmin", "Admin"];
const ROLES_CAN_CHANGE_ROLE = ["SuperAdmin", "Admin"];
const ROLE_OPTIONS = ["SuperAdmin", "Admin", "ReadOnly", "View"];

type InviteResult = { inviteToken: string; expiresAt: string };

export default function CompanyMembersPage() {
  const { companyId, user } = useAuth();
  const [members, setMembers] = useState<CompanyMember[]>([]);
  const [loading, setLoading] = useState(true);
  const [inviteEmail, setInviteEmail] = useState("");
  const [inviteRole, setInviteRole] = useState("Admin");
  const [inviteLoading, setInviteLoading] = useState(false);
  const [inviteError, setInviteError] = useState("");
  const [inviteResult, setInviteResult] = useState<InviteResult | null>(null);
  const [roleUpdating, setRoleUpdating] = useState<string | null>(null);

  const currentMember = members.find(
    (m) => m.userId === user?.id || m.userId === user?.sub
  );
  const canInvite = currentMember && ROLES_CAN_INVITE.includes(currentMember.roleName ?? "");
  const canChangeRole = currentMember && ROLES_CAN_CHANGE_ROLE.includes(currentMember.roleName ?? "");

  useEffect(() => {
    if (!companyId) {
      setLoading(false);
      return;
    }
    let cancelled = false;
    api<CompanyMember[]>(`/companies/${companyId}/members`)
      .then((data) => {
        if (!cancelled) setMembers(Array.isArray(data) ? data : []);
      })
      .catch(() => {
        if (!cancelled) setMembers([]);
      })
      .finally(() => {
        if (!cancelled) setLoading(false);
      });
    return () => {
      cancelled = true;
    };
  }, [companyId]);

  async function handleInvite(e: React.FormEvent) {
    e.preventDefault();
    if (!companyId) return;
    setInviteError("");
    setInviteResult(null);
    setInviteLoading(true);
    try {
      const res = await api<InviteResult>("/auth/invite", {
        method: "POST",
        body: JSON.stringify({
          companyId,
          email: inviteEmail.trim(),
          roleName: inviteRole,
        }),
      });
      setInviteResult({ inviteToken: res.inviteToken, expiresAt: res.expiresAt });
      setInviteEmail("");
      const refreshed = await api<CompanyMember[]>(`/companies/${companyId}/members`);
      setMembers(Array.isArray(refreshed) ? refreshed : []);
    } catch (err) {
      if (err instanceof ApiError) setInviteError(err.message || "Invite failed");
      else setInviteError("Something went wrong");
    } finally {
      setInviteLoading(false);
    }
  }

  async function handleRoleChange(memberId: string, newRole: string) {
    if (!companyId) return;
    setRoleUpdating(memberId);
    try {
      const updated = await api<CompanyMember>(`/companies/${companyId}/members/${memberId}/role`, {
        method: "PATCH",
        body: JSON.stringify({ roleName: newRole }),
      });
      setMembers((prev) =>
        prev.map((m) => (m.id === memberId ? { ...m, roleName: updated.roleName } : m))
      );
    } catch (err) {
      if (err instanceof ApiError) {
        // Could show toast
      }
    } finally {
      setRoleUpdating(null);
    }
  }

  function copyInviteLink() {
    if (!inviteResult || typeof window === "undefined") return;
    const url = `${window.location.origin}/accept-invite?token=${encodeURIComponent(inviteResult.inviteToken)}`;
    void navigator.clipboard.writeText(url);
  }

  if (!companyId) {
    return (
      <div className="space-y-4">
        <p className="text-muted-foreground">Set your company on the dashboard first.</p>
      </div>
    );
  }

  if (loading) return <div className="text-muted-foreground">Loading…</div>;

  return (
    <div className="space-y-6">
      <h1 className="text-2xl font-semibold">Members</h1>
      <Card>
        <CardHeader>
          <CardTitle>Team members</CardTitle>
          <CardDescription>People with access to your company</CardDescription>
        </CardHeader>
        <CardContent>
          {members.length === 0 ? (
            <p className="text-muted-foreground">No members listed.</p>
          ) : (
            <div className="divide-y">
              <div className="py-2 flex items-center gap-4 text-sm font-medium text-muted-foreground">
                <div className="w-12 shrink-0" />
                <span className="flex-1 min-w-0">Name</span>
                <span className="flex-1 min-w-0">Email</span>
                <div className="shrink-0 w-[130px]">Role</div>
              </div>
              {members.map((m, i) => (
                <li key={m.id ?? i} className="py-3 flex items-center gap-4">
                  <ProfileAvatar
                    avatarUrl={m.avatarUrl}
                    avatarEndpoint={m.avatarUrl ?? undefined}
                    alt={m.fullName ?? m.userEmail ?? m.email ?? "Member"}
                    size="sm"
                  />
                  <span className="flex-1 min-w-0 font-medium truncate">
                    {m.fullName ?? "Member"}
                  </span>
                  <span className="flex-1 min-w-0 text-sm text-muted-foreground truncate">
                    {m.userEmail ?? m.email ?? "—"}
                  </span>
                  <div className="shrink-0 w-[130px]">
                    {canChangeRole &&
                    m.userId !== user?.id &&
                    m.userId !== user?.sub ? (
                      <select
                        value={m.roleName ?? ""}
                        onChange={(e) => m.id && handleRoleChange(m.id, e.target.value)}
                        disabled={roleUpdating === m.id}
                        className="flex h-9 w-full rounded-md border border-input bg-transparent px-3 py-1 text-sm shadow-sm"
                      >
                        {ROLE_OPTIONS.map((r) => (
                          <option key={r} value={r}>
                            {r}
                          </option>
                        ))}
                      </select>
                    ) : (
                      <span className="text-sm text-muted-foreground">
                        {m.roleName ?? "—"}
                      </span>
                    )}
                  </div>
                </li>
              ))}
            </div>
          )}
        </CardContent>
      </Card>
      {canInvite && (
        <Card>
          <CardHeader>
            <CardTitle>Invite member</CardTitle>
            <CardDescription>
              Send an invite link; they set a password at accept-invite
            </CardDescription>
          </CardHeader>
          <CardContent className="space-y-4">
            {inviteResult && (
              <div className="rounded-md border bg-muted/50 p-3 space-y-2">
                <p className="text-sm font-medium">
                  Invite link (expires{" "}
                  {new Date(inviteResult.expiresAt).toLocaleString()})
                </p>
                <Button type="button" variant="outline" size="sm" onClick={copyInviteLink}>
                  Copy link
                </Button>
              </div>
            )}
            <form onSubmit={handleInvite} className="space-y-4">
              {inviteError && (
                <p className="text-sm text-destructive">{inviteError}</p>
              )}
              <div className="space-y-2">
                <Label htmlFor="invite-email">Email</Label>
                <Input
                  id="invite-email"
                  type="email"
                  value={inviteEmail}
                  onChange={(e) => setInviteEmail(e.target.value)}
                  placeholder="colleague@company.com"
                  required
                  disabled={inviteLoading}
                />
              </div>
              <div className="space-y-2">
                <Label htmlFor="invite-role">Role</Label>
                <select
                  id="invite-role"
                  className="flex h-9 w-full rounded-md border border-input bg-transparent px-3 py-1 text-sm shadow-sm"
                  value={inviteRole}
                  onChange={(e) => setInviteRole(e.target.value)}
                  disabled={inviteLoading}
                >
                  <option value="Admin">Admin</option>
                  <option value="ReadOnly">ReadOnly</option>
                  <option value="View">View</option>
                </select>
              </div>
              <Button type="submit" disabled={inviteLoading}>
                {inviteLoading ? "Sending…" : "Send invite"}
              </Button>
            </form>
          </CardContent>
        </Card>
      )}
      {!canInvite && currentMember && (
        <p className="text-sm text-muted-foreground">
          You have a {currentMember.roleName} role. Only Admin and SuperAdmin can invite new members.
        </p>
      )}
    </div>
  );
}
