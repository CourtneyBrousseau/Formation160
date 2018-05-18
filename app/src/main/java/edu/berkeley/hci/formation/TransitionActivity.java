package edu.berkeley.hci.formation;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Path;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;

public class TransitionActivity extends AppCompatActivity {
    Button btn;
    Project proj;
    StaticBlock start;
    StaticBlock end;
    ArrayList<Button> buttons;
    ArrayList<Path> paths;
    HashMap<String, Path> pathsHash;
    String title;
    TransitionBlock block;
    Integer isEdit;
    Integer startIndex;
    Integer endIndex;
    TransitionView sv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transition);
        Toolbar toolbar = (Toolbar) findViewById(R.id.transition_toolbar);
        setSupportActionBar(toolbar);
        buttons = new ArrayList<>();
        paths = new ArrayList<>();
        isEdit = -1;
        Bundle bundle = getIntent().getExtras();
        proj = (Project) bundle.getParcelable("project");

        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        LinearLayout v = (LinearLayout) findViewById(R.id.draw);
//


        try {
            Integer index = bundle.getInt("block_to_edit");
            block = (TransitionBlock) proj.getBlocking().getBlocks().get(index);
            isEdit = index;
            startIndex = index - 1;
            start = (StaticBlock) proj.getBlocking().getBlocks().get(index - 1);


            float[] approx = ((Path) block.getPaths().get("1")).approximate(0);


        } catch (RuntimeException  e) {
            startIndex = bundle.getInt("block_to_start");
            endIndex = bundle.getInt("block_to_end");

            boolean isNew = bundle.getBoolean("Creating new");
            if (isNew || proj.getBlocking().getBlocks().size() == endIndex) {
                start = (StaticBlock) proj.getBlocking().getBlocks().get(startIndex);
                block = new TransitionBlock(start);
                block.makePaths();

            } else {
                endIndex = bundle.getInt("block_to_end");
                start = (StaticBlock) proj.getBlocking().getBlocks().get(startIndex);
                end = (StaticBlock) proj.getBlocking().getBlocks().get(endIndex);
                block = new TransitionBlock(start);
                block.addEndBlock(end);
                block.makePaths();
            }
        }
        this.pathsHash = block.getPaths();
        for (Dot d : start.getDots()) {
            makeButton(d);
        }


        sv = new TransitionView(this, this.buttons, paths);

        sv.setDuration(block.duration);
        v.addView(sv, 0, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, 50));
        Button saveButton = (Button) findViewById(R.id.saveTransition);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                save();
            }
        });
        setTitle("");
        TextView title = findViewById(R.id.title);
        title.setText(block.getTitle());
        Button clear = (Button) findViewById(R.id.clear_button);
        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<Path> currPaths = sv.getPaths();
                ArrayList<Path> newPaths = new ArrayList<>();
                for (int i = 0; i < currPaths.size(); i++) {
                    Path p = currPaths.get(i);

                    float[] approx = p.approximate(0);
                    Path newPath = new Path();
                    newPath.moveTo(approx[1], approx[2]);
                    newPath.lineTo(approx[1], approx[2]);
                    newPaths.add(newPath);

                }
                sv.setPaths(newPaths);
            }
        });
        FloatingActionButton play = (FloatingActionButton) findViewById(R.id.play_fab);
        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                sv.initAnimation();


            }
        });
