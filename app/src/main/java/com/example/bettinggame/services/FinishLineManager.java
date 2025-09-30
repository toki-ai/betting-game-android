package com.example.bettinggame.services;

import android.os.Handler;
import android.view.View;
import android.widget.SeekBar;

public class FinishLineManager {
    private final View finishLine;
    private final int screenWidth;
    private final Handler handler = new Handler();
    private boolean finishLineMoving = false;
    private float targetFinishX = 0f;
    private int finishSpeed = 8;

    public FinishLineManager(View finishLine, int screenWidth) {
        this.finishLine = finishLine;
        this.screenWidth = screenWidth;
    }

    public void startFinishLineMovement(SeekBar seekBar) {
        finishLine.setVisibility(View.VISIBLE);
        final float initialFinishX = screenWidth + 400;

        finishLine.post(() -> {
            targetFinishX = seekBar.getX() + seekBar.getWidth();
            finishLine.setX(initialFinishX);
            finishLineMoving = true;
            handler.post(finishRunnable);
        });
    }

    private final Runnable finishRunnable = new Runnable() {
        @Override
        public void run() {
            if (!finishLineMoving) return;
            float curX = finishLine.getX();
            float nextX = curX - finishSpeed;
            if (nextX <= targetFinishX) {
                finishLine.setX(targetFinishX);
                finishLineMoving = false;
            } else {
                finishLine.setX(nextX);
                handler.postDelayed(this, 16);
            }
        }
    };

    public void cleanup() {
        handler.removeCallbacksAndMessages(null);
        finishLineMoving = false;
        if (finishLine != null) finishLine.setVisibility(View.GONE);
    }
}
