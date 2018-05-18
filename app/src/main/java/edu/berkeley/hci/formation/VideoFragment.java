package edu.berkeley.hci.formation;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.UploadTask;

import java.text.DateFormat;
import java.util.Date;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 */
public class VideoFragment extends Fragment {

    private Project mProject;
    private OnListFragmentInteractionListener mListener;
    private Context mContext;
    private RecyclerView mRecyclerView;
    private RelativeLayout mNoVideosView;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public VideoFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mProject = getActivity().getIntent().getParcelableExtra(ProjectDetailActivity.PROJECT_ID);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_video_list, container, false);
        mContext = view.getContext();
        mRecyclerView = view.findViewById(R.id.list);
        mNoVideosView = view.findViewById(R.id.no_videos_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false));

        mRecyclerView.setAdapter(new VideoRecyclerViewAdapter(mProject, mListener));
        updateUI();
        return view;
    }

    public void updateUI() {
        if (mProject.getNumVideos() > 0) {
            mNoVideosView.setVisibility(View.GONE);
            mRecyclerView.setVisibility(View.VISIBLE);
            mRecyclerView.getAdapter().notifyDataSetChanged();
        } else {
            mRecyclerView.setVisibility(View.GONE);
            mNoVideosView.setVisibility(View.VISIBLE);
        }
    }

    public void storeVideo(Uri videoUri, final Date date, final DateFormat dateFormat) {
        final String videoId = videoUri.getLastPathSegment();
        StorageMetadata videoMetadata = new StorageMetadata.Builder()
                .setContentType(mContext.getContentResolver().getType(videoUri))
                .build();
        FirebaseUtil.getVideoStorageRef().child(videoId).putFile(videoUri, videoMetadata)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        mProject.addVideo(videoId, dateFormat.format(date));
                        mProject.updateDatabase();
                        updateUI();
                    }
                });
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnListFragmentInteractionListener) {
            mListener = (OnListFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnListFragmentInteractionListener {
        void onListFragmentInteraction(String videoId);
    }
}
