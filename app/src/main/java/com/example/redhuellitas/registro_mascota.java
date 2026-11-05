package com.example.redhuellitas;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class registro_mascota extends AppCompatActivity {

    private ImageView ivAvatarMascota;
    private EditText etNombreMascota, etRazaMascota, etEdadMascota, etDescripcionMascota;
    private RadioGroup radioGroupEspecie;

    private FirebaseAuth mAuth;
    private FirebaseFirestore mFirestore;

    private Uri imageUri = null;
    private ActivityResultLauncher<String> galleryLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro_mascota);

        // Inicializar Firebase
        mAuth = FirebaseAuth.getInstance();
        mFirestore = FirebaseFirestore.getInstance();

        // Vincular vistas con el XML
        ivAvatarMascota = findViewById(R.id.ivAvatarMascota);
        etNombreMascota = findViewById(R.id.etNombreMascota);
        etRazaMascota = findViewById(R.id.etRazaMascota);
        etEdadMascota = findViewById(R.id.etEdadMascota);
        etDescripcionMascota = findViewById(R.id.etDescripcionMascota);
        radioGroupEspecie = findViewById(R.id.radioGroupEspecie);
        Button btnRegistrarMascota = findViewById(R.id.btnRegistrarMascota);

        // Configurar selector de galería
        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                result -> {
                    if (result != null) {
                        imageUri = result;
                        ivAvatarMascota.setImageURI(imageUri);
                        ivAvatarMascota.setImageTintList(null);
                    }
                }
        );

        ivAvatarMascota.setOnClickListener(v -> galleryLauncher.launch("image/*"));

        // Evento del botón Registrar Mascota
        btnRegistrarMascota.setOnClickListener(v -> registrarMascotaEnFirestore());
    }

    private void registrarMascotaEnFirestore() {
        String nombre = etNombreMascota.getText().toString().trim();
        String raza = etRazaMascota.getText().toString().trim();
        String edad = etEdadMascota.getText().toString().trim();
        String descripcion = etDescripcionMascota.getText().toString().trim();

        // Validar especie
        int selectedId = radioGroupEspecie.getCheckedRadioButtonId();
        if (selectedId == -1) {
            Toast.makeText(this, "Por favor selecciona una especie (Perro o Gato)", Toast.LENGTH_SHORT).show();
            return;
        }
        RadioButton selectedRadioButton = findViewById(selectedId);
        String especie = selectedRadioButton.getText().toString().trim();

        // Validar campos vacíos
        if (nombre.isEmpty() || raza.isEmpty() || edad.isEmpty()) {
            Toast.makeText(this, "Por favor completa nombre, raza y edad", Toast.LENGTH_SHORT).show();
            return;
        }

        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(this, "Error: Usuario no autenticado", Toast.LENGTH_SHORT).show();
            return;
        }

        String userIdDuenio = mAuth.getCurrentUser().getUid();
        String mascotaId = mFirestore.collection("Mascotas").document().getId();

        Map<String, Object> mascotaMap = new HashMap<>();
        mascotaMap.put("idMascota", mascotaId);
        mascotaMap.put("idDuenio", userIdDuenio);
        mascotaMap.put("nombre", nombre);
        mascotaMap.put("especie", especie);
        mascotaMap.put("raza", raza);
        mascotaMap.put("edad", edad);
        mascotaMap.put("descripcion", descripcion);
        mascotaMap.put("fotoUri", imageUri != null ? imageUri.toString() : "");

        Toast.makeText(this, "Enviando datos a Firebase...", Toast.LENGTH_SHORT).show();

        // Enviar a la colección "Mascotas"
        mFirestore.collection("Mascotas").document(mascotaId)
                .set(mascotaMap)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(registro_mascota.this, "¡Mascota registrada y colección creada!", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(registro_mascota.this, inicio.class);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Log.e("FirestoreError", "Error al guardar mascota", e);
                    Toast.makeText(registro_mascota.this, "Fallo al guardar: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
}