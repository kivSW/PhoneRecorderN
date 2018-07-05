package com.kivsw.phonerecorder.ui.record_list;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.kivsw.phonerecorder.model.utils.Utils;

import java.util.List;

import phonerecorder.kivsw.com.phonerecorder.R;

/**
 * Created by ivan on 4/3/18.
 */

public class RecordListAdapter extends RecyclerView.Adapter<RecordListAdapter.ItemViewHolder> {

    public interface UIEventHandler
    {
        void playItem(int position);
        void playItemWithPlayerChoosing(int position);
        void selectItem(int position, boolean select);
        void protectItem(int position, boolean select);
    };

    public class ItemViewHolder extends RecyclerView.ViewHolder
    {
        TextView nameText, phonenumberText, durationText, dateTimeText, commentaryText, downloadPercentageText;
        View fromInternalDirTextView;
        ImageButton playButton;
        CheckBox checkbox;
        ImageView imageViewCallDirection, imageViewProtected;
        ProgressBar downloadProgress;
        int position;

        public ItemViewHolder(View view)
        {
            super(view);
            imageViewProtected = (ImageView)view.findViewById(R.id.imageViewProtected);

            nameText= (TextView)view.findViewById(R.id.nameText);
            phonenumberText= (TextView)view.findViewById(R.id.phonenumberText);
            durationText= (TextView)view.findViewById(R.id.durationText);
            dateTimeText= (TextView)view.findViewById(R.id.dateTimeText);
            commentaryText= (TextView)view.findViewById(R.id.commentaryText);
            downloadProgress = (ProgressBar)view.findViewById(R.id.downloadProgress);
            downloadPercentageText = (TextView)view.findViewById(R.id.downloadPercentageText);
            fromInternalDirTextView = view.findViewById(R.id.fromInternalDirTextView);

            checkbox=(CheckBox)view.findViewById(R.id.checkbox);
            checkbox.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        selectItem(position, checkbox.isChecked());
                    }
                });

            imageViewCallDirection=(ImageView)view.findViewById(R.id.imageViewCallDirection);

            playButton=(ImageButton)view.findViewById(R.id.playButton);
            playButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        play(position);
                    }
                });
            playButton.setOnLongClickListener(new View.OnLongClickListener(){
                    @Override
                    public boolean onLongClick(View v) {
                        selectPlayerItem(position);
                        return true;
                    }
                });

            imageViewProtected.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    protectItem(position, !dataSet.get(position).recordFileNameData.isProtected);
                }
            });
        };

    }



    private UIEventHandler eventHandler;
    public void setUIEventHandler(UIEventHandler eventHandler) {
        this.eventHandler = eventHandler;
    }

    private List<RecordListContract.RecordFileInfo> dataSet;
    public void setData(List<RecordListContract.RecordFileInfo> data) {
        this.dataSet = data;
        notifyDataSetChanged();
    };



    public RecordListAdapter()
    {

    };

    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup parent,
                                             int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.callinfo, parent, false);

        ItemViewHolder vh = new ItemViewHolder(view);
        return vh;
    };


    @Override
    public void onBindViewHolder(ItemViewHolder holder, int position) {

        holder.position = position;

        RecordListContract.RecordFileInfo data = dataSet.get(position);

        holder.nameText.setText(data.callerName);
        holder.phonenumberText.setText(data.recordFileNameData.phoneNumber);
        if(data.fromInternalDir)
            holder.fromInternalDirTextView.setVisibility(View.VISIBLE);
        else
            holder.fromInternalDirTextView.setVisibility(View.GONE);

        if(data.recordFileNameData.isSMS) holder.durationText.setText("");
        else holder.durationText.setText(Utils.durationToStr(data.recordFileNameData.duration));

        holder.dateTimeText.setText(data.recordFileNameData.date+" "+data.recordFileNameData.time);
        holder.commentaryText.setText("");
        holder.checkbox.setChecked(data.selected);
        holder.imageViewCallDirection.setImageResource(data.recordFileNameData.income ? R.drawable.icons_ingoing_call : R.drawable.icons_outgoing_call);
        if(data.recordFileNameData.isProtected)
        {
            holder.imageViewProtected.setImageResource(R.drawable.icon_lock);
            holder.imageViewProtected.setAlpha(1.0f);
        }
        else
        {
            holder.imageViewProtected.setImageResource( R.drawable.icon_unlock);
            holder.imageViewProtected.setAlpha(0.5f);
        }
        //holder.imageViewProtected.setImageResource( data.recordFileNameData.isProtected? R.drawable.icon_lock : R.drawable.icon_unlock);

        // chooses background  colour
        int color=0;
        if((position&1)!=0)
        {
            color=holder.nameText.getCurrentTextColor();
            color = (color & 0x00FFFFFF) | 0x22000000;
        }

        holder.itemView.setBackgroundColor(color);


        if(data.isDownloading)
        {
            holder.downloadPercentageText.setVisibility(View.VISIBLE);
            if(holder.downloadProgress.getVisibility()!=View.VISIBLE)
                holder.downloadProgress.setVisibility(View.VISIBLE);
            holder.downloadPercentageText.setText(String.valueOf(data.percentage)+"%");
            holder.playButton.setVisibility(View.GONE);
        }
        else
        {
            holder.downloadPercentageText.setVisibility(View.GONE);
            holder.downloadProgress.setVisibility(View.GONE);
            holder.playButton.setVisibility(View.VISIBLE);
            if(data.recordFileNameData.isSMS)
                holder.playButton.setImageResource(R.drawable.icon_message_read);
            else
                holder.playButton.setImageResource(R.drawable.ic_play_arrow_black_48dp);
        }

    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        if(dataSet ==null)
            return 0;
        return dataSet.size();
    }

    protected void play(int position)
    {
        if(eventHandler!=null)
            eventHandler.playItem(position);
    };
    protected void selectPlayerItem(int position)
    {
        if(eventHandler!=null)
            eventHandler.playItemWithPlayerChoosing(position);
    };
    protected void selectItem(int position, boolean selected)
    {
        if(eventHandler!=null)
            eventHandler.selectItem(position, selected);
    };

    protected void protectItem(int position, boolean selected)
    {
        if(eventHandler!=null)
            eventHandler.protectItem(position, selected);
    };
}
