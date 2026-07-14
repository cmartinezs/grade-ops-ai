# groq.tf — Groq API key secret for agents/'s Groq provider adapter
# Demo environment
#
# This secret holds the Groq API key used by the agents/ Spring Boot service
# (spring.ai.openai.api-key, see agents/src/main/resources/application-demo.yml).
#
# The secret value must be populated manually after `terraform apply`:
#   gcloud secrets versions add GRADEOPS_GROQ_API_KEY --data-file=- <<< "<your-groq-api-key>"

resource "google_secret_manager_secret" "groq_api_key" {
  project   = var.project_id
  secret_id = "GRADEOPS_GROQ_API_KEY"

  replication {
    auto {}
  }

  depends_on = [google_project_service.secretmanager]
}
