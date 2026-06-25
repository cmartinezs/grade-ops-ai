package cl.gradeops.ai.api.auth.application.result;

import lombok.Builder;

@Builder
public record RegisterResult(String uid, boolean created) {}
