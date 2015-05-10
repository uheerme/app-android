package com.caju.uheer.core;

import java.util.Date;

public class Channel {

    public int Id;
    public String Name;

    public String Owner;
    public String HostIpAddress;
    public String HostMacAddress;

    public boolean Loops;

    public int CurrentId;
    public Date CurrentStartTime;

    public Music[] Musics;

    public Date DateCreated;
    public Date DateUpdated;
    public Date DateDeactivated;

    @Override
    public String toString(){ return this.Name;}
}
