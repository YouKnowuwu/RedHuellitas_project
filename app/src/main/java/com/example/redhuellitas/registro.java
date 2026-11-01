package com.example.redhuellitas;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class registro extends AppCompatActivity {

    private EditText etNombre, etCorreo, etContrasena, etTelefono;
    private RadioGroup radioGroupRol;

    // Instancias de Firebase
    private FirebaseAuth mAuth;
    private FirebaseFirestore mFirestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro);

        // Inicializar Firebase
        mAuth = FirebaseAuth.getInstance();
        mFirestore = FirebaseFirestore.getInstance();

        // Vincular vistas con el XML de registro
        etNombre = findViewById(R.id.etNombre);
        etCorreo = findViewById(R.id.etCorreo);
        etContrasena = findViewById(R.id.etContrasena);
        etTelefono = findViewById(R.id.etTelefono);
        radioGroupRol = findViewById(R.id.radioGroupRol);
        Button btnGuardar = findViewById(R.id.btnGuardar);

        // Evento del botón Guardar
        btnGuardar.setOnClickListener(v -> registrarUsuario());
    }

    private void registrarUsuario() {
        String nombre = etNombre.getText().toString().trim();
        String correo = etCorreo.getText().toString().trim();
        String password = etContrasena.getText().toString().trim();
        String telefono = etTelefono.getText().toString().trim();

        // Obtener el rol seleccionado asegurando que sea una variable final/efectivamente final
        int selectedId = radioGroupRol.getCheckedRadioButtonId();
        if (selectedId == -1) {
            Toast.makeText(this, "Por favor selecciona un rol", Toast.LENGTH_SHORT).show();
            return;
        }
        RadioButton selectedRadioButton = findViewById(selectedId);
        final String rol = selectedRadioButton.getText().toString().trim();

        // Validaciones básicas
        if (nombre.isEmpty() || correo.isEmpty() || password.isEmpty() || telefono.isEmpty() || rol.isEmpty()) {
            Toast.makeText(this, "Por favor completa todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 6) {
            Toast.makeText(this, "La contraseña debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show();
            return;
        }

        // 1. Crear el usuario en Firebase Authentication
        mAuth.createUserWithEmailAndPassword(correo, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        if (mAuth.getCurrentUser() == null) return;
                        String userId = mAuth.getCurrentUser().getUid();

                        // 2. Crear un mapa con los datos para Cloud Firestore
                        Map<String, Object> usuarioMap = new HashMap<>();
                        usuarioMap.put("id", userId);
                        usuarioMap.put("nombre", nombre);
                        usuarioMap.put("correo", correo);
                        usuarioMap.put("telefono", telefono);
                        usuarioMap.put("rol", rol);

                        // 3. Guardar en la colección "Usuarios"
                        mFirestore.collection("Usuarios").document(userId)
                                .set(usuarioMap)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(registro.this, "¡Registro exitoso!", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(registro.this, inicio.class);
                                    startActivity(intent);
                                    finish();
                                })
                                .addOnFailureListener(e -> {
                                    String errorMsg = e.getMessage() != null ? e.getMessage() : "Error desconocido";
                                    Toast.makeText(registro.this, "Error al guardar datos: " + errorMsg, Toast.LENGTH_SHORT).show();
                                });

                    } else {
                        String errorMsg = task.getException() != null && task.getException().getMessage() != null
                                ? task.getException().getMessage()
                                : "Error de autenticación";
                        Toast.makeText(registro.this, "Error en autenticación: " + errorMsg, Toast.LENGTH_SHORT).show();
                    }
                });
    }
}