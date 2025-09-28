package com.example.bettinggame.model;

public class RaceResult {
    private Duck duck;
    private int rank;
    private double amountWon;

    // Constructor
    public RaceResult(Duck duck, int rank, double amountWon) {
        this.duck = duck;
        this.rank = rank;
        this.amountWon = amountWon;
    }

    // Getters
    public Duck getDuck() {
        return duck;
    }

    public int getRank() {
        return rank;
    }

    public double getAmountWon() {
        return amountWon;
    }

    // public void setDuck(Duck duck) { this.duck = duck; }
    // public void setRank(int rank) { this.rank = rank; }
    // public void setAmountWon(double amountWon) { this.amountWon = amountWon; }

    @Override
    public String toString() {
        return "RaceResult{" +
               "duck=" + (duck != null ? duck.getName() : "N/A") +
               ", rank=" + rank +
               ", amountWon=" + amountWon +
               '}';
    }
}
