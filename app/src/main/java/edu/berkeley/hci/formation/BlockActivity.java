package edu.berkeley.hci.formation;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Path;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;

public class BlockActivity extends AppCompatActivity implements  UsersAdapter.ContactsAdapterListener {
    ClickView artView;
    Block block;
    Button btn;
    Project proj;
    int isEdit;
    String title;
    String [] users;
    int[] user_images;
    RecyclerView recyclerView;
    ArrayList<User> userList;
    UsersAdapter mAdapter;
    Dot currDot;
    ArrayList<String> tagged;
    HashMap<String, User> allUserMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_block);
        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
       // this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        Bundle bundle = getIntent().getExtras();
        proj = (Project) bundle.getParcelable("project");

        tagged = new ArrayList<>();
        LinearLayout v = (LinearLayout) findViewById(R.id.draw);
        allUserMap = new HashMap<>();
        userList = new ArrayList<>();
        artView = new ClickView(this, null);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        v.addView(artView);
        artView.setCanvasVars();
        users = new String[] {
                "User1",
                "User2",
                "User3"
        };

        int [] user_images={
                R.drawable.album7,
                R.drawable.album8,
                R.drawable.album9
        };
        ArrayList<Block> blocks = proj.getBlocking().getBlocks();
        try {
            //See if this is an already existing block
            Integer index = bundle.getInt("block_to_edit");
            block = blocks.get(index);

            title = block.getTitle();
            isEdit = index;
            //Add the dots to the bitmap
            for (Dot d : block.getDots()) {
                artView.addXY(d.x, d.y, d.color);
                if (!d.userid.equals("")) {
                    tagUser(d, d.userid, true);
                }
            }

        } catch (NullPointerException | IndexOutOfBoundsException e) {
            //If this is a new block create a new block object
            isEdit = -1;
            if (blocks.size() > 0) {
                //Pass in the last added block as the beforeBlock
                block = new StaticBlock(blocks.get(blocks.size() - 1));
                for (Dot d : block.getDots()) {
                    artView.addXY(d.x, d.y, d.color);
                    if (!d.userid.equals("")) {
                        tagUser(d, d.userid, true);
                    }
                }
            } else {
                block = new StaticBlock((Block) null);
            }
        }

        setTitle("");
        TextView title = findViewById(R.id.title);
        title.setText(block.getTitle());

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.notes);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                save();
                Intent intent = new Intent(BlockActivity.this, NotesActivity.class);
                forceHorizontal();
                intent.putExtra("project", proj);

                intent.putExtra("block", proj.getBlocking().getBlockByUid(block.getUid()));
                startActivity(intent);
            }
        });

        Button back = findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder alert = new AlertDialog.Builder(BlockActivity.this, R.style.AlertDialogTheme);
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
                        Intent launchBlocking = new Intent(BlockActivity.this, ProjectsActivity.class);
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
        //Make block title editable
