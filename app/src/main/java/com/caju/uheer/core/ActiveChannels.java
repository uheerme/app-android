package com.caju.uheer.core;

/**
 * This class maintains the current information of the Active Channels
 * instead of requesting to server every time it is needed.
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