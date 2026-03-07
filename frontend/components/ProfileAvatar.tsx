"use client";

import { useEffect, useState } from "react";
import { getAccessToken } from "@/lib/auth";

function getApiBase(): string {
  const url = process.env.NEXT_PUBLIC_API_URL;
  if (!url) return "";
  return url.replace(/\/$/, "");
}

interface ProfileAvatarProps {
  avatarUrl: string | null | undefined;
  /** API path to fetch avatar from (e.g. /me/avatar or /recruiter/me/avatar). Default: /me/avatar */
  avatarEndpoint?: string;
  alt?: string;
  className?: string;
  size?: "sm" | "md" | "lg";
}

const sizeClasses = {
  sm: "h-12 w-12 text-lg",
  md: "h-20 w-20 text-2xl",
  lg: "h-28 w-28 text-4xl",
};

export function ProfileAvatar({
  avatarUrl,
  avatarEndpoint = "/me/avatar",
  alt = "Profile",
  className = "",
  size = "lg",
}: ProfileAvatarProps) {
  const [blobUrl, setBlobUrl] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (!avatarUrl || !getApiBase()) {
      setBlobUrl(null);
      return;
    }
    let cancelled = false;
    let createdUrl: string | null = null;
    setLoading(true);
    const token = getAccessToken();
    if (!token) {
      setLoading(false);
      return;
    }
    fetch(`${getApiBase()}${avatarEndpoint}`, {
      headers: { Authorization: `Bearer ${token}` },
    })
      .then((res) => {
        if (!res.ok || !cancelled) return null;
        return res.blob();
      })
      .then((blob) => {
        if (cancelled || !blob) return;
        createdUrl = URL.createObjectURL(blob);
        setBlobUrl(createdUrl);
      })
      .catch(() => {})
      .finally(() => {
        if (!cancelled) setLoading(false);
      });
    return () => {
      cancelled = true;
      if (createdUrl) URL.revokeObjectURL(createdUrl);
    };
  }, [avatarUrl, avatarEndpoint]);

  const sizeClass = sizeClasses[size];
  const initials = alt
    .split(/\s+/)
    .map((s) => s[0])
    .join("")
    .toUpperCase()
    .slice(0, 2);

  if (blobUrl) {
    return (
      <img
        src={blobUrl}
        alt={alt}
        className={`rounded-full object-cover ${sizeClass} ${className}`}
      />
    );
  }

  return (
    <div
      className={`flex items-center justify-center rounded-full bg-primary/20 text-primary font-semibold ${sizeClass} ${className}`}
      aria-hidden
    >
      {loading ? "" : initials || "?"}
    </div>
  );
}
