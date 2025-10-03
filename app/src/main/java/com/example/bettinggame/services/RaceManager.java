package com.example.bettinggame.services;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.bettinggame.MainActivity;
import com.example.bettinggame.R;
import com.example.bettinggame.RaceResultsActivity;

import com.example.bettinggame.model.RaceResult;
import com.example.bettinggame.model.Duck;
import com.example.bettinggame.services.AudioManagerUnified;

import pl.droidsonroids.gif.GifDrawable;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RaceManager {
    private final AppCompatActivity activity;
    private final SeekBar[] seekBars;
    private final int screenWidth;
    private final BettingConfig betManager;
    private final BackgroundAnimationManager backgroundAnimationManager;
    private final FinishLineManager finishLineManager;
    private final Handler handler = new Handler();
    private ObjectAnimator[] duckAnimators = new ObjectAnimator[4];
    private GifDrawable[] gifThumbs = new GifDrawable[4];
    private boolean raceRunning = false;
    private boolean raceFinished = false;
    private long raceStartTime = 0;
    private String playerName;
    public final List<Duck> DUCK_LIST = List.of(new Duck("Vịt 1"), new Duck("Vịt 2"), new Duck("Vịt 3"), new Duck("Vịt 4"));

    public RaceManager(AppCompatActivity activity, SeekBar[] seekBars, int screenWidth, BettingConfig betManager,
                       BackgroundAnimationManager backgroundAnimationManager, FinishLineManager finishLineManager, String playerName) {
        this.activity = activity;
        this.seekBars = seekBars;
        this.screenWidth = screenWidth;
        this.betManager = betManager;
        this.backgroundAnimationManager = backgroundAnimationManager;
        this.finishLineManager = finishLineManager;
        this.playerName = playerName;
        initializeGifs();
    }

    private void initializeGifs() {
        int thumbSize = dpToPx(60);
        try {
            for (int i = 0; i < seekBars.length; i++) {
                gifThumbs[i] = new GifDrawable(activity.getResources(), R.drawable.animal_idle);
                gifThumbs[i].setBounds(0, 0, thumbSize, thumbSize);
                seekBars[i].setThumb(gifThumbs[i]);
                gifThumbs[i].start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private int dpToPx(int dp) {
        return (int) (dp * activity.getResources().getDisplayMetrics().density);
    }

    public void startRace() {
        if (raceRunning) return;

        int[] laneBets = ((MainActivity) activity).getBettingManager().getBetAmounts();
        int totalBet = 0; for (int v : laneBets) totalBet += v;
        if (totalBet <= 0) {
            Toast.makeText(activity, "Hãy đặt cược ít nhất 1 lane", Toast.LENGTH_SHORT).show();
            return;
        }
        if (totalBet > betManager.getBalance()) {
            Toast.makeText(activity, "Tổng cược vượt số dư", Toast.LENGTH_SHORT).show();
            return;
        }

        // Trừ tổng cược ngay khi bắt đầu
        betManager.applyBetStart(totalBet);
        ((MainActivity) activity).updateBalanceText();
        Toast.makeText(activity, "Đặt tổng: " + betManager.formatInt(totalBet), Toast.LENGTH_SHORT).show();

        raceRunning = true;
        ((MainActivity) activity).findViewById(R.id.betPanel).setVisibility(View.GONE);
        Button btnStart = activity.findViewById(R.id.btnStart);
        btnStart.setEnabled(false);
        btnStart.setText("Đang đua...");
        activity.findViewById(R.id.btnTutorial).setEnabled(false);
        setBetButtonsEnabled(false);

        // Bắt đầu phát nhạc game
        AudioManagerUnified.startGameMusic(activity);

        updateGifThumbsForRace();
        backgroundAnimationManager.startBackgroundAnimations(); // Start background animations
        finishLineManager.startFinishLineMovement(seekBars[0]); // Start finish line movement

        handler.removeCallbacksAndMessages(null);
        raceFinished = false;
        raceStartTime = System.currentTimeMillis();

        final int[] betsForRace = laneBets.clone();
        final int SMOOTH_MAX = 10000;
        int[] winnerIndex = {-1};

        for (int i = 0; i < seekBars.length; i++) {
            final int index = i;
            seekBars[index].setMax(SMOOTH_MAX);
            seekBars[index].setProgress(0);

            final long totalDuration = 5000 + new Random().nextInt(10000); // 5–15s


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

                                // 1. Snapshot all ducks' position
                                List<RaceResult> results = new ArrayList<>();
                                for (int j = 0; j < seekBars.length; j++) {
                                    int progress = seekBars[j].getProgress();
                                    Duck duck = DUCK_LIST.get(j);
                                    RaceResult rr = new RaceResult(duck, 0, 0);
                                    rr.setProgress(progress);
                                    results.add(rr);
                                }

                                // 2. Sort by progress (descending)
                                results.sort((a, b) -> Integer.compare(b.getProgress(), a.getProgress()));

                                // 3. Assign ranks và tính tiền thắng/thua
                                for (int r = 0; r < results.size(); r++) {
                                    results.get(r).setRank(r + 1);
                                    
                                    // Tìm lane index của duck này
                                    int laneIndex = -1;
                                    for (int k = 0; k < DUCK_LIST.size(); k++) {
                                        if (DUCK_LIST.get(k).getName().equals(results.get(r).getDuck().getName())) {
                                            laneIndex = k;
                                            break;
                                        }
                                    }
                                    
                                    if (laneIndex >= 0 && laneIndex < betsForRace.length) {
                                        int betOnThisLane = betsForRace[laneIndex];
                                        
                                        if (r == 0) {
                                            // Rank 1: Thắng gấp đôi
                                            results.get(r).setAmountWon(betOnThisLane * 2);
                                        } else if (betOnThisLane > 0) {
                                            // Đã đặt nhưng thua: mất tiền
                                            results.get(r).setAmountWon(-betOnThisLane);
                                        } else {
                                            // Không đặt
                                            results.get(r).setAmountWon(0);
                                        }
                                    }
                                }
                                
                                seekBars[index].setMax(100);
                                seekBars[index].setProgress(100);
                                handlePayoutMulti(index, betsForRace);
                                resetRace();
                                
                                // Tắt nhạc game khi kết thúc đua
                                AudioManagerUnified.stopGameMusic();
                                
                                announceWinner(index, results);
                            }, 100);
                        }
                    }
                }
            });

            duckAnimators[index] = animator;
            animator.start();
        }
    }

    private void updateGifThumbsForRace() {
        int thumbSize = dpToPx(60);
        try {
            for (int i = 0; i < seekBars.length; i++) {
                if (gifThumbs[i] != null) {
                    gifThumbs[i].stop();
                    gifThumbs[i].recycle();
                }
                gifThumbs[i] = new GifDrawable(activity.getResources(), R.drawable.animal_run);
                gifThumbs[i].setBounds(0, 0, thumbSize, thumbSize);
                seekBars[i].setThumb(gifThumbs[i]);
                gifThumbs[i].setSpeed(2.0f);
                gifThumbs[i].start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void resetGifs() {
        int thumbSize = dpToPx(60);
        try {
            for (int i = 0; i < seekBars.length; i++) {
                if (gifThumbs[i] != null) {
                    gifThumbs[i].stop();
                    gifThumbs[i].recycle();
                }
                gifThumbs[i] = new GifDrawable(activity.getResources(), R.drawable.animal_idle);
                gifThumbs[i].setBounds(0, 0, thumbSize, thumbSize);
                seekBars[i].setThumb(gifThumbs[i]);
                gifThumbs[i].setSpeed(1.0f);
                gifThumbs[i].start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setBetButtonsEnabled(boolean enabled) {
        Button[] betButtons = new Button[]{
                activity.findViewById(R.id.btnBet1),
                activity.findViewById(R.id.btnBet2),
                activity.findViewById(R.id.btnBet3),
                activity.findViewById(R.id.btnBet4)
        };
        ImageButton[] cancelButtons = new ImageButton[]{
                activity.findViewById(R.id.btnCancelBet1),
                activity.findViewById(R.id.btnCancelBet2),
                activity.findViewById(R.id.btnCancelBet3),
                activity.findViewById(R.id.btnCancelBet4)
        };
        for (Button btn : betButtons) {
            if (btn != null) btn.setEnabled(enabled);
        }
        for (ImageButton btn : cancelButtons) {
            if (btn != null) btn.setEnabled(enabled);
        }
    }

    private void resetRace() {
        for (int i = 0; i < duckAnimators.length; i++) {
            if (duckAnimators[i] != null) {
                duckAnimators[i].cancel();
                duckAnimators[i] = null;
            }
        }
        handler.removeCallbacksAndMessages(null);
        backgroundAnimationManager.cleanup(); // Stop background animations
        finishLineManager.cleanup(); // Stop finish line movement
        raceRunning = false;

        activity.runOnUiThread(() -> {
            Button btnStart = activity.findViewById(R.id.btnStart);
            btnStart.setEnabled(true);
            btnStart.setText("Bắt đầu đua");
            activity.findViewById(R.id.betPanel).setVisibility(View.VISIBLE);
            activity.findViewById(R.id.btnTutorial).setEnabled(true);
            for (int i = 0; i < seekBars.length; i++) {
                try {
                    seekBars[i].setMax(100);
                    seekBars[i].setProgress(0);
                } catch (Exception ignored) {
                }
            }
            resetGifs();
        });
    }

    private void handlePayoutMulti(int winnerIndex, int[] betsForRace) {
        // Nếu người chơi đặt nhiều lane: lane thắng nhận 2x, lane thua không hoàn
        int winAmount = (winnerIndex >= 0 && winnerIndex < betsForRace.length) ? betsForRace[winnerIndex] : 0;
        if (winAmount > 0) {
            betManager.settle(true, winAmount);
            ((MainActivity) activity).updateBalanceText();
        }
    }

    private void announceWinner(int winnerIndex, List<RaceResult> results) {
        Toast.makeText(activity, "🏆 Vịt thắng: " + (winnerIndex + 1), Toast.LENGTH_LONG).show();
        Intent intent = new Intent(activity, RaceResultsActivity.class);
        if (activity instanceof MainActivity) {
            intent.putExtra("username", playerName);
            // Do RaceResult đã là Serializable, ta dùng putExtra cho cả ArrayList
            intent.putExtra("raceResults", new ArrayList<Serializable>(results));
        }
        activity.startActivity(intent);
    }

    private String getBetTaunt(int amount) {
        if (amount == 100) return "Cược ít thế!";
        if (amount == 1000) return "Thần tài đến, thần tài đến!";
        return "Chắc cốp dị cha!";
    }

    public void cleanup() {
        for (GifDrawable gif : gifThumbs) {
            if (gif != null) {
                gif.recycle();
            }
        }
        handler.removeCallbacksAndMessages(null);
        backgroundAnimationManager.cleanup();
        finishLineManager.cleanup();
    }
}
