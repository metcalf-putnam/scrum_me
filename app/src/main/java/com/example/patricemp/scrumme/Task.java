package com.example.patricemp.scrumme;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;

/**
 * Created by patricemp on 2/18/18.
 */

public class Task implements Parcelable {
    private String description;
    private int effort;
    private int importance;
    private String notes;
    private boolean inSprint;
    private boolean completed;
    private String group;
    private String databaseKey;
    private int sprintNum;
    private Date dateCompleted;

    public Date getDateCompleted() {
        return dateCompleted;
    }

    public void setDateCompleted(Date dateCompleted) {
        this.dateCompleted = dateCompleted;
    }



    public static final Parcelable.Creator<Task> CREATOR
            = new Parcelable.Creator<Task>() {
        public Task createFromParcel(Parcel in) {
            return new Task(in);
        }

        public Task[] newArray(int size) {
            return new Task[size];
        }
    };

    public Task(){}

    private Task(Parcel in){
        description = in.readString();
        effort = in.readInt();
        importance = in.readInt();
        notes = in.readString();
        inSprint = (Boolean) in.readValue(null);
        completed = (Boolean) in.readValue(null);
        group = in.readString();
        databaseKey = in.readString();
        sprintNum = in.readInt();
        dateCompleted = new Date(in.readLong());
    }

    public void setDescription(String desc){
        description = desc;
    }
    public void setEffort(int ef){
        effort = ef;
    }
    public void setImportance(int imp){
        importance = imp;
    }
    public void setNotes(String noted){
        notes = noted;
    }
    public void setInSprint(boolean boo){
        inSprint = boo;
    }
    public void setCompleted(boolean boo) {
        completed = boo;
    }
    public void setGroup(String g){
        group = g;
    }
    public void setDatabaseKey(String k){
        databaseKey = k;
    }

    public String getDescription(){
        return description;
    }
    public int getEffort(){
        return effort;
    }
    public int getImportance(){
        return importance;
    }
    public String getNotes(){
        return notes;
    }
    public boolean getInSprint(){
        return inSprint;
    }
    public boolean getCompleted(){
        return completed;
    }
    public String getGroup(){
        return group;
    }
    public String getDatabaseKey(){
        return databaseKey;
    }
    public int getSprintNum() {
        return sprintNum;
    }

    public void setSprintNum(int sprintNum) {
        this.sprintNum = sprintNum;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(description);
        dest.writeInt(effort);
        dest.writeInt(importance);
        dest.writeString(notes);
        dest.writeValue(inSprint);
        dest.writeValue(completed);
        dest.writeString(group);
        dest.writeString(databaseKey);
        dest.writeInt(sprintNum);
        if(dateCompleted!= null){
            dest.writeLong(dateCompleted.getTime());
        }
    }
}
