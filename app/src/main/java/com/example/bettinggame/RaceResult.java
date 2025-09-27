package com.example.bettinggame;

public class RaceResult {
    private Horse horse;
    private int rank;
    private double amountWon;

    // Constructor
    public RaceResult(Horse horse, int rank, double amountWon) {
        this.horse = horse;
        this.rank = rank;
        this.amountWon = amountWon;
    }

    // Getters
    public Horse getHorse() { // Cập nhật getter
        return horse;
    }

    public int getRank() {
        return rank;
    }

    public double getAmountWon() {
        return amountWon;
    }

    // public void setHorse(Horse horse) { this.horse = horse; }
    // public void setRank(int rank) { this.rank = rank; }
    // public void setAmountWon(double amountWon) { this.amountWon = amountWon; }

    @Override
    public String toString() {
        return "RaceResult{" +
               "horse=" + (horse != null ? horse.getName() : "N/A") +
               ", rank=" + rank +
               ", amountWon=" + amountWon +
               '}';
    }
}
