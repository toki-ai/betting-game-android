package com.example.bettinggame.services;

import android.app.AlertDialog;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class BettingManager {
    private final AppCompatActivity activity;
    private final Button[] betButtons;
    private final ImageButton[] cancelBetButtons;
    private final TextView[] betTextViews;
    private final View betPanel;
    private final BettingConfig betManager;
    private int[] betAmounts = new int[]{0,0,0,0};

    public BettingManager(AppCompatActivity activity, Button btnBet1, Button btnBet2, Button btnBet3, Button btnBet4,
                          ImageButton btnCancelBet1, ImageButton btnCancelBet2, ImageButton btnCancelBet3, ImageButton btnCancelBet4,
                          TextView tvBet1, TextView tvBet2, TextView tvBet3, TextView tvBet4, View betPanel, BettingConfig betManager) {
        this.activity = activity;
        this.betButtons = new Button[]{btnBet1, btnBet2, btnBet3, btnBet4};
        this.cancelBetButtons = new ImageButton[]{btnCancelBet1, btnCancelBet2, btnCancelBet3, btnCancelBet4};
        this.betTextViews = new TextView[]{tvBet1, tvBet2, tvBet3, tvBet4};
        this.betPanel = betPanel;
        this.betManager = betManager;
        updateBetLabels();
        updateBetButtonsVisibility();
    }

    public void promptBetForDuck(int duckIndex) {

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("Đặt cho vịt " + (duckIndex + 1));

        final EditText input = new EditText(activity);
        input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        input.setHint("Nhập tiền (min 100)");
        
        // Xóa typing sound để giảm tải
        
        int padding = (int) (8 * activity.getResources().getDisplayMetrics().density);
        input.setPadding(padding, padding, padding, padding);
        builder.setView(input);

        builder.setPositiveButton("OK", (dialog, which) -> {
            // Xóa sound khỏi OK button
            int amount = betManager.parseAmount(input.getText() != null ? input.getText().toString() : "");
            // Tính số dư còn lại sau khi trừ các cược hiện có (không tính vịt đang sửa)
            int committed = 0;
            for (int i = 0; i < betAmounts.length; i++) {
                if (i == duckIndex) continue;
                committed += betAmounts[i];
            }
            int remaining = betManager.getBalance() - committed;
            if (amount < com.example.bettinggame.Constants.MIN_BET) {
                Toast.makeText(activity, "Tối thiểu " + com.example.bettinggame.Constants.MIN_BET, Toast.LENGTH_SHORT).show();
                dialog.dismiss();
                return;
            }
            if (amount > remaining) {
                Toast.makeText(activity, "Không đủ số dư cho cược này", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
                return;
            }
            betAmounts[duckIndex] = amount;
            updateBetLabels();
            updateBetButtonsVisibility();
            Toast.makeText(activity, "Đặt vịt " + (duckIndex + 1) + ": " + betManager.formatInt(amount), Toast.LENGTH_SHORT).show();
        });

        builder.setNegativeButton("Hủy", (dialog, which) -> {
            // Xóa sound khỏi Hủy button
            dialog.dismiss();
        });
        builder.show();
    }

    public void cancelCurrentBet(int duckIndex) {
        if (betAmounts[duckIndex] == 0) {
            Toast.makeText(activity, "Chưa có cược để hủy", Toast.LENGTH_SHORT).show();
            return;
        }
        betAmounts[duckIndex] = 0;
        updateBetLabels();
        updateBetButtonsVisibility();
        Toast.makeText(activity, "Đã hủy cược vịt " + (duckIndex + 1), Toast.LENGTH_SHORT).show();
    }

    private void updateBetLabels() {
        for (TextView tv : betTextViews) {
            if (tv != null) {
                tv.setVisibility(View.GONE);
                tv.setText("0");
            }
        }
        for (ImageButton btn : cancelBetButtons) {
            if (btn != null) btn.setVisibility(View.GONE);
        }
        for (int i = 0; i < betTextViews.length; i++) {
            if (betAmounts[i] > 0 && betTextViews[i] != null) {
                betTextViews[i].setText(betManager.formatInt(betAmounts[i]));
                betTextViews[i].setVisibility(View.VISIBLE);
                if (cancelBetButtons[i] != null) cancelBetButtons[i].setVisibility(View.VISIBLE);
            }
        }
    }

    private void updateBetButtonsVisibility() {
        for (int i = 0; i < betButtons.length; i++) {
            if (betButtons[i] != null) {
                betButtons[i].setVisibility(betAmounts[i] > 0 ? View.GONE : View.VISIBLE);
            }
        }
    }

    public void clearAllBets() {
        for (int i = 0; i < betAmounts.length; i++) betAmounts[i] = 0;
        updateBetLabels();
        updateBetButtonsVisibility();
    }

    public int[] getBetAmounts() { return betAmounts.clone(); }
    public int getTotalBetAmount() {
        int sum = 0; for (int v : betAmounts) sum += v; return sum;
    }
}