package edu.berkeley.hci.formation;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.text.format.DateFormat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Date;

public class ProjectDetailActivity extends AppCompatActivity implements
        BlockingFragment.OnFragmentInteractionListener,
        MusicFragment.OnFragmentInteractionListener,
        VideoFragment.OnListFragmentInteractionListener,
        DancerFragment.OnListFragmentInteractionListener {

    public static final String PROJECT_ID = "project";

    private static final int REQUEST_VIDEO_CAPTURE = 1;

    private BlockingFragment mBlockingFragment;
    private VideoFragment mVideoFragment;
    private DancerFragment mDancerFragment;

    private Project mProject;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        forceVertical();
        setContentView(R.layout.activity_project_detail);
        mProject = getIntent().getParcelableExtra(PROJECT_ID);

        mBlockingFragment = (BlockingFragment) getSupportFragmentManager().findFragmentById(R.id.blocking_fragment);
        mVideoFragment = (VideoFragment) getSupportFragmentManager().findFragmentById(R.id.video_fragment);
        mDancerFragment = (DancerFragment) getSupportFragmentManager().findFragmentById(R.id.dancer_fragment);

        setSupportActionBar((Toolbar) findViewById(R.id.my_toolbar));
        getSupportActionBar().setTitle(mProject.getTitle());
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Button mEditBlockingButton = findViewById(R.id.edit_blocking_button);
        mEditBlockingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent launchEditBlockingIntent = new Intent(ProjectDetailActivity.this, ProjectsActivity.class);
                launchEditBlockingIntent.putExtra("project", mProject);
                forceHorizontal();
                startActivity(launchEditBlockingIntent);
            }
        });

        Button mVideoRecordButton = findViewById(R.id.record_video_button);
        mVideoRecordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
                if (takeVideoIntent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(takeVideoIntent, REQUEST_VIDEO_CAPTURE);
                }
            }
        });

        Button mAddDancerButton = findViewById(R.id.add_dancer_button);
        mAddDancerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent addDancerIntent = new Intent(ProjectDetailActivity.this, UserSearchListActivity.class);
                addDancerIntent.putExtra("project", mProject);
                startActivity(addDancerIntent);
            }
        });

        CardView blockingCard = findViewById(R.id.blocking_card);
        blockingCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent playAnimationActivity = new Intent(ProjectDetailActivity.this, AnimationActivity.class);
                playAnimationActivity.putExtra("project", mProject);
                forceHorizontal();
                startActivity(playAnimationActivity);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_VIDEO_CAPTURE && resultCode == RESULT_OK) {
            mVideoFragment.storeVideo(data.getData(), new Date(), DateFormat.getDateFormat(this));
            mVideoFragment.updateUI();
        }
    }

    @Override
    public void onListFragmentInteraction(String videoId) {
        // TODO: Maybe do something
    }

    @Override
    public void onFragmentInteraction(Uri uri) {
        // TODO: Maybe do something
    }

    @Override
    public void onListFragmentInteraction(User item) {
        // TODO: Maybe do something
    }

    public void forceHorizontal(){
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
    }

    public void forceVertical(){
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.project_detail_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_delete) {
            AlertDialog.Builder alert = new AlertDialog.Builder(ProjectDetailActivity.this, R.style.AlertDialogTheme);
            alert.setTitle("Are you sure you want to delete this project?");

            alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    DatabaseReference projectsRef = FirebaseDatabase.getInstance().getReference().child("Projects");
                    projectsRef.child(mProject.getUuid()).removeValue();
                    Intent intent = new Intent(ProjectDetailActivity.this, ProjectListViewActivity.class);
                    startActivity(intent);
                }
            });

            alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    // Canceled.
                }
            });
            AlertDialog alertDialog = alert.create();

            // show it
            alertDialog.show();
            return true;
        }

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_rename) {
            AlertDialog.Builder alert = new AlertDialog.Builder(ProjectDetailActivity.this, R.style.AlertDialogTheme);
            alert.setTitle("Edit Project Name");
            alert.setMessage("Enter a new project name");


            // Set an EditText view to get user input
            final EditText input = new EditText(ProjectDetailActivity.this);
            alert.setView(input);

            alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    mProject.setTitle(input.getText().toString());
                    getSupportActionBar().setTitle(mProject.getTitle());
                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                    DatabaseReference projectsRef = database.getReference("Projects");
                    projectsRef.child(mProject.getUuid()).child("title").setValue(mProject.getTitle());
                }
            });

            alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    // Canceled.
                }
            });
            AlertDialog alertDialog = alert.create();

            // show it
            alertDialog.show();
            return true;
        }



        return super.onOptionsItemSelected(item);
    }
}
