package com.example.informatica;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Note {

    private final String id;
    private String title;
    private String content;
    private long timestamp;
    private int priority;

    public Note(String id, String title, String content, long timestamp, int priority) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.timestamp = timestamp;
        this.priority = priority;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public String getFormattedDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy • HH:mm", new Locale("es", "PE"));
        return sdf.format(new Date(timestamp)).toUpperCase(new Locale("es", "PE"));
    }

    public String getPriorityLabel() {
        if (priority == 1) {
            return "IMPORTANTE";
        } else if (priority == 2) {
            return "MEDIA";
        } else if (priority == 4) {
            return "ACADÉMICO";
        }
        return "BAJA";
    }

    public JSONObject toJson() throws JSONException {
        JSONObject object = new JSONObject();
        object.put("id", id);
        object.put("title", title);
        object.put("content", content);
        object.put("timestamp", timestamp);
        object.put("priority", priority);
        return object;
    }

    public static Note fromJson(JSONObject object) throws JSONException {
        return new Note(
                object.getString("id"),
                object.getString("title"),
                object.getString("content"),
                object.getLong("timestamp"),
                object.getInt("priority")
        );
    }
}
