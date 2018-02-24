package com.example.patricemp.scrumme;

import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity
        implements MainActivityFragment.OnTaskClickListener,
        MainActivityFragment.TaskProvider{

    private ArrayList<Task> mTasksArray;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mTasksDatabaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mTasksDatabaseReference = mFirebaseDatabase.getReference().child("tasks");

        if(savedInstanceState != null){
            if(savedInstanceState.containsKey("tasks")){
                mTasksArray = savedInstanceState.getParcelableArrayList("tasks");
            }
        }

        ///replace later with Firebase calls
        if(mTasksArray == null){
            mTasksArray = new ArrayList<Task>(0);
        }
        Intent intent = getIntent();
        if(intent != null){
            if(intent.hasExtra("task")){
                Task task = intent.getParcelableExtra("task");
                mTasksArray.add(task);
                MainActivityFragment fragment = new MainActivityFragment();
                android.support.v4.app.FragmentManager manager = getSupportFragmentManager();
                manager.beginTransaction()
                        .add(R.id.fragment, fragment)
                        .commit();
            }
        }

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getBaseContext(), AddTaskActivity.class);
                startActivity(intent);
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
            }
        });


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void OnTaskSelected(Task task) {

    }

    @Override
    public ArrayList<Task> getTasks() {
        return mTasksArray;
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        outState.putParcelableArrayList("tasks", mTasksArray);
    }
}
