package com.example.bettinggame;

public class Horse {
    private String name;
    // private int speed;
    // private double odds;

    public Horse(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "Horse{" +
               "name='" + name + "'" +
               '}';
    }
}
