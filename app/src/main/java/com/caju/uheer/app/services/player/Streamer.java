package com.caju.uheer.app.services.player;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.caju.uheer.app.core.Music;
import com.caju.uheer.debug.GlobalVariables;
import com.caju.uheer.app.interfaces.Routes;
import com.caju.uheer.app.services.infrastructure.StreamItem;

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
    private static File musicDir;
    protected IStreamingListener listener;

    protected HashMap<Integer, StreamItem> streams;

    private static final int NOT_VERIFIED = -1;
    private long dirSize = NOT_VERIFIED;
    private static final int MAX_DIR_SIZE = 300;
    // An unhandled error happens if a song bigger than the MAX_DIR_SIZE is played.

    public Streamer(Context context) {
        this.context = context;
        this.streams = new HashMap<>();
        this.musicDir = context.getFilesDir();

        // Get music direcory size
        if(dirSize == NOT_VERIFIED){
            new GetDirSizeTask().execute();
            Log.d("Dir verified", "size: "+dirSize);
        }
    }

    protected String getMusicPath(Music music) {
        return musicDir + "/" + music.Id + music.Name;
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

    private class StreamTask extends AsyncTask<Music, Void, StreamItem> {

        @Override
        protected StreamItem doInBackground(Music... params) {
            Music music = params[0];

            try {
                // Get file's length. If we already have the audio file stored and its length is the same,
                // we don't have to stream, as this song was streamed already, in a different uheer instance.
                URL url = new URL(Routes.API + Routes.MUSICS + music.Id + Routes.STREAM);
                URLConnection c = url.openConnection();
                c.connect();
                int length = c.getContentLength();

                // Creates or retrieves the file.
                File file = getMusicFile(music);

                // Informs Streamer that we're downloading this file.
                StreamItem item = new StreamItem(music, file, this, true);

                // If the file already exists we don't need to download it again.
                if (!file.exists() || file.length() != length) {
                    // Opens stream for the songs URL.
                    InputStream input = new BufferedInputStream(url.openStream());
                    FileOutputStream output = new FileOutputStream(file);

                    // Transfers data in input to output.
                    byte data[] = new byte[1024];
                    int downloaded = 0;
                    int count;

                    Log.d("Streamer", "Streaming " + music.Name);

                    while ((count = input.read(data)) != -1) {
                        downloaded += count;
                        GlobalVariables.downloadProgress = downloaded * 100 / length;
                        output.write(data, 0, count);
                    }

                    output.flush();
                    output.close();
                    input.close();

                    // sum file to folder in mega bytes.
                    if(dirSize != NOT_VERIFIED) {
                        dirSize += downloaded / 1000000;
                    }
                    Log.d("File Added", "file: " + downloaded / 1000000 + "MB, " + "dirSize: " + dirSize + "MB");
                    new DeleteOldSongsTask().execute();
                }

                streams.put(new Integer(music.Id), item);
                return item;

            } catch (NullPointerException | IOException e) {
                Log.e("Streamer", e.toString(), e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(StreamItem item) {
            Log.d("Streamer", "Finished stream of" + item.music.Name);

            if (listener != null) listener.onFinished();
        }
    }

    private class DeleteOldSongsTask extends AsyncTask{
        @Override
        protected Object doInBackground(Object[] params){
            while(dirSize > MAX_DIR_SIZE) {  // Directory is bigger than it should.
                // Delete the oldest one
                File[] fileList = musicDir.listFiles();
                File fileToDelete = fileList[0]; //Initializing fileToDelete with the first.
                for(int i=1; i<fileList.length; i++){
                    if(!fileList[i].isDirectory() && fileList[i].lastModified() < fileToDelete.lastModified()){
                        fileToDelete = fileList[i];
                    }
                }

                long deletedFileSize = fileToDelete.length();
                String deletedFileName = fileToDelete.getName();
                if(fileToDelete.delete()){
                    dirSize -= deletedFileSize/1000000;
                    Log.d("File deleted", deletedFileName);
                }else{
                    Log.e("DeleteOldSongsTask", "Failed to delete old song.");
                }
            }
            return null;
        }
    }

    private class GetDirSizeTask extends AsyncTask {
        @Override
        protected Object doInBackground(Object[] params) {
            dirSize = getDirSize(musicDir);
            Log.d("Dir verified", "size: "+dirSize);
            return null;
        }
    }

    private long getDirSize(File dir){
        if(!dir.exists()) {
            return 0;
        }else{
            long totalSize = 0;
            File[] fileList = dir.listFiles();
            for(int i=0; i<fileList.length; i++){
                //Recursive call if it's a directory
                if(fileList[i].isDirectory()){
                    totalSize += getDirSize(fileList[i]);
                }else{
                    // Sum the file size in bytes
                    totalSize += fileList[i].length();
                }
            }
            // return size in MB
            return totalSize/1000000;
        }
    }
}
