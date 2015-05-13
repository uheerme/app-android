package com.caju.uheer.services.infrastructure;


public class PlayItem {
    public StreamItem stream;
    public SyncItem sync;

    public PlayItem(StreamItem stream, SyncItem sync) {
        this.stream = stream;
        this.sync = sync;
    }
}
