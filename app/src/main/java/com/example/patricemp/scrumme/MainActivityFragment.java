package com.example.patricemp.scrumme;

import android.content.Context;
import android.os.Parcelable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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
        implements TaskAdapter.TaskClickListener, TaskAdapter.DeleteListener,
        TaskAdapter.SprintListener{

    private LinearLayoutManager mLayoutManager;
    private TaskAdapter mAdapter;
    private Parcelable mListState;
    private OnTaskClickListener mCallback;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mTasksDatabaseReference;
    private ChildEventListener mChildEventListener;
    private Task mLastDeleted;
    private FirebaseAuth mFirebaseAuth;
    private String mOrderBy;


    public MainActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mFirebaseAuth = FirebaseAuth.getInstance();

        FirebaseUser user = mFirebaseAuth.getCurrentUser();
        String uid = user.getUid();

        mTasksDatabaseReference = mFirebaseDatabase.getReference()
                .child("users")
                .child(uid)
                .child("tasks");

        if(savedInstanceState != null){
            mListState = savedInstanceState.getParcelable("state");
            if(savedInstanceState.containsKey("lastRemoved")){
                mLastDeleted = savedInstanceState.getParcelable("lastRemoved");
            }
        }
        Bundle bundle = getArguments();
        if(bundle != null){
            mOrderBy = bundle.getString("orderBy");
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
        mAdapter = new TaskAdapter(this, this, this);
        mAdapter.clearTasks();
        mChildEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Task task = dataSnapshot.getValue(Task.class);
                String key = dataSnapshot.getKey();
                task.setDatabaseKey(key);
                mAdapter.addTask(task);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                Task task = dataSnapshot.getValue(Task.class);
                mAdapter.modifyTask(task);
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                Snackbar snacker = Snackbar.make(getActivity().findViewById(android.R.id.content),
                        R.string.removed_task, Snackbar.LENGTH_LONG);
                snacker.setAction(R.string.undo_remove, new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                mTasksDatabaseReference.push().setValue(mLastDeleted);
                            }
                        }
                );
                snacker.show();
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        };
        if(mOrderBy != null && !mOrderBy.isEmpty()){
            switch (mOrderBy){
                case "inSprint":
                    mTasksDatabaseReference
                            .orderByChild(mOrderBy)
                            .equalTo(true)
                            .addChildEventListener(mChildEventListener);
                    break;
                case "importance":
                    mTasksDatabaseReference
                            .orderByChild(mOrderBy)
                            .addChildEventListener(mChildEventListener);
                    break;

                default:
                    mTasksDatabaseReference
                            .orderByKey()
                            .addChildEventListener(mChildEventListener);
                    break;
            }


        }else{
            mTasksDatabaseReference.orderByKey().addChildEventListener(mChildEventListener);
        }
        tasksView.setAdapter(mAdapter);
        tasksView.setHasFixedSize(true);
        return rootView;
    }

    @Override
    public void onTaskClick(Task task, View view) {
        mCallback.OnTaskSelected(task);
    }

    @Override
    public void onDeleteClick(Task task) {
        mTasksDatabaseReference.child(task.getDatabaseKey()).removeValue();
        mAdapter.deleteTask(task);
        mLastDeleted = task;
    }

    @Override
    public void onSprintClick(Task task) {
        if(task.getInSprint()){
            task.setInSprint(false);
        }else{
            task.setInSprint(true);
        }
        mTasksDatabaseReference.child(task.getDatabaseKey()).setValue(task);
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
        outState.putParcelable("lastRemoved", mLastDeleted);
    }

}
