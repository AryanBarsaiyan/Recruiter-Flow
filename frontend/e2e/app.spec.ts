import { test, expect } from "@playwright/test";

test("home page loads", async ({ page }) => {
  await page.goto("/");
  await expect(page.locator("h1")).toContainText("Recruitment");
});

test("login page loads and form is submittable", async ({ page }) => {
  await page.goto("/login");
  await expect(page.getByLabel("Email")).toBeVisible();
  await page.getByLabel("Email").fill("test@example.com");
  await page.getByLabel("Password").fill("password");
  await page.getByRole("button", { name: /sign in/i }).click();
  await expect(page).toHaveURL(/\/(login|dashboard|me)/);
});

test("signup form submits; when backend is up, redirects to dashboard with company from user.defaultCompanyId", async ({
  page,
}) => {
  const unique = Date.now().toString(36);
  const email = `e2e-${unique}@example.com`;
  await page.goto("/signup");
  await expect(page.getByLabel("Email")).toBeVisible();
  await page.getByLabel(/full name/i).fill("E2E User");
  await page.getByLabel(/company name/i).fill("E2E Company " + unique);
  await page.getByLabel("Email").fill(email);
  await page.getByLabel("Password").fill("password123");
  await page.getByRole("button", { name: /create account/i }).click();
  await expect(page).toHaveURL(/\/(signup|dashboard)/, { timeout: 15000 });
  if (page.url().includes("/dashboard")) {
    await expect(page.getByText("Dashboard")).toBeVisible({ timeout: 3000 });
    await expect(page.getByText("Company not loaded")).not.toBeVisible();
    await expect(
      page.getByText("E2E User").or(page.getByText(email, { exact: true }))
    ).toBeVisible();
  }
});

test("apply page shows upload step when job exists", async ({ page }) => {
  await page.goto("/jobs");
  await page.waitForLoadState("networkidle");
  const applyLink = page.getByRole("link", { name: "Apply" }).first();
  const visible = await applyLink.isVisible().catch(() => false);
  if (!visible) return;
  const href = await applyLink.getAttribute("href");
  if (!href || !href.includes("/jobs/") || href.endsWith("/apply")) return;
  const jobId = href.replace(/.*\/jobs\/([^/]+).*/, "$1");
  if (!jobId) return;
  await page.goto(`/jobs/${jobId}/apply`);
  await expect(page.getByText(/apply to|upload|resume/i)).toBeVisible({ timeout: 5000 });
  await expect(page.getByRole("button", { name: /upload|continue/i })).toBeVisible();
});

test("invite page loads", async ({ page }) => {
  await page.goto("/invite/invalid-token-12345");
  await expect(page).toHaveURL(/\/invite\/invalid-token-12345/);
  await expect(
    page.getByText(/Loading|Invalid or expired|Interview invitation|Book your interview/i)
  ).toBeVisible({ timeout: 10000 });
});

test("request-password-reset page loads and form is submittable", async ({ page }) => {
  await page.goto("/request-password-reset");
  await expect(page).toHaveURL(/\/request-password-reset/);
  await expect(page.getByText("Reset password")).toBeVisible();
  await expect(page.getByLabel("Email")).toBeVisible();
  await page.getByLabel("Email").fill("test@example.com");
  await page.getByRole("button", { name: /send reset link/i }).click();
  await expect(page.getByText(/check your email|request failed|something went wrong/i)).toBeVisible({ timeout: 5000 });
});

test("reset-password page loads with token param", async ({ page }) => {
  await page.goto("/reset-password?token=test-token-123");
  await expect(page).toHaveURL(/\/reset-password/);
  await expect(page.getByText("Set new password")).toBeVisible();
  await expect(page.getByLabel("New password")).toBeVisible();
});

test("verify-email page loads", async ({ page }) => {
  await page.goto("/verify-email");
  await expect(page).toHaveURL(/\/verify-email/);
  await expect(page.getByText("Verify email")).toBeVisible();
});

