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

import java.util.Random;


public class MainActivity extends AppCompatActivity {

    private SeekBar[] seekBars;
    private ImageView[] animals;
    private Handler handler = new Handler();
    private Random random = new Random();
    private boolean isRacing = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        seekBars = new SeekBar[]{
                findViewById(R.id.seek1),
                findViewById(R.id.seek2),
                findViewById(R.id.seek3),
                findViewById(R.id.seek4),
        };

        animals = new ImageView[]{
                findViewById(R.id.animal1),
                findViewById(R.id.animal2),
                findViewById(R.id.animal3),
                findViewById(R.id.animal4),
        };

        Button btnStart = findViewById(R.id.btnStart);
        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isRacing) {
                    startRace();
                }
            }
        });
    }

    private Runnable raceRunnable = new Runnable() {
        @Override
        public void run() {
            boolean finished = false;
            for (int i = 0; i < seekBars.length; i++) {
                if (seekBars[i].getProgress() < seekBars[i].getMax()) {
                    // Random smooth increment
                    int step = random.nextInt(5) + 1;
                    seekBars[i].setProgress(seekBars[i].getProgress() + step);
                }

                if (seekBars[i].getProgress() >= seekBars[i].getMax()) {
                    finished = true;
                    stopRace(i);
                    break;
                }
            }

            if (!finished) {
                handler.postDelayed(this, 50); // smooth update
            }
        }
    };

    private void stopRace(int winnerIndex) {
        isRacing = false;
        handler.removeCallbacks(raceRunnable);

        // Switch all animals to idle
        for (int j = 0; j < animals.length; j++) {
            int resId = getResources().getIdentifier(
                    "animal" + (j + 1) + "_idle",
                    "drawable",
                    getPackageName()
            );
            animals[j].setImageResource(resId);
        }

        Toast.makeText(this, "Animal " + (winnerIndex + 1) + " wins!", Toast.LENGTH_SHORT).show();
    }

    private void startRace() {
        isRacing = true;

        // Reset seekbars
        for (SeekBar sb : seekBars) {
            sb.setProgress(0);
        }

        // Switch all animals to running GIF
        for (int i = 0; i < animals.length; i++) {
            int resId = getResources().getIdentifier(
                    "animal" + (i + 1) + "_run",
                    "drawable",
                    getPackageName()
            );
            animals[i].setImageResource(resId);
        }

        handler.postDelayed(raceRunnable, 100);
    }
}