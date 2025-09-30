package com.example.bettinggame;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.bettinggame.services.BackgroundAnimationManager;
import com.example.bettinggame.services.BettingConfig;
import com.example.bettinggame.services.BettingManager;
import com.example.bettinggame.services.FinishLineManager;
import com.example.bettinggame.services.RaceManager;

import pl.droidsonroids.gif.GifDrawable;
import pl.droidsonroids.gif.GifImageView;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private SeekBar[] seekBars;
    private GifDrawable[] gifThumbs = new GifDrawable[4];
    private GifImageView grass1, grass2;
    private ImageView water1, water2;
    private View finishLine;
    private Button btnStart, btnBet1, btnBet2, btnBet3, btnBet4;
    private ImageButton btnCancelBet1, btnCancelBet2, btnCancelBet3, btnCancelBet4;
    private ImageButton btnTutorial, btnMusic;
    private TextView tvBalance, tvBet1, tvBet2, tvBet3, tvBet4;
    private View betPanel;
    private int screenWidth;

    private RaceManager raceManager;
    private BettingManager bettingManager;
    private BackgroundAnimationManager backgroundAnimationManager;
    private FinishLineManager finishLineManager;
    private final BettingConfig betManager = new BettingConfig(1000);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        initializeUI();
        initializeManagers();
        setupListeners();
        setupAnimatedThumbs();
        updateBalanceText();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initializeUI() {
        String username = getIntent().getStringExtra("username");
        SharedPreferences prefs = getSharedPreferences("BettingGamePrefs", Context.MODE_PRIVATE);
        int coin = prefs.getInt(username, 0);

        seekBars = new SeekBar[]{
                findViewById(R.id.seekBar1),
                findViewById(R.id.seekBar2),
                findViewById(R.id.seekBar3),
                findViewById(R.id.seekBar4)
        };

        for (SeekBar s : seekBars) {
            s.setOnTouchListener((v, event) -> true);
        }

        grass1 = findViewById(R.id.grass1);
        grass2 = findViewById(R.id.grass2);
        water1 = findViewById(R.id.waterBg1);
        water2 = findViewById(R.id.waterBg2);
        finishLine = findViewById(R.id.finishLine);
        btnStart = findViewById(R.id.btnStart);
        btnBet1 = findViewById(R.id.btnBet1);
        btnBet2 = findViewById(R.id.btnBet2);
        btnBet3 = findViewById(R.id.btnBet3);
        btnBet4 = findViewById(R.id.btnBet4);
        btnCancelBet1 = findViewById(R.id.btnCancelBet1);
        btnCancelBet2 = findViewById(R.id.btnCancelBet2);
        btnCancelBet3 = findViewById(R.id.btnCancelBet3);
        btnCancelBet4 = findViewById(R.id.btnCancelBet4);
        tvBalance = findViewById(R.id.tvBalance);
        tvBet1 = findViewById(R.id.tvBet1);
        tvBet2 = findViewById(R.id.tvBet2);
        tvBet3 = findViewById(R.id.tvBet3);
        tvBet4 = findViewById(R.id.tvBet4);
        btnTutorial = findViewById(R.id.btnTutorial);
        btnMusic = findViewById(R.id.btnMusic);
        betPanel = findViewById(R.id.betPanel);

        DisplayMetrics metrics = getResources().getDisplayMetrics();
        screenWidth = metrics.widthPixels;
    }

    private void initializeManagers() {
        raceManager = new RaceManager(this, seekBars, screenWidth, betManager);
        bettingManager = new BettingManager(this, btnBet1, btnBet2, btnBet3, btnBet4,
                btnCancelBet1, btnCancelBet2, btnCancelBet3, btnCancelBet4,
                tvBet1, tvBet2, tvBet3, tvBet4, betPanel, betManager);
        backgroundAnimationManager = new BackgroundAnimationManager(grass1, grass2, water1, water2);
        finishLineManager = new FinishLineManager(finishLine, screenWidth);
    }

    private void setupListeners() {
        btnStart.setOnClickListener(v -> raceManager.startRace(bettingManager.getSelectedDuckIndex(), bettingManager.getCurrentBetAmount()));
        btnBet1.setOnClickListener(v -> bettingManager.promptBetForDuck(0));
        btnBet2.setOnClickListener(v -> bettingManager.promptBetForDuck(1));
        btnBet3.setOnClickListener(v -> bettingManager.promptBetForDuck(2));
        btnBet4.setOnClickListener(v -> bettingManager.promptBetForDuck(3));
        btnCancelBet1.setOnClickListener(v -> bettingManager.cancelCurrentBet(0));
        btnCancelBet2.setOnClickListener(v -> bettingManager.cancelCurrentBet(1));
        btnCancelBet3.setOnClickListener(v -> bettingManager.cancelCurrentBet(2));
        btnCancelBet4.setOnClickListener(v -> bettingManager.cancelCurrentBet(3));
        btnTutorial.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, TutorialActivity.class);
            startActivity(intent);
        });
        btnMusic.setOnClickListener(v -> {
            Toast.makeText(this, "Music toggle clicked", Toast.LENGTH_SHORT).show();
        });
    }

    private void setupAnimatedThumbs() {
        int thumbSize = dpToPx(60);
        try {
            for (int i = 0; i < seekBars.length; i++) {
                gifThumbs[i] = new GifDrawable(getResources(), R.drawable.animal_idle);
                gifThumbs[i].setBounds(0, 0, thumbSize, thumbSize);
                seekBars[i].setThumb(gifThumbs[i]);
                gifThumbs[i].start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }

    @SuppressLint("SetTextI18n")
    public void updateBalanceText() {
        tvBalance.setText("Balance: " + betManager.formatInt(betManager.getBalance()));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        for (GifDrawable gif : gifThumbs) {
            if (gif != null) {
                gif.recycle();
            }
        }
        raceManager.cleanup();
        backgroundAnimationManager.cleanup();
        finishLineManager.cleanup();
    }
}