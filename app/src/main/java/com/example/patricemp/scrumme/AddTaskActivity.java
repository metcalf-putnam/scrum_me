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

import butterknife.BindView;
import butterknife.ButterKnife;

public class AddTaskActivity extends AppCompatActivity {
    @BindView(R.id.button_submit) Button submitButton;
    @BindView(R.id.editText_description) EditText descriptionIn;
    @BindView(R.id.editText_notes) EditText notesIn;
    @BindView(R.id.spinner_effort) Spinner effortIn;
    @BindView(R.id.spinner_importance) Spinner importanceIn;
    @BindView(R.id.switch_in_sprint) Switch inSprintSwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task);
        ButterKnife.bind(this);
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
                intent.putExtra("task", task);
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
}