//        final Button blockTitle = (Button) findViewById(R.id.block_title);
//        blockTitle.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                AlertDialog.Builder alert = new AlertDialog.Builder(BlockActivity.this, R.style.Theme_AppCompat_Light_Dialog);
//                alert.setTitle("Edit Block Name");
//                alert.setMessage("Enter new block name");
//
//// Set an EditText view to get user input
//                final EditText input = new EditText(BlockActivity.this);
//                alert.setView(input);
//
//                alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialog, int whichButton) {
//                        blockTitle.setText(input.getText().toString());
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





        //Set the save button
        Button saveButton = (Button) findViewById(R.id.saveButton);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                save();
            }
        });
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        final View user_recycler = inflater.inflate(R.layout.list_search_activity_main, null);
        recyclerView = user_recycler.findViewById(R.id.recycler_view);
        fetchContacts();


    }

    @Override
    public void onContactSelected(User contact) {
        proj.addSharedUserUUID(contact.getUUID());
        tagUser(currDot, contact.displayName, true);

    }

    public void addDot(Dot dot) {
        dot.setUID(String.valueOf(block.getDots ().size() + 1));
        block.addDot(dot);
    }

    public void changeDotColor(Dot dot, Integer color) {
        ArrayList<Dot> dots = block.getDots();
        for (Dot d : dots) {
            if (d.equals(dot)) {
                d.setColor(color);
            }
        }

    }

    public Dot getDot(float x, float y) {
        for (Dot d : this.block.getDots()) {
            if (d.x - 30 < x && d.x + 30 > x && d.y - 30 < y && d.y + 30 > y) {
                return d;
            }
        }
        return null;
    }

    public void showPopup(Dot dot, float x, float y) {
        //Create a button in the location of the dot so the popup can show up in the right place
        CoordinatorLayout v = (CoordinatorLayout) findViewById(R.id.main);
        btn = new Button(this);
        btn.setBackgroundColor(0);
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(100, 100); // Button width and button height.
        lp.leftMargin = Math.round(x); // your X coordinate.
        lp.topMargin = Math.round(y);
        v.addView(btn, lp);
        final Dot dot1 = dot;
        PopupMenu popup = new PopupMenu(BlockActivity.this, btn);
        //Inflating the Popup using xml file
        if (!dot.userid.equals("")) {
            popup.getMenuInflater().inflate(R.menu.remove_tag_menu, popup.getMenu());

        } else {
            popup.getMenuInflater().inflate(R.menu.dot_options, popup.getMenu());
        }

        //registering popup with OnMenuItemClickListener
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case (R.id.color_dot):
                        showPopupColor(dot1, dot1.x, dot1.y);
                        break;
                    case (R.id.remove_dancer):
                        removeDot(dot1);
                        break;
                    case (R.id.tag_dancer):
                        tag(dot1);

                        break;
                    case (R.id.untag):
                        untag(dot1);
                        break;


                }
                return true;

            }
        });
        popup.show();//showing popup menu
    }

    public void tagUser(Dot dot, String s, Boolean draw) {
        if (!dot.userid.equals("")){
            artView.removeText(dot.x, dot.y, dot.userid);
        }
        if (!tagged.contains(s)) {
            if (draw) {
                artView.tagDancer(dot.x, dot.y, s);
            }
            dot.tag(s);
            tagged.add(s);
        }

    }

    public void  untag(Dot dot) {
        artView.removeText(dot.x, dot.y, dot.userid);
        tagged.remove(dot.userid);
        dot.tag("");

    }
    public void tag(Dot dot) {


        currDot = dot;
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        final View user_recycler = inflater.inflate(R.layout.pop_up_user_list, null);
        recyclerView = user_recycler.findViewById(R.id.recycler_view);
         userList = new ArrayList<>();
        updateContacts();
        mAdapter = new UsersAdapter(this, userList, this);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new MyDividerItemDecoration(this, DividerItemDecoration.VERTICAL, 36));
        recyclerView.setAdapter(mAdapter);

        AlertDialog.Builder alert = new AlertDialog.Builder(BlockActivity.this, R.style.AlertDialogTheme);


        alert.setTitle("Tag Dancer");

        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {

            }
        });

        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Canceled.
            }
        });


        AlertDialog alertDialog = alert.create();
        alertDialog.setView(user_recycler);

        // show it
        alertDialog.show();


    }
    public void setAdapterAndUpdateData() {
        mAdapter = new UsersAdapter(this, userList, this);
        recyclerView.setAdapter(mAdapter);

    }

    private void fetchContacts() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference("Users");
        ref.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                User u = dataSnapshot.getValue(User.class);
                allUserMap.put(dataSnapshot.getKey(), u);

                if (proj.getSharedUserUUIDs().contains(u.getUUID()) && !tagged.contains(u.displayName)) {
                    userList.add(u);
                }
                setAdapterAndUpdateData();

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



    public void showPopupColor(Dot dot, float x, float y) {
        CoordinatorLayout v = (CoordinatorLayout) findViewById(R.id.main);
        btn = new Button(this);
        btn.setBackgroundColor(0);
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(100, 100); // Button width and button height.
        lp.leftMargin = Math.round(x); // your X coordinate.
        lp.topMargin = Math.round(y);
        v.addView(btn, lp);
        final Dot dot1 = dot;
        final PopupMenu popup = new PopupMenu(BlockActivity.this, btn);
        //Inflating the Popup using xml file
        popup.getMenuInflater().inflate(R.menu.color_popup_menu, popup.getMenu());

        //registering popup with OnMenuItemClickListener
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case (R.id.red):
                        artView.changeDotColor(dot1, Color.RED);
                        popup.dismiss();
                        break;
                    case (R.id.blue):
                        artView.changeDotColor(dot1, Color.BLUE);
                        break;
                    case (R.id.green):
                        artView.changeDotColor(dot1, Color.GREEN);
                        break;
                }
                return true;

            }
        });
        System.out.println("Show");
        popup.show();//showing popup menu
        //closing the setOnClickListener method
    }

    public void removeDot(Dot dot) {
        block.removeDot(dot);
        artView.removeDot(dot.x, dot.y);
        artView.removeText(dot.x, dot.y, dot.userid);
    }

    public void setblockTitle(Block block) {
        if (this.title != null) {
            block.setTitle((String) this.title);
        }

    }



    public byte[] compressBitmap() {
        Bitmap imageBitmap = Bitmap.createScaledBitmap(artView.mBitmap, 64, 64, false);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        imageBitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] imageData = baos.toByteArray();
        return imageData;
    }

    public void save() {
        byte[] imageData = compressBitmap();
        block.setThumbnail(imageData);
        setblockTitle(block);
        //If this is a not a previously added block being editing then add it to the project
        if (isEdit == -1) {
            proj.addBlock(block);
        }
        for (Block b : proj.getBlocking().getBlocks()){
            if (b.getType().equals("Static")) {
                for (Dot d : b.getDots()) {
                    for (Dot currDot : block.getDots()) {
                        if (d.getUid().equals(currDot.getUid()) && !d.userid.equals("")) {
                            tagUser(currDot, d.userid, false);
                        }
                        if (d.getUid().equals(currDot.getUid()) && !currDot.userid.equals("")) {
                            tagUser(d, currDot.userid, false);
                        }
                    }
                }
            }
        }


        createAutomaticTransition();
        Intent launchBlocking = new Intent(BlockActivity.this, ProjectsActivity.class);
        forceHorizontal();
        launchBlocking.putExtra("project", (Parcelable) proj);
        startActivity(launchBlocking);
    }

    public void createAutomaticTransition() {
        ArrayList<Block> blocks = proj.getBlocking().getBlocks();
        if (blocks.size() > 1
                && blocks.get(blocks.size() - 1).getType().equals(Block.STATIC)
                && blocks.get(blocks.size() - 2).getType().equals(Block.STATIC)) {
            int startIndex = proj.getBlocking().getBlocks().size() - 2;
            int endIndex = proj.getBlocking().getBlocks().size() - 1;
            StaticBlock start = (StaticBlock) proj.getBlocking().getBlocks().get(startIndex);
            StaticBlock end = (StaticBlock) proj.getBlocking().getBlocks().get(endIndex);


            TransitionBlock tb = new TransitionBlock(start);
            tb.addEndBlock(end);
            tb.makePaths();
            HashMap<String, Path> tbPaths = tb.getPaths();

            ArrayList<Button> buttons = new ArrayList<>();
            ArrayList<Path> pathsToAdd = new ArrayList<>();
            for (int i = 0; i < start.getDots().size(); i++) {
                pathsToAdd.add(tbPaths.get(start.getDots().get(i).getUid()));
                Button btn = new Button(this);
                btn.setWidth(10);
                btn.setHeight(10);
                buttons.add(btn);
            }
            tb.fixEndTime();

            ((StaticBlock) block).setStartTime(tb.getEndTime());
            for (int i = 0; i < tb.defaultLength; i++) {
                block.incrEnd();
            }

            TransitionView tv = new TransitionView(this, buttons, pathsToAdd);
            tv.setCanvasVars();
            tv.draw();
            Bitmap bitmap = tv.getBitmap();
            Bitmap imageBitmap = Bitmap.createScaledBitmap(bitmap, 64, 64, false);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            imageBitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
            byte[] imageData = baos.toByteArray();
            tb.setThumbnail(imageData);

            proj.addBlock(tb, startIndex + 1);


        } else {
            int thisIndex = proj.getBlocking().getBlockByUid(block.getUid());
            if (thisIndex > 0 && proj.getBlocking().getBlocks().get(thisIndex - 1).getType().equals(Block.TRANSITION)) {
                TransitionBlock transBlock = (TransitionBlock) proj.getBlocking().getBlocks().get(thisIndex - 1);
                transBlock.fixEndTime();
                ((StaticBlock) block).setStartTime(transBlock.getEndTime());
                for (int i = 0; i < transBlock.defaultLength; i++) {
                    block.incrEnd();
                }
                transBlock = updateTransition(transBlock, true);
                proj.setBlock(transBlock, thisIndex - 1);

            }
            if (thisIndex < proj.getBlocking().getBlocks().size() - 1 && proj.getBlocking().getBlocks().get(thisIndex + 1).getType().equals(Block.TRANSITION)) {
                TransitionBlock transBlock = (TransitionBlock) proj.getBlocking().getBlocks().get(thisIndex + 1);
                transBlock.fixEndTime();
                ((StaticBlock) block).setStartTime(transBlock.getEndTime());
                for (int i = 0; i < transBlock.defaultLength; i++) {
                    block.incrEnd();
                }
                transBlock = updateTransition(transBlock, false);
                proj.setBlock(transBlock, thisIndex + 1);

            }

        }

    }

    public TransitionBlock updateTransition(TransitionBlock transBlock, Boolean before) {
        ArrayList<Button> buttons = new ArrayList<>();
        ArrayList<Path> pathsToAdd = new ArrayList<>();
        HashMap<String, Path> tbPaths = transBlock.getPaths();

        for (int i = 0; i < block.getDots().size(); i++) {
            Dot d = block.getDots().get(i);
            Path path = tbPaths.get(d.getUid());
            if (path == null) {
                path = new Path();
                if (before) {
                    Integer nextIndex = proj.getBlocking().getBlockByUid(block.getUid()) - 2;
                    Block next = proj.getBlocking().getBlocks().get(nextIndex);
                    if (next.getDots().size() > Integer.valueOf(d.getUid())) {
                        Dot nextDot = next.getDots().get(Integer.valueOf(d.getUid()));
                        path.moveTo(nextDot.getX(), nextDot.getY());
                        path.moveTo(d.getX(), d.getY());

                    }

                }
                if (proj.getBlocking().getBlocks().size() >= proj.getBlocking().getBlockByUid(block.getUid()) + 2) {
                    System.out.println("meow");
                    path.moveTo(d.getX(), d.getY());
                    Integer nextIndex = proj.getBlocking().getBlockByUid(block.getUid()) + 2;
                    Block next = proj.getBlocking().getBlocks().get(nextIndex);
                    if (next.getDots().size() > Integer.valueOf(d.getUid())) {
                        Dot nextDot = next.getDots().get(Integer.valueOf(d.getUid()));
                        path.lineTo(nextDot.getX(), nextDot.getY());
                    }
                }
                tbPaths.put(d.getUid(), path);

            }
            Button btn = new Button(this);
            btn.setWidth(10);
            btn.setHeight(10);
            buttons.add(btn);
            if (d.changed) {
                if (before) {
                    float[] approx = path.approximate(0);
                    path.reset();
                    path.moveTo(approx[1], approx[2]);
                    path.lineTo(approx[1], approx[2]);
                    path.lineTo(d.getX(), d.getY());
                    d.changed = false;
                } else {
                    float[] approx = path.approximate(0);
                    path.reset();
                    path.moveTo(d.getX(), d.getY());
                    path.lineTo(d.getX(), d.getY());
                    path.lineTo(approx[approx.length - 2], approx[approx.length - 1]);
                    d.changed = false;


                }
            }
            pathsToAdd.add(path);

        }
        TransitionView tv = new TransitionView(this, buttons, pathsToAdd);
        tv.setCanvasVars();
        tv.draw();
        Bitmap bitmap = tv.getBitmap();
        Bitmap imageBitmap = Bitmap.createScaledBitmap(bitmap, 64, 64, false);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        imageBitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] imageData = baos.toByteArray();
        transBlock.setThumbnail(imageData);
        return transBlock;


    }
    public void forceHorizontal() {
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

    }

    public void updateContacts() {
        for (User u : allUserMap.values()) {
            if (proj.getSharedUserUUIDs().contains(u.getUUID()) && !tagged.contains(u.displayName)) {
                userList.add(u);
            }
        }
        setAdapterAndUpdateData();

    }


}
