package com.caju.uheer.app.services.exceptions;

/**
 * {@code NoneNextMusicException} is the superclass of all classes that represent
 * exceptional conditions which occur as a result of a UheerPlayer instance operation.
 */
public class NoneNextMusicException extends UheerPlayerException {
    /**
     * Constructs a new {@code NoneNextMusicException} that includes the current stack
     * trace.
     */
    public NoneNextMusicException() {
    }

    /**
     * Constructs a new {@code NoneNextMusicException} with the current stack trace
     * and the specified detail message.
     *
     * @param detailMessage
     *            the detail message for this exception.
     */
    public NoneNextMusicException(String detailMessage) {
        super(detailMessage);
    }

    /**
     * Constructs a new {@code NoneNextMusicException} with the current stack trace,
     * the specified detail message and the specified cause.
     *
     * @param detailMessage
     *            the detail message for this exception.
     * @param throwable
     *            the cause of this exception.
     */
    public NoneNextMusicException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    /**
     * Constructs a new {@code NoneNextMusicException} with the current stack trace
     * and the specified cause.
     *
     * @param throwable
     *            the cause of this exception.
     */
    public NoneNextMusicException(Throwable throwable) {
        super(throwable);
    }
}
