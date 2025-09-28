package com.example.bettinggame;

import android.os.Bundle;
import android.os.Handler;
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

        Button btnStart = findViewById(R.id.btnStart);
        btnStart.setOnClickListener(v -> startRace());
    }

    private void startRace() {
        raceFinished = false;

        for (int i = 0; i < 4; i++) {
            int index = i;

            // Reset seekBars
            seekBars[i].setProgress(0);

            // Switch to running GIF
            int runResId = getResources().getIdentifier("animal" + (i + 1) + "_run", "drawable", getPackageName());
            Glide.with(this).asGif().load(runResId).into(animals[i]);

            int speed = new Random().nextInt(3) + 2; // random speed 2–4

            handler.postDelayed(new Runnable() {
                int progress = 0;

                @Override
                public void run() {
                    if (raceFinished) return;

                    progress += speed;
                    if (progress <= 100) {
                        seekBars[index].setProgress(progress);

                        // Move sprite along seekBar
                        int barWidth = seekBars[index].getWidth() - animals[index].getWidth();
                        float posX = seekBars[index].getX() + (progress / 100f) * barWidth;
                        animals[index].setX(posX);

                        handler.postDelayed(this, 50);
                    } else {
                        raceFinished = true;
                        announceWinner(index);
                    }
                }
            }, 50);
        }
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