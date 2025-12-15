package com.huongcung.core.logistics.external.ghn.exception;

public class GhnApiException extends RuntimeException {
    public GhnApiException(String message) {
        super(message);
    }

    public GhnApiException(String message, Throwable cause) {
        super(message, cause);
    }
}
