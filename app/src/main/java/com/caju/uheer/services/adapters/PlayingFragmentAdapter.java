package com.caju.uheer.services.adapters;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.caju.uheer.fragments.ChannelInfoFragment;
import com.caju.uheer.services.ActiveChannels;
import com.caju.uheer.core.Channel;

public class PlayingFragmentAdapter extends FragmentPagerAdapter
{
    final int CHANNELS_TO_SHOW = 3;
    private Context context;

    public PlayingFragmentAdapter(FragmentManager fm, Context context)
    {
        super(fm);
        this.context = context;
    }

    @Override
    public int getCount()
    {
        return Math.min(CHANNELS_TO_SHOW, ActiveChannels.getNumberOfActiveChannels());
    }

    @Override
    public Fragment getItem(int position)
    {
        return ChannelInfoFragment.newInstance(position);
    }

    @Override
    public CharSequence getPageTitle(int position)
    {
        Channel c = ActiveChannels.getActiveChannel(position);
        return c.Name;
    }
}