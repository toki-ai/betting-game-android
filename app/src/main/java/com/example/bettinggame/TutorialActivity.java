package com.example.bettinggame;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;
import com.example.bettinggame.model.TutorialStep;
import java.util.ArrayList;
import java.util.List;

public class TutorialActivity extends AppCompatActivity {

    private ViewPager2 viewPagerTutorial;
    private TutorialAdapter tutorialAdapter;
    private List<TutorialStep> tutorialSteps;
    private ImageButton buttonPrevious;
    private ImageButton buttonNext;
    private Button buttonStartGame;
    private String playerName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutorial);

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("username")) {
            String name = intent.getStringExtra("username");
            if (name != null && !name.trim().isEmpty()) {
                playerName = name.trim();
            }
        }

        viewPagerTutorial = findViewById(R.id.viewPagerTutorial);
        buttonPrevious = findViewById(R.id.buttonPrevious);
        buttonNext = findViewById(R.id.buttonNext);
        buttonStartGame = findViewById(R.id.buttonStartGame);

        loadTutorialSteps();

        tutorialAdapter = new TutorialAdapter(this, tutorialSteps);
        viewPagerTutorial.setAdapter(tutorialAdapter);

        updateNavigationButtons(0); // Initial state for the first page

        viewPagerTutorial.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                updateNavigationButtons(position);
            }
        });

        buttonPrevious.setOnClickListener(v -> {
            int currentItem = viewPagerTutorial.getCurrentItem();
            if (currentItem > 0) {
                viewPagerTutorial.setCurrentItem(currentItem - 1);
            }
        });

        buttonNext.setOnClickListener(v -> {
            int currentItem = viewPagerTutorial.getCurrentItem();
            if (currentItem < tutorialSteps.size() - 1) {
                viewPagerTutorial.setCurrentItem(currentItem + 1);
            }
        });

        buttonStartGame.setOnClickListener(v -> {
            Intent startGameIntent = new Intent(TutorialActivity.this, MainActivity.class);
            startGameIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startGameIntent.putExtra("username", playerName);
            startActivity(startGameIntent);
            finish(); // Close tutorial activity
        });
    }

    private void loadTutorialSteps() {
        tutorialSteps = new ArrayList<>();
        // Replace with your actual image resources and descriptions
        tutorialSteps.add(new TutorialStep(R.drawable.placeholder_image_1, "Bước 1: Chào mừng bạn đến với Game Đua Vịt!"));
        tutorialSteps.add(new TutorialStep(R.drawable.placeholder_image_2, "Bước 2: Tìm hiểu cách đặt cược vào những chú vịt yêu thích của bạn."));
        tutorialSteps.add(new TutorialStep(R.drawable.placeholder_image_3, "Bước 3: Theo dõi cuộc đua và xem vịt nào sẽ về đích đầu tiên!"));
        tutorialSteps.add(new TutorialStep(R.drawable.placeholder_image_1, "Bước 4: Xem kết quả và nhận phần thưởng nếu bạn thắng!"));
        // Add more steps as needed
    }

    private void updateNavigationButtons(int position) {
        if (tutorialSteps.isEmpty()) {
            buttonPrevious.setVisibility(View.GONE);
            buttonNext.setVisibility(View.GONE);
            buttonStartGame.setVisibility(View.GONE);
            return;
        }

        // Previous button visibility
        if (position == 0) {
            buttonPrevious.setVisibility(View.INVISIBLE); // Hoặc View.GONE nếu bạn không muốn nó chiếm không gian
        } else {
            buttonPrevious.setVisibility(View.VISIBLE);
        }

        // Next button and Start Game button visibility
        if (position == tutorialSteps.size() - 1) {
            buttonNext.setVisibility(View.INVISIBLE); // Hoặc View.GONE
            buttonStartGame.setVisibility(View.VISIBLE);
        } else {
            buttonNext.setVisibility(View.VISIBLE);
            buttonStartGame.setVisibility(View.GONE);
        }
    }
}
