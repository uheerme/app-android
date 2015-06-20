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


public class Streamer {

    private final Context context;
    private IStreamingListener listener;

    public Streamer(Context context) {
        this.context = context;
    }

    public StreamItem stream(Music music) {
        Log.d("Stream request", music.toString());
        Log.d("Stream request", ""+(GlobalVariables.counter++));
        Log.d("Stream request", "currentThread "+Thread.currentThread().getId()+" - "+Thread.currentThread().getName());

        // Tries to get the stream, if it has already started.
        File file = new File(context.getFilesDir() + "/" + music.Id + music.Name);
        StreamItem item = new StreamItem(music, file, null);

        new StreamTask().execute(music);

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
                // Get file's length
                URL url = new URL(Routes.MUSICS + music.Id + "/stream");
                URLConnection conection= url.openConnection();
                conection.connect();
                int fileLength = conection.getContentLength();
                Log.d("fileLength",""+fileLength);

                // Opens stream for the songs URL.
                InputStream input = new BufferedInputStream(url.openStream());

                // Creates the file.
                File file = new File(context.getFilesDir() + "/" + music.Id + music.Name);

                // Informs Streamer that we're downloading this file.
                StreamItem item = new StreamItem(music, file, this);

                // If the file already exists we don't need to download
                if(file.exists())
                    return item;

                FileOutputStream output = new FileOutputStream(file);

                // Transfers data in input to output.
                byte data[] = new byte[1024];
                int downloaded = 0;
                int count;

                while ((count = input.read(data)) != -1) {
                    downloaded += count;
                    Log.d("Downloaded progress",""+downloaded*100/fileLength);
                    GlobalVariables.downloadProgress = downloaded*100/fileLength;
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
            Log.d("onPostExecute", "Streamer");
            if (listener != null) {
                listener.onFinished(item);
            }
        }
    }
}
