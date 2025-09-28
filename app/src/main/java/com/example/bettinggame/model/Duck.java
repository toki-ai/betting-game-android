package com.example.bettinggame.model;

public class Duck { // Đổi tên class
    private String name;
    // Bạn có thể thêm các thuộc tính khác cho vịt ở đây, ví dụ:
    // private int waddleSpeed;
    // private double quackLoudness;

    public Duck(String name) { // Đổi tên constructor
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
        return "Duck{" + // Đổi trong toString
               "name='" + name + "'" +
               '}';
    }
}
