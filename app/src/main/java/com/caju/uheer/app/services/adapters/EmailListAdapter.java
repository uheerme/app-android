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

import java.util.ArrayList;

public class EmailListAdapter extends ArrayAdapter<String>
{
    Context mContext;
    int layoutResourceId;
    ArrayList<String> data = null;

    public EmailListAdapter(Context context, int resource, ArrayList<String> list)
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

        String objectItem = data.get(position);

        TextView textViewItem = (TextView) convertView.findViewById(R.id.email_in_list_view);
        textViewItem.setText(objectItem);
        textViewItem.setTag(objectItem);

        return convertView;
    }
}
