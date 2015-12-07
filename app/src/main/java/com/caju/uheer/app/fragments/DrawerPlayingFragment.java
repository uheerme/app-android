package com.caju.uheer.app.fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.caju.uheer.R;
import com.caju.uheer.app.core.Channel;
import com.caju.uheer.app.services.ServerInformation;

public class DrawerPlayingFragment extends Fragment
{
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {

        View view = inflater.inflate(R.layout.drawer_playing, container, false);

        return view;
    }

    public View onUpdateView(LayoutInflater inflater, ViewGroup container){
        View view = inflater.inflate(R.layout.drawer_playing, container, false);
        TextView your_friends = (TextView) view.findViewById(R.id.your_friends_list);

        TextView channel;
        for(Channel c : ServerInformation.getAllActiveChannels()){
            channel = new TextView(this.getActivity());
            channel.setGravity(View.TEXT_ALIGNMENT_CENTER);
            channel.setText(c.Name);
        }

        return view;
    }
}
