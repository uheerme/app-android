package com.caju.uheer.fragments;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.caju.uheer.R;
import com.caju.uheer.core.ActiveChannels;
import com.caju.uheer.core.Channel;
import com.caju.uheer.services.adapters.MusicListAdapter;

/**
 * A placeholder fragment containing a simple view.
 */
public class ChannelInfoFragment extends Fragment
{
    public static final String ARG_CHANNEL_POSITION = "ARG_CHANNEL_POSITION";

    private Channel mChannel;

    public ChannelInfoFragment()
    {
    }

    public static ChannelInfoFragment newInstance(int position)
    {
        Bundle args = new Bundle();
        args.putInt(ARG_CHANNEL_POSITION, position);
        ChannelInfoFragment fragment = new ChannelInfoFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        int position = getArguments().getInt(ARG_CHANNEL_POSITION);
        mChannel = ActiveChannels.getActiveChannel(position);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {

        View view = inflater.inflate(R.layout.fragment_channel_info, container, false);
        TextView channelName = (TextView) view.findViewById(R.id.channel_name_in_playing);
        channelName.setText(mChannel.Name.toUpperCase());
        TextView owner = (TextView) view.findViewById(R.id.channel_owner_in_playing);
        owner.setText(mChannel.Author.Email);
        TextView ip = (TextView) view.findViewById(R.id.channel_ip_in_playing);
        ip.setText(mChannel.HostIpAddress);
        TextView duration = (TextView) view.findViewById(R.id.channel_duration_in_playing);
        duration.setText("31 min and 34 seg");

        ListView songsListView = (ListView) view.findViewById(R.id.music_list_view_in_playing);

        View v = new LinearLayout(getActivity());

        MusicListAdapter listAdapter = new MusicListAdapter(getActivity(), R.layout.adapter_music_list,mChannel.Musics);
        songsListView.setAdapter(listAdapter);

        return view;

    }


}