package com.caju.uheer.core;

/**
 * Created by camilo on 30/06/15.
 */
public class ActiveChannels
{
    static Channel[] activeChannels;

    public static void setActiveChannels(Channel[] channels)
    {
        activeChannels = channels;
    }

    public static Channel getActiveChannel(int position)
    {
        if(position >= 0 && position <= activeChannels.length)
            return activeChannels[position];
        else
            throw new ArrayIndexOutOfBoundsException("Can't find Channel is this position!");
    }

    public static Channel[] getAllActiveChannels()
    {
        return activeChannels;
    }

    public static int getNumberOfActiveChannels()
    {

        return activeChannels.length;

    }
}