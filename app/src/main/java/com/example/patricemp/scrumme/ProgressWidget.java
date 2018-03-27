package com.example.patricemp.scrumme;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.RemoteViews;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Date;

/**
 * Created by Tegan on 3/25/2018.
 */
//started from https://developer.android.com/guide/topics/appwidgets/index.html
public class ProgressWidget extends AppWidgetProvider {
    private boolean mSprintInProgress;
    private Sprint mSprint;
    private long mCurrentSprint;

    public void onUpdate(final Context context, final AppWidgetManager appWidgetManager, final int[] appWidgetIds) {

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser user = firebaseAuth.getCurrentUser();

        if(user != null) {
            //signed in
            String uid = user.getUid();
            DatabaseReference sprintStatusReference = FirebaseDatabase.getInstance().getReference()
                    .child("users")
                    .child(uid)
                    .child("sprint_status");

            final DatabaseReference sprintReference = FirebaseDatabase.getInstance().getReference()
                    .child("users")
                    .child(uid)
                    .child("sprints");

            final ValueEventListener currentSprintListener = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if(dataSnapshot != null) {
                        mSprint = dataSnapshot.getValue(Sprint.class);
                        updateWidgets(context, appWidgetManager, appWidgetIds);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            };

            ValueEventListener sprintStatusListener = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        if (dataSnapshot.hasChild("sprintInProgress")) {
                            mSprintInProgress = (boolean) dataSnapshot.child("sprintInProgress").getValue();
                            if (mCurrentSprint > 0 && mSprintInProgress) {
                                sprintReference
                                        .child(Long.toString(mCurrentSprint))
                                        .addListenerForSingleValueEvent(currentSprintListener);
                            }else {
                                updateWidgets(context, appWidgetManager, appWidgetIds);
                            }
                        }
                        if (dataSnapshot.hasChild("currentSprint")) {
                            mCurrentSprint = (long) dataSnapshot.child("currentSprint").getValue();
                            if (mCurrentSprint > 0 && mSprintInProgress) {
                                sprintReference
                                        .child(Long.toString(mCurrentSprint))
                                        .addListenerForSingleValueEvent(currentSprintListener);
                            }else{
                                updateWidgets(context, appWidgetManager, appWidgetIds);
                            }
                        }
                    }else{
                        updateWidgets(context, appWidgetManager, appWidgetIds);
                    }
                }
                @Override
                public void onCancelled (DatabaseError databaseError){
                }
            };
            sprintStatusReference.addListenerForSingleValueEvent(sprintStatusListener);
        }else{
            updateWidgets(context, appWidgetManager, appWidgetIds);
        }


    }

    public void updateWidgets(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds){
        final int N = appWidgetIds.length;
        // Perform this loop procedure for each App Widget that belongs to this provider
        for (int i=0; i<N; i++) {
            int appWidgetId = appWidgetIds[i];

            // Create an Intent to launch ExampleActivity
            Intent intent = new Intent(context, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

            // Get the layout for the App Widget and attach an on-click listener
            // to the button
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.progress_widget);
            views.setOnClickPendingIntent(R.id.frame_widget, pendingIntent);
            //views = updateUI(views);
            if(mCurrentSprint > 0){ //have data
                if(mSprintInProgress && mSprint != null){
                    int completed = mSprint.getCompletedEffortPoints();
                    int current = mSprint.getCurrentEffortPoints();
                    views.setProgressBar(R.id.widget_progress_bar, current, completed, false);
                    views.setViewVisibility(R.id.linear_layout_progress_holder, View.VISIBLE);
                    views.setViewVisibility(R.id.tv_widget_error, View.INVISIBLE);
                    String pointsRatio = "" + Integer.toString(completed) + "/" + Integer.toString(current);
                    views.setTextViewText(R.id.tv_widget_points, pointsRatio);
                    //views = checkIfOnTrack(views);
                    if(mSprint.getSprintStart() != null){
                        long start = mSprint.getSprintStart().getTime();
                        long end = mSprint.getSprintEnd().getTime();
                        long totalTime = end - start;
                        Date currentDate = new Date();
                        long timeElapsed = currentDate.getTime() - start;
                        long expectedProgress = timeElapsed * mSprint.getCurrentEffortPoints() / totalTime;
                        int expected = Integer.parseInt(Long.toString(expectedProgress));
                        int completedEffortPoints = mSprint.getCompletedEffortPoints();
                        if(expected > completedEffortPoints){
                            views.setTextViewText(R.id.tv_widget_progress_description, "behind :(");
                        }else if(completedEffortPoints > expected){
                            views.setTextViewText(R.id.tv_widget_progress_description, "Woo hoo!");
                        }else {
                            views.setTextViewText(R.id.tv_widget_progress_description, "on track");
                        }
                    }
                }else{
                    views.setViewVisibility(R.id.linear_layout_progress_holder, View.INVISIBLE);
                    views.setViewVisibility(R.id.tv_widget_error, View.VISIBLE);
                    views.setTextViewText(R.id.tv_widget_error, "no active sprint");
                }
            }else{
                views.setViewVisibility(R.id.linear_layout_progress_holder, View.INVISIBLE);
                views.setViewVisibility(R.id.tv_widget_error, View.VISIBLE);
                views.setTextViewText(R.id.tv_widget_error, "Oops! Please sign in");
            }

            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }


}
