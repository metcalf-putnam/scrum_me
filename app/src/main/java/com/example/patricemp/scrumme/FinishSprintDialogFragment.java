package com.example.patricemp.scrumme;

import android.app.Dialog;
import android.app.DialogFragment;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by Tegan on 3/24/2018.
 */

public class FinishSprintDialogFragment extends DialogFragment{

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog d = getDialog();
        if(d!=null){
            int width = ViewGroup.LayoutParams.MATCH_PARENT;
            int height = ViewGroup.LayoutParams.MATCH_PARENT;
            if(d.getWindow() != null){
                d.getWindow().setLayout(width, height);
            }
        }
    }

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

        //testChart(rootView);
        ArrayList<Task> taskList;
        if(bundle != null){
            if(bundle.containsKey("tasks")){
                taskList = bundle.getParcelableArrayList("tasks");
                if(bundle.containsKey("sprint")){
                    Sprint sprint = bundle.getParcelable("sprint");
                    setText(rootView, sprint);
                    taskList = cleanData(taskList);
                    taskList = sortData(taskList);
                    chartData(rootView, taskList, sprint);
                }
            }
        }

        return rootView;
    }
    private void setText(View view, Sprint sprint){
        TextView completed = view.findViewById(R.id.tv_dialog_completed_effort);
        TextView starting = view.findViewById(R.id.tv_dialog_starting_effort);
        TextView ending = view.findViewById(R.id.tv_dialog_ending_effort);

        completed.setText(Integer.toString(sprint.getCompletedEffortPoints()));
        starting.setText(Integer.toString(sprint.getStartingEffortPoints()));
        ending.setText(Integer.toString(sprint.getCurrentEffortPoints()));
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
    private void testChart(View view){
        LineChart chart = view.findViewById(R.id.lineChart);

        List<Entry> entries = new ArrayList<Entry>();
        long currentTime = TimeUnit.MILLISECONDS.toHours(System.currentTimeMillis());
        long period = 4;
        for(int x = 0; x < 10; x++){
            int y  = x*2;
            currentTime += period;
            float xDate = (float) currentTime;
            float yValue = (float) period * 5.0f*x;
            entries.add(new Entry(xDate, yValue));
        }
        LineDataSet dataSet = new LineDataSet(entries, "entries");
        LineData lineData = new LineData(dataSet);
        lineData.setDrawValues(true);
        lineData.setHighlightEnabled(true);
        chart.setData(lineData);
        chart.getDescription().setText("data data data boi");
        XAxis xAxis = chart.getXAxis();
        YAxis yAxis2 = chart.getAxisRight();
        yAxis2.setEnabled(false);
        //xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        //xAxis.setTextSize(10f);
        //xAxis.setTextColor(Color.RED);
        xAxis.setDrawAxisLine(true);
        xAxis.setDrawGridLines(true);
        xAxis.setDrawLabels(true);
        xAxis.setEnabled(false);
        chart.invalidate(); // refresh
    }

    private void chartData(View view, ArrayList<Task> tasks, Sprint sprint){
        LineChart chart = view.findViewById(R.id.lineChart);
        ArrayList<Entry> actual =  new ArrayList<>();
        ArrayList<Entry> planned = new ArrayList<>();
        List<ILineDataSet> dataSets = new ArrayList<>();

        //getting actual/completed values
        int completedCount = 0;
        if(tasks != null && tasks.size() > 0){
            for(Task task : tasks){
                if(task.getCompleted() && task.getDateCompleted() != null) {
                    long date = task.getDateCompleted().getTime();
                    long formattedDate = TimeUnit.MILLISECONDS.toSeconds(date);
                    completedCount = completedCount + task.getEffort();
                    Entry entry = new Entry(formattedDate, completedCount);
                    actual.add(entry);
                }
            }
            //setting data rigmarole for actual/completed
            LineDataSet setActual = new LineDataSet(actual, "Completed");
            setActual.setCircleColor(R.color.colorAccent);
            setActual.setColor(R.color.colorAccent);
            //setActual.setFillColor(R.color.colorAccent);
            dataSets.add(setActual);
        }


        //getting planned values
        int dataPoints = 10; //arbitrary
        Date startDate = sprint.getSprintStart();
        long start = startDate.getTime();
        //long start = Long.parseLong("1521003240238"); //for testing charting************
        long end = sprint.getSprintEnd().getTime();
        long totalTime = end - start;
        long timeInterval = totalTime/dataPoints;
        int startingEffort = sprint.getStartingEffortPoints();
        for(int i = 0 ; i < dataPoints ; i++){
            long timeElapsed = i*timeInterval;
            long date = start + timeElapsed;
            long formattedDate = TimeUnit.MILLISECONDS.toSeconds(date);
            float expectedComplete = (float) timeElapsed/totalTime*startingEffort;
            Entry entry = new Entry(formattedDate, expectedComplete);
            planned.add(entry);
        }

        //setting data rigmarole for planned
        LineDataSet setPlanned = new LineDataSet(planned, "Planned");
        dataSets.add(setPlanned);

        //charting
        LineData data = new LineData(dataSets);
        data.setDrawValues(false);
        //data.setHighlightEnabled(false);
        chart.setData(data);

        //formatting
        XAxis xAxis = chart.getXAxis();
        YAxis yAxis2 = chart.getAxisRight();
        YAxis yAxis1 = chart.getAxisLeft();
        yAxis2.setEnabled(false);
        chart.getDescription().setEnabled(false);
        xAxis.setDrawAxisLine(false);
        xAxis.setDrawGridLines(false);
        xAxis.setDrawLabels(false);
        xAxis.setEnabled(false);
        yAxis1.setAxisMinimum(0f);
        yAxis1.setDrawGridLines(false);

        chart.invalidate(); //refresh
    }
}
