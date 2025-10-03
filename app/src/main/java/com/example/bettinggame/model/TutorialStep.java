package com.example.bettinggame.model;

public class TutorialStep {
    private int imageResId;
    private String description;

    public TutorialStep(int imageResId, String description) {
        this.imageResId = imageResId;
        this.description = description;
    }

    public int getImageResId() {
        return imageResId;
    }

    public String getDescription() {
        return description;
    }
}
