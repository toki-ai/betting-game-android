package com.example.bettinggame.services;

import android.content.Context;
import android.media.MediaPlayer;

import com.example.bettinggame.R;

public class MusicManager {
    private static MediaPlayer mediaPlayer;
    private static boolean isPlaying = false;

    public static void startMusic(Context context) {
        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer.create(context.getApplicationContext(), R.raw.background_music);
            mediaPlayer.setLooping(true);
        }
        if (!isPlaying) {
            mediaPlayer.start();
            isPlaying = true;
        }
    }

    public static void stopMusic() {
        if (mediaPlayer != null && isPlaying) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
            isPlaying = false;
        }
    }

    public static boolean isPlaying() {
        return isPlaying;
    }
}
