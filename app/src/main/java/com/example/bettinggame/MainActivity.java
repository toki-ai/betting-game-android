package com.example.bettinggame;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import java.util.Random;


public class MainActivity extends AppCompatActivity {

    private SeekBar[] seekBars;
    private ImageView[] animals;
    private String[] duckNames = {"Vịt 1", "Vịt 2", "Vịt 3", "Vịt 4"};
    private Handler handler = new Handler();
    private boolean raceFinished = false;

    // Background
    private ImageView bg1, bg2;
    private View finishLine;
    private Handler bgHandler = new Handler();

    private int bgSpeed = 5;
    private boolean bgRunning = false;

    // finish line movement
    private boolean finishLineMoving = false;
    private float targetFinishX = 0f;
    private int finishSpeed = 8;

    private int screenWidth;

    private Button btnStart, btnTutorial;
    private boolean raceRunning = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        seekBars = new SeekBar[]{
                findViewById(R.id.seekBar1),
                findViewById(R.id.seekBar2),
                findViewById(R.id.seekBar3),
                findViewById(R.id.seekBar4),
        };

        animals = new ImageView[]{
                findViewById(R.id.animal1),
                findViewById(R.id.animal2),
                findViewById(R.id.animal3),
                findViewById(R.id.animal4),
        };

        bg1 = findViewById(R.id.bg1);
        bg2 = findViewById(R.id.bg2);
        finishLine = findViewById(R.id.finishLine);

        DisplayMetrics metrics = getResources().getDisplayMetrics();
        screenWidth = metrics.widthPixels;

        btnStart = findViewById(R.id.btnStart);
        btnTutorial = findViewById(R.id.btnTutorial);

        btnStart.setOnClickListener(v -> startRace());

        btnTutorial.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, TutorialActivity.class);
            startActivity(intent);
        });
    }

    private void startRace() {
        if (raceRunning) return;
        raceRunning = true;
        btnStart.setEnabled(false);
        btnStart.setText("Racing...");
        btnTutorial.setEnabled(false);

        handler.removeCallbacksAndMessages(null);
        bgHandler.removeCallbacksAndMessages(null);
        raceFinished = false;

        finishLine.setVisibility(View.VISIBLE);
        final float initialFinishX = screenWidth + 400;

        finishLine.post(() -> {
            targetFinishX = seekBars[0].getX() + seekBars[0].getWidth();
            finishLine.setX(initialFinishX);
            bg1.setX(0);
            bg2.setX(bg1.getWidth());
            bgRunning = true;
            bgHandler.post(bgRunnable);
            finishLineMoving = true;
            bgHandler.post(finishRunnable);
        });

        for (int i = 0; i < animals.length; i++) {
            int index = i;
            seekBars[index].setProgress(0);
            int runResId = getResources().getIdentifier("animal" + (index + 1) + "_run", "drawable", getPackageName());
            Glide.with(this).asGif().load(runResId).into(animals[index]);
            float speed = (new Random().nextInt(3) + 2) * 0.05f;

            handler.postDelayed(new Runnable() {
                float progress = 0;
                @Override
                public void run() {
                    if (raceFinished) return;
                    progress += speed;
                    if (progress <= 100) {
                        seekBars[index].setProgress((int) progress);
                        int barWidth = seekBars[index].getWidth() - animals[index].getWidth();
                        float posX = seekBars[index].getX() + (progress / 100f) * barWidth;
                        animals[index].setX(posX);
                        handler.postDelayed(this, 16);
                    } else {
                        if (!raceFinished) {
                            raceFinished = true;
                            stopAllRunnables();
                            announceWinner(index);
                        }
                    }
                }
            }, 16);
        }
    }

    private final Runnable bgRunnable = new Runnable() {
        @Override
        public void run() {
            if (!bgRunning) return;
            bg1.setX(bg1.getX() - bgSpeed);
            bg2.setX(bg2.getX() - bgSpeed);
            if (bg1.getX() + bg1.getWidth() <= 0) {
                bg1.setX(bg2.getX() + bg2.getWidth());
            }
            if (bg2.getX() + bg2.getWidth() <= 0) {
                bg2.setX(bg1.getX() + bg1.getWidth());
            }
            bgHandler.postDelayed(this, 16);
        }
    };

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
                bgHandler.postDelayed(this, 16);
            }
        }
    };

    private void stopAllRunnables() {
        handler.removeCallbacksAndMessages(null);
        bgHandler.removeCallbacksAndMessages(null);
        bgRunning = false;
        finishLineMoving = false;
        runOnUiThread(() -> {
            raceRunning = false;
            btnStart.setEnabled(true);
            btnStart.setText("Start Race");
            btnTutorial.setEnabled(true);
        });
    }

    private void announceWinner(int winnerIndex) {
        for (int j = 0; j < animals.length; j++) {
            int idleResId = getResources().getIdentifier("animal" + (j + 1) + "_idle", "drawable", getPackageName());
            Glide.with(this).load(idleResId).into(animals[j]);
        }
 
        Toast.makeText(this, "🏆 Winner: " + duckNames[winnerIndex], Toast.LENGTH_LONG).show();

        // Xóa phần khởi chạy RaceResultsActivity
         Intent intent = new Intent(MainActivity.this, RaceResultsActivity.class);
        // intent.putParcelableArrayListExtra("raceResults", currentRaceResults);
        // startActivity(intent);
    }
}
