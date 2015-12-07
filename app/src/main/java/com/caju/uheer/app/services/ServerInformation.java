package com.caju.uheer.app.services;

import com.caju.uheer.app.core.Channel;

import java.util.ArrayList;

/**
 * This class maintains the current information of the Active Channels
 * instead of requesting to server every time it is needed.
 */
public class ServerInformation
{
    static Channel[] activeChannels;
    static ArrayList<String>[] activeListeners;

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

    public static void setActiveListeners(ArrayList<String>[] listeners)
    {
        activeListeners = listeners;
    }

    public static ArrayList<String> getActiveListeners(int position)
    {
        if(position >= 0 && position <= activeChannels.length)
            return activeListeners[position];
        else
            throw new ArrayIndexOutOfBoundsException("Can't find Channel is this position!");
    }

    public static ArrayList<String>[] getAllActiveListeners()
    {
        return activeListeners;
    }
}