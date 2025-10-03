package com.example.bettinggame.model;

import java.io.Serializable;

public class Duck implements Serializable {

    private String name;

    public Duck(String name) {
        this.name = name;
    }

    // --- Getters and Setters ---
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "Duck{" +
                "name='" + name + '\'' +
                '}';
    }
}
