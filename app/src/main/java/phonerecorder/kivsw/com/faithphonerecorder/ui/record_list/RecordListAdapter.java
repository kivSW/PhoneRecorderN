package phonerecorder.kivsw.com.faithphonerecorder.ui.record_list;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import phonerecorder.kivsw.com.faithphonerecorder.R;

/**
 * Created by ivan on 4/3/18.
 */

public class RecordListAdapter extends RecyclerView.Adapter<RecordListAdapter.ItemViewHolder> {

    public interface UIEventHandler
    {
        void playItem(int position);
        void selectPlayerItem(int position);
        void selectItem(int position, boolean select);
    };

    public class ItemViewHolder extends RecyclerView.ViewHolder
    {
        TextView phoneIdText, textViewProtected, nameText, phonenumberText, durationText, dateTimeText, commentaryText;
        ImageButton playButton;
        CheckBox checkbox;
        ImageView imageViewCallDirection;
        int position;

        public ItemViewHolder(View view)
        {
            super(view);
            phoneIdText = (TextView)view.findViewById(R.id.phoneIdText);
            textViewProtected= (TextView)view.findViewById(R.id.textViewProtected);
            nameText= (TextView)view.findViewById(R.id.nameText);
            phonenumberText= (TextView)view.findViewById(R.id.phonenumberText);
            durationText= (TextView)view.findViewById(R.id.durationText);
            dateTimeText= (TextView)view.findViewById(R.id.dateTimeText);
            commentaryText= (TextView)view.findViewById(R.id.commentaryText);


            checkbox=(CheckBox)view.findViewById(R.id.checkbox);
            checkbox.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        selectItem(position, checkbox.isSelected());
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

        holder.phoneIdText.setText("");
        holder.textViewProtected.setText( data.fileNameData.isProtected?"!":"");
        holder.nameText.setText(data.callerName);
        holder.phonenumberText.setText(data.fileNameData.phoneNumber);
        holder.durationText.setText(durationToStr(data.duration));
        holder.dateTimeText.setText(data.fileNameData.date+" "+data.fileNameData.time);
        holder.commentaryText.setText("");;

        holder.checkbox.setSelected(data.selected);

        holder.imageViewCallDirection.setImageResource(data.fileNameData.income ? R.drawable.income : R.drawable.outgoing);

    }

    protected String durationToStr(int d)
    {
        int m,s;
        s=d%60;
        d=d/60;

        m=d;

        return String.format("%02d:%02d", m,s);
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
            eventHandler.selectPlayerItem(position);
    };
    protected void selectItem(int position, boolean selected)
    {
        if(eventHandler!=null)
            eventHandler.selectItem(position, selected);
    };

}
