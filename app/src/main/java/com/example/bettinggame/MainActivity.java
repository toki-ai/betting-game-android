package com.example.bettinggame;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.Intent;
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

import com.bumptech.glide.Glide;
import com.example.bettinggame.bet.BetManager;
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

    // Betting UI
    // Per-lane bet buttons (mở dialog nhập tiền)
    private Button btnBet1, btnBet2, btnBet3, btnBet4, btnCancelBet;
    private TextView tvBet1, tvBet2, tvBet3, tvBet4;
    private TextView tvBalance, tvResult;
    private View betPanel;
    private int selectedDuckIndex = -1; // chưa chọn
    // Tách sang BetManager để quản lý số dư và validate/payout
    private BetManager betManager = new BetManager(1000);

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

        // Ánh xạ view đặt cược
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

        // Thiết lập chọn đặt cược qua dialog, chỉ cho phép một lựa chọn.
        btnBet1.setOnClickListener(v -> promptBetForDuck(0));
        btnBet2.setOnClickListener(v -> promptBetForDuck(1));
        btnBet3.setOnClickListener(v -> promptBetForDuck(2));
        btnBet4.setOnClickListener(v -> promptBetForDuck(3));
        btnCancelBet.setOnClickListener(v -> cancelCurrentBet());

        // Khởi tạo hiển thị lần đầu
        updateBetLabels();
        updateBetButtonsVisibility();

        // Cập nhật text số dư ban đầu
        updateBalanceText();

        // Không còn radio chọn vịt; xác định qua ô nhập tiền của từng lane

        btnTutorial.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, TutorialActivity.class);
            startActivity(intent);
        });
    }

    private void startRace() {
        if (raceRunning) return;

        // Sử dụng lựa chọn đã được lưu từ dialog
        if (selectedDuckIndex < 0) {
            Toast.makeText(this, "Chọn 1 con vịt để đặt cược", Toast.LENGTH_SHORT).show();
            return;
        }
        int betAmount = currentBetAmount;
        // Lấy tiền cược và kiểm tra hợp lệ trước khi bắt đầu đua
        BetManager.ValidationResult vr = betManager.validateBet(betAmount);
        if (!vr.valid) {
            Toast.makeText(this, vr.message != null ? vr.message : "Tiền cược không hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }

        // Trừ tiền cược ngay khi bắt đầu để tránh spam đổi kết quả
        betManager.applyBetStart(betAmount);
        updateBalanceText();
        // Thông báo vui tuỳ theo mức tiền cược
        Toast.makeText(this, getBetTaunt(betAmount), Toast.LENGTH_SHORT).show();
        tvResult.setText("Đang đua... Chúc may mắn!");
        tvResult.setVisibility(View.VISIBLE);
        // Ẩn panel đặt cược để người chơi theo dõi đường đua rõ ràng
        if (betPanel != null) betPanel.setVisibility(View.GONE);

        // bật cờ chạy, disable button và các control đặt cược
        raceRunning = true;
        btnStart.setEnabled(false);
        btnStart.setText("Đang đua...");
        btnTutorial.setEnabled(false);
        setBetButtonsEnabled(false);

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

        // Chốt tiền cược và lựa chọn cho vòng này để dùng trong inner class (phải là final/effectively final)
        final int betForRace = betAmount;
        final int chosenDuckAtStart = selectedDuckIndex;

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
                            // Tính payout trước khi reset trạng thái để không mất lựa chọn
                            handlePayout(index, betForRace, chosenDuckAtStart);
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
            btnStart.setText("Bắt đầu đua");
            // Mở lại control đặt cược
            setBetButtonsEnabled(true);
            // Hiện lại panel đặt cược và xoá tiền cược cũ để vòng mới rõ ràng
            if (betPanel != null) betPanel.setVisibility(View.VISIBLE);
            clearCurrentBet();

            // Đưa vạch đích và các vịt về vị trí ban đầu
            if (finishLine != null) finishLine.setVisibility(View.GONE);
            for (int i = 0; i < animals.length; i++) {
                try {
                    seekBars[i].setProgress(0);
                    // Vị trí bắt đầu = mép trái của seekbar
                    animals[i].setX(seekBars[i].getX());
                } catch (Exception ignored) {}
            }
            btnTutorial.setEnabled(true);
        });
    }

    private void announceWinner(int winnerIndex) {
        for (int j = 0; j < animals.length; j++) {
            int idleResId = getResources().getIdentifier("animal" + (j + 1) + "_idle", "drawable", getPackageName());
            Glide.with(this).load(idleResId).into(animals[j]);
        }

        Toast.makeText(this, "🏆 Vịt thắng: " + (winnerIndex + 1), Toast.LENGTH_LONG).show();
        Intent intent = new Intent(MainActivity.this, RaceResultsActivity.class);
    }

    // ====== Betting helpers ======
    private void updateBalanceText() {
        tvBalance.setText("Balance: " + betManager.formatInt(betManager.getBalance()));
    }

    // parseBetAmount -> đã chuyển sang BetManager

    private void setBetButtonsEnabled(boolean enabled) {
        if (btnBet1 != null) btnBet1.setEnabled(enabled);
        if (btnBet2 != null) btnBet2.setEnabled(enabled);
        if (btnBet3 != null) btnBet3.setEnabled(enabled);
        if (btnBet4 != null) btnBet4.setEnabled(enabled);
        if (btnCancelBet != null) btnCancelBet.setEnabled(enabled);
    }

    private int currentBetAmount = 0;

    private void promptBetForDuck(int duckIndex) {
        // Nếu đã chọn con khác rồi thì yêu cầu hủy trước khi chọn lại
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
        // Ẩn tất cả trước
        if (tvBet1 != null) { tvBet1.setVisibility(View.GONE); tvBet1.setText("0"); }
        if (tvBet2 != null) { tvBet2.setVisibility(View.GONE); tvBet2.setText("0"); }
        if (tvBet3 != null) { tvBet3.setVisibility(View.GONE); tvBet3.setText("0"); }
        if (tvBet4 != null) { tvBet4.setVisibility(View.GONE); tvBet4.setText("0"); }

        // Hiện nhãn ở con đã chọn
        if (currentBetAmount > 0 && selectedDuckIndex >= 0) {
            String text = betManager.formatInt(currentBetAmount);
            if (selectedDuckIndex == 0 && tvBet1 != null) { tvBet1.setText(text); tvBet1.setVisibility(View.VISIBLE); }
            if (selectedDuckIndex == 1 && tvBet2 != null) { tvBet2.setText(text); tvBet2.setVisibility(View.VISIBLE); }
            if (selectedDuckIndex == 2 && tvBet3 != null) { tvBet3.setText(text); tvBet3.setVisibility(View.VISIBLE); }
            if (selectedDuckIndex == 3 && tvBet4 != null) { tvBet4.setText(text); tvBet4.setVisibility(View.VISIBLE); }
        }
    }

    private void updateBetButtonsVisibility() {
        // Khi đã chọn 1 con: ẩn nút Đặt của con đó, giữ các con khác để có thể bấm -> nhưng cấm bằng thông báo
        // Theo yêu cầu: chỉ hiện lại nút đặt khi hủy
        if (btnBet1 != null) btnBet1.setVisibility(selectedDuckIndex == 0 && currentBetAmount > 0 ? View.GONE : View.VISIBLE);
        if (btnBet2 != null) btnBet2.setVisibility(selectedDuckIndex == 1 && currentBetAmount > 0 ? View.GONE : View.VISIBLE);
        if (btnBet3 != null) btnBet3.setVisibility(selectedDuckIndex == 2 && currentBetAmount > 0 ? View.GONE : View.VISIBLE);
        if (btnBet4 != null) btnBet4.setVisibility(selectedDuckIndex == 3 && currentBetAmount > 0 ? View.GONE : View.VISIBLE);
    }

    private void handlePayout(int winnerIndex, int betAmount, int chosenIndex) {
        // Nếu chọn đúng vịt thắng -> + 2x tiền cược (lợi nhuận ròng = +betAmount)
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

    // Câu châm chọc theo mức cược
    private String getBetTaunt(int amount) {
        if (amount == 100) return "Cược ít thế!";
        if (amount == 1000) return "Thần tài đến, thần tài đến!";
        return "Chắc cốp dị cha!";
    }
}
