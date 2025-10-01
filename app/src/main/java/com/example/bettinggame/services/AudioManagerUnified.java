package com.example.bettinggame.services;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Build;
import android.util.Log;

import com.example.bettinggame.R;

public class AudioManagerUnified {
    private static MediaPlayer mediaPlayer;
    private static boolean musicPlaying = false;
    private static boolean musicPrepared = false;
    private static SoundPool soundPool;
    private static int soundButtonId = -1;
    private static int soundTypeId = -1;
    private static boolean soundsLoaded = false;
    private static AudioManager audioManager;
    private static AudioFocusRequest audioFocusRequest;
    private static int failCount = 0;

    // Focus listener chung cho tất cả
    private static final AudioManager.OnAudioFocusChangeListener focusChangeListener = new AudioManager.OnAudioFocusChangeListener() {
        @Override
        public void onAudioFocusChange(int focusChange) {
            switch (focusChange) {
                case AudioManager.AUDIOFOCUS_LOSS:
                    // Chỉ pause, không stop/release để có thể resume sau
                    pauseMusic();
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                    pauseMusic();
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                    // Duck thay vì pause hoàn toàn
                    if (mediaPlayer != null && musicPlaying) {
                        mediaPlayer.setVolume(0.2f, 0.2f);
                    }
                    break;
                case AudioManager.AUDIOFOCUS_GAIN:
                case AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK:
                    // Restore volume và resume
                    if (mediaPlayer != null) {
                        mediaPlayer.setVolume(1.0f, 1.0f);
                    }
                    resumeMusic();
                    break;
            }
        }
    };

    public static void initialize(Context context) {
        audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        initMediaPlayer(context);
        initSoundPool(context);
    }

    private static void initMediaPlayer(Context context) {
        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer.create(context, R.raw.enjoy);
            if (mediaPlayer != null) {
                mediaPlayer.setLooping(true);
                musicPrepared = true;
            }
        }
    }

    private static void initSoundPool(Context context) {
        if (soundPool != null) return;
        AudioAttributes attrs = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();
        soundPool = new SoundPool.Builder().setMaxStreams(5).setAudioAttributes(attrs).build();

        soundButtonId = soundPool.load(context, R.raw.click, 1);
        soundTypeId = soundPool.load(context, R.raw.typing, 1);
        // Bỏ silent sound vì không cần thiết

        soundPool.setOnLoadCompleteListener((pool, sampleId, status) -> {
            if (status == 0) {
                soundsLoaded = true;
                Log.d("AudioUnified", "Sounds loaded");
            }
        });
    }

    // Music methods
    public static void startMusic(Context context) {
        requestFocus(context, AudioManager.AUDIOFOCUS_GAIN);
        if (!musicPrepared) initMediaPlayer(context);
        if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
            mediaPlayer.start();
            musicPlaying = true;
        }
    }

    public static void pauseMusic() {
        if (mediaPlayer != null && musicPlaying) {
            mediaPlayer.pause();
            musicPlaying = false;
        }
    }

    public static void resumeMusic() {
        if (mediaPlayer != null && musicPrepared && !musicPlaying) {
            mediaPlayer.start();
            musicPlaying = true;
        }
    }

    public static void stopMusic() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.reset();
            mediaPlayer.release();
            mediaPlayer = null;
            musicPlaying = false;
            musicPrepared = false;
        }
        abandonFocus();
    }

    public static boolean isMusicPlaying() {
        return musicPlaying && (mediaPlayer != null && mediaPlayer.isPlaying());
    }

    // Sound methods (tương tự cũ, nhưng dùng focus chung)
    public static void playButtonSound(Context context) {
        playSoundWithReset(context, soundButtonId, 1.0f, 1.0f);
    }

    public static void playTypeSound(Context context) {
        playSoundWithReset(context, soundTypeId, 0.5f, 0.5f);
    }

    private static void playSoundWithReset(Context context, int soundId, float leftVol, float rightVol) {
        if (!soundsLoaded || soundPool == null || soundId <= 0) {
            initSoundPool(context);
            return;
        }

        // Không request audio focus cho sound effect - để music tiếp tục phát
        int streamId = soundPool.play(soundId, leftVol, rightVol, 1, 0, 1f);
        if (streamId == 0) failCount++;
        else failCount = 0;

        if (failCount > 3) {
            cleanupSounds();
            initSoundPool(context);
        }
    }

    // Focus helpers
    private static void requestFocus(Context context, int focusGain) {
        if (audioManager == null) return;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            AudioAttributes attrs = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build();
            audioFocusRequest = new AudioFocusRequest.Builder(focusGain)
                    .setAudioAttributes(attrs)
                    .setOnAudioFocusChangeListener(focusChangeListener)
                    .build();
            audioManager.requestAudioFocus(audioFocusRequest);
        } else {
            audioManager.requestAudioFocus(focusChangeListener, AudioManager.STREAM_MUSIC, focusGain);
        }
    }

    private static void abandonFocus() {
        if (audioManager != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && audioFocusRequest != null) {
                audioManager.abandonAudioFocusRequest(audioFocusRequest);
            } else {
                audioManager.abandonAudioFocus(focusChangeListener);
            }
        }
    }

    public static void cleanup() {
        stopMusic();
        cleanupSounds();
    }

    private static void cleanupSounds() {
        if (soundPool != null) {
            soundPool.release();
            soundPool = null;
            soundsLoaded = false;
            soundButtonId = soundTypeId = -1;
        }
    }
}