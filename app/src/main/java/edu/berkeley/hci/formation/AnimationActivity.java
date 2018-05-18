package edu.berkeley.hci.formation;

import android.animation.ObjectAnimator;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Path;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import android.os.CountDownTimer;
import android.os.Parcelable;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.util.ArrayList;
import java.util.HashMap;

import static java.lang.Math.max;
import static java.lang.Math.min;

public class AnimationActivity extends AppCompatActivity {
    Project proj;
    Blocking blocking;
    ArrayList<ObjectAnimator> animationObj;
    Integer currIndex;
    ArrayList<Button> buttons;
    ConstraintLayout animationLayout;
    Integer timeSoFar;
    HashMap<Integer, Block> blockTimes;
    boolean pause;

    ProgressBar seek;
    CountDownTimer cdt;
    TextView startTime;
    Integer progress = 0;
    boolean translated;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_animation);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle("");
        blockTimes = new HashMap<>();
        pause = false;
//        setBackButton(toolbar);
        translated = false;
        Bundle bundle = getIntent().getExtras();
        this.proj = bundle.getParcelable("project");
        blocking = proj.getBlocking();
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        final ToggleButton play_button = (ToggleButton) findViewById(R.id.play_button);
        play_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!play_button.isChecked()) {
                    if (!pause) {
                        play();
                    } else {
                        makeCountDown();
                        cdt.start();
                        for (ObjectAnimator animation : animationObj) {

                            animation.resume();
                            pause = false;
                        }
                    }

                } else {
                    pause();
                }
            }
        });
        timeSoFar = 0;
        startTime = (TextView) findViewById(R.id.start_time);
        buttons = new ArrayList<>();
        animationLayout = (ConstraintLayout) findViewById(R.id.animation_layout);
        animationObj = new ArrayList<>();
        TextView endTime = (TextView) findViewById(R.id.end_time);
        Block lastBlock = blocking.getBlocks().get(blocking.getBlocks().size() - 1);
        long minutes = Math.floorDiv( lastBlock.getEndTime(), new Long(60));
        long seconds =  lastBlock.getEndTime() % 60;
        String time = String.valueOf(minutes) + ":" + String.valueOf(seconds);
        endTime.setText(String.valueOf(time));
        seek  = (ProgressBar) findViewById(R.id.progress_bar);
        seek.setMax(Math.round(lastBlock.getEndTime()));
        seek.setProgress(0, true);

        System.out.println(lastBlock.getEndTime());
         makeCountDown();


        Button back = findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent launchBlocking = new Intent(AnimationActivity.this, ProjectsActivity.class);
                launchBlocking.putExtra("project", (Parcelable) proj);
                startActivity(launchBlocking);

            }
        });
    }

    public void makeCountDown() {
        Block lastBlock = blocking.getBlocks().get(blocking.getBlocks().size() - 1);
        cdt = new CountDownTimer(Math.round(lastBlock.getEndTime() - progress) * 1000, 1000) {

            public void onTick(long millisUntilFinished) {
                Block currBlock = blockTimes.get(progress * 1000);
                if (currBlock != null) {
                    for (int i = 0; i < currBlock.getDots().size(); i++) {
                        if (currBlock.getType().equals(Block.STATIC)) {
                            setButtonColor(buttons.get(i), currBlock.getDots().get(i));
                        }
                    }
                    if (buttons.size() > currBlock.getDots().size()) {
                        for (int i = currBlock.getDots().size(); i < buttons.size(); i++) {
                            Dot d = new Dot();
                            d.setColor(Color.WHITE);
                            setButtonColor(buttons.get(i), d);

                        }
                    }
                }
                progress += 1;
                seek.setProgress(progress, true);
                String curr = startTime.getText().toString();
                String[] split = curr.split(":");
                if (split[1].equals("59")){
                    startTime.setText(String.valueOf(Integer.valueOf(split[0]) + 1) + ":00");
                } if (Integer.valueOf(split[1]) < 9) {
                    startTime.setText(split[0] + ":0" + String.valueOf(Integer.valueOf(split[1]) + 1));

                } else {

                    startTime.setText(split[0] + ":" + String.valueOf(Integer.valueOf(split[1]) + 1));

                }


            }

            public void onFinish() {

            }
        };
    }

    public void pause() {
        for (ObjectAnimator animation : animationObj) {
            animation.pause();
            pause = true;
        }
        cdt.cancel();
    }




    public void play()  {
        timeSoFar = 0;
        int max = 0;
        Block maxBlock = null;
        for (Block b : blocking.getBlocks()) {
            if (b.getDots().size() > max){
                max = b.getDots().size();
                maxBlock = b;

            }
        }
        if (buttons.size() != max) {
//            for (int i = 0; i < max; i++) {
//                makeButton(maxBlock.getDots().get(i));
//            }
        }

        cdt.start();

        for (int i = 0; i < blocking.getBlocks().size(); i++) {
            Block block = blocking.getBlocks().get(i);

            if (block.getType().equals(Block.STATIC)) {
                buttons = makeStaticAnimation(block);

            } else {
                buttons = makeTransitionAnimation(block);

            }
            for (ObjectAnimator ani : animationObj) {
                ani.start();
            }

        }

    }

    public ArrayList<Button> makeStaticAnimation(Block block) {
        int duration = (int) ((block.getEndTime() - block.getStartTime()) * 1000);
        for (int i = 0; i < block.getDots().size(); i++) {
            Dot d = block.getDots().get(i);
            Path path = new Path();
            path.moveTo(d.getX(), d.getY());
            path.lineTo(d.getX(), d.getY());
            Matrix translate = new Matrix();
            translate.setTranslate(-80, -40);

            path.transform(translate);
            if (i >= buttons.size()){
               makeButton(d);

            }

            ObjectAnimator animation = ObjectAnimator.ofFloat(buttons.get(i), View.X, View.Y, path).setDuration(duration);
            animation.setStartDelay(timeSoFar);
            blockTimes.put(timeSoFar, block);
            animationObj.add(animation);


        }
        timeSoFar += duration;
        return buttons;
    }

    public ArrayList<Button> makeTransitionAnimation(Block block) {
        int duration = (int) ((block.getEndTime() - block.getStartTime()) * 1000);

        HashMap<String, Path> pathHashMap = ((TransitionBlock) block).getPaths();
        for (int i = 0; i < ((TransitionBlock) block).getDotsBefore().size(); i++) {
            Dot d = ((TransitionBlock) block).getDotsBefore().get(i);
            Path path = pathHashMap.get(d.getUid());
            if (i > buttons.size()){
                makeButton(d);

            }
            Matrix translate = new Matrix();
            translate.setTranslate(-80, -40);

            path.transform(translate);
            ObjectAnimator animation = ObjectAnimator.ofFloat(buttons.get(i), View.X, View.Y, path).setDuration(duration);
            animation.setStartDelay(timeSoFar);
            animationObj.add(animation);
        }
        blockTimes.put(timeSoFar, block);

        timeSoFar += duration;

        return buttons;
    }


    public void setButtonColor(Button btn, Dot dot){
        btn.setBackgroundColor(Color.TRANSPARENT);
        switch (dot.color) {
            case (Color.BLACK):
                // btn.setBackground(getDrawable(R.drawable.black_dot));
                btn.setCompoundDrawablesRelativeWithIntrinsicBounds(null, getDrawable(R.drawable.black_dot), null, null );

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

    }
    public Button makeButton(Dot dot){
        Button btn = new Button(this);


        btn.setBackgroundColor(Color.TRANSPARENT);



        btn.setText(dot.userid);
        btn.setTextColor(Color.BLACK);

        animationLayout.addView(btn);

        btn.getLayoutParams().width = 200;
        btn.getLayoutParams().height = 150;
        btn.setX(dot.getX());
        btn.setY(dot.getY());
        buttons.add(btn);
        return btn;
    }

    public void removeButtons(ArrayList<Button> buttons) {
        for (Button b : buttons) {
            animationLayout.removeView(b);
        }
    }

//    public void setBackButton(Toolbar mToolbar) {
//        //Set logo to back arrow
//        mToolbar.setLogo(R.drawable.ic_arrow_back_white_18dp);
//        mToolbar.setTitleMarginStart(100);
//
//        boolean hadContentDescription = android.text.TextUtils.isEmpty(mToolbar.getLogoDescription());
//        String contentDescription = String.valueOf(!hadContentDescription ? mToolbar.getLogoDescription() : "logoContentDescription");
//        mToolbar.setLogoDescription(contentDescription);
//        ArrayList<View> potentialViews = new ArrayList<View>();
//        //find the view based on it's content description, set programatically or with android:contentDescription
//        mToolbar.findViewsWithText(potentialViews, contentDescription, View.FIND_VIEWS_WITH_CONTENT_DESCRIPTION);
//        //Nav icon is always instantiated at this point because calling setLogoDescription ensures its existence
//        View logoIcon = null;
//        if (potentialViews.size() > 0) {
//            logoIcon = potentialViews.get(0);
//        }
//        //Clear content description if not previously present
//        if (hadContentDescription)
//            mToolbar.setLogoDescription(null);
//        logoIcon.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(AnimationActivity.this, ProjectsActivity.class);
//                intent.putExtra("project", proj);
//                startActivity(intent);
//            }
//
//        });
//    }


}
