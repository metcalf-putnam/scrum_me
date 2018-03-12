package com.example.patricemp.scrumme;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by patricemp on 2/18/18.
 */

public class Task implements Parcelable {
    private String description;
    private int effort;
    private String importance;
    private String notes;
    private boolean inSprint;
    private boolean completed;
    private String group;
    private String databaseKey;

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
        importance = in.readString();
        notes = in.readString();
        inSprint = (Boolean) in.readValue(null);
        completed = (Boolean) in.readValue(null);
        group = in.readString();
        databaseKey = in.readString();
    }

    public void setDescription(String desc){
        description = desc;
    }
    public void setEffort(int ef){
        effort = ef;
    }
    public void setImportance(String imp){
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
    public String getImportance(){
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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(description);
        dest.writeInt(effort);
        dest.writeString(importance);
        dest.writeString(notes);
        dest.writeValue(inSprint);
        dest.writeValue(completed);
        dest.writeString(group);
        dest.writeString(databaseKey);
    }
}
