package com.example.informatica;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.widget.CompoundButtonCompat;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final int SORT_DATE = 0;
    private static final int SORT_PRIORITY = 1;

    private TextView tvUserHeader;
    private TextView tvClock;
    private TextView tvCount;
    private TextView tvEmpty;
    private LinearLayout notesContainer;
    private Button btnSortDate;
    private Button btnSortPriority;

    private List<Note> notes;
    private int sortMode = SORT_DATE;

    private final Handler clockHandler = new Handler(Looper.getMainLooper());
    private final Runnable clockRunnable = new Runnable() {
        @Override
        public void run() {
            SimpleDateFormat sdf = new SimpleDateFormat("EEEE dd 'DE' MMMM 'DE' yyyy", new Locale("es", "PE"));
            tvClock.setText(sdf.format(new Date()).toUpperCase(new Locale("es", "PE")));
            clockHandler.postDelayed(this, 60_000);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences session = getSharedPreferences("fap_session", MODE_PRIVATE);
        String apellidos = session.getString("apellidos", null);
        String serie = session.getString("serie", null);

        if (apellidos == null || serie == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_main);

        tvUserHeader = findViewById(R.id.tvUserHeader);
        tvClock = findViewById(R.id.tvClock);
        tvCount = findViewById(R.id.tvCount);
        tvEmpty = findViewById(R.id.tvEmpty);
        notesContainer = findViewById(R.id.rvNotes);
        btnSortDate = findViewById(R.id.btnSortDate);
        btnSortPriority = findViewById(R.id.btnSortPriority);
        Button btnNewNote = findViewById(R.id.btnNewNote);
        ImageButton btnLogout = findViewById(R.id.btnLogout);

        tvUserHeader.setText(getString(R.string.operador_format, apellidos, serie));

        notes = NoteStore.load(this);

        btnNewNote.setOnClickListener(v -> showNoteDialog(null));

        btnSortDate.setOnClickListener(v -> {
            sortMode = SORT_DATE;
            render();
        });

        btnSortPriority.setOnClickListener(v -> {
            sortMode = SORT_PRIORITY;
            render();
        });

        btnLogout.setOnClickListener(v -> {
            session.edit().clear().apply();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });

        render();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (tvClock == null) {
            return;
        }
        clockHandler.removeCallbacks(clockRunnable);
        clockHandler.post(clockRunnable);
    }

    @Override
    protected void onPause() {
        super.onPause();
        clockHandler.removeCallbacks(clockRunnable);
    }

    private void render() {
        notesContainer.removeAllViews();

        List<Note> ordered = new ArrayList<>(notes);
        if (sortMode == SORT_DATE) {
            Collections.sort(ordered, (a, b) -> Long.compare(b.getTimestamp(), a.getTimestamp()));
        } else {
            Collections.sort(ordered, (a, b) -> {
                int byPriority = Integer.compare(a.getPriority(), b.getPriority());
                return byPriority != 0 ? byPriority : Long.compare(b.getTimestamp(), a.getTimestamp());
            });
        }

        tvCount.setText(getString(R.string.registros_format, notes.size()));
        btnSortDate.setAlpha(sortMode == SORT_DATE ? 1f : 0.5f);
        btnSortPriority.setAlpha(sortMode == SORT_PRIORITY ? 1f : 0.5f);

        if (ordered.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
            return;
        }

        tvEmpty.setVisibility(View.GONE);
        LayoutInflater inflater = LayoutInflater.from(this);

        for (Note note : ordered) {
            View item = inflater.inflate(R.layout.item_note, notesContainer, false);
            bindNote(item, note);
            notesContainer.addView(item);
        }
    }

    private void bindNote(View item, Note note) {
        TextView tvPriority = item.findViewById(R.id.tvPriority);
        TextView tvDate = item.findViewById(R.id.tvDate);
        TextView tvTitle = item.findViewById(R.id.tvTitle);
        TextView tvContent = item.findViewById(R.id.tvContent);

        int colorRes;
        if (note.getPriority() == 1) {
            colorRes = R.color.priority_high;
        } else if (note.getPriority() == 2) {
            colorRes = R.color.priority_medium;
        } else if (note.getPriority() == 4) {
            colorRes = R.color.priority_academic;
        } else {
            colorRes = R.color.priority_low;
        }

        tvPriority.setText(note.getPriorityLabel());
        tvPriority.setBackgroundTintList(
                ColorStateList.valueOf(ContextCompat.getColor(this, colorRes)));
        tvDate.setText(note.getFormattedDate());
        tvTitle.setText(note.getTitle());
        tvContent.setText(note.getContent());

        item.setOnClickListener(v -> showNoteDialog(note));
        item.setOnLongClickListener(v -> {
            confirmDelete(note);
            return true;
        });
    }

    private void showNoteDialog(Note existing) {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_note, null);

        TextView tvDialogTitle = view.findViewById(R.id.tvDialogTitle);
        EditText etTitle = view.findViewById(R.id.etTitle);
        EditText etContent = view.findViewById(R.id.etContent);
        RadioGroup rgPriority = view.findViewById(R.id.rgPriority);
        Button btnCancel = view.findViewById(R.id.btnCancel);
        Button btnSave = view.findViewById(R.id.btnSave);

        tintRadio(view, R.id.rbImportante, R.color.priority_high);
        tintRadio(view, R.id.rbMedia, R.color.priority_medium);
        tintRadio(view, R.id.rbBaja, R.color.priority_low);
        tintRadio(view, R.id.rbAcademico, R.color.priority_academic);

        boolean editing = existing != null;
        tvDialogTitle.setText(editing ? R.string.dialog_editar : R.string.dialog_nueva);

        if (editing) {
            etTitle.setText(existing.getTitle());
            etContent.setText(existing.getContent());
            int checked;
            if (existing.getPriority() == 1) {
                checked = R.id.rbImportante;
            } else if (existing.getPriority() == 2) {
                checked = R.id.rbMedia;
            } else if (existing.getPriority() == 4) {
                checked = R.id.rbAcademico;
            } else {
                checked = R.id.rbBaja;
            }
            rgPriority.check(checked);
        } else {
            rgPriority.check(R.id.rbMedia);
        }

        AlertDialog dialog = new AlertDialog.Builder(this).setView(view).create();
        dialog.setOnShowListener(d -> {
            Window window = dialog.getWindow();
            if (window != null) {
                window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                int width = (int) (getResources().getDisplayMetrics().widthPixels * 0.92);
                window.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT);
            }
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnSave.setOnClickListener(v -> {
            String title = etTitle.getText().toString().trim();
            String content = etContent.getText().toString().trim();

            if (TextUtils.isEmpty(title)) {
                etTitle.setError(getString(R.string.error_asunto));
                etTitle.requestFocus();
                return;
            }
            if (TextUtils.isEmpty(content)) {
                etContent.setError(getString(R.string.error_detalle));
                etContent.requestFocus();
                return;
            }

            int priority;
            if (rgPriority.getCheckedRadioButtonId() == R.id.rbImportante) {
                priority = 1;
            } else if (rgPriority.getCheckedRadioButtonId() == R.id.rbBaja) {
                priority = 3;
            } else if (rgPriority.getCheckedRadioButtonId() == R.id.rbAcademico) {
                priority = 4;
            } else {
                priority = 2;
            }

            if (editing) {
                existing.setTitle(title);
                existing.setContent(content);
                existing.setPriority(priority);
            } else {
                long now = System.currentTimeMillis();
                notes.add(new Note(String.valueOf(now), title, content, now, priority));
            }

            NoteStore.save(this, notes);
            render();
            dialog.dismiss();
        });

        dialog.show();
    }

    private void tintRadio(View root, int radioId, int colorRes) {
        RadioButton radio = root.findViewById(radioId);
        CompoundButtonCompat.setButtonTintList(
                radio, ColorStateList.valueOf(ContextCompat.getColor(this, colorRes)));
    }

    private void confirmDelete(Note note) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.eliminar_titulo)
                .setMessage(getString(R.string.eliminar_mensaje, note.getTitle()))
                .setPositiveButton(R.string.eliminar, (dialog, which) -> {
                    notes.remove(note);
                    NoteStore.save(this, notes);
                    render();
                    Toast.makeText(this, R.string.registro_eliminado, Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton(R.string.cancelar, null)
                .show();
    }
}
