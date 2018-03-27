package com.example.patricemp.scrumme;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by patricemp on 2/20/18.
 */

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {
    final private TaskClickListener mOnTaskClick;
    final private DeleteListener mOnDeleteClick;
    final private SprintListener mOnSprintClick;
    final private CompletedListener mOnCompleteClick;
    private ArrayList<Task> mTaskList;
    private Context mContext;

    public int countSprintPoints(){
        int count = 0;
        for (Task task : mTaskList){
            if(task.getInSprint()){ //if in sprint
                count = count + task.getEffort();
            }
        }
        return count;
    }
    public int countCompleted(){
        int count = 0;
        for (Task task : mTaskList){
            if(task.getCompleted()){ //if in sprint
                count = count + task.getEffort();
            }
        }
        return count;
    }

    public ArrayList<Task> getTaskList(){
        return mTaskList;
    }

    public TaskAdapter(TaskClickListener clickListener, DeleteListener deleteListener,
                       SprintListener sprintListener, CompletedListener completeListener) {
        mOnTaskClick = clickListener;
        mOnDeleteClick = deleteListener;
        mOnSprintClick = sprintListener;
        mOnCompleteClick = completeListener;
    }

    public interface TaskClickListener {
        void onTaskClick(Task task, View view);
    }

    public interface DeleteListener{
        void onDeleteClick(Task task);
    }

    public interface SprintListener{
        void onSprintClick(Task task);
    }
    public interface CompletedListener{
        void onCompleteClick(Task task);
    }


    class TaskViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        TextView descriptionView;
        TextView effortView;
        Button deleteButton;
        Button sprintButton;
        Button completeButton;
        CardView cardView;

        public TaskViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            deleteButton = itemView.findViewById(R.id.card_button_delete);
            deleteButton.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    int clickedPosition = getAdapterPosition();
                    Task task = mTaskList.get(clickedPosition);
                    mOnDeleteClick.onDeleteClick(task);
                    //mTaskList.remove(clickedPosition);
                    //notifyItemRemoved(clickedPosition);
                }
            });
            sprintButton = itemView.findViewById(R.id.card_button_add_remove);
            sprintButton.setOnClickListener(new View.OnClickListener(){

                @Override
                public void onClick(View v) {
                    int clickedPosition = getAdapterPosition();
                    Task task = mTaskList.get(clickedPosition);
                    mOnSprintClick.onSprintClick(task);
                }
            });
            completeButton = itemView.findViewById(R.id.card_button_complete);
            completeButton.setOnClickListener(new View.OnClickListener(){

                @Override
                public void onClick(View v) {
                    int clickedPosition = getAdapterPosition();
                    Task task = mTaskList.get(clickedPosition);
                    mOnCompleteClick.onCompleteClick(task);
                }
            });
            descriptionView = itemView.findViewById(R.id.tv_card_description);
            effortView = itemView.findViewById(R.id.tv_card_effort);
            cardView = itemView.findViewById(R.id.card_view_top);
        }

        void bind(Task task){
            if(task != null){
                String description = task.getDescription();
                if(description != null && !description.isEmpty()){
                    descriptionView.setText(description);
                }
                int effort = task.getEffort();
                if(effort > 0){
                    String effortString = "" + task.getEffort();
                    effortView.setText(effortString);
                }
                if(task.getInSprint()){
                    sprintButton.setText(R.string.card_remove_from_sprint_text);
                }else{
                    sprintButton.setText(R.string.card_add_to_sprint_text);
                }
                if(task.getCompleted()){
                    completeButton.setText(R.string.card_undo_complete);
                    cardView.setCardBackgroundColor(mContext.getResources().getColor(R.color.gray));
                }else{
                    completeButton.setText(R.string.card_mark_complete);
                }
            }
        }

        @Override
        public void onClick(View view) {

            int clickedPosition = getAdapterPosition();

            Task task = mTaskList.get(clickedPosition);
            mOnTaskClick.onTaskClick(task, view);
        }
    }

    @Override
    public TaskViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        mContext = parent.getContext();
        int taskLayoutId = R.layout.task_card;
        LayoutInflater inflater = LayoutInflater.from(mContext);
        boolean shouldAttachToParentImmediately = false;

        View view = inflater.inflate(taskLayoutId, parent, shouldAttachToParentImmediately);
        return new TaskViewHolder(view);

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
        if(mTaskList == null){
            mTaskList = new ArrayList<Task>(0);
        }
        int check = getPosition(task);
        if(check == -1){ //don't already have task
            mTaskList.add(task);
            notifyDataSetChanged();
        }
    }

    public void addTask(Task task, int position){
        if(mTaskList == null){
            mTaskList = new ArrayList<Task>(0);
        }
        if(position >=0 && position < mTaskList.size()){
            mTaskList.add(position, task);
            notifyItemInserted(position);
        }else{
            mTaskList.add(task);
            notifyDataSetChanged();
        }
    }

    public void deleteTask(Task task){
        if(mTaskList != null){
            if(mTaskList.contains(task)){
                int index = mTaskList.indexOf(task);
                mTaskList.remove(index);
                notifyItemRemoved(index);
            }
        }
    }


    public void clearTasks(){
        if(mTaskList != null){
            int currentSize = mTaskList.size();
            mTaskList.clear();
            notifyItemRangeRemoved(0, currentSize);
        }
    }
    public void modifyTask(Task task){
        int position = getPosition(task);
        if(position >= 0){
            mTaskList.set(position, task);
            notifyItemChanged(position);
        }
    }
    public int getPosition(Task task){
        if(task != null && mTaskList != null){
            for(int i=0; i < mTaskList.size(); i++){
                if(mTaskList.get(i).getDatabaseKey().matches(task.getDatabaseKey())){
                    return i;
                }
            }
        }
        return -1;

    }
}
