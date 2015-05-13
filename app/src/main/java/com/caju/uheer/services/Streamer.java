package com.caju.uheer.services;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.caju.uheer.core.Music;
import com.caju.uheer.interfaces.Routes;
import com.caju.uheer.services.infrastructure.StreamItem;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;


public class Streamer {

    private final Context context;
    private IStreamingListener listener;
    private HashMap<Integer, StreamItem> streams = new HashMap<>();

    public Streamer(Context context) {
        this.context = context;
    }

    public StreamItem stream(Music music) {
        Log.d("Stream request", music.toString());

        // Tries to get the stream, if it has already started.
        StreamItem item = streams.get(new Integer(music.Id));

        // That's the first time the streaming is being done.
        if (item == null) {
            new StreamTask().execute(music);
        } else {
            Log.d("Stream", "Stream found");
        }

        return item;
    }

    public interface IStreamingListener {
        void onFinished(StreamItem item);
    }

    public Streamer setListener(IStreamingListener listener) {
        this.listener = listener;
        return this;
    }

    private class StreamTask extends AsyncTask<Music, Void, StreamItem> {

        @Override
        protected StreamItem doInBackground(Music... params) {
            Music music = params[0];

            try {
                // Opens stream for the songs URL.
                InputStream input = new URL(Routes.MUSICS + music.Id + "/stream").openStream();

                // Creates the file.
                File file = new File(context.getFilesDir() + "/" + music.Id + music.Name);

                // Informs Streamer that we're downloading this file.
                StreamItem item = new StreamItem(music, file, this);

                streams.put(new Integer(item.music.Id), item);

                FileOutputStream output = new FileOutputStream(file);

                // Transfers data in input to output.
                byte data[] = new byte[1024];
                int total =0;
                int count;

                while ((count = input.read(data)) != -1) {
                    total += count;
                    Log.d("StreamItem","Total downloaded " + total);
                    output.write(data, 0,count );
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
            if (listener != null) {
                listener.onFinished(item);
            }
        }
    }
}
