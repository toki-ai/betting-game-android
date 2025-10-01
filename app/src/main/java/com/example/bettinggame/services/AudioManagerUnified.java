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
    private static boolean isGameMusic = false;  // Đánh dấu đây có phải nhạc game không
    private static SoundPool soundPool;
    private static int soundButtonId = -1;
    private static int soundTypeId = -1;
    private static boolean soundsLoaded = false;
    private static AudioManager audioManager;
    private static AudioFocusRequest audioFocusRequest;
    private static int failCount = 0;
    private static boolean isLowMemoryMode = false;
    private static Context appContext; // Giữ context để recovery

    // Focus listener cho game music
    private static final AudioManager.OnAudioFocusChangeListener focusChangeListener = new AudioManager.OnAudioFocusChangeListener() {
        @Override
        public void onAudioFocusChange(int focusChange) {
            switch (focusChange) {
                case AudioManager.AUDIOFOCUS_LOSS:
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                    if (mediaPlayer != null && musicPlaying) {
                        mediaPlayer.pause();
                        musicPlaying = false;
                    }
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                    if (mediaPlayer != null && musicPlaying) {
                        mediaPlayer.setVolume(0.2f, 0.2f);
                    }
                    break;
                case AudioManager.AUDIOFOCUS_GAIN:
                    if (mediaPlayer != null) {
                        mediaPlayer.setVolume(0.8f, 0.8f);
                        if (!musicPlaying && isGameMusic) {
                            mediaPlayer.start();
                            musicPlaying = true;
                        }
                    }
                    break;
            }
        }
    };

    public static void initialize(Context context) {
        appContext = context.getApplicationContext(); // Lưu application context
        audioManager = (AudioManager) appContext.getSystemService(Context.AUDIO_SERVICE);
        initMediaPlayer(appContext);
        initSoundPool(appContext);
    }

    private static void initMediaPlayer(Context context) {
        try {
            if (mediaPlayer == null && !isLowMemoryMode) {
                mediaPlayer = MediaPlayer.create(context, R.raw.enjoy);
                if (mediaPlayer != null) {
                    mediaPlayer.setLooping(true);
                    mediaPlayer.setVolume(0.8f, 0.8f);
                    
                    mediaPlayer.setOnErrorListener((mp, what, extra) -> {
                        Log.e("AudioUnified", "MediaPlayer error: " + what + "/" + extra);
                        resetMediaPlayer();
                        return true;
                    });
                    
                    musicPrepared = true;
                    Log.d("AudioUnified", "MediaPlayer initialized");
                } else {
                    Log.e("AudioUnified", "Failed to create MediaPlayer");
                }
            }
        } catch (Exception e) {
            Log.e("AudioUnified", "Exception in initMediaPlayer", e);
            musicPrepared = false;
            isLowMemoryMode = true;
        }
    }

    private static void resetMediaPlayer() {
        try {
            if (mediaPlayer != null) {
                mediaPlayer.release();
                mediaPlayer = null;
            }
            musicPlaying = false;
            musicPrepared = false;
        } catch (Exception e) {
            Log.e("AudioUnified", "Error resetting MediaPlayer", e);
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
    // Game music control
    public static void startGameMusic(Context context) {
        try {
            isGameMusic = true;
            
            if (isLowMemoryMode) {
                Log.w("AudioUnified", "Low memory mode, skipping music");
                return;
            }
            
            requestFocus(context, AudioManager.AUDIOFOCUS_GAIN);
            
            if (!musicPrepared) {
                initMediaPlayer(context);
            }
            
            if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
                mediaPlayer.start();
                musicPlaying = true;
                Log.d("AudioUnified", "Game music started");
            }
        } catch (Exception e) {
            Log.e("AudioUnified", "Exception in startGameMusic", e);
            resetMediaPlayer();
        }
    }

    public static void stopGameMusic() {
        try {
            isGameMusic = false;
            if (mediaPlayer != null) {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                }
                mediaPlayer.release();
                mediaPlayer = null;
                musicPlaying = false;
                musicPrepared = false;
                Log.d("AudioUnified", "Game music stopped");
            }
            abandonFocus();
        } catch (Exception e) {
            Log.e("AudioUnified", "Exception in stopGameMusic", e);
            mediaPlayer = null;
            musicPlaying = false;
            musicPrepared = false;
        }
    }

    // Sound effects
    public static void playButtonSound(Context context) {
        playSoundEffect(context, soundButtonId, 1.0f);
    }

    public static void playTypeSound(Context context) {
        playSoundEffect(context, soundTypeId, 0.5f);
    }

    private static void playSoundEffect(Context context, int soundId, float volume) {
        if (!soundsLoaded || soundPool == null || soundId <= 0) {
            initSoundPool(context);
            return;
        }

        int streamId = soundPool.play(soundId, volume, volume, 1, 0, 1f);
        if (streamId == 0) {
            failCount++;
            if (failCount > 3) {
                cleanupSounds();
                initSoundPool(context);
                failCount = 0;
            }
        }
    }

    // Memory management
    public static void onLowMemory() {
        Log.w("AudioUnified", "Low memory detected");
        isLowMemoryMode = true;
        if (mediaPlayer != null) {
            resetMediaPlayer();
        }
    }

    public static void onTrimMemory(int level) {
        if (level >= android.content.ComponentCallbacks2.TRIM_MEMORY_MODERATE) {
            onLowMemory();
        }
    }

    // Lifecycle methods (minimal)
    public static void onActivityResumed(Context context) {
        // Chỉ để giữ compatibility
    }

    public static void onActivityPaused() {
        // Chỉ để giữ compatibility
    }

    // Helper methods
    private static void requestFocus(Context context, int focusGain) {
        if (audioManager == null) return;
        try {
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
        } catch (Exception e) {
            Log.e("AudioUnified", "Error requesting audio focus", e);
        }
    }

    private static void abandonFocus() {
        if (audioManager != null) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && audioFocusRequest != null) {
                    audioManager.abandonAudioFocusRequest(audioFocusRequest);
                } else {
                    audioManager.abandonAudioFocus(focusChangeListener);
                }
            } catch (Exception e) {
                Log.e("AudioUnified", "Error abandoning audio focus", e);
            }
        }
    }

    // Cleanup
    public static void cleanup() {
        stopGameMusic();
        cleanupSounds();
    }

    private static void cleanupSounds() {
        if (soundPool != null) {
            soundPool.release();
            soundPool = null;
            soundsLoaded = false;
            soundButtonId = soundTypeId = -1;
            failCount = 0;
        }
    }
}