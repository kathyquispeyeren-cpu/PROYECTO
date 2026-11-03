package com.example.informatica;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;

public class LoginActivity extends AppCompatActivity {

    private EditText etApellidos;
    private EditText etSerie;
    private LinearLayout layoutRegistration;
    private LinearLayout layoutWelcome;
    private TextView tvRegisteredName;
    private TextView tvRegisteredCode;
    private Button btnRegistrar;
    private Button btnIngresar;
    private TextView tvResetStudent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etApellidos = findViewById(R.id.etApellidos);
        etSerie = findViewById(R.id.etSerie);
        layoutRegistration = findViewById(R.id.layoutRegistration);
        layoutWelcome = findViewById(R.id.layoutWelcome);
        tvRegisteredName = findViewById(R.id.tvRegisteredName);
        tvRegisteredCode = findViewById(R.id.tvRegisteredCode);
        btnRegistrar = findViewById(R.id.btnRegistrar);
        btnIngresar = findViewById(R.id.btnIngresar);
        tvResetStudent = findViewById(R.id.tvResetStudent);

        SharedPreferences session = getSharedPreferences("fap_session", MODE_PRIVATE);
        checkSessionState(session);

        btnRegistrar.setOnClickListener(v -> registrarAlumno(session));
        btnIngresar.setOnClickListener(v -> ingresarAlSistema());
        tvResetStudent.setOnClickListener(v -> reiniciarRegistro(session));
    }

    private void checkSessionState(SharedPreferences session) {
        String apellidos = session.getString("apellidos", null);
        String serie = session.getString("serie", null);

        if (apellidos != null && serie != null) {
            // Already registered -> Show welcome with name and INGRESAR button
            layoutRegistration.setVisibility(View.GONE);
            layoutWelcome.setVisibility(View.VISIBLE);
            tvRegisteredName.setText(apellidos);
            tvRegisteredCode.setText("Código: " + serie);
        } else {
            // Not registered -> Show registration form
            layoutRegistration.setVisibility(View.VISIBLE);
            layoutWelcome.setVisibility(View.GONE);
        }
    }

    private void registrarAlumno(SharedPreferences session) {
        String apellidos = etApellidos.getText().toString().trim();
        String serie = etSerie.getText().toString().trim();

        if (TextUtils.isEmpty(apellidos)) {
            etApellidos.setError("Ingrese los apellidos y nombres del alumno");
            etApellidos.requestFocus();
            return;
        }

        if (apellidos.length() < 3) {
            etApellidos.setError("Ingrese un nombre válido");
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

    private void ingresarAlSistema() {
        startActivity(new Intent(LoginActivity.this, MainActivity.class));
        finish();
    }

    private void reiniciarRegistro(SharedPreferences session) {
        session.edit().clear().apply();
        etApellidos.setText("");
        etSerie.setText("");
        checkSessionState(session);
    }
}
