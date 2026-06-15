# artifact_registry.tf — Docker repository for api/ and agents/ container images

resource "google_project_service" "artifact_registry" {
  project            = var.project_id
  service            = "artifactregistry.googleapis.com"
  disable_on_destroy = false
}

resource "google_artifact_registry_repository" "grade_ops_ai" {
  project       = var.project_id
  location      = var.region
  repository_id = "grade-ops-ai"
  format        = "DOCKER"

  depends_on = [google_project_service.artifact_registry]
}
