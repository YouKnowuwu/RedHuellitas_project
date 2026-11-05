package com.example.redhuellitas;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin, btnRegistrate; // 1. Declarar el botón de registro
    private FirebaseFirestore db;
    private TextView btnSoyAdmin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnRegistrate = findViewById(R.id.btnRegistrate);
        TextView btnSoyAdmin= findViewById(R.id.btnSoyAdmin);
        db = FirebaseFirestore.getInstance();

        btnLogin.setOnClickListener(v -> validarUsuario());

        // 3. Configurar el evento para ir a la pantalla de registro
        btnRegistrate.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, registro.class);
            startActivity(intent);
        });



        btnSoyAdmin.setOnClickListener(v -> {
                Intent intent = new Intent(LoginActivity.this, Admin_login.class);
                startActivity(intent);
        });
    }

    private void validarUsuario() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Por favor completa todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("Usuarios")
                .whereEqualTo("email", email)
                .whereEqualTo("password", password)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        QuerySnapshot querySnapshot = task.getResult();
                        if (!querySnapshot.isEmpty()) {
                            Toast.makeText(LoginActivity.this, "¡Bienvenido a RedHuellitas!", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(LoginActivity.this, inicio.class);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(LoginActivity.this, "Correo o contraseña incorrectos", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(LoginActivity.this, "Error al conectar con la base de datos", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}