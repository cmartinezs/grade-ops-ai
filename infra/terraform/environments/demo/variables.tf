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

variable "api_cloud_run_sa_email" {
  description = "Email of the service account attached to the api/ Cloud Run service. Used to grant Secret Manager access."
  type        = string
}

variable "google_oauth_client_id" {
  description = "OAuth 2.0 client ID for the Google sign-in provider (from GCP Console → APIs & Services → Credentials)."
  type        = string
}

variable "google_oauth_client_secret" {
  description = "OAuth 2.0 client secret for the Google sign-in provider."
  type        = string
  sensitive   = true
}
