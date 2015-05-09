package com.caju.uheer.services.exceptions;

/**
 * {@code UheerPlayerException} is the superclass of all classes that represent
 * exceptional conditions which occur as a result of a UheerPlayer instance operation.
 */
public class UheerPlayerException extends RuntimeException {
    /**
     * Constructs a new {@code UheerPlayerException} that includes the current stack
     * trace.
     */
    public UheerPlayerException() {
    }

    /**
     * Constructs a new {@code UheerPlayerException} with the current stack trace
     * and the specified detail message.
     *
     * @param detailMessage
     *            the detail message for this exception.
     */
    public UheerPlayerException(String detailMessage) {
        super(detailMessage);
    }

    /**
     * Constructs a new {@code UheerPlayerException} with the current stack trace,
     * the specified detail message and the specified cause.
     *
     * @param detailMessage
     *            the detail message for this exception.
     * @param throwable
     *            the cause of this exception.
     */
    public UheerPlayerException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    /**
     * Constructs a new {@code UheerPlayerException} with the current stack trace
     * and the specified cause.
     *
     * @param throwable
     *            the cause of this exception.
     */
    public UheerPlayerException(Throwable throwable) {
        super(throwable);
    }
}
