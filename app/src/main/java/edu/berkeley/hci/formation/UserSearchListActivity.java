package edu.berkeley.hci.formation;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.Image;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Base64;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class UserSearchListActivity extends AppCompatActivity implements UsersAdapter.ContactsAdapterListener {
    private static final String TAG = UserSearchListActivity.class.getSimpleName();
    private RecyclerView recyclerView;
//    private List<User> userList;
    private UsersAdapter mAdapter;
    private SearchView searchView;
    private Project mProj;
    private List<User> notSharedUserList;
    //    private List<User> allUserList;
    private HashMap<String, User> allUserMap;

    // url to fetch contacts json
    private static final String URL = "https://api.androidhive.info/json/contacts.json";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_search_activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        notSharedUserList = new ArrayList<>();
        allUserMap = new HashMap<>();

        mProj = getIntent().getParcelableExtra("project");
        mProj.setSharedUserUUIDs(new ArrayList<String>());

        // toolbar fancy stuff
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.toolbar_title);

        recyclerView = findViewById(R.id.recycler_view);
//        userList = new ArrayList<>();
        mAdapter = new UsersAdapter(this, notSharedUserList, this);

        // white background notification bar
        whiteNotificationBar(recyclerView);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new MyDividerItemDecoration(this, DividerItemDecoration.VERTICAL, 36));
        recyclerView.setAdapter(mAdapter);

        fetchContacts();
        fetchNotSharedContacts();

        android.support.design.widget.FloatingActionButton checkButton = findViewById(R.id.fab);
        checkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseDatabase database = FirebaseDatabase.getInstance();
                DatabaseReference ref = database.getReference("Projects").child(mProj.getUuid());
                for (Block b : mProj.getBlocking().getBlocks()) {
                    if (b.thumbnailimage != null) {
                        b.thumbnailimageString = Base64.encodeToString(b.thumbnailimage, Base64.DEFAULT);
                    }
                    b.setThumbnail(null);
                }
                ref.setValue(mProj);
                DatabaseReference permissionRef = database.getReference("Permissions");
                for (String uuid : mProj.getSharedUserUUIDs()) {
                    permissionRef.child(uuid).child(mProj.getUuid()).setValue(true);
                }
//                Toast.makeText(getApplicationContext(), "Selected: " + contact.getName() + ", " + contact.getEmail(), Toast.LENGTH_LONG).show();
                Intent intent = new Intent(UserSearchListActivity.this, ProjectDetailActivity.class);
                intent.putExtra("project", mProj);
                startActivity(intent);
            }
        });

    }


    private void fetchContacts() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference("Users");
        ref.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                User u = dataSnapshot.getValue(User.class);
                allUserMap.put(dataSnapshot.getKey(), u);
                notSharedUserList.add(u);
                mAdapter.notifyDataSetChanged();
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

    private void fetchNotSharedContacts () {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference("Projects").child(mProj.getUuid()).child("sharedUserUUIDs");
        ref.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                String uuid = (String) dataSnapshot.getValue();
                if (notSharedUserList.contains(allUserMap.get(uuid))) {
                    mProj.addSharedUserUUID(uuid);
                    notSharedUserList.remove(allUserMap.get(uuid));
                    mAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                String uuid = (String) dataSnapshot.getValue();
                if (notSharedUserList.contains(allUserMap.get(uuid))) {
                    mProj.addSharedUserUUID(uuid);
                    notSharedUserList.remove(allUserMap.get(uuid));
                    mAdapter.notifyDataSetChanged();
                }
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.list_search_menu, menu);
        // Associate searchable configuration with the SearchView
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView = (SearchView) menu.findItem(R.id.action_search)
                .getActionView();
        searchView.setSearchableInfo(searchManager
                .getSearchableInfo(getComponentName()));
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
        } else {
//            Toast.makeText(this,"onBackPressed",Toast.LENGTH_SHORT).show();
            onBackPressed();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        // close search view on back button pressed
//        Toast.makeText(this,"onBackPressed",Toast.LENGTH_SHORT).show();
        if (!searchView.isIconified()) {
            searchView.setIconified(true);
            return;
        }
        super.onBackPressed();
        Intent intent = new Intent(UserSearchListActivity.this, ProjectDetailActivity.class);
        intent.putExtra("project", mProj);
        startActivity(intent);
    }

    private void whiteNotificationBar(View view) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int flags = view.getSystemUiVisibility();
            flags |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            view.setSystemUiVisibility(flags);
            getWindow().setStatusBarColor(Color.WHITE);
        }
    }

    @Override
    public void onContactSelected(User contact) {
        if (!mProj.getSharedUserUUIDs().contains(contact.getUUID())) {
            mProj.addSharedUserUUID(contact.getUUID());
            notSharedUserList.remove(contact);
        }
        else
        {
            List<String> newLst = mProj.getSharedUserUUIDs();
            newLst.remove(contact.getUUID());
            mProj.setSharedUserUUIDs(newLst);
            notSharedUserList.add(contact);
        }
    }
}