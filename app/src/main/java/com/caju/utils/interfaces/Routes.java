package com.caju.utils.interfaces;

public interface Routes {

    static final String ROUTE = "http://uheer.me/api";

    static final String CHANNEL_ROUTE = ROUTE+"/channels/";
    static final String MUSIC_ROUTE = ROUTE+"/musics/";
    static final String STATUS_ROUTE = ROUTE+"/status/";

    static final String MUSIC_SUB_ROUTE = "/musics/";
    static final String PLAY_SUB_ROUTE = "/play/";

    static final String STREAM_END_ROUTE = "/stream";
    static final String DEACTIVATE_END_ROUTE = "/deactivate";
    static final String NOW_END_ROUTE = "/now";

    static final String SKIP = "skip=";
    static final String TAKE = "take=";
    static final String PARAM = "?"; //used for the first param
    static final String AND_PARAM = "&"; //used for the following params
}
