package com.example.patricemp.scrumme;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by patricemp on 2/20/18.
 */

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {
    final private TaskClickListener mOnTaskClick;
    private ArrayList<Task> mTaskList;
    private Context mContext;

    public TaskAdapter(TaskClickListener clickListener) {
        mOnTaskClick = clickListener;
    }

    public interface TaskClickListener {
        void onTaskClick(Task task);
    }

    class TaskViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        TextView descriptionView;
        TextView effortView;

        public TaskViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            descriptionView = itemView.findViewById(R.id.tv_card_description);
            effortView = itemView.findViewById(R.id.tv_card_effort);
        }

        void bind(Task task){
//            descriptionView.setText("Oh Hai");
//            effortView.setText("2");
            if(task != null){
                String description = task.getDescription();
                if(description != null && !description.isEmpty()){
                    descriptionView.setText(description);
                }
                int effort = task.getEffort();
                if(effort > 0){
                    effortView.setText("" + task.getEffort());
                }
            }
        }

        @Override
        public void onClick(View view) {
            int clickedPosition = getAdapterPosition();
            Task task = mTaskList.get(clickedPosition);
            mOnTaskClick.onTaskClick(task);
        }
    }

    @Override
    public TaskViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        mContext = parent.getContext();
        int taskLayoutId = R.layout.task_card;
        LayoutInflater inflater = LayoutInflater.from(mContext);
        boolean shouldAttachToParentImmediately = false;

        View view = inflater.inflate(taskLayoutId, parent, shouldAttachToParentImmediately);
        TaskViewHolder viewHolder = new TaskViewHolder(view);

        return viewHolder;
    }


    @Override
    public void onBindViewHolder(TaskAdapter.TaskViewHolder holder, int position) {
        holder.bind(mTaskList.get(position));
    }

    @Override
    public int getItemCount() {
        if(mTaskList != null){
            return mTaskList.size();
        }
        return 0;
    }

    public void setTasks(ArrayList<Task> tasks){
        clearTasks();
        mTaskList = tasks;
        notifyItemRangeInserted(0, tasks.size());
    }

    public void addTask(Task task){
        int currentSize = 0;
        if(mTaskList != null){
            currentSize = mTaskList.size();
        }else{
            mTaskList = new ArrayList<Task>(0);
        }

        mTaskList.add(task);
        notifyDataSetChanged();
    }


    public void clearTasks(){
        if(mTaskList != null){
            int currentSize = mTaskList.size();
            mTaskList.clear();
            notifyItemRangeRemoved(0, currentSize);
        }
    }
}
