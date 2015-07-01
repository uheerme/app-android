package com.caju.uheer.activities;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.caju.uheer.R;


/**
 * A placeholder fragment containing a simple view.
 */
public class StartActivityFragment extends Fragment
{

    public StartActivityFragment()
    {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        return inflater.inflate(R.layout.fragment_start, container, false);
    }
}
