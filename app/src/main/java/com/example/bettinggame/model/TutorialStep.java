package com.example.bettinggame.model;

public class TutorialStep {
    private int imageResId; // ID của ảnh drawable placeholder
    private String description; // Nội dung hướng dẫn

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
