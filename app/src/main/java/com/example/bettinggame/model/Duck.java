package com.example.bettinggame.model;

import android.os.Parcel;
import android.os.Parcelable;

public class Duck implements Parcelable {    private String name;
    // private int waddleSpeed;
    // private double quackLoudness;

    public Duck(String name) {
        this.name = name;
    }

    protected Duck(Parcel in) {
        name = in.readString();
    }

    public static final Creator<Duck> CREATOR = new Creator<Duck>() {
        @Override
        public Duck createFromParcel(Parcel in) {
            return new Duck(in);
        }

        @Override
        public Duck[] newArray(int size) {
            return new Duck[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name); // Write the name to the Parcel
        // If you add other fields, write them here in the same order as you read them:
        // dest.writeInt(waddleSpeed);
        // dest.writeDouble(quackLoudness);
    }

    // --- Getters and Setters ---
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    // Add getters and setters for any other fields you might add later

    @Override
    public String toString() {
        // Đổi trong toString (Keeping your original comment)
        return "Duck{" +
                "name='" + name + '\'' +
                // If you add other fields, include them in toString() for easier debugging
                // ", waddleSpeed=" + waddleSpeed +
                // ", quackLoudness=" + quackLoudness +
                '}';
    }
}
