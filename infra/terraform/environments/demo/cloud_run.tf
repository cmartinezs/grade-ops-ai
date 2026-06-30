# cloud_run.tf — Cloud Run v2 services for api/ and agents/

resource "google_project_service" "cloud_run" {
  project            = var.project_id
  service            = "run.googleapis.com"
  disable_on_destroy = false
}

# INTERNAL_API_SECRET — created here; version added manually after terraform apply
resource "google_secret_manager_secret" "internal_api_secret" {
  project   = var.project_id
  secret_id = "INTERNAL_API_SECRET"

  replication {
    auto {}
  }

  depends_on = [google_project_service.secretmanager]
}

# ---------------------------------------------------------------------------
# api/ Cloud Run service
# ---------------------------------------------------------------------------
resource "google_cloud_run_v2_service" "api" {
  project  = var.project_id
  name     = "grade-ops-ai-api"
  location = var.region
  ingress  = "INGRESS_TRAFFIC_ALL"

  template {
    service_account = google_service_account.api.email

    annotations = {
      "run.googleapis.com/cloudsql-instances" = "${var.project_id}:${var.region}:gradeops-demo"
    }

    containers {
      image = "us-docker.pkg.dev/cloudrun/container/hello"

      env {
        name  = "SPRING_PROFILES_ACTIVE"
        value = "demo"
      }

      env {
        name  = "SPRING_DATASOURCE_URL"
        value = "jdbc:postgresql:///gradeops?cloudSqlInstance=${var.project_id}:${var.region}:gradeops-demo&socketFactory=com.google.cloud.sql.postgres.SocketFactory"
      }

      env {
        name  = "SPRING_DATASOURCE_USERNAME"
        value = "gradeops"
      }

      env {
        name = "SPRING_DATASOURCE_PASSWORD"
        value_source {
          secret_key_ref {
            secret  = google_secret_manager_secret.db_password.secret_id
            version = "latest"
          }
        }
      }

      env {
        name = "FIREBASE_ADMIN_CREDENTIALS"
        value_source {
          secret_key_ref {
            secret  = google_secret_manager_secret.firebase_admin_credentials.secret_id
            version = "latest"
          }
        }
      }

      env {
        name = "INTERNAL_API_SECRET"
        value_source {
          secret_key_ref {
            secret  = google_secret_manager_secret.internal_api_secret.secret_id
            version = "latest"
          }
        }
      }

      env {
        name = "SMTP_HOST"
        value_source {
          secret_key_ref {
            secret  = google_secret_manager_secret.smtp_host.secret_id
            version = "latest"
          }
        }
      }

      env {
        name = "SMTP_PORT"
        value_source {
          secret_key_ref {
            secret  = google_secret_manager_secret.smtp_port.secret_id
            version = "latest"
          }
        }
      }

      env {
        name = "SMTP_USERNAME"
        value_source {
          secret_key_ref {
            secret  = google_secret_manager_secret.smtp_username.secret_id
            version = "latest"
          }
        }
      }

      env {
        name = "SMTP_PASSWORD"
        value_source {
          secret_key_ref {
            secret  = google_secret_manager_secret.smtp_password.secret_id
            version = "latest"
          }
        }
      }

      env {
        name = "GRADEOPS_MAIL_FROM"
        value_source {
          secret_key_ref {
            secret  = google_secret_manager_secret.mail_from.secret_id
            version = "latest"
          }
        }
      }

      env {
        name  = "GRADEOPS_WEB_BASE_URL"
        value = "https://placeholder.hosted.app"
      }

      env {
        name  = "CORS_ALLOWED_ORIGINS"
        value = "https://placeholder.hosted.app"
      }

      resources {
        limits = {
          memory = "512Mi"
          cpu    = "1"
        }
      }
    }
  }

  lifecycle {
    ignore_changes = [template]
  }

  depends_on = [google_project_service.cloud_run]
}

resource "google_cloud_run_v2_service_iam_member" "api_public" {
  project  = var.project_id
  location = var.region
  name     = google_cloud_run_v2_service.api.name
  role     = "roles/run.invoker"
  member   = "allUsers"
}

# ---------------------------------------------------------------------------
# agents/ Cloud Run service
# ---------------------------------------------------------------------------
resource "google_cloud_run_v2_service" "agents" {
  project  = var.project_id
  name     = "grade-ops-ai-agents"
  location = var.region
  ingress  = "INGRESS_TRAFFIC_INTERNAL_ONLY"

  template {
    service_account = google_service_account.agents.email

    containers {
      image = "us-docker.pkg.dev/cloudrun/container/hello"

      env {
        name  = "SPRING_PROFILES_ACTIVE"
        value = "demo"
      }

      resources {
        limits = {
          memory = "512Mi"
          cpu    = "1"
        }
      }
    }
  }

  lifecycle {
    ignore_changes = [template]
  }

  depends_on = [google_project_service.cloud_run]
}

resource "google_cloud_run_v2_service_iam_member" "agents_api_invoker" {
  project  = var.project_id
  location = var.region
  name     = google_cloud_run_v2_service.agents.name
  role     = "roles/run.invoker"
  member   = "serviceAccount:${google_service_account.api.email}"
}
