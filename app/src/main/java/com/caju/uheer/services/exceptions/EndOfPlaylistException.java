package com.caju.uheer.services.exceptions;

/**
 * {@code EndOfPlaylistException} is the superclass of all classes that represent
 * exceptional conditions which occur as a result of a UheerPlayer instance operation.
 */
public class EndOfPlaylistException extends NoneNextMusicException {
    /**
     * Constructs a new {@code EndOfPlaylistException} that includes the current stack
     * trace.
     */
    public EndOfPlaylistException() {
    }

    /**
     * Constructs a new {@code EndOfPlaylistException} with the current stack trace
     * and the specified detail message.
     *
     * @param detailMessage
     *            the detail message for this exception.
     */
    public EndOfPlaylistException(String detailMessage) {
        super(detailMessage);
    }

    /**
     * Constructs a new {@code EndOfPlaylistException} with the current stack trace,
     * the specified detail message and the specified cause.
     *
     * @param detailMessage
     *            the detail message for this exception.
     * @param throwable
     *            the cause of this exception.
     */
    public EndOfPlaylistException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    /**
     * Constructs a new {@code EndOfPlaylistException} with the current stack trace
     * and the specified cause.
     *
     * @param throwable
     *            the cause of this exception.
     */
    public EndOfPlaylistException(Throwable throwable) {
        super(throwable);
    }
}
