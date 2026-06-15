# ---------------------------------------------------------------------------
# firebase_admin_iam.tf — Firebase Admin SDK service account and credentials
# Demo environment
# ---------------------------------------------------------------------------
# Creates a dedicated service account for the Firebase Admin SDK used by the
# api/ Spring Boot service, stores the generated key in Secret Manager, and
# grants the api/ Cloud Run service account access to retrieve it at runtime.
#
# Security note: For MVP, a service-account key stored in Secret Manager is
# acceptable. When time allows, migrate to Workload Identity Federation so no
# long-lived key file is necessary.
# ---------------------------------------------------------------------------

# Enable Secret Manager API (idempotent — safe to include here)
resource "google_project_service" "secretmanager" {
  project            = var.project_id
  service            = "secretmanager.googleapis.com"
  disable_on_destroy = false
}

# ---------------------------------------------------------------------------
# Service account for Firebase Admin SDK
# ---------------------------------------------------------------------------
resource "google_service_account" "firebase_admin" {
  project      = var.project_id
  account_id   = "firebase-admin-sa"
  display_name = "Firebase Admin SDK — api/ service"
  description  = "Service account used by the api/ Cloud Run service to call Firebase Admin SDK (token verification, user management)."
}

# Grant firebaseauth.admin role so the SA can verify ID tokens and manage users
resource "google_project_iam_member" "firebase_admin_role" {
  project = var.project_id
  role    = "roles/firebaseauth.admin"
  member  = "serviceAccount:${google_service_account.firebase_admin.email}"
}

# ---------------------------------------------------------------------------
# Service-account key — stored in Secret Manager, never committed to the repo
# ---------------------------------------------------------------------------
resource "google_service_account_key" "firebase_admin" {
  service_account_id = google_service_account.firebase_admin.name
}

# ---------------------------------------------------------------------------
# Secret Manager secret — FIREBASE_ADMIN_CREDENTIALS
# ---------------------------------------------------------------------------
resource "google_secret_manager_secret" "firebase_admin_credentials" {
  project   = var.project_id
  secret_id = "FIREBASE_ADMIN_CREDENTIALS"

  replication {
    auto {}
  }

  depends_on = [google_project_service.secretmanager]
}

# Store the key JSON as the first version of the secret
resource "google_secret_manager_secret_version" "firebase_admin_credentials" {
  secret      = google_secret_manager_secret.firebase_admin_credentials.id
  secret_data = base64decode(google_service_account_key.firebase_admin.private_key)
}

# ---------------------------------------------------------------------------
# IAM — grant the api/ Cloud Run service account access to the secret
# ---------------------------------------------------------------------------
resource "google_secret_manager_secret_iam_member" "api_sa_secret_accessor" {
  project   = var.project_id
  secret_id = google_secret_manager_secret.firebase_admin_credentials.secret_id
  role      = "roles/secretmanager.secretAccessor"
  member    = "serviceAccount:${google_service_account.api.email}"
}
