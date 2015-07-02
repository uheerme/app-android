package com.caju.uheer.services.adapters;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.caju.uheer.activities.Playing;
import com.caju.uheer.activities.PlayingFragment;
import com.caju.uheer.core.ActiveChannels;
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
        return PlayingFragment.newInstance(position);
    }

    @Override
    public CharSequence getPageTitle(int position)
    {
        // Generate title based on item position
        Channel c = ActiveChannels.getActiveChannel(position);
        return c.Name;
    }
}