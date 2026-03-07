/** User info returned with login, signup, refresh (avoids extra GET /me for dashboard). */
export interface AuthUserDto {
  id: string;
  email: string;
  userType: string;
  fullName?: string | null;
  defaultCompanyId?: string | null;
}

/** Auth response from POST /auth/login, /auth/signup-super-admin, /auth/refresh, /auth/accept-invite */
export interface AuthResponse {
  accessToken: string;
  tokenType?: string;
  refreshToken: string;
  user?: AuthUserDto;
}

export interface RecruiterDashboard {
  companyId: string;
  totalJobs: number;
  totalApplications: number;
}

export interface Company {
  id: string;
  name: string;
  slug?: string;
  brandingConfigJson?: string;
}

export interface CompanyMember {
  id?: string;
  userId?: string;
  /** From API: userEmail */
  email?: string;
  userEmail?: string;
  fullName?: string;
  roleName?: string;
  avatarUrl?: string | null;
}

export interface Pipeline {
  id: string;
  companyId: string;
  name?: string;
  isDefault?: boolean;
}

export interface PipelineStage {
  id: string;
  pipelineId: string;
  name?: string;
  type?: string;
  orderIndex?: number;
}

export interface Job {
  id: string;
  companyId: string;
  title: string;
  description?: string;
  location?: string;
  employmentType?: string;
  published?: boolean;
  applicationDeadline?: string;
  maxApplications?: number;
  pipelineId?: string;
  pipelineName?: string;
  createdAt?: string;
  customFormSchemaJson?: string | null;
  companyName?: string;
  brandingConfigJson?: string | null;
}

/** Company branding config (from job.brandingConfigJson or company.brandingConfigJson). */
export interface BrandingConfig {
  logoUrl?: string;
  primaryColor?: string;
  secondaryColor?: string;
  accentColor?: string;
}

/** Single custom form field schema (from job.customFormSchemaJson array item). */
export interface CustomFormFieldSchema {
  key: string;
  label?: string;
  type?: "text" | "textarea" | "number" | "email" | "url";
  required?: boolean;
}

/** Application (for list by job or candidate). */
export interface Application {
  id: string;
  jobId: string;
  jobTitle?: string;
  companyName?: string;
  candidateId: string;
  candidateName?: string | null;
  candidateEmail?: string | null;
  resumeId?: string | null;
  resumeOriginalFilename?: string | null;
  status: string;
  appliedAt: string;
}

/** Response from POST /jobs/:id/upload-resume (resume analyze + storage). */
export interface UploadResumeResponse {
  storagePath: string;
  originalFilename: string;
  extractedFullName?: string | null;
  extractedEmail?: string | null;
  extractedPhone?: string | null;
}

export interface SpringPage<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}
