package com.example.bettinggame.model;

import android.os.Parcel;
import android.os.Parcelable;

public class RaceResult implements Parcelable {
    private Duck duck;
    private int rank;
    private double amountWon;
    private transient int progress;

    // Constructor
    public RaceResult(Duck duck, int rank, double amountWon) {
        this.duck = duck;
        this.rank = rank;
        this.amountWon = amountWon;
    }

    public Duck getDuck() {
        return duck;
    }

    public void setDuck(Duck duck) {
        this.duck = duck;
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public double getAmountWon() {
        return amountWon;
    }

    public void setAmountWon(double amountWon) {
        this.amountWon = amountWon;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    protected RaceResult(Parcel in) {
        duck = in.readParcelable(Duck.class.getClassLoader());
        rank = in.readInt();
        amountWon = in.readDouble();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(duck, flags);
        dest.writeInt(rank);
        dest.writeDouble(amountWon);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<RaceResult> CREATOR = new Creator<RaceResult>() {
        @Override
        public RaceResult createFromParcel(Parcel in) {
            return new RaceResult(in);
        }

        @Override
        public RaceResult[] newArray(int size) {
            return new RaceResult[size];
        }
    };

    @Override
    public String toString() {
        return "RaceResult{" +
               "duck=" + (duck != null ? duck.getName() : "N/A") +
               ", rank=" + rank +
               ", amountWon=" + amountWon +
               '}';
    }
}
