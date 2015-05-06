package com.caju.utils.interfaces;

public interface Routes {

    String BASE = "http://54.207.33.242/api/";

    String CHANNELS = BASE + "channels/";
    String MUSICS   = BASE + "musics/";
    String STATUS   = BASE + "status/";

    String MUSIC_SUB_ROUTE = "/musics/";
    String PLAY_SUB_ROUTE = "/play/";

    String STREAM = "/stream";
    String DEACTIVATE = "/deactivate";
    String NOW = "/now";

    String SKIP = "skip=";
    String TAKE = "take=";
    String PARAM = "?"; //used for the first param
    String AND_PARAM = "&"; //used for the following params
}
