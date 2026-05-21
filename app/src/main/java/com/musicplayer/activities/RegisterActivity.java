package com.musicplayer.activities;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.musicplayer.R;
import com.musicplayer.database.DatabaseManager;

public class RegisterActivity extends AppCompatActivity {

    private EditText etEmail, etUsername, etPassword, etConfirmPassword;
    private Button btnRegister;
    private TextView tvLogin;
    private DatabaseManager dbManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        dbManager = new DatabaseManager(this);

        etEmail = findViewById(R.id.etEmail);
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnRegister = findViewById(R.id.btnRegister);
        tvLogin = findViewById(R.id.tvLogin);

        btnRegister.setOnClickListener(v -> performRegister());
        tvLogin.setOnClickListener(v -> finish());
    }

    private void performRegister() {
        String email = etEmail.getText().toString().trim();
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        if (email.isEmpty() || !email.contains("@")) {
            etEmail.setError("Correo inválido");
            return;
        }
        if (username.isEmpty() || username.length() < 3) {
            etUsername.setError("Mínimo 3 caracteres");
            return;
        }
        if (password.isEmpty() || password.length() < 4) {
            etPassword.setError("Mínimo 4 caracteres");
            return;
        }
        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError("Las contraseñas no coinciden");
            return;
        }

        dbManager.open();
        long result = dbManager.registerUser(email, username, password);
        dbManager.close();

        if (result > 0) {
            Toast.makeText(this, "Registro exitoso", Toast.LENGTH_LONG).show();
            finish();
        } else {
            Toast.makeText(this, "Usuario o correo ya existe", Toast.LENGTH_SHORT).show();
        }
    }
}