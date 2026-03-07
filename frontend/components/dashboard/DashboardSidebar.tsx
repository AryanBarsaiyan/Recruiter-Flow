"use client";

import { useState } from "react";
import { usePathname } from "next/navigation";
import { Link } from "@/components/link";
import { useAuth } from "@/contexts/AuthContext";
import {
  LayoutDashboard,
  Building2,
  Users,
  Palette,
  Briefcase,
  BarChart3,
  FileText,
  Upload,
  Webhook,
  Shield,
  ChevronRight,
  User,
} from "lucide-react";

const navItems = [
  { href: "/dashboard", label: "Dashboard", icon: LayoutDashboard },
  { href: "/dashboard/profile", label: "Profile", icon: User },
  {
    label: "Company",
    icon: Building2,
    children: [
      { href: "/company", label: "Overview" },
      { href: "/company/members", label: "Members", icon: Users },
      { href: "/company/branding", label: "Branding", icon: Palette },
    ],
  },
  { href: "/dashboard/jobs", label: "Jobs", icon: Briefcase },
  { href: "/dashboard/reports", label: "Reports", icon: BarChart3 },
  { href: "/dashboard/audit", label: "Audit", icon: FileText },
  { href: "/dashboard/bulk-import", label: "Bulk import", icon: Upload },
  { href: "/dashboard/webhooks", label: "Webhooks", icon: Webhook },
];

function NavLink({
  href,
  label,
  isActive,
  indent = false,
  icon: Icon,
}: {
  href: string;
  label: string;
  isActive: boolean;
  indent?: boolean;
  icon?: React.ComponentType<{ className?: string }>;
}) {
  return (
    <Link
      href={href}
      className={`flex items-center gap-2.5 rounded-lg px-3 py-2 text-sm font-medium transition-colors ${
        indent ? "pl-9" : ""
      } ${
        isActive
          ? "bg-primary/10 text-primary"
          : "text-muted-foreground hover:bg-muted hover:text-foreground"
      }`}
    >
      {Icon && <Icon className="h-4 w-4 shrink-0 opacity-80" />}
      {label}
    </Link>
  );
}

export function DashboardSidebar() {
  const pathname = usePathname();
  const { user } = useAuth();
  const [companyOpen, setCompanyOpen] = useState(
    pathname.startsWith("/company")
  );

  return (
    <aside className="flex w-56 flex-col border-r bg-card/30">
      <div className="flex h-14 items-center gap-2 border-b px-4">
        <Link href="/dashboard" className="font-semibold text-lg tracking-tight">
          Future Scope
        </Link>
      </div>
      <nav className="flex-1 overflow-y-auto p-3 space-y-0.5">
        {navItems.map((item) => {
          if ("children" in item) {
            const isOpen = companyOpen;
            const hasActive = item.children.some((c) =>
              pathname === c.href || (c.href !== "/company" && pathname.startsWith(c.href + "/"))
            );
            return (
              <div key={item.label} className="space-y-0.5">
                <button
                  type="button"
                  onClick={() => setCompanyOpen(!isOpen)}
                  className={`flex w-full items-center gap-2.5 rounded-lg px-3 py-2 text-sm font-medium transition-colors ${
                    hasActive
                      ? "bg-primary/10 text-primary"
                      : "text-muted-foreground hover:bg-muted hover:text-foreground"
                  }`}
                >
                  <item.icon className="h-4 w-4 shrink-0 opacity-80" />
                  <span className="flex-1 text-left">{item.label}</span>
                  <ChevronRight
                    className={`h-4 w-4 shrink-0 transition-transform ${
                      isOpen ? "rotate-90" : ""
                    }`}
                  />
                </button>
                {isOpen &&
                  item.children.map((child) => (
                    <NavLink
                      key={child.href}
                      href={child.href}
                      label={child.label}
                      isActive={
                        pathname === child.href ||
                        (child.href !== "/company" &&
                          pathname.startsWith(child.href + "/"))
                      }
                      indent
                      icon={child.icon}
                    />
                  ))}
              </div>
            );
          }
          return (
            <NavLink
              key={item.href}
              href={item.href}
              label={item.label}
              isActive={
                pathname === item.href ||
                (item.href !== "/dashboard" && pathname.startsWith(item.href + "/"))
              }
              icon={item.icon}
            />
          );
        })}
        {user?.userType === "platform_admin" && (
          <div className="pt-2 mt-2 border-t">
            <NavLink
              href="/dashboard/admin"
              label="Platform admin"
              isActive={pathname.startsWith("/dashboard/admin")}
              icon={Shield}
            />
          </div>
        )}
      </nav>
    </aside>
  );
}
