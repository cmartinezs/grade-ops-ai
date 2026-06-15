# firebase_app_hosting.tf — Enable Firebase App Hosting API
# The backend itself is created manually via Firebase CLI (requires GitHub OAuth flow)

resource "google_project_service" "firebase_app_hosting" {
  project            = var.project_id
  service            = "firebaseapphosting.googleapis.com"
  disable_on_destroy = false
}
