package edu.berkeley.hci.formation;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class VideoPlaybackActivity extends AppCompatActivity {
    private static final String LOG_TAG = "VideoPlaybackActivity";

    public static final String VIDEO_ID = "videoId";

    //video playback
    private Project proj;
    private VideoView videoPlaybackView;
    private String videoID;
    private MediaController mediaController;
    private Runnable runnable;
    private Handler handler = new Handler();

    //Toolbar
    private Toolbar toolbar;
    private Button publishButton;
    private Button backButton;

    //to know which view to show them
    private boolean isChoreographer = true;

    //comment entry and publication
    private EditText commentInputBox;
    private RelativeLayout commentLayout;
    private ImageView commentPostButton;
    private TextView publishCommentsTextView;
    private HashMap<Integer, String> comments;

    //Firebase
    private DatabaseReference commentsRef;
    private DatabaseReference videoCommentsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //forceLandscape();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_playback);
        Bundle bundle = getIntent().getExtras();
        proj = bundle.getParcelable(ProjectDetailActivity.PROJECT_ID);
        videoID = bundle.getString(VIDEO_ID);

        videoPlaybackView = (VideoView) findViewById(R.id.videoPlaybackView);
        commentInputBox = (EditText) findViewById(R.id.comment_input_edit_text);
        commentLayout = (RelativeLayout) findViewById(R.id.comment_layout);
        commentPostButton = (ImageView) findViewById(R.id.comment_publish_button);
        publishCommentsTextView = (TextView) findViewById(R.id.publish_comments_textview);
        publishButton = (Button) findViewById(R.id.publish_comments);
        backButton = (Button) findViewById(R.id.back);
        comments = new HashMap<>();

        //Firebase Initialization
        commentsRef = FirebaseUtil.getCommentsRef();
        videoCommentsRef = commentsRef.child(videoID);
        ValueEventListener myDataListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                updateComments(dataSnapshot);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d("0", "cancelled");
            }
        };
        videoCommentsRef.addValueEventListener(myDataListener);

        //hide irrelevant views
        setVisibility();

        // TODO: Use toolbar back button instead of relaunching the activity

        FirebaseUtil.getVideoStorageRef().child(videoID).getDownloadUrl().addOnSuccessListener(
                new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        //logic to set up the video + controls
                        setVideoPlayback(uri);
                        //logic to post comments
                        setOnClickForPostButton();
                        //logic to listen to video and post comments when appropriate
                        listenToVideo();
                        //set on click listener for publish comments button
                        publishComments();
                        //set on click for back button
                        setBackButton();
                    }
                }
        );
    }

    private void updateComments(DataSnapshot dataSnapshot){
        for (DataSnapshot commentDs: dataSnapshot.getChildren()) {
            String newComment = commentDs.getValue(String.class);
            comments.put(Integer.parseInt(commentDs.getKey()), newComment);
        }
    }

    private void putInComments(HashMap<String, String> hashMap) {
        for (String key: hashMap.keySet()){
            comments.put(Integer.parseInt(key),hashMap.get(key));
        }
    }

    private void setVisibility() {
        if (!isChoreographer) {
            commentLayout.setVisibility(View.GONE);
        }
    }

    private void setVideoPlayback(Uri videoUri) {
        videoPlaybackView.setVideoURI(videoUri);
        mediaController = new MediaController(this);
        mediaController.setAnchorView(videoPlaybackView);
        videoPlaybackView.setMediaController(mediaController);
        videoPlaybackView.start();
    }

    private void setOnClickForPostButton() {
        commentPostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String comment = commentInputBox.getText().toString();
                if (TextUtils.isEmpty(comment)) {
                    commentInputBox.requestFocus();
                } else {
                    addComment(new VideoComment(comment, videoPlaybackView.getCurrentPosition()));
                }
            }
        });
    }

    private void addComment(VideoComment videoComment) {
        commentInputBox.setText("");
        String comment = videoComment.getComment();
        comments.put(videoComment.getPosition(), comment);
        publishCommentsTextView.setText(comment);
        videoCommentsRef
                .child(Integer.toString(videoComment.getPosition())).setValue(comment);
    }
    private void publishComments(){
        publishButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //In future add push notifications for dancers
                Toast.makeText(VideoPlaybackActivity.this, "Your comments have been published", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void listenToVideo(){
        //Logic to add listener for the comment times
        runnable = new Runnable(){
            int lastComment = 0;
            public void run() {
                Log.i(LOG_TAG, "::getting current position and adding appropriate comments");
                handler.postDelayed(runnable, 0000);
                if(videoPlaybackView.isPlaying()) {
                    int currPosition = videoPlaybackView.getCurrentPosition();
                    String currComment = comments.get(currPosition);
                    if (currPosition - lastComment >= TimeUnit.SECONDS.toMillis(10)){
                        publishCommentsTextView.setText("");
                        lastComment = currPosition;
                    }
                    if (currComment != null) {
                        publishCommentsTextView.setText(currComment);
                        lastComment = currPosition;
                    }
                }
            }
        };
        handler.postDelayed(runnable, 0000);
    }

    private void forceLandscape(){
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
    }

    private void setBackButton() {
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent launchProjectDetailView = new Intent(VideoPlaybackActivity.this, ProjectDetailActivity.class);
                launchProjectDetailView.putExtra("project", proj);
                startActivity(launchProjectDetailView);
            }
        });
    }

}