test("recruiter can open company page and see invite section", async ({ page }) => {
  const unique = Date.now().toString(36);
  const email = `e2e-company-${unique}@example.com`;
  await page.goto("/signup");
  await page.getByLabel(/full name/i).fill("E2E Company User");
  await page.getByLabel(/company name/i).fill("E2E Company " + unique);
  await page.getByLabel("Email").fill(email);
  await page.getByLabel("Password").fill("password123");
  await page.getByRole("button", { name: /create account/i }).click();
  await expect(page).toHaveURL(/\/dashboard/, { timeout: 15000 });
  await page.goto("/company/members");
  await expect(page).toHaveURL(/\/company\/members/);
  await expect(page.getByText("Invite member")).toBeVisible();
  await expect(page.getByRole("button", { name: /send invite/i })).toBeVisible();
  await expect(page.getByRole("link", { name: "Branding" })).toBeVisible();
});

test("recruiter can open audit logs page", async ({ page }) => {
  const unique = Date.now().toString(36);
  const email = `e2e-audit-${unique}@example.com`;
  await page.goto("/signup");
  await page.getByLabel(/full name/i).fill("E2E Audit User");
  await page.getByLabel(/company name/i).fill("E2E Audit Co " + unique);
  await page.getByLabel("Email").fill(email);
  await page.getByLabel("Password").fill("password123");
  await page.getByRole("button", { name: /create account/i }).click();
  await expect(page).toHaveURL(/\/dashboard/, { timeout: 15000 });
  await page.goto("/dashboard/audit");
  await expect(page).toHaveURL(/\/dashboard\/audit/);
  await expect(page.getByRole("heading", { name: "Audit logs" })).toBeVisible({ timeout: 10000 });
});

test("recruiter can open bulk import page", async ({ page }) => {
  const unique = Date.now().toString(36);
  const email = `e2e-bulk-${unique}@example.com`;
  await page.goto("/signup");
  await page.getByLabel(/full name/i).fill("E2E Bulk User");
  await page.getByLabel(/company name/i).fill("E2E Bulk Co " + unique);
  await page.getByLabel("Email").fill(email);
  await page.getByLabel("Password").fill("password123");
  await page.getByRole("button", { name: /create account/i }).click();
  await expect(page).toHaveURL(/\/dashboard/, { timeout: 15000 });
  await page.goto("/dashboard/bulk-import");
  await expect(page).toHaveURL(/\/dashboard\/bulk-import/);
  await expect(
    page.getByRole("heading", { name: "Bulk import" }).or(
      page.getByText("Import batches")
    )
  ).toBeVisible({ timeout: 15000 });
});

test("recruiter can open webhooks page", async ({ page }) => {
  const unique = Date.now().toString(36);
  const email = `e2e-web-${unique}@example.com`;
  await page.goto("/signup");
  await page.getByLabel(/full name/i).fill("E2E Webhook User");
  await page.getByLabel(/company name/i).fill("E2E Web Co " + unique);
  await page.getByLabel("Email").fill(email);
  await page.getByLabel("Password").fill("password123");
  await page.getByRole("button", { name: /create account/i }).click();
  await expect(page).toHaveURL(/\/dashboard/, { timeout: 15000 });
  await page.goto("/dashboard/webhooks");
  await expect(page).toHaveURL(/\/dashboard\/webhooks/);
  await expect(page.getByRole("heading", { name: "Webhooks" })).toBeVisible({ timeout: 10000 });
});

