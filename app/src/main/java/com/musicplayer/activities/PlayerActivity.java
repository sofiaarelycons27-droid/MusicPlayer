package com.musicplayer.activities;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.musicplayer.R;
import com.musicplayer.database.DatabaseManager;
import com.musicplayer.models.Song;

public class PlayerActivity extends AppCompatActivity {

    // Views - tvGenre ELIMINADO
    private ImageView ivArtwork;
    private TextView tvTitle, tvArtist, tvCurrentTime, tvTotalTime;  // ← tvGenre removido
    private ImageButton btnPlayPause, btnPrevious, btnNext;
    private SeekBar sbProgress, sbVolume;
    private Toolbar toolbar;

    // Variables
    private MediaPlayer mediaPlayer;
    private DatabaseManager dbManager;
    private Song currentSong;
    private Handler handler = new Handler();
    private int userId;
    private int songId;
    private boolean isUserSeeking = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        dbManager = new DatabaseManager(this);

        songId = getIntent().getIntExtra("song_id", -1);
        userId = getIntent().getIntExtra("user_id", -1);

        if (songId == -1) {
            Toast.makeText(this, "Error: No se recibió el ID de la canción", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        setupToolbar();
        loadSong();
        setupVolumeControl();
        setupNextPrevious();
    }

    private void initViews() {
        ivArtwork = findViewById(R.id.ivArtwork);
        tvTitle = findViewById(R.id.tvTitle);
        tvArtist = findViewById(R.id.tvArtist);
        // tvGenre = findViewById(R.id.tvGenre);  ← ELIMINADO
        tvCurrentTime = findViewById(R.id.tvCurrentTime);
        tvTotalTime = findViewById(R.id.tvTotalTime);
        btnPlayPause = findViewById(R.id.btnPlayPause);
        btnPrevious = findViewById(R.id.btnPrevious);
        btnNext = findViewById(R.id.btnNext);
        sbProgress = findViewById(R.id.sbProgress);
        sbVolume = findViewById(R.id.sbVolume);
        toolbar = findViewById(R.id.toolbar);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("");
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void loadSong() {
        dbManager.open();
        currentSong = dbManager.getSongById(songId);
        dbManager.close();

        if (currentSong == null) {
            Toast.makeText(this, "Error al cargar la canción", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        tvTitle.setText(currentSong.getTitle() != null ? currentSong.getTitle() : "Sin título");
        tvArtist.setText(currentSong.getArtist() != null ? currentSong.getArtist() : "Artista desconocido");
        // tvGenre.setText(currentSong.getGenre() ...);  ← ELIMINADO
        tvTotalTime.setText(currentSong.getDuration() != null ? currentSong.getDuration() : "0:00");

        setupMediaPlayer();
    }

    private void setupMediaPlayer() {
        String fileName = currentSong.getFileName();

        if (fileName == null || fileName.isEmpty()) {
            fileName = currentSong.getTitle();
        }

        int resourceId = getResources().getIdentifier(fileName, "raw", getPackageName());

        if (resourceId == 0) {
            Toast.makeText(this, "Archivo de audio no encontrado: " + fileName, Toast.LENGTH_LONG).show();
            return;
        }

        try {
            if (mediaPlayer != null) {
                mediaPlayer.release();
            }
            mediaPlayer = MediaPlayer.create(this, resourceId);

            if (mediaPlayer == null) {
                Toast.makeText(this, "Error al crear el reproductor", Toast.LENGTH_SHORT).show();
                return;
            }

            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            sbProgress.setMax(mediaPlayer.getDuration());

            mediaPlayer.setOnCompletionListener(mp -> {
                btnPlayPause.setImageResource(R.drawable.ic_play_arrow);
                sbProgress.setProgress(0);
                tvCurrentTime.setText("0:00");
            });

            setupSeekBar();

        } catch (Exception e) {
            Toast.makeText(this, "Error al reproducir: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void setupSeekBar() {
        sbProgress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && mediaPlayer != null) {
                    mediaPlayer.seekTo(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                isUserSeeking = true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                isUserSeeking = false;
                if (mediaPlayer != null) {
                    mediaPlayer.seekTo(seekBar.getProgress());
                }
            }
        });
    }

    private void setupNextPrevious() {
        btnPlayPause.setOnClickListener(v -> togglePlayPause());

        btnPrevious.setOnClickListener(v -> {
            if (mediaPlayer != null) {
                mediaPlayer.seekTo(0);
                if (!mediaPlayer.isPlaying()) {
                    togglePlayPause();
                }
            }
        });

        btnNext.setOnClickListener(v -> {
            Toast.makeText(this, "Función en desarrollo", Toast.LENGTH_SHORT).show();
        });
    }

    private void togglePlayPause() {
        if (mediaPlayer == null) return;

        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            btnPlayPause.setImageResource(R.drawable.ic_play_arrow);
            handler.removeCallbacks(progressRunnable);
        } else {
            mediaPlayer.start();
            btnPlayPause.setImageResource(R.drawable.ic_pause);
            startProgressUpdate();
        }
    }

    private Runnable progressRunnable = new Runnable() {
        @Override
        public void run() {
            updateProgress();
        }
    };

    private void startProgressUpdate() {
        handler.post(progressRunnable);
    }

    private void updateProgress() {
        if (mediaPlayer != null && mediaPlayer.isPlaying() && !isUserSeeking) {
            int currentPosition = mediaPlayer.getCurrentPosition();
            sbProgress.setProgress(currentPosition);
            tvCurrentTime.setText(formatTime(currentPosition));
            handler.postDelayed(progressRunnable, 500);
        }
    }

    private void setupVolumeControl() {
        try {
            AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
            if (audioManager != null) {
                int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
                int currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);

                sbVolume.setMax(maxVolume);
                sbVolume.setProgress(currentVolume);

                sbVolume.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        if (fromUser && audioManager != null) {
                            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0);
                        }
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {}

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {}
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String formatTime(int milliseconds) {
        if (milliseconds <= 0) return "0:00";
        int seconds = milliseconds / 1000;
        int minutes = seconds / 60;
        seconds = seconds % 60;
        return String.format("%d:%02d", minutes, seconds);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            btnPlayPause.setImageResource(R.drawable.ic_play_arrow);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        handler.removeCallbacksAndMessages(null);
        if (dbManager != null) {
            dbManager.close();
        }
    }
}