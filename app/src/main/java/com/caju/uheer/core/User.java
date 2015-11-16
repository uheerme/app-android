package com.caju.uheer.core;

import com.caju.uheer.services.player.UheerPlayer;

/**
 * Created by camilo on 24/08/15.
 */
public class User
{
    public String Id;
    public String Email;
    public String Password;
    public double[] Localization;


    public void conectToChannel(Channel c){
        UheerPlayer.changeChannel(c);
    }

    public void changeChannel(Channel c){
        if(UheerPlayer.isPlaying()){
            UheerPlayer.stop();
        }
        UheerPlayer.changeChannel(c);
    }
}
