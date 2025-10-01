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
        tutorialSteps.add(new TutorialStep(R.drawable.gameplay, "Đây là màn hình chính của trò chơi, nơi bạn có thể thấy các đường đua và những chú vịt."));
        tutorialSteps.add(new TutorialStep(R.drawable.balance, "Đây là số dư hiện tại của bạn. Hãy theo dõi để biết mình còn bao nhiêu tiền nhé!"));
        tutorialSteps.add(new TutorialStep(R.drawable.music, "Bạn có thể bật hoặc tắt nhạc nền của trò chơi bằng nút này."));
        tutorialSteps.add(new TutorialStep(R.drawable.tutorial, "Nhấn vào đây để xem lại hướng dẫn chơi game bất cứ lúc nào."));
        tutorialSteps.add(new TutorialStep(R.drawable.logout, "Sử dụng nút này để đăng xuất khỏi tài khoản hiện tại."));
        tutorialSteps.add(new TutorialStep(R.drawable.deposit, "Nếu hết tiền, bạn có thể nạp thêm bằng cách nhấn vào nút này."));
        tutorialSteps.add(new TutorialStep(R.drawable.deposit_detail, "Nhập số tiền bạn muốn nạp và xác nhận."));
        tutorialSteps.add(new TutorialStep(R.drawable.bet, "Chọn một chú vịt và nhấn nút 'Đặt' để đặt cược cho chú vịt đó."));
        tutorialSteps.add(new TutorialStep(R.drawable.bet_detail, "Nhập số tiền bạn muốn cược cho chú vịt đã chọn."));
        tutorialSteps.add(new TutorialStep(R.drawable.start, "Sau khi đã sẵn sàng, nhấn 'Bắt đầu đua' để cuộc đua bắt đầu!"));
    }

    private void updateNavigationButtons(int position) {
        if (tutorialSteps.isEmpty()) {
            buttonPrevious.setVisibility(View.GONE);
            buttonNext.setVisibility(View.GONE);
            buttonStartGame.setVisibility(View.GONE);
            return;
        }

        if (position == 0) {
            buttonPrevious.setVisibility(View.INVISIBLE);
        } else {
            buttonPrevious.setVisibility(View.VISIBLE);
        }

        if (position == tutorialSteps.size() - 1) {
            buttonNext.setVisibility(View.INVISIBLE);
            buttonStartGame.setVisibility(View.VISIBLE);
        } else {
            buttonNext.setVisibility(View.VISIBLE);
            buttonStartGame.setVisibility(View.GONE);
        }
    }
}
