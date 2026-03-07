"use client";

import NextLink, { type LinkProps } from "next/link";

/**
 * Link component with prefetching disabled by default.
 * Use this instead of next/link to avoid prefetching linked pages.
 */
export function Link({ prefetch = false, ...props }: LinkProps) {
  return <NextLink prefetch={prefetch} {...props} />;
}
