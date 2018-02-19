package com.example.patricemp.scrumme;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by patricemp on 2/18/18.
 */

public class Task implements Parcelable {
    private String mDescription;
    private int mEffort;
    private String mImportance;
    private String mNotes;
    private boolean mInSprint;
    private String mGroup;

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
        mDescription = in.readString();
        mEffort = in.readInt();
        mImportance = in.readString();
        mNotes = in.readString();
        mInSprint = (Boolean) in.readValue(null);
        mGroup = in.readString();
    }

    public void setDescription(String description){
        mDescription = description;
    }
    public void setEffort(int effort){
        mEffort = effort;
    }
    public void setImportance(String importance){
        mImportance = importance;
    }
    public void setNotes(String notes){
        mNotes = notes;
    }
    public void setInSprint(boolean inSprint){
        mInSprint = inSprint;
    }
    public void setGroup(String group){
        mGroup = group;
    }

    public String getDescription(){
        return mDescription;
    }
    public int getEffort(){
        return mEffort;
    }
    public String getImportance(){
        return mImportance;
    }
    public String getNotes(){
        return mNotes;
    }
    public boolean getInSprint(){
        return mInSprint;
    }
    public String getGroup(){
        return mGroup;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mDescription);
        dest.writeInt(mEffort);
        dest.writeString(mImportance);
        dest.writeString(mNotes);
        dest.writeValue(mInSprint);
        dest.writeString(mGroup);
    }
}
