package edu.berkeley.hci.formation;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import edu.berkeley.hci.formation.VideoFragment.OnListFragmentInteractionListener;

/**
 * {@link RecyclerView.Adapter} that can display a video and makes a call to the
 * specified {@link OnListFragmentInteractionListener}.
 */
public class VideoRecyclerViewAdapter extends RecyclerView.Adapter<VideoRecyclerViewAdapter.ViewHolder> {

    private final Project mProject;
    private final OnListFragmentInteractionListener mListener;

    public VideoRecyclerViewAdapter(Project project, OnListFragmentInteractionListener listener) {
        mProject = project;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_video, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final String videoId = mProject.getVideoId(position);
        holder.mTextView.setText(mProject.getVideoTitle(position));
        GlideApp.with(holder.mView)
                .load(FirebaseUtil.getVideoStorageRef().child(videoId))
                .centerCrop()
                .into(holder.mImageView);
        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    mListener.onListFragmentInteraction(videoId);
                }
                Context context = v.getContext();
                Intent intent = new Intent(context, VideoPlaybackActivity.class);
                intent.putExtra(ProjectDetailActivity.PROJECT_ID, mProject);
                intent.putExtra(VideoPlaybackActivity.VIDEO_ID, videoId);
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mProject.getNumVideos();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final ImageView mImageView;
        public final TextView mTextView;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mImageView = view.findViewById(R.id.video_thumbnail);
            mTextView = view.findViewById(R.id.title_text);
        }
    }
}
