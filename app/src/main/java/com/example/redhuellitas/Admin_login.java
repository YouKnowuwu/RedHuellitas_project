package com.example.redhuellitas;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class Admin_login extends AppCompatActivity {

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_login);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        EditText etUsuario = findViewById(R.id.etUsuario);
        EditText etContrasena = findViewById(R.id.etContrasena);
        Button btnIngresa = findViewById(R.id.btnIngresa);

        btnIngresa.setOnClickListener(v -> {
            String email = etUsuario.getText().toString().trim();
            String password = etContrasena.getText().toString().trim();

            if (!email.isEmpty() && !password.isEmpty()) {

                auth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(this, task -> {
                            if (task.isSuccessful()) {
                                String userEmail = auth.getCurrentUser() != null ? auth.getCurrentUser().getEmail() : email;

                                // Verificar si el correo está registrado como documento en la colección "admin"
                                db.collection("admin").document(email)
                                        .get()
                                        .addOnSuccessListener(document -> {
                                            if (document.exists()) {
                                                // ¡Es administrador!
                                                Toast.makeText(Admin_login.this, "¡Bienvenido Administrador!", Toast.LENGTH_SHORT).show();

                                                // AQUÍ ES DONDE DEBE ABRIR EL PANEL DE ADMIN
                                                Intent intent = new Intent(Admin_login.this, panel_admin.class);
                                                startActivity(intent);
                                                finish();

                                            } else {
                                                Toast.makeText(Admin_login.this, "Acceso denegado: Este usuario no es administrador", Toast.LENGTH_LONG).show();
                                            }
                                        });

                            } else {
                                // Credenciales inválidas en Auth
                                Toast.makeText(Admin_login.this, "Correo o contraseña incorrectos", Toast.LENGTH_LONG).show();
                            }
                        });
            } else {
                Toast.makeText(Admin_login.this, "Por favor completa todos los campos", Toast.LENGTH_SHORT).show();
            }
        });
    }
}