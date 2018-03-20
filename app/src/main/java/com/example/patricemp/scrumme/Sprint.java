package com.example.patricemp.scrumme;

import android.os.Parcel;
import android.os.Parcelable;

import java.lang.reflect.Array;
import java.util.Date;

/**
 * Created by Tegan on 3/18/2018.
 */

public class Sprint implements Parcelable{
    private Date sprintStart;
    private Date sprintEnd;
    private int startingEffortPoints;
    private int completedEffortPoints;
    private int effortPointsAdded;
    private int effortPointsSubtracted;


    public Date getSprintStart() {
        return sprintStart;
    }

    public void setSprintStart(Date sprintStart) {
        this.sprintStart = sprintStart;
    }

    public Date getSprintEnd() {
        return sprintEnd;
    }

    public void setSprintEnd(Date sprintEnd) {
        this.sprintEnd = sprintEnd;
    }

    public int getStartingEffortPoints() {
        return startingEffortPoints;
    }

    public void setStartingEffortPoints(int startingEffortPoints) {
        this.startingEffortPoints = startingEffortPoints;
    }

    public int getCompletedEffortPoints() {
        return completedEffortPoints;
    }

    public void setCompletedEffortPoints(int completedEffortPoints) {
        this.completedEffortPoints = completedEffortPoints;
    }

    public int getEffortPointsAdded() {
        return effortPointsAdded;
    }

    public void setEffortPointsAdded(int effortPointsAdded) {
        this.effortPointsAdded = effortPointsAdded;
    }

    public int getEffortPointsSubtracted() {
        return effortPointsSubtracted;
    }

    public void setEffortPointsSubtracted(int effortPointsSubtracted) {
        this.effortPointsSubtracted = effortPointsSubtracted;
    }

    public static final Parcelable.Creator<Sprint> CREATOR
            = new Parcelable.Creator<Sprint>() {
        public Sprint createFromParcel(Parcel in) {
            return new Sprint(in);
        }

        public Sprint[] newArray(int size) {
            return new Sprint[size];
        }
    };
    public Sprint() {
    }

    private Sprint(Parcel in){
        sprintStart = new Date(in.readLong());
        sprintEnd = new Date(in.readLong());
        startingEffortPoints = in.readInt();
        completedEffortPoints = in.readInt();
        effortPointsAdded = in.readInt();
        effortPointsSubtracted = in.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(sprintStart.getTime());
        dest.writeLong(sprintEnd.getTime());
        dest.writeInt(startingEffortPoints);
        dest.writeInt(completedEffortPoints);
        dest.writeInt(effortPointsAdded);
        dest.writeInt(effortPointsSubtracted);
    }
}
