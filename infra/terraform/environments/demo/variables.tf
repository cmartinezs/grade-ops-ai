variable "project_id" {
  description = "The GCP project ID for the demo environment."
  type        = string
}

variable "region" {
  description = "The GCP region used for all regional resources."
  type        = string
  default     = "us-central1"
}

variable "environment" {
  description = "Environment name (e.g. demo, staging, prod)."
  type        = string
  default     = "demo"
}

variable "github_repo" {
  description = "GitHub repository in owner/repo format (e.g. cmartinezs/grade-ops-ai)."
  type        = string
}

variable "db_password" {
  description = "PostgreSQL password for the gradeops database user."
  type        = string
  sensitive   = true
}

variable "google_oauth_client_id" {
  description = "OAuth 2.0 client ID for the Google sign-in provider."
  type        = string
}

variable "google_oauth_client_secret" {
  description = "OAuth 2.0 client secret for the Google sign-in provider."
  type        = string
  sensitive   = true
}
