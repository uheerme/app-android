package com.caju.uheer.app.services.infrastructure;


public class PlayItem {
    public StreamItem stream;
    public SyncItem sync;

    public PlayItem(StreamItem stream, SyncItem sync) {
        this.stream = stream;
        this.sync = sync;
    }
}
