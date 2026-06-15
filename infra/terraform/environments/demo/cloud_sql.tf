# cloud_sql.tf — PostgreSQL 15 instance for the api/ service

resource "google_project_service" "sqladmin" {
  project            = var.project_id
  service            = "sqladmin.googleapis.com"
  disable_on_destroy = false
}

resource "google_sql_database_instance" "gradeops_demo" {
  project          = var.project_id
  name             = "gradeops-demo"
  database_version = "POSTGRES_15"
  region           = var.region

  settings {
    tier = "db-f1-micro"

    ip_configuration {
      ipv4_enabled = true
    }

    backup_configuration {
      enabled = false
    }
  }

  deletion_protection = false

  depends_on = [google_project_service.sqladmin]
}

resource "google_sql_database" "gradeops" {
  project  = var.project_id
  name     = "gradeops"
  instance = google_sql_database_instance.gradeops_demo.name
}

resource "google_sql_user" "gradeops" {
  project  = var.project_id
  name     = "gradeops"
  instance = google_sql_database_instance.gradeops_demo.name
  password = var.db_password
}

resource "google_secret_manager_secret" "db_password" {
  project   = var.project_id
  secret_id = "DB_PASSWORD"

  replication {
    auto {}
  }

  depends_on = [google_project_service.secretmanager]
}

resource "google_secret_manager_secret_version" "db_password" {
  secret      = google_secret_manager_secret.db_password.id
  secret_data = var.db_password
}
