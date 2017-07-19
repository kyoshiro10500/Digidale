package com.example.jonathan.applicationtest;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import java.util.Arrays;


/**
 * Created by jonathan on 05/07/17.
 */

public class ImageAdapter2 extends BaseAdapter {
    private Context mContext ;
    private float scale ;
    private int ecrans ;
    public ImageAdapter2(Context c, int nb_ecrans) {
        mContext = c;
        scale = c.getResources().getDisplayMetrics().density ;
        ecrans = nb_ecrans ;
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
        ImageView imageView;
        if (convertView == null) {
            // if it's not recycled, initialize some attributes
            imageView = new ImageView(mContext);
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setLayoutParams(new GridView.LayoutParams((int) (35*scale+0.5f), (int) (35*scale+0.5f)));

        } else {
            imageView = (ImageView) convertView;
        }
        imageView.setImageResource(mThumbIds[position]);
        return imageView;
    }

    // references to our images
    private Integer[] mThumbIds = {R.drawable.screen};
    public void updateThumb(int nb){
        this.mThumbIds= new Integer[]{};
        this.mThumbIds  = Arrays.copyOf(this.mThumbIds, this.mThumbIds.length + nb);
        for(int i=1;i<=nb;i++) {
            this.mThumbIds[this.mThumbIds.length - i] = R.drawable.screen;
        }

    }
    public void updateThumbSelecter(int nb){
        this.mThumbIds= new Integer[]{};
        this.mThumbIds  = Arrays.copyOf(this.mThumbIds, this.mThumbIds.length + nb);
        for(int i=1;i<=nb;i++) {
            this.mThumbIds[this.mThumbIds.length - i] = R.drawable.ecranvert;
        }

    }
    public void showNumThumb(Integer indice, Integer position){
        if(indice==0){
            this.mThumbIds[position]=R.drawable.num1;
        }
        else if(indice==1){
            this.mThumbIds[position]=R.drawable.num2;

        }
        else if(indice==2){
            this.mThumbIds[position]=R.drawable.num3;

        }
        else if(indice==3){
            this.mThumbIds[position]=R.drawable.num3;

        }
    }
    public void hideNumThumb(Integer position){
        this.mThumbIds[position]=R.drawable.ecranvert;
    }
}