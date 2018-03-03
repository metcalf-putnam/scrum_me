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
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment
        implements TaskAdapter.TaskClickListener{

    private LinearLayoutManager mLayoutManager;
    private TaskAdapter mAdapter;
    private Parcelable mListState;
    private OnTaskClickListener mCallback;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mTasksDatabaseReference;
    private ChildEventListener mChildEventListener;


    public MainActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mTasksDatabaseReference = mFirebaseDatabase.getReference().child("tasks");

        if(savedInstanceState != null){
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
        mAdapter = new TaskAdapter(this);
        mAdapter.clearTasks();
        mChildEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Task task = dataSnapshot.getValue(Task.class);
                mAdapter.addTask(task);
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
        };
        mTasksDatabaseReference.addChildEventListener(mChildEventListener);
        tasksView.setAdapter(mAdapter);
        tasksView.setHasFixedSize(true);
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
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable("state", mListState);
    }
}
