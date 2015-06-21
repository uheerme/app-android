package com.caju.uheer.services;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.caju.uheer.core.Music;
import com.caju.uheer.debug.GlobalVariables;
import com.caju.uheer.interfaces.Routes;
import com.caju.uheer.services.infrastructure.StreamItem;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;


public class Streamer {

    protected final Context context;
    protected IStreamingListener listener;

    protected HashMap<Integer, StreamItem> streams;

    public Streamer(Context context) {
        this.context = context;
        this.streams = new HashMap<>();
    }

    protected String getMusicPath(Music music) {
        return context.getFilesDir() + "/" + music.Id + music.Name;
    }

    protected File getMusicFile(Music music) {
        return new File(getMusicPath((music)));
    }
    
    public StreamItem stream(Music music) {
        Log.d("Stream request", music.toString() + " " + (GlobalVariables.counter++));

        // Tries to get the stream, if it has already started in this uheer instance.
        StreamItem stream = streams.get(new Integer(music.Id));

        if (stream == null) {
            Log.d("Streamer", "Stream not found. Trying to stream...");

            // It didn't. Let's try to stream it.
            stream = new StreamItem(
                    music,
                    getMusicFile(music),
                    new StreamTask().execute(music));

            streams.put(new Integer(music.Id), stream);
        }

        return stream;
    }

    public interface IStreamingListener {
        void onFinished();
    }

    public Streamer setListener(IStreamingListener listener) {
        this.listener = listener;
        return this;
    }

    protected class StreamTask extends AsyncTask<Music, Void, StreamItem> {

        @Override
        protected StreamItem doInBackground(Music... params) {
            Music music = params[0];

            try {
                // Get file's length. If we already have the audio file stored and its length is the same,
                // we don't have to stream, as this song was streamed already, in a different uheer instance.
                URL url = new URL(Routes.MUSICS + music.Id + "/stream");
                URLConnection c = url.openConnection();
                c.connect();
                int length = c.getContentLength();

                // Creates or retrieves the file.
                File file = getMusicFile(music);

                // Informs Streamer that we're downloading this file.
                StreamItem item = new StreamItem(music, file, this);

                // If the file already exists we don't need to download
                if (file.exists() && file.length() == length) {
                    Log.d("StreamTask", "An old copy of the song was found. Stream is unnecessary.");
                    return item;
                }

                // Opens stream for the songs URL.
                InputStream input = new BufferedInputStream(url.openStream());
                FileOutputStream output = new FileOutputStream(file);

                // Transfers data in input to output.
                byte data[] = new byte[1024];
                int downloaded = 0;
                int count;

                while ((count = input.read(data)) != -1) {
                    downloaded += count;
                    GlobalVariables.downloadProgress = downloaded * 100 / length;
                    output.write(data, 0, count);
                }

                output.flush();
                output.close();
                input.close();

                return item;

            } catch (NullPointerException | IOException e) {
                Log.e("Streamer", e.toString(), e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(StreamItem item) {
            Log.d("Streamer", "Finished stream of" + item.music.Name);

            if (listener != null) {
                listener.onFinished();
            }
        }
    }
}
