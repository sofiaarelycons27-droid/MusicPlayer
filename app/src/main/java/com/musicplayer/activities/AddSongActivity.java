package com.musicplayer.activities;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.musicplayer.R;
import com.musicplayer.database.DatabaseManager;
import com.musicplayer.models.Song;
import com.musicplayer.utils.SessionManager;

public class AddSongActivity extends AppCompatActivity {

    private EditText etTitle, etArtist, etGenre, etDuration, etFileName;
    private Button btnSave;
    private DatabaseManager dbManager;
    private SessionManager sessionManager;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_song);

        dbManager = new DatabaseManager(this);
        sessionManager = new SessionManager(this);

        initViews();
        setupToolbar();
        setupListeners();
    }

    private void initViews() {
        etTitle = findViewById(R.id.etTitle);
        etArtist = findViewById(R.id.etArtist);
        etGenre = findViewById(R.id.etGenre);
        etDuration = findViewById(R.id.etDuration);
        etFileName = findViewById(R.id.etFileName);
        btnSave = findViewById(R.id.btnSave);
        toolbar = findViewById(R.id.toolbar);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupListeners() {
        btnSave.setOnClickListener(v -> saveSong());
    }

    private void saveSong() {
        String title = etTitle.getText().toString().trim();
        String artist = etArtist.getText().toString().trim();
        String genre = etGenre.getText().toString().trim();
        String duration = etDuration.getText().toString().trim();
        String fileName = etFileName.getText().toString().trim();

        if (title.isEmpty()) {
            etTitle.setError("Ingresa el título");
            return;
        }
        if (artist.isEmpty()) {
            etArtist.setError("Ingresa el artista");
            return;
        }
        if (genre.isEmpty()) {
            etGenre.setError("Ingresa el género");
            return;
        }
        if (duration.isEmpty()) {
            etDuration.setError("Ingresa la duración");
            return;
        }
        if (fileName.isEmpty()) {
            etFileName.setError("Ingresa el nombre del archivo");
            return;
        }

        Song song = new Song(0, title, artist, genre, duration, fileName, false, sessionManager.getUserId());

        dbManager.open();
        long result = dbManager.addSong(song);
        dbManager.close();

        if (result > 0) {
            Toast.makeText(this, "Canción agregada", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Error al guardar", Toast.LENGTH_SHORT).show();
        }
    }
}