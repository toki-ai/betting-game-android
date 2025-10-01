package com.example.bettinggame;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.bettinggame.services.AudioManagerUnified;

public class LoginActivity extends AppCompatActivity {

    private EditText etUsername;
    private Button btnLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        // Initialize audio
        AudioManagerUnified.initialize(this);

        etUsername = findViewById(R.id.etUsername);
        btnLogin = findViewById(R.id.btnLogin);

        etUsername.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (count > 0) { // Chỉ phát sound khi thêm text
                    AudioManagerUnified.playTypeSound(LoginActivity.this);
                }
            }
            
            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });

        // Xóa music button listener

        btnLogin.setOnClickListener(v -> {
            AudioManagerUnified.playButtonSound(this);
            String username = etUsername.getText().toString().trim();
            if (username.isEmpty()) {
                Toast.makeText(this, "Please enter username", Toast.LENGTH_SHORT).show();
                return;
            }

            SharedPreferences prefs = getSharedPreferences("BettingGamePrefs", Context.MODE_PRIVATE);

            int coin;
            if (prefs.contains(username)) {
                coin = prefs.getInt(username, 0);
                Toast.makeText(this, "Welcome back " + username + " (Coins: " + coin + ")", Toast.LENGTH_SHORT).show();
            } else {
                coin = Constants.DEFAULT_BALANCE;
                prefs.edit().putInt(username, coin).apply();
                Toast.makeText(this, "New user created: " + username + " (Coins: " + coin + ")", Toast.LENGTH_SHORT).show();
            }

            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            intent.putExtra("username", username);
            startActivity(intent);
            finish();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        AudioManagerUnified.onActivityResumed(this);
        // Xóa updateMusicUI
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