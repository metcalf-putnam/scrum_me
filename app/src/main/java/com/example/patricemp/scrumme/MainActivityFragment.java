package com.example.patricemp.scrumme;

import android.content.Context;
import android.content.Intent;
import android.os.Parcelable;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
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
        TaskAdapter.SprintListener, TaskAdapter.CompletedListener{

    private LinearLayoutManager mLayoutManager;
    private TaskAdapter mAdapter;
    private Parcelable mListState;
    private onTaskClickListener mCallback;
    private checkInSprint mInSprintCallback;
    private getSprint mGetSprintCallback;
    private getSprintNum mGetSprintNum;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mTasksDatabaseReference;
    private ChildEventListener mChildEventListener;
    private Task mLastDeleted;
    private int mLastDeletedPosition;
    private FirebaseAuth mFirebaseAuth;
    private String mOrderBy;
    private DatabaseReference mSprintDatabaseReference;


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

        mSprintDatabaseReference = mFirebaseDatabase.getReference()
                .child("users")
                .child(uid)
                .child("sprints");

        if(savedInstanceState != null){
            mListState = savedInstanceState.getParcelable("state");
            if(savedInstanceState.containsKey("lastRemoved")){
                mLastDeleted = savedInstanceState.getParcelable("lastRemoved");
                mLastDeletedPosition = savedInstanceState.getInt("lastDeletedPosition");
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
        mAdapter = new TaskAdapter(this, this, this, this);
        mAdapter.clearTasks();
        mChildEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Task task = dataSnapshot.getValue(Task.class);
                String key = dataSnapshot.getKey();
                task.setDatabaseKey(key);

                if(mLastDeleted != null && key.matches(mLastDeleted.getDatabaseKey())){
                    mAdapter.addTask(task, mLastDeletedPosition);
                }else if(task.getSprintNum()==0){
                    mAdapter.addTask(task);
                }
                updateSprint();
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                Task task = dataSnapshot.getValue(Task.class);
                mAdapter.modifyTask(task);
                updateSprint();
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

                if(getActivity() != null){
                    Snackbar snacker = Snackbar.make(getActivity().findViewById(R.id.cl_main),
                            R.string.removed_task, Snackbar.LENGTH_LONG);
                    snacker.setAction(R.string.undo_remove, new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    if(mOrderBy != null && mOrderBy.matches("inSprint")){
                                        mLastDeleted.setInSprint(true);
                                    }
                                    mTasksDatabaseReference
                                            .child(mLastDeleted.getDatabaseKey())
                                            .setValue(mLastDeleted);
                                }
                            }
                    );
                    snacker.show();
                    updateSprint();
                }

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
        mCallback.onTaskSelected(task);
    }

    @Override
    public void onDeleteClick(Task task) {
        if(getActivity() != null){
            mTasksDatabaseReference.child(task.getDatabaseKey()).removeValue();
            mLastDeleted = task;
            mLastDeletedPosition = mAdapter.getPosition(task);
            mAdapter.deleteTask(task);
        }
    }

    @Override
    public void onSprintClick(Task task) {
        if(getActivity() != null){
            if(task.getInSprint()){
                if(mOrderBy != null && mOrderBy.matches("inSprint")){
                    mLastDeleted = task;
                    mLastDeletedPosition = mAdapter.getPosition(task);
                    mAdapter.deleteTask(task);
                }
                task.setInSprint(false);
            }else{
                task.setInSprint(true);
            }
            mTasksDatabaseReference.child(task.getDatabaseKey()).setValue(task);
        }

    }

    private void updateSprint(){
        if(getActivity() != null){
            Long sprintNum = mGetSprintNum.getSprintNum();
            if(sprintNum != null){
                Sprint sprint = mGetSprintCallback.currentSprint();
                if(sprint == null){
                    sprint = new Sprint();
                }
                sprint.setCurrentEffortPoints(mAdapter.countSprintPoints());
                mSprintDatabaseReference.child(Long.toString(sprintNum)).setValue(sprint);
            }
        }
    }

    @Override
    public void onCompleteClick(Task task) {
        if(getActivity() != null){
            if(task.getCompleted()){ //if was previously marked complete
                task.setCompleted(false);
                task.setSprintNum(0);
            }else if(mInSprintCallback.isInSprint()){
                task.setCompleted(true);
                Long sprint = mGetSprintNum.getSprintNum();
                task.setSprintNum(sprint.intValue());
                task.setInSprint(true);
            }else{
                Toast.makeText(getContext(), "Start sprint before completing tasks",
                        Toast.LENGTH_SHORT).show();
            }
            mTasksDatabaseReference.child(task.getDatabaseKey()).setValue(task);
        }
    }

    public interface onTaskClickListener{
        void onTaskSelected(Task task);
    }

    public interface checkInSprint {
        boolean isInSprint();
    }

    public interface getSprint{
        Sprint currentSprint();
    }

    public interface getSprintNum{
        Long getSprintNum();
    }

    public ArrayList<Task> getTaskList(){
        return mAdapter.getTaskList();
    }



    @Override
    public void onAttach(Context context){
        super.onAttach(context);
        try{
            mCallback = (onTaskClickListener) context;
            mInSprintCallback = (checkInSprint) context;
            mGetSprintCallback = (getSprint) context;
            mGetSprintNum = (getSprintNum) context;
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable("state", mListState);
        outState.putParcelable("lastRemoved", mLastDeleted);
        outState.putInt("lastRemovedPosition", mLastDeletedPosition);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        FloatingActionButton fab = (FloatingActionButton) getActivity().findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(getActivity().getBaseContext(), AddTaskActivity.class);
                    startActivity(intent);
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
                }
        });
    }
}
