package com.caju.uheer.core;

import java.util.Date;

public class Music {
    public int Id;

    public int ChannelId;

    public String Name;
    public int SizeInBytes;
    public int LengthInMilliseconds;

    public Date DateCreated;
    public Date DateUpdated;

    @Override
    public String toString() {
        return "#" + Id + " " + Name;
    }
}
