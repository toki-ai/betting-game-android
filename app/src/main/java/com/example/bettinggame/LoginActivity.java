package com.example.bettinggame;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    private EditText etUsername;
    private Button btnLogin;
    private ImageButton btnTutorial, btnMusic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        etUsername = findViewById(R.id.etUsername);
        btnLogin = findViewById(R.id.btnLogin);

        btnTutorial.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, TutorialActivity.class);
            startActivity(intent);
        });

        btnMusic.setOnClickListener(v -> {
            Toast.makeText(this, "Music toggle clicked", Toast.LENGTH_SHORT).show();
        });

        btnLogin.setOnClickListener(v -> {
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
                coin = 500;
                prefs.edit().putInt(username, coin).apply();
                Toast.makeText(this, "New user created: " + username + " (Coins: " + coin + ")", Toast.LENGTH_SHORT).show();
            }

            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            intent.putExtra("username", username);
            startActivity(intent);
            finish();
        });
    }
}