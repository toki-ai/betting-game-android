package com.example.bettinggame.services;

import android.os.Handler;
import android.widget.ImageView;

import pl.droidsonroids.gif.GifImageView;

public class BackgroundAnimationManager {
    private final GifImageView grass1, grass2;
    private final ImageView water1, water2;
    private final Handler grassHandler = new Handler();
    private final Handler waterHandler = new Handler();
    private int grassSpeed = 5;
    private int waterSpeed = 5;
    private boolean grassRunning = false;
    private boolean waterRunning = false;
    private int grassWidth = 0;
    private int waterWidth = 0;

    public BackgroundAnimationManager(GifImageView grass1, GifImageView grass2, ImageView water1, ImageView water2) {
        this.grass1 = grass1;
        this.grass2 = grass2;
        this.water1 = water1;
        this.water2 = water2;
    }

    public void startBackgroundAnimations() {
        grass1.post(() -> {
            grassWidth = grass1.getWidth();
            grass1.setX(0);
            grass2.setX(grassWidth);
            grassRunning = true;
            grassHandler.post(grassRunnable);
        });

        water1.post(() -> {
            waterWidth = water1.getWidth();
            water1.setX(0);
            water2.setX(waterWidth);
            waterRunning = true;
            waterHandler.post(waterRunnable);
        });
    }

    private final Runnable grassRunnable = new Runnable() {
        @Override
        public void run() {
            if (!grassRunning || grassWidth == 0) return;

            grass1.setX(grass1.getX() - grassSpeed);
            grass2.setX(grass2.getX() - grassSpeed);

            if (grass1.getX() + grassWidth <= 0) {
                grass1.setX(grass2.getX() + grassWidth);
            }
            if (grass2.getX() + grassWidth <= 0) {
                grass2.setX(grass1.getX() + grassWidth);
            }
            grassHandler.postDelayed(this, 16);
        }
    };

    private final Runnable waterRunnable = new Runnable() {
        @Override
        public void run() {
            if (!waterRunning || waterWidth == 0) return;

            water1.setX(water1.getX() - waterSpeed);
            water2.setX(water2.getX() - waterSpeed);

            if (water1.getX() + waterWidth <= 0) {
                water1.setX(water2.getX() + waterWidth);
            }
            if (water2.getX() + waterWidth <= 0) {
                water2.setX(water1.getX() + waterWidth);
            }
            waterHandler.postDelayed(this, 16);
        }
    };

    public void cleanup() {
        grassHandler.removeCallbacksAndMessages(null);
        waterHandler.removeCallbacksAndMessages(null);
        grassRunning = false;
        waterRunning = false;
    }
}