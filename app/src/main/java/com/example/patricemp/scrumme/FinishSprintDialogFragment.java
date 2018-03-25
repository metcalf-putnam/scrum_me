package com.example.patricemp.scrumme;

import android.app.DialogFragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Tegan on 3/24/2018.
 */

public class FinishSprintDialogFragment extends DialogFragment{
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.dialog_finish_sprint_fragment, container, false);
        getDialog().setTitle("Sprint Complete");

        Button close = rootView.findViewById(R.id.button_dialog_close);
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        Bundle bundle = getArguments();

        ArrayList<Task> taskList;
        if(bundle != null){
            if(bundle.containsKey("tasks")){
                taskList = bundle.getParcelableArrayList("tasks");
                if(bundle.containsKey("sprint")){
                    Sprint sprint = bundle.getParcelable("sprint");

                    taskList = cleanData(taskList);
                    taskList = sortData(taskList);
                    chartData(rootView, taskList, sprint);
                }
            }
        }

        return rootView;
    }
    private ArrayList<Task> cleanData(ArrayList<Task> tasks){
        if(tasks != null && tasks.size() > 0){
            Iterator<Task> iter = tasks.iterator();

            while (iter.hasNext()) {
                Task task = iter.next();
                if (!task.getCompleted())
                    iter.remove();
            }
        }
        return tasks;
    }
    private ArrayList<Task> sortData(ArrayList<Task> tasks){
        if(tasks != null && tasks.size() > 0){
            Collections.sort(tasks, new Comparator<Task>() {
                @Override
                public int compare(Task o1, Task o2) {
                    return o1.getDateCompleted().compareTo(o2.getDateCompleted());
                }
            });
        }
        return tasks;
    }

    private void chartData(View view, ArrayList<Task> tasks, Sprint sprint){
        LineChart chart = view.findViewById(R.id.lineChart);
        ArrayList<Entry> actual =  new ArrayList<>();
        ArrayList<Entry> planned = new ArrayList<>();
        List<ILineDataSet> dataSets = new ArrayList<>();

        //getting actual/completed values
        int completedCount = 0;
        if(tasks.size() > 0){
            for(Task task : tasks){
                if(task.getCompleted()) {
                    long date = task.getDateCompleted().getTime();
                    completedCount = completedCount + task.getEffort();
                    Entry entry = new Entry((float) date, (float) completedCount);
                    actual.add(entry);
                }
            }
            //setting data rigmarole for actual/completed
            LineDataSet setActual = new LineDataSet(actual, "Completed");
            dataSets.add(setActual);
        }


        //getting planned values
        int dataPoints = 10; //arbitrary
        float start = (float) sprint.getSprintStart().getTime();
        float end = (float) sprint.getSprintEnd().getTime();
        float totalTime = end - start;
        float timeInterval = totalTime/dataPoints;
        int startingEffort = sprint.getStartingEffortPoints();
        for(int i = 0 ; i < dataPoints ; i++){
            float date = start + i*timeInterval;
            float expectedComplete = date/totalTime*startingEffort;
            Entry entry = new Entry(date, expectedComplete);
            planned.add(entry);
        }

        //setting data rigmarole for planned
        LineDataSet setPlanned = new LineDataSet(planned, "Planned");
        dataSets.add(setPlanned);

        //charting
        LineData data = new LineData(dataSets);
        chart.setData(data);
        chart.invalidate(); //refresh
    }
}
