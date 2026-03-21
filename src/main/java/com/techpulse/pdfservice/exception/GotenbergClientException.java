package com.techpulse.pdfservice.exception;

public class GotenbergClientException extends RuntimeException {

    public GotenbergClientException(String message) {
        super(message);
    }

    public GotenbergClientException(String message, Throwable cause) {
        super(message, cause);
    }
}

