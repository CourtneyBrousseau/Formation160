package edu.berkeley.hci.formation;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import android.util.Base64;
import android.widget.Toast;

public class ProjectsActivity extends AppCompatActivity {

    Project proj;
    ArrayList<Block> blocks;
    Boolean showing;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_projects);
        Toolbar mToolbar = (Toolbar) findViewById(R.id.project_toolbar);
        setSupportActionBar(mToolbar);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        //Rebuilds project from existing project
        Bundle bundle = getIntent().getExtras();
        try {
            proj = bundle.getParcelable("project");
            //To do: rebuild blocking etc
        } catch (NullPointerException e) {
            proj = new Project();
        }
        setTitle("");
        TextView title = findViewById(R.id.project_title);
        title.setText(proj.getTitle());
        setAdapterAndUpdateData();
        showing = false;
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        final View customView = inflater.inflate(R.layout.change_start_end_time, null);


        Button back = findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        FirebaseDatabase database = FirebaseDatabase.getInstance();
                                        DatabaseReference projectsRef = database.getReference("Projects");

//                 This is essential for uploading to Firebase to work
                                        for (Block block : proj.getBlocking().blocks) {
                                            block.thumbnailimageString = Base64.encodeToString(block.thumbnailimage, Base64.DEFAULT);
                                            block.thumbnailimage = null;
                                        }

                                        projectsRef.child(proj.getUuid()).setValue(proj);
                                        Intent intent = new Intent(ProjectsActivity.this, ProjectDetailActivity.class);
                                        forceHorizontal();
                                        intent.putExtra("project", proj);
                                        startActivity(intent);
                                    }
                                });

        findViewById(R.id.project).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                findViewById(R.id.add_transition_button).setVisibility(View.INVISIBLE);
                findViewById(R.id.add_blocking_button).setVisibility(View.INVISIBLE);
                findViewById(R.id.add_transition_text).setVisibility(View.INVISIBLE);
                findViewById(R.id.add_blocking_text).setVisibility(View.INVISIBLE);
                FloatingActionButton fab = findViewById(R.id.show_options);
                fab.setImageResource(R.drawable.ic_add_black_24dp);

            }});

        findViewById(R.id.show_options).setOnClickListener(new View.OnClickListener() {
                                                               @Override
                                                               public void onClick(View v) {
                   if (proj.getBlocking().getBlocks().size() > 0) {
                       FloatingActionButton fab = findViewById(R.id.show_options);

                       if (showing) {
                           findViewById(R.id.add_transition_button).setVisibility(View.INVISIBLE);
                           findViewById(R.id.add_blocking_button).setVisibility(View.INVISIBLE);
                           findViewById(R.id.add_transition_text).setVisibility(View.INVISIBLE);
                           findViewById(R.id.add_blocking_text).setVisibility(View.INVISIBLE);
                           fab.setImageResource(R.drawable.ic_add_black_24dp);
                           showing = false;
                       } else {

                           findViewById(R.id.add_transition_button).setVisibility(View.VISIBLE);
                           findViewById(R.id.add_blocking_button).setVisibility(View.VISIBLE);
                           findViewById(R.id.add_transition_text).setVisibility(View.VISIBLE);
                           findViewById(R.id.add_blocking_text).setVisibility(View.VISIBLE);
                           fab.setImageResource(R.drawable.ic_clear_black_24dp);
                           showing = true;
                       }


                   } else {
                       Intent launchBlocking = new Intent(ProjectsActivity.this, BlockActivity.class);
                       forceHorizontal();
                       launchBlocking.putExtra("project", (Project) proj);
                       launchBlocking.putExtra("block_to_edit", -1);
                       startActivity(launchBlocking);
                   }

               }
           });


                findViewById(R.id.add_blocking_button).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        Intent launchBlocking = new Intent(ProjectsActivity.this, BlockActivity.class);
                        forceHorizontal();
                        launchBlocking.putExtra("project", (Project) proj);
                        launchBlocking.putExtra("block_to_edit", -1);
                        startActivity(launchBlocking);
                    }
                });
        findViewById(R.id.add_transition_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent launchBlocking = new Intent(ProjectsActivity.this, TransitionActivity.class);
                forceHorizontal();
                launchBlocking.putExtra("project", (Project) proj);
                launchBlocking.putExtra("Creating new", true);
                launchBlocking.putExtra("block_to_start", proj.getBlocking().getBlocks().size() - 1);
                //launchBlocking.putExtra("block_to_end", proj.getBlocking().getBlocks().size() - 1);

                startActivity(launchBlocking);
            }
        });
        final Button playAnimation = (Button) findViewById(R.id.play_animation);
        if (proj.getBlocking().getBlocks().size() == 0){
            playAnimation.setVisibility(View.INVISIBLE);
        }

        playAnimation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (proj.getBlocking().getBlocks().size()>0) {
                    Intent intent = new Intent(ProjectsActivity.this, AnimationActivity.class);
                    forceHorizontal();
                    intent.putExtra("project", (Parcelable) proj);
                    startActivity(intent);
                } else {
                    Toast.makeText(getApplicationContext(), "Cannot play animation without formations", Toast.LENGTH_LONG);
                }
            }
        });

        //Allows the project title to be editable
    }

    public void setAdapterAndUpdateData() {

        RecyclerView rv = (RecyclerView) findViewById(R.id.thumbnail_recycler);
        TextView tv = (TextView) findViewById(R.id.empty);
        //If no comments show "no blocks to show"
        if (proj.getBlocking().getBlocks().size() == 0) {
            tv.setVisibility(View.VISIBLE);
            rv.setVisibility(View.GONE);
        } else {
            rv.setVisibility(View.VISIBLE);
            tv.setVisibility(View.GONE);

            LinearLayoutManager llm = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.HORIZONTAL, false);
            rv.setLayoutManager(llm);
            //Set up the recycler view and instantiate all the buttons within a card
            ProjectsRecyclerAdapter mAdapter = new ProjectsRecyclerAdapter(this, proj.getBlocking().getBlocks(), new MyAdapterListener() {


                @Override
                public void clickImage(View v, int position) {
                    int pos = position;
                    Block b = proj.getBlocking().getBlocks().get(pos);
                    if (b.getType().equals(Block.STATIC)) {
                        Intent intent = new Intent(ProjectsActivity.this, BlockActivity.class);
                        forceHorizontal();
                        intent.putExtra("block_to_edit", pos);
                        intent.putExtra("project", proj);
                        startActivity(intent);
                    } else {
                        Intent intent = new Intent(ProjectsActivity.this, TransitionActivity.class);
                        forceHorizontal();
                        intent.putExtra("block_to_edit", pos);
                        intent.putExtra("block_to_start", pos - 1);
                        intent.putExtra("block_to_end", pos + 1);
                        intent.putExtra("project", proj);
                        startActivity(intent);
                    }

                }

                @Override
                public void moreInfo(View v, int position) {
                    final int pos = position;
                    Button btn = (Button) findViewById(R.id.more_info);
                    PopupMenu popup = new PopupMenu(ProjectsActivity.this, v);
                    //Inflating the Popup using xml file
                    popup.getMenuInflater().inflate(R.menu.more_info_menu, popup.getMenu());

                    //registering popup with OnMenuItemClickListener
                    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        public boolean onMenuItemClick(MenuItem item) {
                            switch (item.getItemId()) {
                                case (R.id.remove_block):
                                    AlertDialog.Builder alert = new AlertDialog.Builder(ProjectsActivity.this, R.style.AlertDialogTheme);

                                    alert.setTitle("Are you sure you want to delete this block?");
                                    alert.setMessage("Formation will be lost if you delete");


                                    alert.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int whichButton) {
                                            proj.getBlocking().removeBlock(pos);

                                            setAdapterAndUpdateData();
                                            if (proj.getBlocking().getBlocks().size() == 0) {
                                                Button playAnimation = (Button) findViewById(R.id.play_animation);
                                                playAnimation.setVisibility(View.INVISIBLE);
                                            }


                                        }
                                    });
                                    alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int whichButton) {
                                            // Cancele

                                        }
                                    });

                                    AlertDialog alertDialog = alert.create();

                                    // show it
                                    alertDialog.show();
                                    break;
                                case (R.id.rename):
                                        alert = new AlertDialog.Builder(ProjectsActivity.this, R.style.AlertDialogTheme);
                                        alert.setTitle("Edit Block Name");
                                        alert.setMessage("Enter new block name");

