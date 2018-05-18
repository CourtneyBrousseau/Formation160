package edu.berkeley.hci.formation;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.io.File;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

// Adapter for the recycler view in CommentFeedActivity. You do not need to modify this file
public class ProjectAdapter extends RecyclerView.Adapter {

    private Context mContext;
    private ArrayList<Project> mProjects;

    public ProjectAdapter(Context context, Collection<Project> projects) {
        mContext = context;
        mProjects = new ArrayList<Project>(projects);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // here, we specify what kind of view each cell should have. In our case, all of them will have a view
        // made from comment_cell_layout
        View view = LayoutInflater.from(mContext).inflate(R.layout.project_cell_layout, parent, false);
        return new ProjectViewHolder(mContext, view);
    }


    // - get element from your dataset at this position
    // - replace the contents of the view with that element
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        // here, we the comment that should be displayed at index `position` in our recylcer view
        // everytime the recycler view is refreshed, this method is called getItemCount() times (because
        // it needs to recreate every cell).
        Project project = mProjects.get(position);
        ((ProjectViewHolder) holder).bind(project);
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mProjects.size();
    }
}

class ProjectViewHolder extends RecyclerView.ViewHolder {

    // each data item is just a string in this case
    Context c;
    TextView mProjectNameView;
    Project proj;

    public ProjectViewHolder(Context context, View itemView) {
        super(itemView);
        c = context;
        mProjectNameView = itemView.findViewById(R.id.project_name_text_view);

        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent launchProject = new Intent(v.getContext(), ProjectDetailActivity.class);
                launchProject.putExtra("project", proj);
                v.getContext().startActivity(launchProject);
//                Intent launchProject = new Intent(v.getContext(), ProjectsActivity.class);
//                launchProject.putExtra("project", proj);
//                v.getContext().startActivity(launchProject);
            }
        });
    }

    void bind(Project project) {
        proj = project;
        mProjectNameView.setText(proj.getTitle());
    }
}
