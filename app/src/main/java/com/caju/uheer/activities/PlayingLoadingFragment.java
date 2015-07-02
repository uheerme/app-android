package com.caju.uheer.activities;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.caju.uheer.R;


/**
 * A placeholder fragment containing a simple view.
 */
public class PlayingLoadingFragment extends Fragment
{

    public PlayingLoadingFragment()
    {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        return inflater.inflate(R.layout.fragment_playing_loading, container, false);
    }
}
