package com.example.bettinggame;

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
    private Handler handler = new Handler();
    private boolean raceFinished = false;

    // Background
    private ImageView bg1, bg2;
    private View finishLine;
    private Handler bgHandler = new Handler();

    private int bgSpeed = 5; // px per frame for background loop (keeps looping)
    private boolean bgRunning = false;

    // finish line movement
    private boolean finishLineMoving = false;
    private float targetFinishX = 0f;
    private int finishSpeed = 8; // px per frame the finish line moves (from right -> left)

    private int screenWidth;

    // new additions for start button / guarding multiple starts
    private Button btnStart;
    private boolean raceRunning = false; // prevents multiple starts

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

        // Lấy chiều rộng màn hình
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        screenWidth = metrics.widthPixels;

        btnStart = findViewById(R.id.btnStart);
        btnStart.setOnClickListener(v -> startRace());
    }

    private void startRace() {
        // Nếu đang chạy thì không làm gì
        if (raceRunning) return;

        // bật cờ chạy, disable button
        raceRunning = true;
        btnStart.setEnabled(false);
        btnStart.setText("Racing...");

        // remove previous callbacks (reset)
        handler.removeCallbacksAndMessages(null);
        bgHandler.removeCallbacksAndMessages(null);
        raceFinished = false;

        // hiển thị finishLine và đặt ngoài màn hình bên phải
        finishLine.setVisibility(View.VISIBLE);
        final float initialFinishX = screenWidth + 400; // offset ngoài màn hình

        // compute measurements after layout is ready
        finishLine.post(() -> {
            // target = right edge của seekbar (khi animal progress = 100 thì right edge animal = seekbarX + seekbarWidth)
            // dùng seekBars[0] vì tất cả lane cùng width/position
            targetFinishX = seekBars[0].getX() + seekBars[0].getWidth();

            // đặt finish line bắt đầu ở ngoài màn hình
            finishLine.setX(initialFinishX);

            // reset backgrounds (đảm bảo bg2 được đặt ngay sau bg1)
            bg1.setX(0);
            bg2.setX(bg1.getWidth());
            bgRunning = true;
            bgHandler.post(bgRunnable);

            // bắt đầu di chuyển finish line từ từ vào target
            finishLineMoving = true;
            bgHandler.post(finishRunnable);
        });

        // start each animal's progress + sprite
        for (int i = 0; i < animals.length; i++) {
            int index = i;

            // reset seekBars
            seekBars[index].setProgress(0);

            // set running gif if available
            int runResId = getResources().getIdentifier("animal" + (index + 1) + "_run", "drawable", getPackageName());
            Glide.with(this).asGif().load(runResId).into(animals[index]);

            // slower speed to lengthen race
            float speed = (new Random().nextInt(3) + 2) * 0.05f; // 0.1 - 0.2 per tick

            handler.postDelayed(new Runnable() {
                float progress = 0;

                @Override
                public void run() {
                    if (raceFinished) return;

                    progress += speed;
                    if (progress <= 100) {
                        seekBars[index].setProgress((int) progress);

                        // Move sprite theo seekbar progress
                        int barWidth = seekBars[index].getWidth() - animals[index].getWidth();
                        float posX = seekBars[index].getX() + (progress / 100f) * barWidth;
                        animals[index].setX(posX);

                        handler.postDelayed(this, 16); // ~60 FPS
                    } else {
                        // Nếu progress vượt 100 -> thắng theo luật bạn đặt
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

    // background loop (only moves bg images)
    private final Runnable bgRunnable = new Runnable() {
        @Override
        public void run() {
            if (!bgRunning) return;

            bg1.setX(bg1.getX() - bgSpeed);
            bg2.setX(bg2.getX() - bgSpeed);

            // loop bg images
            if (bg1.getX() + bg1.getWidth() <= 0) {
                bg1.setX(bg2.getX() + bg2.getWidth());
            }
            if (bg2.getX() + bg2.getWidth() <= 0) {
                bg2.setX(bg1.getX() + bg1.getWidth());
            }

            bgHandler.postDelayed(this, 16);
        }
    };

    // finish line moves from right -> left until it reaches targetFinishX, then dừng
    private final Runnable finishRunnable = new Runnable() {
        @Override
        public void run() {
            if (!finishLineMoving) return;

            float curX = finishLine.getX();
            float nextX = curX - finishSpeed;

            if (nextX <= targetFinishX) {
                // chạm target -> dính chặt ở đó
                finishLine.setX(targetFinishX);
                finishLineMoving = false;
            } else {
                finishLine.setX(nextX);
                bgHandler.postDelayed(this, 16);
            }
        }
    };

    private void stopAllRunnables() {
        // stop handler callbacks and bg handler callbacks
        handler.removeCallbacksAndMessages(null);
        bgHandler.removeCallbacksAndMessages(null);
        bgRunning = false;
        finishLineMoving = false;

        // reset flags and UI on main thread
        runOnUiThread(() -> {
            raceRunning = false;
            btnStart.setEnabled(true);
            btnStart.setText("Start Race");
        });
    }

    private void announceWinner(int winnerIndex) {
        // Reset all animals to idle
        for (int j = 0; j < animals.length; j++) {
            int idleResId = getResources().getIdentifier("animal" + (j + 1) + "_idle", "drawable", getPackageName());
            Glide.with(this).load(idleResId).into(animals[j]);
        }

        Toast.makeText(this, "🏆 Winner: Animal " + (winnerIndex + 1), Toast.LENGTH_LONG).show();
    }
}