// Set an EditText view to get user input
                                        final EditText input = new EditText(ProjectsActivity.this);
                                        alert.setView(input);

                                        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int whichButton) {
                                                proj.getBlocking().getBlocks().get(pos).setTitle(input.getText().toString());
                                                setAdapterAndUpdateData();
                                            }
                                        });

                                        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int whichButton) {
                                                // Canceled.
                                            }
                                        });
                                       alertDialog = alert.create();

                                        // show it
                                        alertDialog.show();
                                        break;



                                case (R.id.change_duration):

                                    LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
                                   final  View customView = inflater.inflate(R.layout.change_start_end_time, null);
                                    EditText endTimeInput = (EditText) customView.findViewById(R.id.end_time_input);
                                    EditText startTimeInput = (EditText) customView.findViewById(R.id.start_time_input);
                                    Block block = proj.getBlocking().getBlocks().get(pos);

                                    String time = convertSecToString(Math.round(block.getStartTime()));
                                    startTimeInput.setHint(time);
                                    String timeEnd = convertSecToString(Math.round(block.getEndTime()));
                                    endTimeInput.setHint(timeEnd    );



                                    alert = new AlertDialog.Builder(ProjectsActivity.this, R.style.AlertDialogTheme);

                                    alert.setTitle("Update Duration");
                                    alert.setMessage("Enter start and end times");

                                    alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int whichButton) {
                                            Block block = proj.getBlocking().getBlocks().get(pos);
                                            EditText endTimeInput = (EditText) customView.findViewById(R.id.end_time_input);
                                            EditText startTimeInput = (EditText) customView.findViewById(R.id.start_time_input);

                                            if (startTimeInput.getText().length() != 0) {

                                                int startTime = convertStringtoSec(startTimeInput.getText().toString());
                                                if (startTime > block.getStartTime()) {
                                                    int diff = Math.round(startTime - block.getStartTime());
                                                    for (int i = 0; i < diff; i++) {
                                                        proj.getBlocking().incrTimeStart(block);
                                                        if (block.getType().equals(Block.TRANSITION)) {
                                                            ((TransitionBlock) block).setDuration((block.getEndTime() - block.getStartTime()) * 1000);
                                                        }
                                                    }



                                                    setAdapterAndUpdateData();
                                                } else {
                                                    int diff = Math.round(block.getStartTime() - startTime);
                                                    for (int i = 0; i < diff; i++) {
                                                        proj.getBlocking().decrStart(block);
                                                        if (block.getType().equals(Block.TRANSITION)) {
                                                            ((TransitionBlock) block).setDuration((block.getEndTime() - block.getStartTime()) * 1000);
                                                        }
                                                    }

                                                    setAdapterAndUpdateData();
                                                }
                                            }


                                            if (endTimeInput.getText().length() != 0) {
                                                int endTime = convertStringtoSec(endTimeInput.getText().toString());

                                                if (endTime > block.getEndTime()) {
                                                    int diff = Math.round(endTime - block.getEndTime());
                                                    for (int i = 0; i < diff; i++) {
                                                        proj.getBlocking().incrTimeEnd(block);
                                                        if (block.getType().equals(Block.TRANSITION)) {
                                                            ((TransitionBlock) block).setDuration((block.getEndTime() - block.getStartTime()) * 1000);
                                                        }
                                                    }

                                                    setAdapterAndUpdateData();
                                                } else {
                                                    int diff = Math.round(block.getEndTime() - endTime);
                                                    for (int i = 0; i < diff; i++) {
                                                        proj.getBlocking().decrEnd(block);
                                                        if (block.getType().equals(Block.TRANSITION)) {
                                                            ((TransitionBlock) block).setDuration((block.getEndTime() - block.getStartTime()) * 1000);
                                                        }
                                                    }

                                                    setAdapterAndUpdateData();
                                                }
//
                                            }
                                        }
                                    });

                                    alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int whichButton) {
                                            // Canceled.
                                        }
                                    });
                                    alert.setView(customView);

                                    alertDialog = alert.create();

                                    // show it
                                    alertDialog.show();
                                    break;

                            }
                            return true;
                        }

                    });


                    popup.show();//s


                }
            });
            rv.setAdapter(mAdapter);

        }
    }

    public String convertSecToString(int seconds) {
        String str = "";
        float minutes = Math.floorDiv(seconds, new Long(60));
        int sec = seconds % 60;
        str = String.valueOf(Math.round(minutes)) + ":";
        if (sec < 10) {
            str += "0" + String.valueOf(sec);
        } else {
            str +=  String.valueOf(sec);

        }
        return str;

    }

    public int convertStringtoSec(String str) {
        String[] start = str.split(":");
        int startTime = 0;
        if (start.length > 0) {
            if (start.length == 2) {
                if (start[0].length() > 0) {
                    startTime += Integer.valueOf(start[0]) * 60;
                }
                startTime += Integer.valueOf(start[1]);
            } else {
                if (start[0].length() > 0) {

                    startTime += Integer.valueOf(start[0]);
                }
            }
            return startTime;
        }
        return -1;
    }
    public void forceHorizontal() {
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

    }



}
