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
import com.example.bettinggame.services.MusicManager;
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
    private ImageButton btnTutorial, btnMusic, btnLogOut, btnDeposit;
    private TextView tvBalance, tvBet1, tvBet2, tvBet3, tvBet4, tvUsername, tvMusic;
    private View betPanel;
    private int screenWidth;

    private RaceManager raceManager;
    private BettingManager bettingManager;
    private BackgroundAnimationManager backgroundAnimationManager;
    private FinishLineManager finishLineManager;
    private BettingConfig betManager;

    public String playerName = Constants.DEFAULT_PLAYER_NAME;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("username")) {
            String name = intent.getStringExtra("username");
            if (name != null && !name.trim().isEmpty()) {
                playerName = name.trim();
            }
        }

        initializeUI();
        initializeManagers();
        setupListeners();
        setupAnimatedThumbs();
        updateBalanceText();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initializeUI() {
        tvUsername = findViewById(R.id.tvUsername); 
        if (tvUsername != null) {
            tvUsername.setText(playerName);
        }

        SharedPreferences prefs = getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE);
        int coin = prefs.getInt(playerName, 0);
        betManager = new BettingConfig(coin);

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
        tvMusic = findViewById(R.id.tvMusic);
        btnTutorial = findViewById(R.id.btnTutorial);
        btnMusic = findViewById(R.id.btnMusic);
        btnLogOut = findViewById(R.id.btnLogOut);
        btnDeposit = findViewById(R.id.btnDeposit);
        betPanel = findViewById(R.id.betPanel);

        DisplayMetrics metrics = getResources().getDisplayMetrics();
        screenWidth = metrics.widthPixels;
    }

    private void initializeManagers() {
        bettingManager = new BettingManager(this, btnBet1, btnBet2, btnBet3, btnBet4,
                btnCancelBet1, btnCancelBet2, btnCancelBet3, btnCancelBet4,
                tvBet1, tvBet2, tvBet3, tvBet4, betPanel, betManager);
        backgroundAnimationManager = new BackgroundAnimationManager(grass1, grass2, water1, water2);
        finishLineManager = new FinishLineManager(finishLine, screenWidth);
        raceManager = new RaceManager(this, seekBars, screenWidth, betManager, backgroundAnimationManager, finishLineManager, playerName);
    }

    private void setupListeners() {
        btnStart.setOnClickListener(v -> raceManager.startRace(-1, 0));
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
            intent.putExtra("username", playerName);
            startActivity(intent);
        });
        btnMusic.setOnClickListener(v -> {
            if (!MusicManager.isPlaying()) {
                MusicManager.startMusic(this);
                tvMusic.setText("Tắt Nhạc 🎵");
            } else {
                MusicManager.stopMusic();
                tvMusic.setText("Bật Nhạc 🎶");
            }
        });
        btnLogOut.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });
        btnDeposit.setOnClickListener(v -> {
            android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
            builder.setTitle("Nạp thêm xu");
            final android.widget.EditText input = new android.widget.EditText(this);
            input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
            builder.setView(input);
            builder.setPositiveButton("Nạp", (dialog, which) -> {
                String value = input.getText().toString().trim();
                int addAmount = 0;
                try {
                    addAmount = Integer.parseInt(value);
                } catch (Exception e) {
                    addAmount = 0;
                }
                if (addAmount > 0) {
                    betManager.setBalance(betManager.getBalance() + addAmount);
                    SharedPreferences prefs = getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE);
                    prefs.edit().putInt(playerName, betManager.getBalance()).apply();
                    updateBalanceText();
                    Toast.makeText(this, "Nạp thành công!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Vui lòng nhập số hợp lệ", Toast.LENGTH_SHORT).show();
                }
            });
            builder.setNegativeButton("Hủy", (dialog, which) -> dialog.cancel());
            builder.show();
        });
    }

    public BettingManager getBettingManager() {
        return bettingManager;
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

           SharedPreferences prefs = getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE);
           prefs.edit().putInt(playerName, betManager.getBalance()).apply();
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