package com.example.patricemp.scrumme;

import android.app.FragmentManager;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.os.PersistableBundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Layout;
import android.text.format.DateUtils;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

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

import java.lang.reflect.Array;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity
        implements MainActivityFragment.onTaskClickListener, MainActivityFragment.checkInSprint,
        MainActivityFragment.getSprint, MainActivityFragment.getSprintNum{

    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mSprintDatabaseReference;
    private DatabaseReference mSprintStatusDatabaseReference;
    private FirebaseAuth mFirebaseAuth;
    private MainActivityFragment mFragment;
    private boolean mFragmentAttached;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private ValueEventListener mSprintInProgressListener;
    private ValueEventListener mSprintStatusListener;
    private DatabaseReference mSprintAverageReference;
    private Sprint mSprint;
    private boolean mSprintInProgress;
    private Long mCurrentSprint;
    private String mUid;
    private int mAveragePoints;
    private ValueEventListener mAverageListener;
    public static final int RC_SIGN_IN = 1;
    private static final long WEEK = 3600*1000*168;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        setTheme(R.style.AppTheme_NoActionBar);
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
                if(mSprint != null && mSprint.getCurrentEffortPoints() > 0){
                    Date date = new Date();
                    if(!mSprintInProgress){ //starting new sprint
                        mSprintStatusDatabaseReference.child("sprintInProgress").setValue(true);
                        mSprint.setSprintNum(mCurrentSprint);
                        mSprint.setSprintStart(date);

                        //setting planned endDate for tracking purposes
                        Spinner spinner = findViewById(R.id.spinner_sprint_length);
                        int lengthSelection = spinner.getSelectedItemPosition();
                        String[] lengths = getResources().getStringArray(R.array.sprint_length_entries);
                        int numWeeks = Integer.parseInt(lengths[lengthSelection].substring(0,1));
                        Date endDate = new Date(date.getTime() + WEEK*numWeeks); //168 hours in a week
                        mSprint.setSprintEnd(endDate);

                        int points = mSprint.getCurrentEffortPoints();
                        mSprint.setStartingEffortPoints(points);
                        mSprintDatabaseReference.setValue(mSprint);
                        updateButton();
                    }else{ //ending sprint
                        mSprintDatabaseReference.removeEventListener(mSprintInProgressListener);
                        mSprintInProgressListener = null;

                        //update sprintValues
                        mCurrentSprint = mCurrentSprint + 1;
                        mSprintStatusDatabaseReference.child("sprintInProgress").setValue(false);
                        mSprintStatusDatabaseReference.child("currentSprint").setValue(mCurrentSprint);
                        mSprint.setSprintEnd(date);
                        mSprintDatabaseReference.setValue(mSprint);

                        //show dialog
                        ArrayList<Task> list = mFragment.getTaskList();
                        FragmentManager fm = getFragmentManager();
                        FinishSprintDialogFragment dialog = new FinishSprintDialogFragment();
                        Bundle bundle = new Bundle();
                        bundle.putParcelable("sprint", mSprint);
                        bundle.putParcelableArrayList("tasks", list);
                        dialog.setArguments(bundle);
                        dialog.setCancelable(true);
                        WindowManager.LayoutParams windowParams = new WindowManager.LayoutParams();
                        windowParams.copyFrom(getWindow().getAttributes());
                        windowParams.width = WindowManager.LayoutParams.FILL_PARENT; // this is where the magic happens
                        windowParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
                        dialog.show(fm, "Results");

                       //reset
                        mSprint = null;
                        resetTasks(mCurrentSprint-1, list);
                        newFragment("importance");
                    }
                }else{
                    Toast.makeText(getBaseContext(), "Can't start a sprint without tasks!",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });


        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if(user != null){
                    //signed in
                    mUid = user.getUid();
                    if(mFragment == null && !mFragmentAttached){
                        newFragment("importance");
                    }

                    if(mSprintStatusListener == null){
                        getSprintStatus();
                    }
                    if(mAveragePoints <= 0){
                        //fetch sprint data and find average points
                        getAverage();
                    }

                } else{
                    mUid = null;
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
    private void getAverage(){
        mSprintAverageReference = mFirebaseDatabase.getReference()
                .child("users")
                .child(mUid)
                .child("sprints");
        mSprintAverageReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot != null){
                    double totalPointsCompleted = 0;
                    double totalSprints = 0;
                    for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                        Sprint sprint = snapshot.getValue(Sprint.class);
                        if(sprint != null && sprint.getCompletedEffortPoints() > 0){
                            totalSprints+=1;
                            totalPointsCompleted+=sprint.getCompletedEffortPoints();
                        }
                    }
                    double average = totalPointsCompleted/totalSprints;
                    mAveragePoints = (int) average;
                    updateSprintUI();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

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
        if (id == R.id.action_importance) {
            newFragment("importance");
            return true;
        }
        if(id == R.id.action_dateAdded){
            newFragment(""); //default is to order by date added
            return true;
        }
        if(id == R.id.action_inSprint){
            newFragment("inSprint");
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    private void newFragment(String order){
        if(!isFinishing()) {
            Bundle bundle = new Bundle();
            bundle.putString("orderBy", order);
            mFragment = new MainActivityFragment();
            mFragment.setArguments(bundle);
            View view = findViewById(R.id.fragment);
            if(view != null){
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment, mFragment).commit();
                updateSprintUI();
            }

        }
    }

    private void resetTasks(Long sprintNum, ArrayList<Task> tasks){
        DatabaseReference currentReference = mFirebaseDatabase.getReference()
                .child("users")
                .child(mUid)
                .child("tasks");
        DatabaseReference archivedReference = mFirebaseDatabase.getReference()
                .child("users")
                .child(mUid)
                .child("task_archive");
        if(tasks != null){
            for(Task task : tasks){
                if(task.getCompleted()){
                    task.setSprintNum(sprintNum.intValue());
                    currentReference.child(task.getDatabaseKey()).removeValue(); //remove from active tasks
                    archivedReference.child(task.getDatabaseKey()).setValue(task); //add to archive
                }else if(task.getInSprint()){
                    task.setInSprint(false);
                    currentReference.child(task.getDatabaseKey()).setValue(task);
                }
            }
        }

        //if isCompleted, set sprintNum
        //if is not, remove inSprint value
    }

    private void updateButton(){
        Button sprintButton = findViewById(R.id.button_sprint);
        if(mSprint != null && mSprint.getCurrentEffortPoints() > 0){
            if(mSprintInProgress){
                sprintButton.setText(R.string.end_sprint);
            }else{
                sprintButton.setText(R.string.start_sprint);
            }
        }else{
            sprintButton.setText(R.string.plan_sprint);
        }

    }
    private void getSprintStatus(){

        mSprintStatusDatabaseReference = mFirebaseDatabase.getReference()
                                                          .child("users")
                                                          .child(mUid)
                                                          .child("sprint_status");

        mSprintStatusListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    if(dataSnapshot.hasChild("sprintInProgress")){
                        mSprintInProgress = (boolean) dataSnapshot.child("sprintInProgress").getValue();
                        updateSprintUI();
                        updateWidget();
                    }
                    if(dataSnapshot.hasChild("currentSprint")){
                        mCurrentSprint = (long) dataSnapshot.child("currentSprint").getValue();
                        if(mSprintInProgressListener != null){
                            mSprintDatabaseReference.removeEventListener(mSprintInProgressListener);
                        }
                        if(mCurrentSprint != null){
                            setUpSprintListener(Long.toString(mCurrentSprint));
                        }
                        updateSprintUI();
                    }
                }else{
                    mSprintStatusDatabaseReference.child("sprintInProgress").setValue(false);
                    mSprintStatusDatabaseReference.child("currentSprint").setValue(1);
                    setUpSprintListener("0");

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        mSprintStatusDatabaseReference.addValueEventListener(mSprintStatusListener);
    }

    private void setUpSprintListener(String sprintNum){
        mSprintDatabaseReference = mFirebaseDatabase.getReference()
                .child("users")
                .child(mUid)
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
                    if(mSprint != null && mSprint.getSprintStart() != null){
                        updateWidget();
                    }
                    updateSprintUI();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        mSprintDatabaseReference.addValueEventListener(mSprintInProgressListener);

    }

    private void updateWidget(){
        //update widget
        AppWidgetManager.getInstance(this);
        int[] ids = AppWidgetManager
                .getInstance(getApplication())
                .getAppWidgetIds(new ComponentName(getApplication(), ProgressWidget.class));
        ProgressWidget widget = new ProgressWidget();
        widget.onUpdate(this, AppWidgetManager.getInstance(this),ids);
    }

    private void updateSprintUI(){
        View planning = findViewById(R.id.sprint_planning);
        View inProgress = findViewById(R.id.sprint_in_progress);
        if(mSprintInProgress){
            planning.setVisibility(View.INVISIBLE);
            inProgress.setVisibility(View.VISIBLE);
            if(mSprint != null){
                //defining views to be changed
                TextView sprintNum = findViewById(R.id.tv_sprint_in_progress_number);
                TextView startDate = findViewById(R.id.tv_start_date);
                TextView endDate = findViewById(R.id.tv_end_date);
                TextView progressDescription = findViewById(R.id.tv_progress_description);
                TextView pointsRatio = findViewById(R.id.tv_points_ratio);
                ProgressBar progressBar = findViewById(R.id.progressBar);

                //fetching, formatting, and setting dates
                Date start = mSprint.getSprintStart();
                Date end = mSprint.getSprintEnd();
                if(start != null && end != null) {
                    DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT);
                    String startString = df.format(start);
                    String endString = df.format(end);
                    startDate.setText(startString);
                    endDate.setText(endString);
                    //finding expected progress

                    //setting ratio + progress bar
                    if (mCurrentSprint != null) {
                        sprintNum.setText(Long.toString(mCurrentSprint));
                    }
                    int currentEffortPoints = mSprint.getCurrentEffortPoints();
                    int completedEffortPoints = mSprint.getCompletedEffortPoints();
                    String ratio = "" + Integer.toString(completedEffortPoints)
                            + "/"
                            + Integer.toString(currentEffortPoints);
                    pointsRatio.setText(ratio);
                    progressBar.setMax(currentEffortPoints);
                    progressBar.setProgress(completedEffortPoints);

                    //secondary progress bar (expected progress)
                    long totalTime = end.getTime() - start.getTime();
                    Date currentDate = new Date();
                    long timeElapsed = currentDate.getTime() - start.getTime();
                    long expectedProgress = timeElapsed * currentEffortPoints / totalTime;
                    int expected = Integer.parseInt(Long.toString(expectedProgress));
                    progressBar.setSecondaryProgress(expected);
                    if(expected > completedEffortPoints){
                        progressDescription.setText(R.string.sprint_progress_bad);
                    } else if(completedEffortPoints > expected){
                        progressDescription.setText(R.string.sprint_progress_good);
                    }else {
                        progressDescription.setText(R.string.sprint_progress_on_track);
                    }
                }
            }

        }else{ //in planning stage
            inProgress.setVisibility(View.INVISIBLE);
            planning.setVisibility(View.VISIBLE);

            TextView sprintAverage = findViewById(R.id.tv_average_sprint_points);
            TextView sprintPoints = findViewById(R.id.tv_sprint_points);
            TextView sprintNum = findViewById(R.id.tv_sprint_number);
            if(mSprint != null){
                if(mSprint.getCurrentEffortPoints() >= 0){
                    String points = Integer.toString(mSprint.getCurrentEffortPoints());
                    sprintPoints.setText(points);
                }
            }else{
                sprintPoints.setText("N/A");
            }
            if(mCurrentSprint != null){
                String num = Long.toString(mCurrentSprint);
                sprintNum.setText(num);
            }
            if(mAveragePoints > 0){
                sprintAverage.setText(Integer.toString(mAveragePoints));
            }else{
                sprintAverage.setText("N/A");
            }
        }

        updateButton();
    }

    @Override
    public void onTaskSelected(Task task) {
        Intent intent = new Intent(this, AddTaskActivity.class);
        intent.putExtra("task", task);
        startActivity(intent);
    }


    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        //outState.putBoolean("fragment_exists", mFragmentAttached);
        if(mFragment != null){
            getSupportFragmentManager().beginTransaction().remove(mFragment).commit();
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        if(mAuthStateListener != null){
            mFirebaseAuth.addAuthStateListener(mAuthStateListener);
        }
        if(mSprintInProgressListener != null){
            mSprintDatabaseReference.addValueEventListener(mSprintInProgressListener);
        }
        if(mSprintStatusListener != null){
            mSprintStatusDatabaseReference.addValueEventListener(mSprintStatusListener);
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        if(mAuthStateListener != null){
            mFirebaseAuth.removeAuthStateListener(mAuthStateListener);
        }
        if(mSprintInProgressListener != null){
            mSprintDatabaseReference.removeEventListener(mSprintInProgressListener);
        }
        if(mSprintStatusListener != null){
            mSprintStatusDatabaseReference.removeEventListener(mSprintStatusListener);
        }
    }

    @Override
    public boolean isInSprint() {
        return mSprintInProgress;
    }

    @Override
    public Sprint currentSprint() {
        return mSprint;
    }

    @Override
    public Long getSprintNum() {
        return mCurrentSprint;
    }
}
