package com.example.aisupport.ai.exception;

/**
 * Thrown when the AI service encounters an error — provider unavailable,
 * timeout, malformed response, or validation failure.
 */
public class AiServiceException extends RuntimeException {

    private final AiErrorType errorType;

    public AiServiceException(String message, AiErrorType errorType) {
        super(message);
        this.errorType = errorType;
    }

    public AiServiceException(String message, AiErrorType errorType, Throwable cause) {
        super(message, cause);
        this.errorType = errorType;
    }

    public AiErrorType getErrorType() {
        return errorType;
    }

    public enum AiErrorType {
        PROVIDER_UNAVAILABLE,
        TIMEOUT,
        MALFORMED_RESPONSE,
        VALIDATION_FAILURE,
        UNKNOWN
    }
}
