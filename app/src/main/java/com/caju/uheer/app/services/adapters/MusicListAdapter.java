package com.caju.uheer.app.services.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.caju.uheer.R;
import com.caju.uheer.app.core.Music;

/**
 * Created by camilo on 24/10/15.
 */
public class MusicListAdapter extends ArrayAdapter<Music>
{
    Context mContext;
    int layoutResourceId;
    Music data[] = null;

    public MusicListAdapter(Context context, int resource, Music[] list)
    {
        super(context, resource, list);

        this.layoutResourceId = resource;
        this.mContext = context;
        this.data = list;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        View view;
        TextView text;

        if(convertView==null){
            LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
            convertView = inflater.inflate(layoutResourceId, parent, false);
        }

        Music objectItem = data[position];

        TextView textViewItem = (TextView) convertView.findViewById(R.id.song_name_in_list_view);
        textViewItem.setText(objectItem.Name.replaceAll("\\.mp3",""));
        textViewItem.setTag(objectItem.Id);

        return convertView;
    }
}
