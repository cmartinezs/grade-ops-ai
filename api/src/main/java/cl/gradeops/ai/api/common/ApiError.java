package cl.gradeops.ai.api.common;

public record ApiError(String error, String message) {

    public static ApiError of(String error) {
        return new ApiError(error, null);
    }

    public static ApiError of(String error, String message) {
        return new ApiError(error, message);
    }
}
