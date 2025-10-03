package com.example.bettinggame.model;

import java.io.Serializable;

public class RaceResult implements Serializable {

    private Duck duck;
    private int rank;
    private double amountWon;
    private int progress;

    // Constructor
    public RaceResult(Duck duck, int rank, double amountWon) {
        this.duck = duck;
        this.rank = rank;
        this.amountWon = amountWon;
        this.progress = 0;
    }

    // --- Getters and Setters ---
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

    @Override
    public String toString() {
        return "RaceResult{" +
               "duck=" + (duck != null ? duck.getName() : "N/A") +
               ", rank=" + rank +
               ", amountWon=" + amountWon +
               '}';
    }
}
