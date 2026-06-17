package com.liftlogai.ai.provider;

public class AiProviderException extends RuntimeException {

    private final String errorCode;

    public AiProviderException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public AiProviderException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public String errorCode() {
        return errorCode;
    }
}
