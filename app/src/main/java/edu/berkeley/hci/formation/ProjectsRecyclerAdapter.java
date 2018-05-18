package edu.berkeley.hci.formation;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by daphnenhuch on 4/10/18.
 */

public class ProjectsRecyclerAdapter extends RecyclerView.Adapter {

    CardView cv;
    TextView blockTitle;
    ImageView thumbnail;
    Context mContext;
    ArrayList<Block> mBlocks;
    MyAdapterListener onClickListener;

    public ProjectsRecyclerAdapter(Context context, ArrayList<Block> blocks, MyAdapterListener listener) {
        mContext = context;
        mBlocks = blocks;
        onClickListener = listener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // here, we specify what kind of view each cell should have. In our case, all of them will have a view
        // made from comment_cell_layout

        View view = LayoutInflater.from(mContext).inflate(R.layout.card_view, parent, false);

        return new BlockViewHolder(view, mContext, onClickListener);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        // here, we the comment that should be displayed at index `position` in our recylcer view
        // everytime the recycler view is refreshed, this method is called getItemCount() times (because
        // it needs to recreate every cell).
        Block block = mBlocks.get(position);
        ((BlockViewHolder) holder).bind(block);
    }

    @Override
    public int getItemCount() {
        return mBlocks.size();
    }

    @Override
    public int getItemViewType(int position) {
        return (position == mBlocks.size()) ? 0 : 1;
    }

}

class BlockViewHolder extends RecyclerView.ViewHolder {
    TextView blockTitle;
    ImageView thumbnail;

    Context context;
    TextView startTime;
    TextView endTime;
    MyAdapterListener onClickListener;
    Button moreInfo;

    BlockViewHolder(View itemView, Context context, MyAdapterListener listener) {
        super(itemView);
        CardView cv = (CardView) itemView.findViewById(R.id.card_view);
        blockTitle = (TextView) itemView.findViewById(R.id.block_title);
        thumbnail = (ImageView) itemView.findViewById(R.id.thumbnail);
//
        startTime = (TextView) itemView.findViewById(R.id.start_time);
        endTime = (TextView) itemView.findViewById(R.id.end_time);
        moreInfo = (Button) itemView.findViewById(R.id.more_info);
        onClickListener = listener;
        this.context = context;
//
        thumbnail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onClickListener.clickImage(view, getAdapterPosition());
            }
        });
        moreInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickListener.moreInfo(v, getAdapterPosition());

            }
        });
    }

    void bind(Block block) {
        ((TextView) itemView.findViewById(R.id.block_title)).setText(block.title);

        if (block.thumbnailimageString != null) {
            block.setThumbnail(Base64.decode(block.thumbnailimageString, Base64.DEFAULT));
        }
        Bitmap bm = BitmapFactory.decodeByteArray(block.getThumbnail(), 0, block.getThumbnail().length);
        thumbnail.setImageBitmap(bm);

        Long startMins = Math.floorDiv(block.startTime, new Long(60));
        Long startSec = block.startTime % 60;
        String sSecs = String.valueOf(startSec);
        if (startSec < 10) {
             sSecs = "0" + String.valueOf(startSec);
        }
        Long endMins = Math.floorDiv(block.endTime, new Long(60));
        Long endSec = block.endTime % 60;
        String eSecs = String.valueOf(endSec);

        if (endSec < 10) {
             eSecs = "0" + String.valueOf(endSec);
        }
        startTime.setText(String.valueOf(startMins) + ":" + sSecs);
        endTime.setText(String.valueOf(endMins) + ":" + eSecs);


    }

}
