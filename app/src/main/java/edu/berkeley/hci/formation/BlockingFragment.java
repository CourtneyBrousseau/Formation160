package edu.berkeley.hci.formation;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link BlockingFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 */
public class BlockingFragment extends Fragment {

    private OnFragmentInteractionListener mListener;
    private Project mProject;

    public BlockingFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mProject = getActivity().getIntent().getParcelableExtra(ProjectDetailActivity.PROJECT_ID);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_blocking, container, false);
        ImageView thumbnail = (ImageView) v.findViewById(R.id.thumbnail);
        CardView card = (CardView) v.findViewById(R.id.blocking_card);
        RelativeLayout noBlockingView = (RelativeLayout) v.findViewById(R.id.no_blocking_view);
        if (mProject.getBlocking().getBlocks().size() > 0) {
            card.setVisibility(View.VISIBLE);
            noBlockingView.setVisibility(View.GONE);
            Block b = mProject.getBlocking().getBlocks().get(0);
            b.setThumbnail(Base64.decode(b.thumbnailimageString, Base64.DEFAULT));
            Bitmap bm = BitmapFactory.decodeByteArray(b.getThumbnail(), 0, b.getThumbnail().length);
            thumbnail.setImageBitmap(bm);
        }
        else {
            card.setVisibility(View.GONE);
            noBlockingView.setVisibility(View.VISIBLE);
        }
        return v;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
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
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
