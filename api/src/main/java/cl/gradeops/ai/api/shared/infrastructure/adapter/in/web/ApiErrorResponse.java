package cl.gradeops.ai.api.shared.infrastructure.adapter.in.web;

public record ApiErrorResponse(String error, String message) {
    public static ApiErrorResponse of(String error) { return new ApiErrorResponse(error, null); }
    public static ApiErrorResponse of(String error, String message) { return new ApiErrorResponse(error, message); }
}
