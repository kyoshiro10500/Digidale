package com.example.jonathan.applicationtest;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

/**
 * Created by victor on 13/07/17.
 */

public class TextAdapter extends BaseAdapter {
    private Context mContext ;

    public TextAdapter(Context c) {
        mContext = c;
    }

    public int getCount() {
        return mThumbIds.length;
    }

    public Object getItem(int position) {
        return null;
    }

    public long getItemId(int position) {
        return 0;
    }

    // create a new ImageView for each item referenced by the Adapter
    public View getView(int position, View convertView, ViewGroup parent) {
        TextView textView;
        if (convertView == null) {
            // if it's not recycled, initialize some attributes
            textView = new TextView(mContext);

        } else {
            textView = (TextView) convertView;
        }

        textView.setText(mThumbIds[position]);
        textView.setTextSize(20f);
        return textView;
    }

    // references to our images
    private String[] mThumbIds = {};

    public void updateThumb(String[] string){
        this.mThumbIds= new String[string.length];
        for(int i=0;i<string.length;i++) {
            this.mThumbIds[i] = string[i];
        }

    }

    public String getThumb(int i)
    {
        return mThumbIds[i] ;
    }

}
