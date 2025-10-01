package com.example.bettinggame;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

// Import các lớp model
import com.example.bettinggame.model.Duck;
import com.example.bettinggame.model.RaceResult;
import com.example.bettinggame.services.AudioManagerUnified;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class RaceResultsActivity extends AppCompatActivity {

    private RecyclerView recyclerViewResults;
    private RaceResultAdapter adapter;
    private List<RaceResult> raceResultsList;
    private String playerName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_race_results);

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("username")) {
            String name = intent.getStringExtra("username");
            if (name != null && !name.trim().isEmpty()) {
                playerName = name.trim();
            }
        }

        TextView textViewTitle = findViewById(R.id.textViewTitle);
        recyclerViewResults = findViewById(R.id.recyclerViewResults);
        Button buttonNewGame = findViewById(R.id.buttonNewGame);
        Button buttonQuit = findViewById(R.id.buttonQuit);

        // Nhận danh sách raceResultsList từ Intent
        if (getIntent().hasExtra("raceResults")) {
            raceResultsList = getIntent().getParcelableArrayListExtra("raceResults", RaceResult.class);
        }

        // Fallback nếu không có dữ liệu nào được truyền hoặc lỗi xảy ra
        if (raceResultsList == null) {
            createSampleData();
        }

        // Sắp xếp raceResultsList theo rank
        Collections.sort(raceResultsList, Comparator.comparingInt(RaceResult::getRank));
        
        recyclerViewResults.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RaceResultAdapter(raceResultsList, this);
        recyclerViewResults.setAdapter(adapter);

        buttonNewGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AudioManagerUnified.playButtonSound(RaceResultsActivity.this);
                Intent intent = new Intent(RaceResultsActivity.this, MainActivity.class);
                intent.putExtra("username", playerName);
                // Bỏ flags để không clear stack và giữ music state
                startActivity(intent);
                finish();
            }
        });

        buttonQuit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AudioManagerUnified.playButtonSound(RaceResultsActivity.this);
                finishAffinity();
            }
        });
    }

    // Phương thức tạo dữ liệu mẫu (chỉ dùng làm fallback hoặc cho testing ban đầu)
    private void createSampleData() {
        raceResultsList = new ArrayList<>();
        raceResultsList.add(new RaceResult(new Duck("Vịt Donald Mẫu"), 2, -50.0));
        raceResultsList.add(new RaceResult(new Duck("Vịt Daisy Mẫu"), 1, 150.0));
        raceResultsList.add(new RaceResult(new Duck("Vịt Daffy Mẫu"), 3, 0.0));
    }

    @Override
    protected void onResume() {
        super.onResume();
        AudioManagerUnified.onActivityResumed(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        AudioManagerUnified.onActivityPaused();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        AudioManagerUnified.onLowMemory();
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        AudioManagerUnified.onTrimMemory(level);
    }
}
