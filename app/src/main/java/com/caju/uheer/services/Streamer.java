package com.caju.uheer.services;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import com.caju.uheer.core.Music;
import com.caju.uheer.interfaces.Routes;
import com.caju.uheer.services.infrastructure.MusicStreamItem;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;


public class Streamer {

    private final Context context;
    private IStreamingListener listener;
    private HashMap<Integer, MusicStreamItem> streams = new HashMap<>();

    public Streamer(Context context) {
        this.context = context;
    }

    public MusicStreamItem stream(Music music) {

        // Tries to get the stream, if it has already started.
        MusicStreamItem item = streams.get(new Integer(music.Id));

        // That's the first time the streaming is being done.
        if (item == null) {
            new StreamTask().execute(music);
        }

        return item;
    }

    public interface IStreamingListener {
        public void onFinished(MusicStreamItem item);
    }

    public Streamer setStreamingListener(IStreamingListener listener) {
        this.listener = listener;
        return this;
    }

    private class StreamTask extends AsyncTask<Music, Void, MusicStreamItem> {

        @Override
        protected MusicStreamItem doInBackground(Music... params) {
            Music music = params[0];

            try {
                // Opens stream for the songs URL.
                InputStream input = new URL(Routes.MUSICS + music.Id + "/stream").openStream();

                // Creates the file.
                File file = new File(
                        context.getExternalFilesDir(Environment.DIRECTORY_MUSIC),
                        music.Name);

                // Informs Streamer that we're downloading this file.
                MusicStreamItem item = new MusicStreamItem(music, file, this);

                streams.put(new Integer(item.music.Id), item);

                FileOutputStream output = new FileOutputStream(file);

                // Transfers data in input to output.
                int c;

                while ((c = input.read()) != -1) {
                    output.write(c);
                }

                input.close();
                output.close();

                item.finished = true;

                return item;

            } catch (NullPointerException | IOException e) {
                Log.e("Streamer", e.toString(), e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(MusicStreamItem item) {
            if (listener != null) {
                listener.onFinished(item);
            }
        }
    }
}
