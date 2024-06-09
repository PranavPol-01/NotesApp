package com.example.notesapp;

public class Note {
    private long id;
    private String note;

    public Note(long id, String note) {
        this.id = id;
        this.note = note;
    }

    public long getId() {
        return id;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
