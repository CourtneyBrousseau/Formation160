package edu.berkeley.hci.formation;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class CreateProjectNameActivity extends AppCompatActivity {
    Project newProj;
    String currUserUUID;
    String projectUUID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_project_name);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle("Add Project Name");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        newProj = getIntent().getParcelableExtra("project");
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            currUserUUID = extras.getString("currUserUUID");
        }

        projectUUID = newProj.getUuid();

        final EditText projectNameEditText = findViewById(R.id.projectName);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String projectName = projectNameEditText.getText().toString();
                if (projectName.equals("")) {
                    TextInputLayout tilProjectName = (TextInputLayout) findViewById(R.id.tilProjectName);
                    tilProjectName.setError("Enter a Project Name");
                }
                else {
                    Intent launchCreateProject = new Intent(CreateProjectNameActivity.this, ProjectDetailActivity.class);

                    newProj.setTitle(projectName);
                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                    DatabaseReference permsRef = database.getReference("Permissions");
                    permsRef.child(currUserUUID).child(projectUUID).setValue(true);
                    DatabaseReference projectsRef = database.getReference("Projects");
                    projectsRef.child(projectUUID).setValue(newProj);

                    launchCreateProject.putExtra("project", newProj);
                    startActivity(launchCreateProject);
                }
            }
        });
    }

}
