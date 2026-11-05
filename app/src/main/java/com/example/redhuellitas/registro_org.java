package com.example.redhuellitas;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
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

public class registro_org extends AppCompatActivity {

    private ImageView ivAvatarOrg;
    private EditText etNombreOrg, etDireccionOrg, etTelefonoOrg;
    private RadioGroup radioGroupTipoOrg;

    private FirebaseAuth mAuth;
    private FirebaseFirestore mFirestore;

    private Uri imageUri = null;
    private ActivityResultLauncher<String> galleryLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro_org);

        mAuth = FirebaseAuth.getInstance();
        mFirestore = FirebaseFirestore.getInstance();

        ivAvatarOrg = findViewById(R.id.ivAvatarOrg);
        etNombreOrg = findViewById(R.id.etNombreOrg);
        etDireccionOrg = findViewById(R.id.etDireccionOrg);
        etTelefonoOrg = findViewById(R.id.etTelefonoOrg);
        radioGroupTipoOrg = findViewById(R.id.radioGroupTipoOrg);
        Button btnGuardarOrg = findViewById(R.id.btnGuardarOrg);

        // Selector de imagen de galería
        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                result -> {
                    if (result != null) {
                        imageUri = result;
                        ivAvatarOrg.setImageURI(imageUri);
                        ivAvatarOrg.setImageTintList(null);
                    }
                }
        );

        ivAvatarOrg.setOnClickListener(v -> galleryLauncher.launch("image/*"));

        // Botón Guardar datos en Firestore
        btnGuardarOrg.setOnClickListener(v -> guardarOrganizacion());


    }

    private void guardarOrganizacion() {
        String nombre = etNombreOrg.getText().toString().trim();
        String direccion = etDireccionOrg.getText().toString().trim();
        String telefono = etTelefonoOrg.getText().toString().trim();

        int selectedId = radioGroupTipoOrg.getCheckedRadioButtonId();
        if (selectedId == -1) {
            Toast.makeText(this, "Selecciona si es Organización o Refugio", Toast.LENGTH_SHORT).show();
            return;
        }
        RadioButton selectedRb = findViewById(selectedId);
        String tipoOrg = selectedRb.getText().toString().trim();

        if (nombre.isEmpty() || direccion.isEmpty() || telefono.isEmpty()) {
            Toast.makeText(this, "Por favor completa todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(this, "Error: Usuario no autenticado", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = mAuth.getCurrentUser().getUid();
        String orgId = mFirestore.collection("Organizaciones").document().getId();

        Map<String, Object> orgMap = new HashMap<>();
        orgMap.put("idOrg", orgId);
        orgMap.put("idUsuario", userId);
        orgMap.put("nombre", nombre);
        orgMap.put("tipo", tipoOrg);
        orgMap.put("direccion", direccion);
        orgMap.put("telefono", telefono);
        orgMap.put("fotoUri", imageUri != null ? imageUri.toString() : "");

        mFirestore.collection("Organizaciones").document(orgId)
                .set(orgMap)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(registro_org.this, "¡Organización registrada con éxito!", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(registro_org.this, inicio.class));
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(registro_org.this, "Error al guardar: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
}