package edu.berkeley.hci.formation;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.Path;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class ProjectListViewActivity extends AppCompatActivity {

    private static final String TAG = ProjectListViewActivity.class.getSimpleName();

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private HashMap<String, Project> mProjects = new HashMap<String, Project>();
    private DatabaseReference projectsRef;
    private String currUserUUID;
    private HashSet<String> mProjectUUIDs;
    private DatabaseReference permsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project_list_view);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mProjectUUIDs = new HashSet<>();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        currUserUUID = user.getUid();

        // get projects from server
        // projectsRef = FirebaseUtil.getProjectsRef();
        projectsRef = FirebaseDatabase.getInstance().getReference("Projects");
        permsRef = FirebaseDatabase.getInstance().getReference("Permissions");

        getUserProjectLists();
        setDataListener();

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent launchCreateNewProject = new Intent(ProjectListViewActivity.this, CreateProjectNameActivity.class);
                Project newProj = new Project();
                String uuid = newProj.getUuid();
//                newProj.addSharedUserUUID(currUserUUID);
                FirebaseDatabase database = FirebaseDatabase.getInstance();
                DatabaseReference permsRef = database.getReference("Permissions");
                permsRef.child(currUserUUID).child(uuid).setValue(true);
                mProjectUUIDs.add(uuid);
                projectsRef.child(uuid).setValue(newProj);

                launchCreateNewProject.putExtra("project", newProj);
                launchCreateNewProject.putExtra("currUserUUID", currUserUUID);
                startActivity(launchCreateNewProject);
            }
        });

        mRecyclerView = (RecyclerView) findViewById(R.id.project_recycler);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
    }

    private void getUserProjectLists() {
        permsRef.child(currUserUUID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    mProjectUUIDs.add(snapshot.getKey());
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }



    private void setDataListener() {
        projectsRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {

                Log.d(TAG, "onChildAdded:" + dataSnapshot.getKey());
                Project project = parseProject(dataSnapshot);
                if (mProjectUUIDs.contains(dataSnapshot.getKey())) {
                    mProjects.put(dataSnapshot.getKey(), project);
                    setAdapterAndUpdateData();
                }

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {
                Log.d(TAG, "onChildChanged:" + dataSnapshot.getKey());
                Project project = parseProject(dataSnapshot);
                if (mProjectUUIDs.contains(project.getUuid())) {
                    mProjects.put(project.getUuid(), project);
                    setAdapterAndUpdateData();
                }
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                Log.d(TAG, "onChildRemoved:" + dataSnapshot.getKey());

                String projectUUID = (String) dataSnapshot.child("uuid").getValue();
                mProjects.remove(projectUUID);
                setAdapterAndUpdateData();
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName) {
                Log.d(TAG, "onChildMoved:" + dataSnapshot.getKey());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "postComments:onCancelled", databaseError.toException());
                Toast.makeText(ProjectListViewActivity.this, "Failed to load comments.",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private Project parseProject(DataSnapshot dataSnapshot) {
        ArrayList<String> sharedUserUUIDs = new ArrayList<String>();
        for (DataSnapshot UUIDData : dataSnapshot.child("sharedUserUUIDs").getChildren()) {
            String UUID = UUIDData.getValue(String.class);
            sharedUserUUIDs.add(UUID);
        }
        List<String> videoIds = new ArrayList<>();
        // TODO: Extremely hacky fix and abstraction violations
        for (DataSnapshot videoIdData : dataSnapshot.child("videoIds").getChildren()) {
            String videoId = videoIdData.getValue(String.class);
            videoIds.add(videoId);
        }
        List<String> videoTitles = new ArrayList<>();
        // TODO: Extremely hacky fix and abstraction violations
        for (DataSnapshot videoTitleData : dataSnapshot.child("videoTitles").getChildren()) {
            String videoTitle = videoTitleData.getValue(String.class);
            videoTitles.add(videoTitle);
        }

        ArrayList<Block> blocks = new ArrayList<Block>();

        for (DataSnapshot blockData : dataSnapshot.child("blocking").child("blocks").getChildren()) {
            String blockType = (String) blockData.child("type").getValue();

            // TODO: Rewrite in a functional or factory style to avoid forgetting to set a value
            if (blockType.equals(Block.STATIC)) {
                StaticBlock block = new StaticBlock();
                block.defaultLength = (long) blockData.child("defaultLength").getValue();
                block.endTime = (long) blockData.child("endTime").getValue();
                block.startTime = (long) blockData.child("startTime").getValue();
                block.title = (String) blockData.child("title").getValue();
                block.type = (String) blockData.child("type").getValue();
                block.uid = (String) blockData.child("uid").getValue();
                ArrayList<Dot> dots = new ArrayList<Dot>();

                for (DataSnapshot dotData : blockData.child("dots").getChildren()) {
                    Dot dot = dotData.getValue(Dot.class);
                    dots.add(dot);
                }

                block.dots = dots;

                block.thumbnailimageString = (String) blockData.child("thumbnailimageString").getValue();
                block.setThumbnail(Base64.decode(block.thumbnailimageString, Base64.DEFAULT));


                blocks.add(block);
            }

            if (blockType.equals(Block.TRANSITION)) {
                TransitionBlock block = new TransitionBlock();
                block.defaultLength = blockData.child("defaultLength").getValue(Integer.class);
                block.endTime = (long) blockData.child("endTime").getValue();
                block.startTime = (long) blockData.child("startTime").getValue();
                block.title = (String) blockData.child("title").getValue();
                block.type = (String) blockData.child("type").getValue();
                block.uid = (String) blockData.child("uid").getValue();
                block.duration = (long) blockData.child("duration").getValue();

                ArrayList<Dot> dotsBefore = new ArrayList<Dot>();

                for (DataSnapshot dotData : blockData.child("dotsBefore").getChildren()) {
                    Dot dot = dotData.getValue(Dot.class);
                    dotsBefore.add(dot);
                }

                block.dotsBefore = dotsBefore;

                ArrayList<Dot> dotsAfter = new ArrayList<Dot>();

                for (DataSnapshot dotData : blockData.child("dotsAfter").getChildren()) {
                    Dot dot = dotData.getValue(Dot.class);
                    dotsAfter.add(dot);
                }

                block.dotsAfter = dotsAfter;

                HashMap<String, Path> paths = new HashMap<String, Path>();

                for (DataSnapshot pathData : blockData.child("paths").getChildren()) {
                    Path path = pathData.getValue(Path.class);
                    paths.put(pathData.getKey(), path);
                }

                block.paths = paths;

                block.thumbnailimageString = (String) blockData.child("thumbnailimageString").getValue();
                block.setThumbnail(Base64.decode(block.thumbnailimageString, Base64.DEFAULT));

                blocks.add(block);
            }
        }
        Project project = new Project(new Blocking(blocks),
                dataSnapshot.child("title").getValue(String.class),
                dataSnapshot.child("uuid").getValue(String.class));
        project.setVideoIds(videoIds);
        project.setVideoTitles(videoTitles);
        project.setSharedUserUUIDs(sharedUserUUIDs);
        return project;
    }

    private void setAdapterAndUpdateData() {
        // create a new adapter with the updated mComments array
        // this will "refresh" our recycler view
        mAdapter = new ProjectAdapter(this, mProjects.values());
        mRecyclerView.setAdapter(mAdapter);
    }

    private void forceLandscape(){
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
    }

}
