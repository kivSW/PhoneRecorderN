package com.kivsw.phonerecorder.ui.settings;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import phonerecorder.kivsw.com.phonerecorder.R;


/**
 * Created by ivan on 5/11/16.
 */
public class IconSpinnerAdapter extends ArrayAdapter<String> {

    Drawable[] icons=null;

    static public IconSpinnerAdapter create(Context context, /*int textViewResourceId,*/
                              String[] objects, int[] icons_res)
    {

        Drawable[] icons=new Drawable[icons_res.length];
        for(int i=0;i<icons_res.length;i++)
           icons[i]=context.getResources().getDrawable(icons_res[i]);

        return new IconSpinnerAdapter(context, objects, icons);
    }

    static public IconSpinnerAdapter create(Context context, int[] icons_res)
    {

        String labels[]=new String[icons_res.length];
        for(int i=0;i<icons_res.length;i++)
             labels[i]="";

        return create(context, labels, icons_res);
    }

    public IconSpinnerAdapter(Context context, /*int textViewResourceId,*/
                              String[] objects, Drawable[] icons) {
        super(context, R.layout.row, objects);
        this.icons = icons;
    }

    @Override
    public View getDropDownView(int position, View convertView,
                                ViewGroup parent) {
        return getCustomView(position, convertView, parent);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return getCustomView(position, convertView, parent);
    }

    public View getCustomView(int position, View convertView, ViewGroup parent) {

        View row;
        if(convertView!=null)
            row =convertView;
        else {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = inflater.inflate(R.layout.row, parent, false);
        }

        TextView label=(TextView)row.findViewById(R.id.textView);
        label.setText(getItem(position));

        ImageView icon=(ImageView)row.findViewById(R.id.imageView);

        if (icons!=null && icons.length>position){
            icon.setImageDrawable(icons[position]);
        }
        else{
            icon.setImageResource(0);
        }

        return row;
    }
}

