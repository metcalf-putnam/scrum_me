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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.lang.reflect.Array;

import butterknife.BindView;
import butterknife.ButterKnife;

public class AddTaskActivity extends AppCompatActivity {
    @BindView(R.id.button_submit) Button submitButton;
    @BindView(R.id.editText_description) EditText descriptionIn;
    @BindView(R.id.editText_notes) EditText notesIn;
    @BindView(R.id.spinner_effort) Spinner effortIn;
    @BindView(R.id.spinner_importance) Spinner importanceIn;
    @BindView(R.id.switch_in_sprint) Switch inSprintSwitch;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mTasksDatabaseReference;
    private FirebaseAuth mFirebaseAuth;

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
        if(intent != null){
            if(intent.hasExtra("task")){
                Task task = intent.getParcelableExtra("task");
            }

        }

        if(savedInstanceState != null){
            Task task = savedInstanceState.getParcelable("task");
            if(task != null){
                int effortPosition = savedInstanceState.getInt("effort_position");
                int importancePosition = savedInstanceState.getInt("importance_position");
                descriptionIn.setText(task.getDescription());
                notesIn.setText(task.getNotes());
                effortIn.setSelection(effortPosition);
                importanceIn.setSelection(importancePosition);
                inSprintSwitch.setChecked(task.getInSprint());
            }

        }
        submitButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Task task = getTask();
                Intent intent = new Intent(getBaseContext(), MainActivity.class);
                intent.putExtra("task", task); //if is modify, should modify, not push new
                mTasksDatabaseReference.push().setValue(task);
                startActivity(intent);
            }
        });
    }

    private Task getTask(){
        Task newTask = new Task();
        newTask.setDescription(descriptionIn.getText().toString());
        newTask.setEffort(Integer.parseInt(effortIn.getSelectedItem().toString()));
        newTask.setImportance(importanceIn.getSelectedItem().toString());
        newTask.setInSprint(inSprintSwitch.isChecked());
        newTask.setNotes(notesIn.getText().toString());
        return newTask;
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        outState.putParcelable("task", getTask());
        outState.putInt("effort_position", effortIn.getSelectedItemPosition());
        outState.putInt("importance_position", importanceIn.getSelectedItemPosition());
    }

    private void fillOutData(Task task){
        //Array stringArray = R.string.task_importance;
        descriptionIn.setText(task.getDescription());
        notesIn.setText(task.getNotes());
        //effortIn.setSelection(effortPosition);
        //importanceIn.setSelection(importancePosition);
        inSprintSwitch.setChecked(task.getInSprint());
    }
}
