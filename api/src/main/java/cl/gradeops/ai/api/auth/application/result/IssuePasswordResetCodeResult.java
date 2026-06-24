package cl.gradeops.ai.api.auth.application.result;

import lombok.Builder;
import java.time.Instant;

@Builder
public record IssuePasswordResetCodeResult(String rawCode, Instant expiresAt) {}
