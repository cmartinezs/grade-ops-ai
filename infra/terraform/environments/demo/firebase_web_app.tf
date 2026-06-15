# ---------------------------------------------------------------------------
# firebase_web_app.tf — Firebase web app registration
# Demo environment
# ---------------------------------------------------------------------------
# Registers a Firebase web app on the project so we can retrieve the public
# client configuration (apiKey, authDomain, appId, messagingSenderId) needed
# by the web/ Next.js application.
#
# These values are PUBLIC by design — Firebase web config is not a secret and
# is safe to embed in environment variables exposed to the browser bundle.
# ---------------------------------------------------------------------------

# Register the Firebase web app
resource "google_firebase_web_app" "web" {
  provider     = google-beta
  project      = var.project_id
  display_name = "GradeOps AI — Web (${var.environment})"

  depends_on = [
    google_project_service.firebase,
    google_identity_platform_config.default,
  ]
}

# Read back the web app configuration so we can expose it as Terraform outputs
data "google_firebase_web_app_config" "web" {
  provider   = google-beta
  project    = var.project_id
  web_app_id = google_firebase_web_app.web.app_id
}
