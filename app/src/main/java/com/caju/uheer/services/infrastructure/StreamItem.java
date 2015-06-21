package com.caju.uheer.services.infrastructure;

import android.os.AsyncTask;

import com.caju.uheer.core.Music;

import java.io.File;

public class StreamItem {
    public Music music;
    public File file;
    public AsyncTask streamTask;
    public boolean finished;

    public StreamItem(Music music, File file, AsyncTask streamTask) {
        this(music, file, streamTask, false);
    }

    public StreamItem(Music music, File file, AsyncTask streamTask, boolean finished) {
        this.music = music;
        this.file = file;
        this.streamTask = streamTask;
        this.finished = finished;
    }
}
