package com.example.bettinggame;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.SeekBar;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import com.example.bettinggame.bet.BetManager;
import java.util.Random;
import pl.droidsonroids.gif.GifDrawable;
import pl.droidsonroids.gif.GifImageView;
import java.io.IOException;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.view.animation.LinearInterpolator;

public class MainActivity extends AppCompatActivity {

    private SeekBar[] seekBars;
    private String[] duckNames = {"Vịt 1", "Vịt 2", "Vịt 3", "Vịt 4"};
    private Handler handler = new Handler();
    private boolean raceFinished = false;

    // Grass background (GIF)
    private GifImageView grass1, grass2;
    private Handler grassHandler = new Handler();
    private int grassSpeed = 5;
    private boolean grassRunning = false;
    private int grassWidth = 0;

    // Water background (PNG)
    private ImageView water1, water2;
    private Handler waterHandler = new Handler();
    private int waterSpeed = 5;
    private boolean waterRunning = false;
    private int waterWidth = 0;

    private View finishLine;

    // Finish line movement
    private boolean finishLineMoving = false;
    private float targetFinishX = 0f;
    private int finishSpeed = 8;

    private int screenWidth;

    private Button btnStart, btnTutorial;
    private boolean raceRunning = false;

    // Betting UI
    private Button btnBet1, btnBet2, btnBet3, btnBet4, btnCancelBet;
    private TextView tvBet1, tvBet2, tvBet3, tvBet4;
    private TextView tvBalance, tvResult;
    private View betPanel;
    private int selectedDuckIndex = -1;
    private BetManager betManager = new BetManager(1000);
    private int currentBetAmount = 0;

    // Animation
    private ObjectAnimator[] duckAnimators = new ObjectAnimator[4];
    private long raceStartTime = 0;

    // GIF thumbs
    private GifDrawable[] gifThumbs = new GifDrawable[4];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        String username = getIntent().getStringExtra("username");
        SharedPreferences prefs = getSharedPreferences("BettingGamePrefs", Context.MODE_PRIVATE);
        int coin = prefs.getInt(username, 0);

        seekBars = new SeekBar[]{
                findViewById(R.id.seekBar1),
                findViewById(R.id.seekBar2),
                findViewById(R.id.seekBar3),
                findViewById(R.id.seekBar4)
        };

        // Setup Grass background
        grass1 = findViewById(R.id.grass1);
        grass2 = findViewById(R.id.grass2);

        // Setup Water background
        water1 = findViewById(R.id.waterBg1);
        water2 = findViewById(R.id.waterBg2);

        finishLine = findViewById(R.id.finishLine);

        DisplayMetrics metrics = getResources().getDisplayMetrics();
        screenWidth = metrics.widthPixels;

        btnStart = findViewById(R.id.btnStart);
        btnTutorial = findViewById(R.id.btnTutorial);

        btnStart.setOnClickListener(v -> startRace());

        // Betting UI setup
        tvBalance = findViewById(R.id.tvBalance);
        tvResult = findViewById(R.id.tvResult);
        betPanel = findViewById(R.id.betPanel);
        btnBet1 = findViewById(R.id.btnBet1);
        btnBet2 = findViewById(R.id.btnBet2);
        btnBet3 = findViewById(R.id.btnBet3);
        btnBet4 = findViewById(R.id.btnBet4);
        btnCancelBet = findViewById(R.id.btnCancelBet);
        tvBet1 = findViewById(R.id.tvBet1);
        tvBet2 = findViewById(R.id.tvBet2);
        tvBet3 = findViewById(R.id.tvBet3);
        tvBet4 = findViewById(R.id.tvBet4);

        btnBet1.setOnClickListener(v -> promptBetForDuck(0));
        btnBet2.setOnClickListener(v -> promptBetForDuck(1));
        btnBet3.setOnClickListener(v -> promptBetForDuck(2));
        btnBet4.setOnClickListener(v -> promptBetForDuck(3));
        btnCancelBet.setOnClickListener(v -> cancelCurrentBet());

