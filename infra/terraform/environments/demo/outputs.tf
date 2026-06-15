# ---------------------------------------------------------------------------
# outputs.tf — Terraform outputs for the demo environment
# ---------------------------------------------------------------------------

# ---------------------------------------------------------------------------
# Firebase / Identity Platform — core identifiers
# ---------------------------------------------------------------------------

output "firebase_project_id" {
  description = "GCP project ID — also the Firebase project ID."
  value       = var.project_id
}

output "firebase_auth_domain" {
  description = "Firebase Authentication domain used by the web client SDK (e.g. <project>.firebaseapp.com)."
  value       = data.google_firebase_web_app_config.web.auth_domain
}

output "firebase_api_key" {
  description = "Firebase web API key — public, safe to expose in frontend env vars."
  value       = data.google_firebase_web_app_config.web.api_key
  sensitive   = false
}

output "firebase_app_id" {
  description = "Firebase web app ID — used to initialize the client SDK."
  value       = google_firebase_web_app.web.app_id
}

output "firebase_messaging_sender_id" {
  description = "Firebase Cloud Messaging sender ID — required by the web SDK config object."
  value       = data.google_firebase_web_app_config.web.messaging_sender_id
}

# ---------------------------------------------------------------------------
# Firebase web config — prefixed as NEXT_PUBLIC_ for Next.js
# These match the environment variable names consumed by web/src/lib/firebase/client.ts
# ---------------------------------------------------------------------------

output "NEXT_PUBLIC_FIREBASE_API_KEY" {
  description = "Firebase API key for the web/ Next.js app (NEXT_PUBLIC_ prefix required for browser exposure)."
  value       = data.google_firebase_web_app_config.web.api_key
  sensitive   = false
}

output "NEXT_PUBLIC_FIREBASE_AUTH_DOMAIN" {
  description = "Firebase Authentication domain for the web/ Next.js app."
  value       = data.google_firebase_web_app_config.web.auth_domain
}

output "NEXT_PUBLIC_FIREBASE_PROJECT_ID" {
  description = "GCP/Firebase project ID for the web/ Next.js app."
  value       = var.project_id
}

output "NEXT_PUBLIC_FIREBASE_APP_ID" {
  description = "Firebase web app ID for the web/ Next.js app."
  value       = google_firebase_web_app.web.app_id
}

output "NEXT_PUBLIC_FIREBASE_MESSAGING_SENDER_ID" {
  description = "Firebase Cloud Messaging sender ID for the web/ Next.js app."
  value       = data.google_firebase_web_app_config.web.messaging_sender_id
}

# ---------------------------------------------------------------------------
# Firebase Admin — secret reference (not the secret value)
# ---------------------------------------------------------------------------

output "firebase_admin_credentials_secret_id" {
  description = "Secret Manager secret ID containing the Firebase Admin SDK service-account key. Mount this in the api/ Cloud Run service."
  value       = google_secret_manager_secret.firebase_admin_credentials.secret_id
}

output "firebase_admin_sa_email" {
  description = "Email of the Firebase Admin SDK service account."
  value       = google_service_account.firebase_admin.email
}

# ---------------------------------------------------------------------------
# Cloud Run — api service
# ---------------------------------------------------------------------------

output "api_url" {
  description = "Cloud Run URL for the api service. Use as CORS_ALLOWED_ORIGINS source and as API_BASE_URL in Firebase App Hosting."
  value       = google_cloud_run_v2_service.api.uri
}

# ---------------------------------------------------------------------------
# Workload Identity Federation — GitHub Actions
# ---------------------------------------------------------------------------

output "wif_provider" {
  description = "Full WIF provider resource name. Set as WIF_PROVIDER GitHub Secret."
  value       = google_iam_workload_identity_pool_provider.github.name
}

output "github_actions_sa_email" {
  description = "Email of the GitHub Actions service account. Set as WIF_SERVICE_ACCOUNT GitHub Secret."
  value       = google_service_account.github_actions.email
}

output "artifact_registry_host" {
  description = "Artifact Registry hostname for Docker push/pull."
  value       = "${var.region}-docker.pkg.dev"
}
