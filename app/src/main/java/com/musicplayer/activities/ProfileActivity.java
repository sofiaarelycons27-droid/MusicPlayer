package com.musicplayer.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.musicplayer.R;
import com.musicplayer.database.DatabaseManager;
import com.musicplayer.utils.SessionManager;

public class ProfileActivity extends AppCompatActivity {

    private TextView tvUsername, tvEmail, tvJoinDate, tvSongCount;
    private Button btnLogout, btnDeleteAccount;
    private Toolbar toolbar;
    private SessionManager sessionManager;
    private DatabaseManager dbManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        sessionManager = new SessionManager(this);
        dbManager = new DatabaseManager(this);

        initViews();
        setupToolbar();
        loadUserInfo();

        btnLogout.setOnClickListener(v -> logout());
        btnDeleteAccount.setOnClickListener(v -> deleteAccount());
    }

    private void initViews() {
        tvUsername = findViewById(R.id.tvUsername);
        tvEmail = findViewById(R.id.tvEmail);
        tvJoinDate = findViewById(R.id.tvJoinDate);
        tvSongCount = findViewById(R.id.tvSongCount);
        btnLogout = findViewById(R.id.btnLogout);
        btnDeleteAccount = findViewById(R.id.btnDeleteAccount);
        toolbar = findViewById(R.id.toolbar);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Mi Perfil");
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void loadUserInfo() {
        tvUsername.setText(sessionManager.getUsername());
        tvEmail.setText(sessionManager.getUserEmail());
        tvJoinDate.setText("Miembro desde: 2024");

        dbManager.open();
        int songCount = dbManager.getAllSongs(sessionManager.getUserId()).size();
        dbManager.close();

        tvSongCount.setText(songCount + " canciones");
    }

    private void logout() {
        new AlertDialog.Builder(this)
                .setTitle("Cerrar Sesión")
                .setMessage("¿Estás seguro?")
                .setPositiveButton("Sí", (dialog, which) -> {
                    sessionManager.logout();
                    Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("No", null)
                .show();
    }

    private void deleteAccount() {
        new AlertDialog.Builder(this)
                .setTitle("Eliminar Cuenta")
                .setMessage("Esta acción es irreversible. ¿Deseas continuar?")
                .setPositiveButton("Eliminar", (dialog, which) -> {
                    dbManager.open();
                    boolean deleted = dbManager.deleteUser(sessionManager.getUserId());
                    dbManager.close();

                    if (deleted) {
                        sessionManager.logout();
                        Toast.makeText(this, "Cuenta eliminada", Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }
}