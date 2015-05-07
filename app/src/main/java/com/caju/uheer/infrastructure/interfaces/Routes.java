package com.caju.uheer.infrastructure.interfaces;

public interface Routes {

    String URL = "http://uheer.me/api/";

    String CHANNELS = URL + "channels/";
    String MUSICS   = URL + "musics/";
    String STATUS   = URL + "status/";

    String MUSIC_SUB_ROUTE = "/musics/";
    String PLAY_SUB_ROUTE = "/play/";

    String STREAM = "/stream";
    String DEACTIVATE = "/deactivate";
    String NOW = "/now";

    String SKIP = "skip=";
    String TAKE = "take=";
    String PARAM = "?";
    String AND_PARAM = "&";
}
