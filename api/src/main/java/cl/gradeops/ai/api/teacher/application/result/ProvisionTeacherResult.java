package cl.gradeops.ai.api.teacher.application.result;

import lombok.Builder;

@Builder
public record ProvisionTeacherResult(String firebaseUid, String rawCode) {}
