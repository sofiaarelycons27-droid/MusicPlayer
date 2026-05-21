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

    private ImageView ivArtwork;
    private TextView tvTitle, tvArtist, tvGenre, tvCurrentTime, tvTotalTime;
    private ImageButton btnPlayPause, btnPrevious, btnNext;
    private SeekBar sbProgress, sbVolume;
    private Toolbar toolbar;

    private MediaPlayer mediaPlayer;
    private DatabaseManager dbManager;
    private Song currentSong;
    private Handler handler = new Handler();
    private int userId;
    private int songId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        dbManager = new DatabaseManager(this);

        songId = getIntent().getIntExtra("song_id", -1);
        userId = getIntent().getIntExtra("user_id", -1);

        initViews();
        setupToolbar();
        loadSong();
        setupVolumeControl();
    }

    private void initViews() {
        ivArtwork = findViewById(R.id.ivArtwork);
        tvTitle = findViewById(R.id.tvTitle);
        tvArtist = findViewById(R.id.tvArtist);
        tvGenre = findViewById(R.id.tvGenre);
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
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("");
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

        tvTitle.setText(currentSong.getTitle());
        tvArtist.setText(currentSong.getArtist());
        tvGenre.setText(currentSong.getGenre());
        tvTotalTime.setText(currentSong.getDuration());

        setupMediaPlayer();
    }

    private void setupMediaPlayer() {
        String fileName = currentSong.getFileName();
        int resourceId = getResources().getIdentifier(fileName, "raw", getPackageName());

        if (resourceId == 0) {
            Toast.makeText(this, "Archivo de audio no encontrado: " + fileName, Toast.LENGTH_SHORT).show();
            return;
        }

        mediaPlayer = MediaPlayer.create(this, resourceId);
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

        btnPlayPause.setOnClickListener(v -> togglePlayPause());

        sbProgress.setMax(mediaPlayer.getDuration());

        mediaPlayer.setOnCompletionListener(mp -> {
            btnPlayPause.setImageResource(R.drawable.ic_play);
            sbProgress.setProgress(0);
            tvCurrentTime.setText("0:00");
        });

        updateProgress();
    }

    private void togglePlayPause() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            btnPlayPause.setImageResource(R.drawable.ic_play);
        } else {
            mediaPlayer.start();
            btnPlayPause.setImageResource(R.drawable.ic_pause);
            updateProgress();
        }
    }

    private void updateProgress() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            int currentPosition = mediaPlayer.getCurrentPosition();
            sbProgress.setProgress(currentPosition);
            tvCurrentTime.setText(formatTime(currentPosition));

            sbProgress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (fromUser) {
                        mediaPlayer.seekTo(progress);
                    }
                }
                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {}
                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {}
            });

            handler.postDelayed(this::updateProgress, 500);
        }
    }

    private void setupVolumeControl() {
        AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        int currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);

        sbVolume.setMax(maxVolume);
        sbVolume.setProgress(currentVolume);

        sbVolume.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0);
                }
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    private String formatTime(int milliseconds) {
        int seconds = milliseconds / 1000;
        int minutes = seconds / 60;
        seconds = seconds % 60;
        return String.format("%d:%02d", minutes, seconds);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        handler.removeCallbacksAndMessages(null);
    }
}