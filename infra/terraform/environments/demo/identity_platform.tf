# ---------------------------------------------------------------------------
# identity_platform.tf — Google Identity Platform (Firebase Authentication)
# Demo environment
# ---------------------------------------------------------------------------
# Enables the Identity Toolkit and Firebase APIs, configures Identity Platform
# with the email/password sign-in provider, and sets the allowed authorized
# domains for the demo project.
# ---------------------------------------------------------------------------

# Enable the Identity Toolkit (Firebase Authentication) API
resource "google_project_service" "identitytoolkit" {
  project            = var.project_id
  service            = "identitytoolkit.googleapis.com"
  disable_on_destroy = false
}

# Enable the Firebase API (required by google_identity_platform_config)
resource "google_project_service" "firebase" {
  project            = var.project_id
  service            = "firebase.googleapis.com"
  disable_on_destroy = false
}

# Enable the Firebase Rules API (companion service)
resource "google_project_service" "firebaserules" {
  project            = var.project_id
  service            = "firebaserules.googleapis.com"
  disable_on_destroy = false
}

# ---------------------------------------------------------------------------
# Identity Platform configuration
# ---------------------------------------------------------------------------
# Note: google_identity_platform_config uses the google-beta provider and
# requires the Identity Toolkit API to be enabled first.
resource "google_identity_platform_config" "default" {
  provider = google-beta
  project  = var.project_id

  # Sign-in configuration — enable email/password
  sign_in {
    allow_duplicate_emails = false

    email {
      enabled           = true
      password_required = true
    }
  }

  # Authorized domains: Firebase Auth allows sign-in callbacks from these origins.
  # The default Firebase hosting domain is always included; add your Cloud Run
  # web service domain once it is provisioned.
  authorized_domains = [
    "localhost",
    "${var.project_id}.firebaseapp.com",
    "${var.project_id}.web.app",
  ]

  depends_on = [
    google_project_service.identitytoolkit,
    google_project_service.firebase,
  ]
}

# Note: email/password sign-in is configured directly in the sign_in block of
# google_identity_platform_config above. google_identity_platform_default_supported_idp_config
# is only needed for OAuth providers (Google, Facebook, etc.).

# ---------------------------------------------------------------------------
# Google OAuth sign-in provider (US-011)
# ---------------------------------------------------------------------------
# Registers the Google identity provider with Firebase Authentication.
# client_id and client_secret come from GCP Console → APIs & Services →
# Credentials → OAuth 2.0 Client IDs. Pass them as TF_VAR_ env vars or
# a .tfvars file — never commit real values.
resource "google_identity_platform_default_supported_idp_config" "google" {
  provider  = google-beta
  project   = var.project_id
  idp_id    = "google.com"
  client_id = var.google_oauth_client_id

  client_secret = var.google_oauth_client_secret
  enabled       = true

  depends_on = [google_identity_platform_config.default]
}