//        final Button transitionTitle = (Button) findViewById(R.id.transition_title);
//        transitionTitle.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                AlertDialog.Builder alert = new AlertDialog.Builder(TransitionActivity.this, R.style.Theme_AppCompat_Light_Dialog);
//                alert.setTitle("Edit Transition Name");
//                alert.setMessage("Enter new transition name");
//
//// Set an EditText view to get user input
//                final EditText input = new EditText(TransitionActivity.this);
//                alert.setView(input);
//
//                alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialog, int whichButton) {
//                        transitionTitle.setText(input.getText().toString());
//                        block.setTitle(transitionTitle.getText().toString());
//                    }
//                });
//
//                alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialog, int whichButton) {
//                        // Canceled.
//                    }
//                });
//                AlertDialog alertDialog = alert.create();
//
//                // show it
//                alertDialog.show();
//            }
//        });

        Button back = findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "hello", Toast.LENGTH_LONG);
                AlertDialog.Builder alert = new AlertDialog.Builder(TransitionActivity.this, R.style.AlertDialogTheme);
                alert.setTitle("Are you sure you want to go back without saving?");
                alert.setMessage("Changes will not be saved");

                //Confirmation to save
                alert.setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        save();
                    }
                });
                //Back without saving option
                alert.setNegativeButton("Back without saving", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        Intent launchBlocking = new Intent(TransitionActivity.this, ProjectsActivity.class);
                        forceHorizontal();
                        launchBlocking.putExtra("project", (Parcelable) proj);
                        startActivity(launchBlocking);
                    }
                });

                AlertDialog alertDialog = alert.create();

                // show it
                alertDialog.show();
            }
        });

    }

    public void makeButton(Dot dot) {
        LinearLayout v = (LinearLayout) findViewById(R.id.draw);
        v.setOrientation(LinearLayout.HORIZONTAL);

        Button btn = new Button(this);

        switch (dot.color) {
            case (Color.BLACK):
               // btn.setBackground(getDrawable(R.drawable.black_dot));
                Drawable dot_draw = getDrawable(R.drawable.black_dot);


               // dot_draw.setBounds(30,30 ,80, 80);
                btn.setCompoundDrawablesWithIntrinsicBounds(null,dot_draw , null, null );

                break;
            case (Color.BLUE):
                btn.setCompoundDrawablesRelativeWithIntrinsicBounds(null, getDrawable(R.drawable.blue_dot), null, null );
                break;
            case (Color.GREEN):
                btn.setCompoundDrawablesRelativeWithIntrinsicBounds(null, getDrawable(R.drawable.green_dot), null, null );
                break;
            case (Color.RED):
                btn.setCompoundDrawablesRelativeWithIntrinsicBounds(null, getDrawable(R.drawable.red_dot), null, null );


                break;


        }
        btn.setBackgroundColor(Color.TRANSPARENT);



        btn.setText(dot.userid);
        btn.setTextColor(Color.BLACK);

        paths.add(this.pathsHash.get(dot.getUid()));
        v.addView(btn);
        btn.getLayoutParams().width = 50;
        btn.getLayoutParams().height = 150;
        int x = Math.round(dot.getX());
        int y = Math.round(dot.getY());

        ((LinearLayout.LayoutParams) btn.getLayoutParams()).setMarginStart(x+50);

        buttons.add(btn);

    }



    public void setTransitionTitle(Block block) {
        if (this.title != null) {
            block.setTitle((String) this.title);
        }

    }

    public byte[] compressBitmap() {
        Bitmap imageBitmap = Bitmap.createScaledBitmap(sv.getBitmap(), 64, 64, false);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        imageBitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] imageData = baos.toByteArray();
        return imageData;
    }

    public void save() {
        byte[] imageData = compressBitmap();
        block.setThumbnail(imageData);
        setTransitionTitle(block);
        ArrayList<Path> svPaths = sv.getPaths();
        ArrayList<Dot> finalPositions = new ArrayList<>();
        for (int i = 0; i < svPaths.size(); i++) {
            block.updatePath(String.valueOf(i + 1), svPaths.get(i));
            Path p = svPaths.get(i);
            float[] approx = p.approximate(0);
            Dot dot = new Dot();
            dot.setCoordinates(approx[approx.length - 2], approx[approx.length - 1]);
            Button btn = sv.getButtons().get(i);
            Drawable d = btn.getCompoundDrawables()[1];
            Bitmap bitmap = ((BitmapDrawable) d).getBitmap();
            Bitmap bitmapRed = ((BitmapDrawable) getDrawable(R.drawable.red_dot)).getBitmap();
            Bitmap bitmapGreen = ((BitmapDrawable) getDrawable(R.drawable.green_dot)).getBitmap();
            Bitmap bitmapBlue = ((BitmapDrawable) getDrawable(R.drawable.blue_dot)).getBitmap();
            Bitmap bitmapBlack = ((BitmapDrawable) getDrawable(R.drawable.black_dot)).getBitmap();

            if (bitmap == bitmapRed) {
                dot.setColor(Color.RED);
            }
            if (bitmap == bitmapBlack) {
                dot.setColor(Color.BLACK);
            }
            if (bitmap == bitmapBlue) {
                dot.setColor(Color.BLUE);
            }
            if (bitmap == bitmapGreen) {
                dot.setColor(Color.GREEN);
            }
            dot.setUID(String.valueOf(i + 1 ));
            finalPositions.add(dot);
        }
        block.setDotsAfter(finalPositions);
        //If this is a not a previously added block being editing then add it to the project
        if (isEdit == -1) {
            proj.addBlock(block, startIndex + 1);
        }

        ClickView artView = new ClickView(this, null);
        artView.setCanvasVars();
        StaticBlock sb = new StaticBlock(block);
        for (Dot d : block.getDots()) {
            artView.drawDotFromAfar(d);
        }

        for (Block b : proj.getBlocking().getBlocks()){
            if (b.getType().equals("Static")) {
                for (Dot d : b.getDots()) {
                    for (Dot currDot : block.getDots()) {
                        if (d.getUid().equals(currDot.getUid()) && !d.userid.equals("")) {
                            currDot.tag(d.userid);
                        }
                        if (d.getUid().equals(currDot.getUid()) && !currDot.userid.equals("")) {
                            d.tag(currDot.userid);
                        }
                    }
                }
            }
        }
        Bitmap bitmap = artView.getBitmap();
        Bitmap imageBitmap = Bitmap.createScaledBitmap(bitmap, 64, 64, false);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        imageBitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        imageData = baos.toByteArray();
        sb.setThumbnail(imageData);

        for (int i = 0; i < sb.getDots().size(); i ++){
            sb.getDots().get(i).setUID(String.valueOf(i + 1));
        }

        if (proj.getBlocking().getBlocks().size() > startIndex + 2) {
            proj.setBlock(sb, startIndex + 2);


        } else {
            proj.addBlock(sb, startIndex + 2);
        }



            Intent launchBlocking = new Intent(TransitionActivity.this, ProjectsActivity.class);
        forceHorizontal();
        launchBlocking.putExtra("project", (Parcelable) proj);
        startActivity(launchBlocking);
    }

    public void forceHorizontal() {
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

    }

}