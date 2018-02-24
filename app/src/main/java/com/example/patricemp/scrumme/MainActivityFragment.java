package com.example.patricemp.scrumme;

import android.content.Context;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment
        implements TaskAdapter.TaskClickListener{

    private ArrayList<Task> mTasks;
    private LinearLayoutManager mLayoutManager;
    private Parcelable mListState;
    private OnTaskClickListener mCallback;

    public MainActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if(savedInstanceState != null){
            mTasks = savedInstanceState.getParcelableArrayList("tasks");
            mListState = savedInstanceState.getParcelable("state");
        }
        final View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        RecyclerView tasksView = rootView.findViewById(R.id.rv_tasks);
        if(tasksView.getLayoutManager() == null){
            mLayoutManager = new LinearLayoutManager(getActivity());
            tasksView.setLayoutManager(mLayoutManager);
        }
        if(mListState != null){
            mLayoutManager.onRestoreInstanceState(mListState);
        }
        TaskAdapter adapter = new TaskAdapter(this);
        tasksView.setAdapter(adapter);
        tasksView.setHasFixedSize(true);
//        mTasks = new ArrayList<Task>(0);
//        Task dummy = new Task();
//        dummy.setDescription("derp");
//        dummy.setEffort(1);
//        mTasks.add(dummy);
        if(mTasks != null){
            adapter.setTasks(mTasks);
        }



        return rootView;
    }

    @Override
    public void onTaskClick(Task task) {
        mCallback.OnTaskSelected(task);
    }

    public interface OnTaskClickListener{
        void OnTaskSelected(Task task);
    }

    @Override
    public void onAttach(Context context){
        super.onAttach(context);
        try{
            mCallback = (OnTaskClickListener) context;
            mTasks = ((TaskProvider) context).getTasks();

        } catch(Exception e){
            e.printStackTrace();
        }
    }

    public interface TaskProvider{
        ArrayList<Task> getTasks();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList("tasks", mTasks);
        outState.putParcelable("state", mListState);
    }
}
