package com.musicplayer.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.musicplayer.R;
import com.musicplayer.adapters.SongAdapter;
import com.musicplayer.database.DatabaseManager;
import com.musicplayer.models.Song;
import com.musicplayer.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements SongAdapter.OnSongChangedListener {

    private RecyclerView rvSongs;
    private EditText etSearch;
    private LinearLayout genreContainer;
    private TextView tvGreeting;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;
    private FloatingActionButton btnAddFab;

    private DatabaseManager dbManager;
    private SessionManager sessionManager;
    private List<Song> allSongs;
    private List<Song> filteredSongs;
    private SongAdapter adapter;
    private String selectedGenre = "Todos";
    private String searchQuery = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbManager = new DatabaseManager(this);
        sessionManager = new SessionManager(this);

        initViews();
        setupToolbar();
        setupDrawer();
        setupSearch();
        loadSongs();

        // Configurar el FAB - Nota: ya está inicializado en initViews()
        btnAddFab.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, AddSongActivity.class));
        });
    }

    private void initViews() {
        rvSongs = findViewById(R.id.rvSongs);
        etSearch = findViewById(R.id.etSearch);
        genreContainer = findViewById(R.id.genreContainer);
        tvGreeting = findViewById(R.id.tvGreeting);
        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navView);
        btnAddFab = findViewById(R.id.btnAddFab);  // Solo una vez aquí

        rvSongs.setLayoutManager(new LinearLayoutManager(this));

        String username = sessionManager.getUsername();
        tvGreeting.setText("Hola, " + (username != null ? username : "Usuario"));
    }

    private void setupToolbar() {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        getSupportActionBar().setTitle("");
    }

    private void setupDrawer() {
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, android.R.string.ok, android.R.string.cancel);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_profile) {
                startActivity(new Intent(MainActivity.this, ProfileActivity.class));
            } else if (id == R.id.nav_add_song) {
                startActivity(new Intent(MainActivity.this, AddSongActivity.class));
            } else if (id == R.id.nav_logout) {
                logout();
            }

            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });

        View headerView = navigationView.getHeaderView(0);
        if (headerView == null) {
            headerView = navigationView.inflateHeaderView(R.layout.header_nav);
        }
        TextView navUsername = headerView.findViewById(R.id.navUsername);
        TextView navEmail = headerView.findViewById(R.id.navEmail);
        navUsername.setText(sessionManager.getUsername());
        navEmail.setText(sessionManager.getUserEmail());
    }

    private void setupSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchQuery = s.toString();
                filterSongs();
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void loadSongs() {
        dbManager.open();
        allSongs = dbManager.getAllSongs(sessionManager.getUserId());
        dbManager.close();

        filteredSongs = new ArrayList<>(allSongs);
        setupGenreFilters();
        filterSongs();
    }

    private void setupGenreFilters() {
        dbManager.open();
        List<String> genres = dbManager.getAllGenres(sessionManager.getUserId());
        dbManager.close();

        genreContainer.removeAllViews();

        // Agregar opción "Todos"
        Button todosChip = new Button(this);
        todosChip.setText("Todos");
        todosChip.setPadding(40, 12, 40, 12);
        todosChip.setAllCaps(false);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMarginEnd(12);
        todosChip.setLayoutParams(params);

        if (selectedGenre.equals("Todos")) {
            todosChip.setBackgroundResource(R.drawable.bg_chip_selected);
            todosChip.setTextColor(getColor(R.color.white));
        } else {
            todosChip.setBackgroundResource(R.drawable.bg_chip_normal);
            todosChip.setTextColor(getColor(R.color.primary));
        }

        todosChip.setOnClickListener(v -> {
            selectedGenre = "Todos";
            setupGenreFilters();
            filterSongs();
        });

        genreContainer.addView(todosChip);

        for (String genre : genres) {
            Button chip = new Button(this);
            chip.setText(genre);
            chip.setPadding(40, 12, 40, 12);
            chip.setAllCaps(false);

            LinearLayout.LayoutParams chipParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            chipParams.setMarginEnd(12);
            chip.setLayoutParams(chipParams);

            if (genre.equals(selectedGenre)) {
                chip.setBackgroundResource(R.drawable.bg_chip_selected);
                chip.setTextColor(getColor(R.color.white));
            } else {
                chip.setBackgroundResource(R.drawable.bg_chip_normal);
                chip.setTextColor(getColor(R.color.primary));
            }

            chip.setOnClickListener(v -> {
                selectedGenre = genre;
                setupGenreFilters();
                filterSongs();
            });

            genreContainer.addView(chip);
        }
    }

    private void filterSongs() {
        filteredSongs.clear();

        for (Song song : allSongs) {
            boolean matchesGenre = selectedGenre.equals("Todos") || song.getGenre().equals(selectedGenre);
            boolean matchesSearch = searchQuery.isEmpty() ||
                    song.getTitle().toLowerCase().contains(searchQuery.toLowerCase()) ||
                    song.getArtist().toLowerCase().contains(searchQuery.toLowerCase());

            if (matchesGenre && matchesSearch) {
                filteredSongs.add(song);
            }
        }

        if (adapter == null) {
            adapter = new SongAdapter(this, filteredSongs, sessionManager.getUserId(), this);
            rvSongs.setAdapter(adapter);
        } else {
            adapter.updateList(filteredSongs);
        }
    }

    @Override
    public void onSongChanged() {
        loadSongs();
    }

    private void logout() {
        new AlertDialog.Builder(this)
                .setTitle("Cerrar Sesión")
                .setMessage("¿Estás seguro?")
                .setPositiveButton("Sí", (dialog, which) -> {
                    sessionManager.logout();
                    Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("No", null)
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadSongs();
    }
}