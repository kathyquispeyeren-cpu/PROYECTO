package com.example.informatica;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;

public class LoginActivity extends AppCompatActivity {

    private EditText etApellidos;
    private EditText etSerie;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etApellidos = findViewById(R.id.etApellidos);
        etSerie = findViewById(R.id.etSerie);
        Button btnIngresar = findViewById(R.id.btnIngresar);

        SharedPreferences session = getSharedPreferences("fap_session", MODE_PRIVATE);
        String apellidos = session.getString("apellidos", null);
        String serie = session.getString("serie", null);

        if (apellidos != null) {
            etApellidos.setText(apellidos);
        }
        if (serie != null) {
            etSerie.setText(serie);
        }

        btnIngresar.setOnClickListener(v -> validarIngreso(session));
    }

    private void validarIngreso(SharedPreferences session) {
        String apellidos = etApellidos.getText().toString().trim();
        String serie = etSerie.getText().toString().trim();

        if (TextUtils.isEmpty(apellidos)) {
            etApellidos.setError("Ingrese sus apellidos");
            etApellidos.requestFocus();
            return;
        }

        if (apellidos.length() < 3) {
            etApellidos.setError("Ingrese un apellido válido");
            etApellidos.requestFocus();
            return;
        }

        if (serie.length() < 4) {
            etSerie.setError(getString(R.string.error_serie));
            etSerie.requestFocus();
            return;
        }

        session.edit()
                .putString("apellidos", apellidos.toUpperCase(new Locale("es", "PE")))
                .putString("serie", serie)
                .remove("grado")
                .apply();

        startActivity(new Intent(LoginActivity.this, MainActivity.class));
        finish();
    }
}
