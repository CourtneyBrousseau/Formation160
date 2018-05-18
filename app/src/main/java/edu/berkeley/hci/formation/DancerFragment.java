package edu.berkeley.hci.formation;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 */
public class DancerFragment extends Fragment implements UsersAdapter.ContactsAdapterListener{

    // TODO: Customize parameter argument names
    private static final String ARG_COLUMN_COUNT = "column-count";
    // TODO: Customize parameters
    private int mColumnCount = 1;
    private OnListFragmentInteractionListener mListener;
    private Project mProject;
    private List<User> sharedUserList;
//    private List<User> allUserList;
    private HashMap<String, User> allUserMap;
    private UsersAdapter mAdapter;
    private SearchView searchView;

    private RecyclerView mRecyclerView;
    private RelativeLayout mNoDancersView;
    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public DancerFragment() {
    }

    // TODO: Customize parameter initialization
    @SuppressWarnings("unused")
    public static DancerFragment newInstance(int columnCount) {
        DancerFragment fragment = new DancerFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }

        mProject = getActivity().getIntent().getParcelableExtra(ProjectDetailActivity.PROJECT_ID);
        fetchContacts();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_dancer_list, container, false);

        Context context = view.getContext();
        mRecyclerView = view.findViewById(R.id.list);
        mNoDancersView = view.findViewById(R.id.no_dancers_view);

        if (mColumnCount <= 1) {
            mRecyclerView.setLayoutManager(new LinearLayoutManager(context));

            allUserMap = new HashMap<>();
            sharedUserList = new ArrayList<>();

            mAdapter = new UsersAdapter(getActivity().getApplicationContext(), sharedUserList, this);
            mRecyclerView.setItemAnimator(new DefaultItemAnimator());
            mRecyclerView.addItemDecoration(new MyDividerItemDecoration(getActivity().getApplicationContext(), DividerItemDecoration.VERTICAL, 36));
            mRecyclerView.setAdapter(mAdapter);
            fetchSharedContacts();

            ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
                @Override
                public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                    return false;
                }

                @Override
                public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                    int pos = viewHolder.getAdapterPosition();
                    User u = sharedUserList.remove(pos);

                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                    DatabaseReference ref = database.getReference("Projects").child(mProject.getUuid());
                    List<String> oldLst = new ArrayList<>();
                    for (User user : sharedUserList) {
                        if (!oldLst.contains(user))
                            oldLst.add(user.getUUID());
                    }
                    mProject.setSharedUserUUIDs(oldLst);
                    ref.setValue(mProject);
//                        mAdapter.notifyDataSetChanged();
                }
            });

            itemTouchHelper.attachToRecyclerView(mRecyclerView);
        } else {
            mRecyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
        }
        // TODO: Fix the NullPointerException
//      mRecyclerView.setAdapter(new DancerRecyclerViewAdapter(null, mListener));
        return view;
    }

    public void updateUI() {


        if (mProject.getSharedUserUUIDs().size() > 0) {
            mNoDancersView.setVisibility(View.GONE);
            mRecyclerView.setVisibility(View.VISIBLE);
        } else {
            mRecyclerView.setVisibility(View.GONE);
            mNoDancersView.setVisibility(View.VISIBLE);
        }
    }

    private void fetchContacts() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference("Users");
        ref.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                User u = dataSnapshot.getValue(User.class);
                allUserMap.put(dataSnapshot.getKey(), u);
                mAdapter.notifyDataSetChanged();
                updateUI();
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void fetchSharedContacts () {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference("Projects").child(mProject.getUuid()).child("sharedUserUUIDs");
        ref.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                String uuid = (String) dataSnapshot.getValue();
                if (!sharedUserList.contains(allUserMap.get(uuid))) {
                    sharedUserList.add(allUserMap.get(uuid));
                    mAdapter.notifyDataSetChanged();
                    updateUI();
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                String uuid = (String) dataSnapshot.getValue();
//                mProject.getSharedUserUUIDs().add(uuid);
                sharedUserList.add(allUserMap.get(uuid));
                mAdapter.notifyDataSetChanged();
                updateUI();
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
//                User u = dataSnapshot.getValue(User.class);
//                sharedUserList.remove(u);;
                mAdapter.notifyDataSetChanged();
                updateUI();
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

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
        // TODO: Update argument type and name
        void onListFragmentInteraction(User item);
    }


    public boolean onCreateOptionsMenu(Menu menu) {
        getActivity().getMenuInflater().inflate(R.menu.list_search_menu, menu);

        // Associate searchable configuration with the SearchView
        SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
        searchView = (SearchView) menu.findItem(R.id.action_search)
                .getActionView();
        searchView.setSearchableInfo(searchManager
                .getSearchableInfo(getActivity().getComponentName()));
        searchView.setMaxWidth(Integer.MAX_VALUE);

        // listening to search query text change
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // filter recycler view when query submitted
                mAdapter.getFilter().filter(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                // filter recycler view when text is changed
                mAdapter.getFilter().filter(query);
                return false;
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_search) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onContactSelected(User contact) {
//        mProject.addSharedUserUUID(contact.getUUID());
//        Toast.makeText(getActivity().getApplicationContext(), "Selected: " + contact.getName() + ", " + contact.getEmail(), Toast.LENGTH_LONG).show();
//        Intent intent = new Intent(getActivity().getApplicationContext(), ProjectDetailActivity.class);
//        intent.putExtra("project", mProject);
//        startActivity(intent);
    }
}
