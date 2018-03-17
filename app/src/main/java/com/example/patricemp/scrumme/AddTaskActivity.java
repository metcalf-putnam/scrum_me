package com.example.patricemp.scrumme;

import android.content.Intent;
import android.os.PersistableBundle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class AddTaskActivity extends AppCompatActivity {
    @BindView(R.id.button_submit) Button submitButton;
    @BindView(R.id.editText_description) EditText descriptionIn;
    @BindView(R.id.editText_notes) EditText notesIn;
    @BindView(R.id.spinner_effort) Spinner effortIn;
    @BindView(R.id.spinner_importance) Spinner importanceIn;
    @BindView(R.id.switch_in_sprint) Switch inSprintSwitch;
    @BindView(R.id.tv_task_header) TextView header;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mTasksDatabaseReference;
    private FirebaseAuth mFirebaseAuth;
    private boolean newTask = true;
    private Task mTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task);
        ButterKnife.bind(this);

        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mFirebaseAuth = FirebaseAuth.getInstance();

        FirebaseUser user = mFirebaseAuth.getCurrentUser();
        String uid = user.getUid();

        mTasksDatabaseReference = mFirebaseDatabase.getReference()
                .child("users")
                .child(uid)
                .child("tasks");

        Intent intent = getIntent();
        importanceIn.setSelection(3); //sets default importance value to "not important"
        if(savedInstanceState != null){
            Task task = savedInstanceState.getParcelable("task");
            if(task != null){
                fillOutData(task);
            }
        } else if(intent != null){
            if(intent.hasExtra("task")){
                mTask = intent.getParcelableExtra("task");
                newTask = false;
                fillOutData(mTask);
                updateLabels();
            }
        }

        submitButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                getTask();
                Intent intent = new Intent(getBaseContext(), MainActivity.class);
                intent.putExtra("task", mTask);
                if(newTask == true){
                    mTasksDatabaseReference.push().setValue(mTask);
                }else{
                    mTasksDatabaseReference.child(mTask.getDatabaseKey()).setValue(mTask);
                }
                startActivity(intent);
            }
        });
    }

    private void getTask(){
        if (mTask == null){
            mTask = new Task();
        }
        mTask.setDescription(descriptionIn.getText().toString());
        mTask.setEffort(Integer.parseInt(effortIn.getSelectedItem().toString()));
        mTask.setImportance(importanceIn.getSelectedItemPosition());
        mTask.setInSprint(inSprintSwitch.isChecked());
        mTask.setNotes(notesIn.getText().toString());
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        outState.putParcelable("task", mTask);
    }

    private void fillOutData(Task task){
        int importancePosition = task.getImportance();
        List<String> effortArray = Arrays.asList(getResources()
                .getStringArray(R.array.task_effort));
        int effortPosition = effortArray.indexOf(Integer.toString(task.getEffort()));

        descriptionIn.setText(task.getDescription());
        notesIn.setText(task.getNotes());
        effortIn.setSelection(effortPosition);
        importanceIn.setSelection(importancePosition);
        inSprintSwitch.setChecked(task.getInSprint());
    }

    private void updateLabels(){
        if(newTask==false){
            submitButton.setText(R.string.update);
            header.setText(R.string.modify_task_header);
        }
    }
}
