package edu.berkeley.hci.formation;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;

public class NotesActivity extends AppCompatActivity {
    Project proj;
    String notes;
    Block block;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notes);
        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        Bundle bundle = getIntent().getExtras();
        proj = (Project) bundle.getParcelable("project");
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        int index = bundle.getInt("block");
        block = proj.getBlocking().getBlocks().get(index);
        notes = block.getNotes();
        setTitle("");
        EditText notes_edit = (EditText) findViewById(R.id.notes_edit);
        notes_edit.setText(notes);
        notes_edit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                block.setNotes(s.toString());

            }
        });

        Button back = findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                   Intent intent = new Intent(NotesActivity.this, BlockActivity.class);
                   intent.putExtra("project", proj);
                   startActivity(intent);
                }
            });

    }



}