        updateBetLabels();
        updateBetButtonsVisibility();
        updateBalanceText();

        btnTutorial.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, TutorialActivity.class);
            startActivity(intent);
        });

        // Setup animated GIF thumbs
        int thumbSize = dpToPx(60);
        try {
            for (int i = 0; i < seekBars.length; i++) {
                gifThumbs[i] = new GifDrawable(getResources(), R.drawable.animal);
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

    private void startRace() {
        if (raceRunning) return;

        if (selectedDuckIndex < 0) {
            Toast.makeText(this, "Chọn 1 con vịt để đặt cược", Toast.LENGTH_SHORT).show();
            return;
        }
        int betAmount = currentBetAmount;
        BetManager.ValidationResult vr = betManager.validateBet(betAmount);
        if (!vr.valid) {
            Toast.makeText(this, vr.message != null ? vr.message : "Tiền cược không hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }

        betManager.applyBetStart(betAmount);
        updateBalanceText();
        Toast.makeText(this, getBetTaunt(betAmount), Toast.LENGTH_SHORT).show();
        tvResult.setText("Đang đua... Chúc may mắn!");
        tvResult.setVisibility(View.VISIBLE);
        if (betPanel != null) betPanel.setVisibility(View.GONE);

        raceRunning = true;
        btnStart.setEnabled(false);
        btnStart.setText("Đang đua...");
        btnTutorial.setEnabled(false);
        setBetButtonsEnabled(false);

        // Tăng tốc GIF khi đua
        for (GifDrawable gif : gifThumbs) {
            if (gif != null) {
                gif.setSpeed(2.0f);
            }
        }

        // Hủy tất cả animation cũ
        for (int i = 0; i < duckAnimators.length; i++) {
            if (duckAnimators[i] != null) {
                duckAnimators[i].cancel();
                duckAnimators[i] = null;
            }
        }
        handler.removeCallbacksAndMessages(null);
        grassHandler.removeCallbacksAndMessages(null);
        waterHandler.removeCallbacksAndMessages(null);
        raceFinished = false;
        raceStartTime = System.currentTimeMillis();

        finishLine.setVisibility(View.VISIBLE);
        final float initialFinishX = screenWidth + 400;

        finishLine.post(() -> {
            targetFinishX = seekBars[0].getX() + seekBars[0].getWidth();
            finishLine.setX(initialFinishX);

            // Khởi tạo Grass background
            grass1.post(() -> {
                grassWidth = grass1.getWidth();
                grass1.setX(0);
                grass2.setX(grassWidth);
                grassRunning = true;
                grassHandler.post(grassRunnable);
            });

            // Khởi tạo Water background
            water1.post(() -> {
                waterWidth = water1.getWidth();
                water1.setX(0);
                water2.setX(waterWidth);
                waterRunning = true;
                waterHandler.post(waterRunnable);
            });

            finishLineMoving = true;
            waterHandler.post(finishRunnable);
        });

        final int betForRace = betAmount;
        final int chosenDuckAtStart = selectedDuckIndex;

        final int SMOOTH_MAX = 10000;
        int[] winnerIndex = {-1};

        for (int i = 0; i < seekBars.length; i++) {
            final int index = i;
            seekBars[index].setMax(SMOOTH_MAX);
            seekBars[index].setProgress(0);

            final float baseSpeed = (new Random().nextFloat() * 0.5f + 0.5f);
            final long totalDuration = (long)(10000 + baseSpeed * 5000);

            ObjectAnimator animator = ObjectAnimator.ofInt(seekBars[index], "progress", 0, SMOOTH_MAX);
            animator.setDuration(totalDuration);
            animator.setInterpolator(new LinearInterpolator());

            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                private boolean hasFinished = false;

                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    if (raceFinished || hasFinished) return;

                    int currentValue = (int) animation.getAnimatedValue();

                    if (currentValue >= SMOOTH_MAX && !hasFinished) {
                        hasFinished = true;
                        if (!raceFinished) {
                            raceFinished = true;
                            winnerIndex[0] = index;

                            handler.postDelayed(() -> {
                                for (ObjectAnimator anim : duckAnimators) {
                                    if (anim != null) anim.cancel();
                                }

                                seekBars[index].setMax(100);
                                seekBars[index].setProgress(100);

                                handlePayout(index, betForRace, chosenDuckAtStart);
                                stopAllRunnables();
                                announceWinner(index);
                            }, 100);
                        }
                    }
                }
            });

            duckAnimators[index] = animator;
            animator.start();
        }
    }

    // Runnable cho Grass background
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

    // Runnable cho Water background
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
                waterHandler.postDelayed(this, 16);
            }
        }
    };

    private void stopAllRunnables() {
        // Reset tốc độ GIF về bình thường
        for (GifDrawable gif : gifThumbs) {
            if (gif != null) {
                gif.setSpeed(1.0f);
            }
        }

        // Hủy tất cả animators
        for (int i = 0; i < duckAnimators.length; i++) {
            if (duckAnimators[i] != null) {
                duckAnimators[i].cancel();
                duckAnimators[i] = null;
            }
        }

        handler.removeCallbacksAndMessages(null);
        grassHandler.removeCallbacksAndMessages(null);
        waterHandler.removeCallbacksAndMessages(null);
        grassRunning = false;
        waterRunning = false;
        finishLineMoving = false;

        runOnUiThread(() -> {
            raceRunning = false;
            btnStart.setEnabled(true);
            btnStart.setText("Bắt đầu đua");
            setBetButtonsEnabled(true);
            if (betPanel != null) betPanel.setVisibility(View.VISIBLE);
            clearCurrentBet();

            if (finishLine != null) finishLine.setVisibility(View.GONE);
            for (int i = 0; i < seekBars.length; i++) {
                try {
                    seekBars[i].setMax(100);
                    seekBars[i].setProgress(0);
                } catch (Exception ignored) {}
            }
            btnTutorial.setEnabled(true);
        });
    }

    private void announceWinner(int winnerIndex) {
        Toast.makeText(this, "🏆 Vịt thắng: " + (winnerIndex + 1), Toast.LENGTH_LONG).show();
        Intent intent = new Intent(MainActivity.this, RaceResultsActivity.class);
        startActivity(intent);
    }

    private void updateBalanceText() {
        tvBalance.setText("Balance: " + betManager.formatInt(betManager.getBalance()));
    }

    private void setBetButtonsEnabled(boolean enabled) {
        if (btnBet1 != null) btnBet1.setEnabled(enabled);
        if (btnBet2 != null) btnBet2.setEnabled(enabled);
        if (btnBet3 != null) btnBet3.setEnabled(enabled);
        if (btnBet4 != null) btnBet4.setEnabled(enabled);
        if (btnCancelBet != null) btnCancelBet.setEnabled(enabled);
    }

    private void promptBetForDuck(int duckIndex) {
        if (currentBetAmount > 0 && selectedDuckIndex != duckIndex) {
            Toast.makeText(this, "Đã có cược đang chọn, bấm Hủy cược để đổi", Toast.LENGTH_SHORT).show();
            return;
        }

        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Đặt cho vịt " + (duckIndex + 1));

        final EditText input = new EditText(this);
        input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        input.setHint("Nhập tiền (min 100)");
        int padding = (int) (8 * getResources().getDisplayMetrics().density);
        input.setPadding(padding, padding, padding, padding);
        builder.setView(input);

        builder.setPositiveButton("OK", (dialog, which) -> {
            int amount = betManager.parseAmount(input.getText() != null ? input.getText().toString() : "");
            BetManager.ValidationResult vr = betManager.validateBet(amount);
            if (!vr.valid) {
                Toast.makeText(this, vr.message != null ? vr.message : "Tiền cược không hợp lệ", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
                return;
            }
            selectedDuckIndex = duckIndex;
            currentBetAmount = amount;
            updateBetLabels();
            updateBetButtonsVisibility();
            Toast.makeText(this, "Đã chọn vịt " + (duckIndex + 1) + ": " + betManager.formatInt(amount), Toast.LENGTH_SHORT).show();
        });

        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void cancelCurrentBet() {
        if (currentBetAmount == 0) {
            Toast.makeText(this, "Chưa có cược để hủy", Toast.LENGTH_SHORT).show();
            return;
        }
        selectedDuckIndex = -1;
        currentBetAmount = 0;
        updateBetLabels();
        updateBetButtonsVisibility();
        Toast.makeText(this, "Đã hủy cược", Toast.LENGTH_SHORT).show();
    }

    private void clearCurrentBet() {
        selectedDuckIndex = -1;
        currentBetAmount = 0;
        updateBetLabels();
        updateBetButtonsVisibility();
    }

    private void updateBetLabels() {
        if (tvBet1 != null) { tvBet1.setVisibility(View.GONE); tvBet1.setText("0"); }
        if (tvBet2 != null) { tvBet2.setVisibility(View.GONE); tvBet2.setText("0"); }
        if (tvBet3 != null) { tvBet3.setVisibility(View.GONE); tvBet3.setText("0"); }
        if (tvBet4 != null) { tvBet4.setVisibility(View.GONE); tvBet4.setText("0"); }

        if (currentBetAmount > 0 && selectedDuckIndex >= 0) {
            String text = betManager.formatInt(currentBetAmount);
            if (selectedDuckIndex == 0 && tvBet1 != null) { tvBet1.setText(text); tvBet1.setVisibility(View.VISIBLE); }
            if (selectedDuckIndex == 1 && tvBet2 != null) { tvBet2.setText(text); tvBet2.setVisibility(View.VISIBLE); }
            if (selectedDuckIndex == 2 && tvBet3 != null) { tvBet3.setText(text); tvBet3.setVisibility(View.VISIBLE); }
            if (selectedDuckIndex == 3 && tvBet4 != null) { tvBet4.setText(text); tvBet4.setVisibility(View.VISIBLE); }
        }
    }

    private void updateBetButtonsVisibility() {
        if (btnBet1 != null) btnBet1.setVisibility(selectedDuckIndex == 0 && currentBetAmount > 0 ? View.GONE : View.VISIBLE);
        if (btnBet2 != null) btnBet2.setVisibility(selectedDuckIndex == 1 && currentBetAmount > 0 ? View.GONE : View.VISIBLE);
        if (btnBet3 != null) btnBet3.setVisibility(selectedDuckIndex == 2 && currentBetAmount > 0 ? View.GONE : View.VISIBLE);
        if (btnBet4 != null) btnBet4.setVisibility(selectedDuckIndex == 3 && currentBetAmount > 0 ? View.GONE : View.VISIBLE);
    }

    private void handlePayout(int winnerIndex, int betAmount, int chosenIndex) {
        boolean win = (winnerIndex == chosenIndex);
        int profit = betManager.settle(win, betAmount);
        if (win) {
            tvResult.setText("Bạn thắng! +" + betManager.formatInt(profit) + ". Vịt " + (winnerIndex + 1) + " về nhất.");
        } else {
            tvResult.setText("Bạn thua cược. Vịt " + (winnerIndex + 1) + " về nhất.");
        }
        tvResult.setVisibility(View.VISIBLE);
        updateBalanceText();
    }

    private String getBetTaunt(int amount) {
        if (amount == 100) return "Cược ít thế!";
        if (amount == 1000) return "Thần tài đến, thần tài đến!";
        return "Chắc cốp dị cha!";
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Dọn dẹp GIF khi destroy
        for (GifDrawable gif : gifThumbs) {
            if (gif != null) {
                gif.recycle();
            }
        }
    }
}