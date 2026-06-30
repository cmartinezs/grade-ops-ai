# smtp.tf — SMTP secrets for password reset email service
# Demo environment
#
# These secrets hold the SMTP credentials used by the api/ Spring Boot service
# to send password reset emails via JavaMail + Thymeleaf.
#
# Secret versions must be populated manually after `terraform apply`:
#   gcloud secrets versions add SMTP_HOST --data-file=- <<< "sandbox.smtp.mailtrap.io"
#   gcloud secrets versions add SMTP_PORT --data-file=- <<< "587"
#   gcloud secrets versions add SMTP_USERNAME --data-file=- <<< "<your-smtp-username>"
#   gcloud secrets versions add SMTP_PASSWORD --data-file=- <<< "<your-smtp-password>"
#   gcloud secrets versions add GRADEOPS_MAIL_FROM --data-file=- <<< "noreply@gradeops.app"

resource "google_secret_manager_secret" "smtp_host" {
  project   = var.project_id
  secret_id = "SMTP_HOST"

  replication {
    auto {}
  }

  depends_on = [google_project_service.secretmanager]
}

resource "google_secret_manager_secret" "smtp_port" {
  project   = var.project_id
  secret_id = "SMTP_PORT"

  replication {
    auto {}
  }

  depends_on = [google_project_service.secretmanager]
}

resource "google_secret_manager_secret" "smtp_username" {
  project   = var.project_id
  secret_id = "SMTP_USERNAME"

  replication {
    auto {}
  }

  depends_on = [google_project_service.secretmanager]
}

resource "google_secret_manager_secret" "smtp_password" {
  project   = var.project_id
  secret_id = "SMTP_PASSWORD"

  replication {
    auto {}
  }

  depends_on = [google_project_service.secretmanager]
}

resource "google_secret_manager_secret" "mail_from" {
  project   = var.project_id
  secret_id = "GRADEOPS_MAIL_FROM"

  replication {
    auto {}
  }

  depends_on = [google_project_service.secretmanager]
}
