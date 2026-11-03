package com.example.informatica;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

public class NoteStore {

    private static final String PREF = "fap_bitacora";
    private static final String KEY_NOTES = "notes";
    private static final String KEY_EXAMPLES_CLEANED = "examples_cleaned";

    public static List<Note> load(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF, Context.MODE_PRIVATE);
        List<Note> notes = new ArrayList<>();

        String raw = prefs.getString(KEY_NOTES, null);
        if (raw != null) {
            try {
                JSONArray array = new JSONArray(raw);
                for (int i = 0; i < array.length(); i++) {
                    notes.add(Note.fromJson(array.getJSONObject(i)));
                }
            } catch (Exception ignored) {
            }
        }

        if (!prefs.getBoolean(KEY_EXAMPLES_CLEANED, false)) {
            notes.removeIf(NoteStore::isExample);
            save(context, notes);
            prefs.edit().putBoolean(KEY_EXAMPLES_CLEANED, true).apply();
        }

        return notes;
    }

    public static void save(Context context, List<Note> notes) {
        JSONArray array = new JSONArray();
        for (Note note : notes) {
            try {
                array.put(note.toJson());
            } catch (Exception ignored) {
            }
        }
        context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
                .edit()
                .putString(KEY_NOTES, array.toString())
                .apply();
    }

    private static boolean isExample(Note note) {
        return ("1".equals(note.getId()) && "INSPECCIÓN HANGAR 2".equals(note.getTitle()))
                || ("2".equals(note.getId()) && "BRIEFING OPERATIVO".equals(note.getTitle()))
                || ("3".equals(note.getId()) && "MANTENIMIENTO DE EQUIPO".equals(note.getTitle()));
    }
}
