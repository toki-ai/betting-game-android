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
    private int selectedDuckIndex = -1;
    private int currentBetAmount = 0;

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
        if (currentBetAmount > 0 && selectedDuckIndex != duckIndex) {
            Toast.makeText(activity, "Đã có cược đang chọn, bấm Hủy cược để đổi", Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("Đặt cho vịt " + (duckIndex + 1));

        final EditText input = new EditText(activity);
        input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        input.setHint("Nhập tiền (min 100)");
        int padding = (int) (8 * activity.getResources().getDisplayMetrics().density);
        input.setPadding(padding, padding, padding, padding);
        builder.setView(input);

        builder.setPositiveButton("OK", (dialog, which) -> {
            int amount = betManager.parseAmount(input.getText() != null ? input.getText().toString() : "");
            BettingConfig.ValidationResult vr = betManager.validateBet(amount);
            if (!vr.valid) {
                Toast.makeText(activity, vr.message != null ? vr.message : "Tiền cược không hợp lệ", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
                return;
            }
            selectedDuckIndex = duckIndex;
            currentBetAmount = amount;
            updateBetLabels();
            updateBetButtonsVisibility();
            Toast.makeText(activity, "Đã chọn vịt " + (duckIndex + 1) + ": " + betManager.formatInt(amount), Toast.LENGTH_SHORT).show();
        });

        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    public void cancelCurrentBet(int duckIndex) {
        if (currentBetAmount == 0 || selectedDuckIndex != duckIndex) {
            Toast.makeText(activity, "Chưa có cược để hủy", Toast.LENGTH_SHORT).show();
            return;
        }
        selectedDuckIndex = -1;
        currentBetAmount = 0;
        updateBetLabels();
        updateBetButtonsVisibility();
        Toast.makeText(activity, "Đã hủy cược", Toast.LENGTH_SHORT).show();
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

        if (currentBetAmount > 0 && selectedDuckIndex >= 0) {
            String text = betManager.formatInt(currentBetAmount);
            if (betTextViews[selectedDuckIndex] != null) {
                betTextViews[selectedDuckIndex].setText(text);
                betTextViews[selectedDuckIndex].setVisibility(View.VISIBLE);
                cancelBetButtons[selectedDuckIndex].setVisibility(View.VISIBLE);
            }
        }
    }

    private void updateBetButtonsVisibility() {
        for (int i = 0; i < betButtons.length; i++) {
            if (betButtons[i] != null) {
                betButtons[i].setVisibility(selectedDuckIndex == i && currentBetAmount > 0 ? View.GONE : View.VISIBLE);
            }
        }
    }

    public void clearCurrentBet() {
        selectedDuckIndex = -1;
        currentBetAmount = 0;
        updateBetLabels();
        updateBetButtonsVisibility();
    }

    public int getSelectedDuckIndex() {
        return selectedDuckIndex;
    }

    public int getCurrentBetAmount() {
        return currentBetAmount;
    }
}