test("recruiter opening platform admin sees access required when not platform_admin", async ({ page }) => {
  const unique = Date.now().toString(36);
  const email = `e2e-admin-${unique}@example.com`;
  await page.goto("/signup");
  await page.getByLabel(/full name/i).fill("E2E Recruiter");
  await page.getByLabel(/company name/i).fill("E2E Admin Co " + unique);
  await page.getByLabel("Email").fill(email);
  await page.getByLabel("Password").fill("password123");
  await page.getByRole("button", { name: /create account/i }).click();
  await expect(page).toHaveURL(/\/dashboard/, { timeout: 15000 });
  await page.goto("/dashboard/admin");
  await expect(page).toHaveURL(/\/dashboard\/admin/);
  await expect(
    page.getByRole("link", { name: /go to dashboard/i }).or(
      page.getByRole("button", { name: /go to dashboard/i })
    )
  ).toBeVisible({ timeout: 10000 });
});

test("recruiter welcome name and job detail with applications section", async ({ page }) => {
  const unique = Date.now().toString(36);
  const email = `e2e-rec-${unique}@example.com`;
  await page.goto("/signup");
  await page.getByLabel(/full name/i).fill("E2E Recruiter");
  await page.getByLabel(/company name/i).fill("E2E Co " + unique);
  await page.getByLabel("Email").fill(email);
  await page.getByLabel("Password").fill("password123");
  await page.getByRole("button", { name: /create account/i }).click();
  await expect(page).toHaveURL(/\/dashboard/, { timeout: 15000 });
  await expect(
    page.getByText("E2E Recruiter").or(page.getByText(email, { exact: true }))
  ).toBeVisible({ timeout: 5000 });
  await page.goto("/dashboard/jobs/new");
  await expect(page.getByLabel("Title", { exact: true })).toBeVisible({ timeout: 15000 });
  await page.getByLabel("Title", { exact: true }).fill("E2E Test Job");
  await page.getByRole("button", { name: "Create job" }).click();
  await expect(page).toHaveURL(/\/dashboard\/jobs\/[^/]+/);
  await expect(page.getByText("E2E Test Job")).toBeVisible();
  await expect(page.getByText(/public apply link|applications/i)).toBeVisible();
});

test("recruiter can open reports page", async ({ page }) => {
  const unique = Date.now().toString(36);
  const email = `e2e-reports-${unique}@example.com`;
  await page.goto("/signup");
  await page.getByLabel(/full name/i).fill("E2E Reports User");
  await page.getByLabel(/company name/i).fill("E2E Reports Co " + unique);
  await page.getByLabel("Email").fill(email);
  await page.getByLabel("Password").fill("password123");
  await page.getByRole("button", { name: /create account/i }).click();
  await expect(page).toHaveURL(/\/dashboard/, { timeout: 15000 });
  await page.goto("/dashboard/reports");
  await expect(page).toHaveURL(/\/dashboard\/reports/);
  await expect(page.getByRole("heading", { name: "Reports" })).toBeVisible({ timeout: 10000 });
});

test("unauthenticated user visiting /me redirects to login", async ({ page }) => {
  await page.goto("/me");
  await expect(page).toHaveURL(/\/login/, { timeout: 10000 });
});

test("candidate can view and edit profile when logged in", async ({ page }) => {
  const candidateEmail = process.env.E2E_TEST_CANDIDATE_EMAIL ?? "test-candidate@example.com";
  const candidatePassword = process.env.E2E_TEST_CANDIDATE_PASSWORD ?? "password123";
  await page.goto("/login");
  await page.getByLabel("Email").fill(candidateEmail);
  await page.getByLabel("Password").fill(candidatePassword);
  await page.getByRole("button", { name: /sign in/i }).click();
  await expect(page).toHaveURL(/\/me/, { timeout: 10000 });
  await expect(page.getByRole("heading", { name: "My profile" })).toBeVisible({ timeout: 5000 });
  await expect(page.getByText(/email cannot be changed/i)).toBeVisible();
  await page.getByRole("button", { name: /edit profile/i }).click();
  await expect(page.getByLabel("Full name")).toBeVisible();
  await page.getByLabel("Full name").fill("Updated E2E Name");
  await page.getByRole("button", { name: "Save" }).click();
  await expect(page.getByText("Updated E2E Name")).toBeVisible({ timeout: 5000 });
});
