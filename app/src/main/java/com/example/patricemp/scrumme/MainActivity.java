package com.example.patricemp.scrumme;

import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;

import com.firebase.ui.auth.AuthUI;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity
        implements MainActivityFragment.OnTaskClickListener{

    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mSprintDatabaseReference;
    private DatabaseReference mSprintStatusDatabaseReference;
    private FirebaseAuth mFirebaseAuth;
    private MainActivityFragment mFragment;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private ValueEventListener mSprintInProgressListener;
    private ValueEventListener mSprintStatusListener;
    private Sprint mSprint;
    private boolean mSprintInProgress;
    private Long mCurrentSprint;
    public static final int RC_SIGN_IN = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mFirebaseAuth = FirebaseAuth.getInstance();
        Button sprintButton = findViewById(R.id.button_sprint);
        sprintButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mSprint != null){
                    if(!mSprintInProgress){
                        mSprintStatusDatabaseReference.child("sprintInProgress").setValue(true);
                        String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
                        //set sprint start and end dates in Sprint
                    }else{ //ending sprint
                        mSprintDatabaseReference.removeEventListener(mSprintInProgressListener);
                        mSprintStatusDatabaseReference.child("sprintInProgress").setValue(false);
                        mCurrentSprint = mCurrentSprint + 1;
                        mSprintStatusDatabaseReference.child("currentSprint").setValue(mCurrentSprint);
                        mSprint = null;
                    }
                }
            }
        });


        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if(user != null){
                    //signed in
                    mFragment = (MainActivityFragment) getSupportFragmentManager().findFragmentById(R.id.fragment);
                    if(mFragment == null){
                        mFragment = new MainActivityFragment();
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.fragment, mFragment)
                                .commit();
                    }
                    String uid = user.getUid();
                    getSprintStatus(uid);

                } else{
                    //not signed in
                    List<AuthUI.IdpConfig> providers = Arrays.asList(
                            new AuthUI.IdpConfig.Builder(AuthUI.EMAIL_PROVIDER).build(),
                            new AuthUI.IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER).build());

                    // Create and launch sign-in intent
                    startActivityForResult(
                            AuthUI.getInstance()
                                    .createSignInIntentBuilder()
                                    .setAvailableProviders(providers)
                                    .build(),
                            RC_SIGN_IN);
                }
            }
        };
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
        if (id == R.id.action_importance) {
            Bundle bundle = new Bundle();
            bundle.putString("orderBy", "importance");
            MainActivityFragment fragment = new MainActivityFragment();
            fragment.setArguments(bundle);
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment, fragment).commit();
            return true;
        }
        if(id == R.id.action_dateAdded){
            //default is to order by date added
            MainActivityFragment fragment = new MainActivityFragment();
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment, fragment).commit();
            return true;
        }
        if(id == R.id.action_inSprint){
            Bundle bundle = new Bundle();
            bundle.putString("orderBy", "inSprint");
            MainActivityFragment fragment = new MainActivityFragment();
            fragment.setArguments(bundle);
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment, fragment).commit();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void getSprintStatus(final String uid){

        mSprintStatusDatabaseReference = mFirebaseDatabase.getReference()
                                                          .child("users")
                                                          .child(uid)
                                                          .child("sprint_status");
        mSprintDatabaseReference = mFirebaseDatabase.getReference()
                                                    .child("users")
                                                    .child(uid)
                                                    .child("sprints");

        mSprintStatusListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    if(dataSnapshot.hasChild("sprintInProgress")){
                        mSprintInProgress = (Boolean) dataSnapshot.child("sprintInProgress").getValue();
                        updateSprintUI();
                    }
                    if(dataSnapshot.hasChild("currentSprint")){
                        mCurrentSprint = (Long) dataSnapshot.child("currentSprint").getValue();
                        updateSprintUI();
                    }
                }else{
                    mSprintStatusDatabaseReference.child("sprintInProgress").setValue(false);
                    mSprintStatusDatabaseReference.child("currentSprint").setValue(0);
                    setUpSprintListener(uid, "0");
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        mSprintStatusDatabaseReference.addValueEventListener(mSprintStatusListener);
    }

    private void setUpSprintListener(String uid, @Nullable String sprintNum){
        mSprintDatabaseReference = mFirebaseDatabase.getReference()
                .child("users")
                .child(uid)
                .child("sprints");

        if(sprintNum != null && !sprintNum.isEmpty()){
            mSprintDatabaseReference = mSprintDatabaseReference.child(sprintNum);
        }else {
            mSprintDatabaseReference = mSprintDatabaseReference.child(Long.toString(mCurrentSprint));
        }
        mSprintInProgressListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot != null){
                    mSprint = dataSnapshot.getValue(Sprint.class);
                    updateSprintUI();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };


    }

    private void updateSprintUI(){
        TextView sprintPoints = findViewById(R.id.tv_sprint_points);
        TextView sprintNum = findViewById(R.id.tv_sprint_number);
        if(mSprint != null){
            if(mSprint.getStartingEffortPoints() >= 0){
                String startingPoints = Integer.toString(mSprint.getStartingEffortPoints());
                sprintPoints.setText(startingPoints);
            }
        }else{
            sprintPoints.setText("N/A");
        }
        if(mCurrentSprint != null){
            String num = Long.toString(mCurrentSprint);
            sprintNum.setText(num);
        }
    }

    @Override
    public void OnTaskSelected(Task task) {
        Intent intent = new Intent(this, AddTaskActivity.class);
        intent.putExtra("task", task);
        startActivity(intent);
    }


    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        //getSupportFragmentManager().beginTransaction().remove(mFragment).commit();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(mAuthStateListener != null){
            mFirebaseAuth.addAuthStateListener(mAuthStateListener);
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        if(mAuthStateListener != null){
            mFirebaseAuth.removeAuthStateListener(mAuthStateListener);
        }
    }
}
