package com.example.notesapp;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private SQLiteDatabase database;
    private SimpleCursorAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbHelper = new DatabaseHelper(this);
        database = dbHelper.getWritableDatabase();

        Button addNoteButton = findViewById(R.id.addNoteButton);
        ListView listView = findViewById(R.id.notesListView);

        addNoteButton.setOnClickListener(v -> showAddEditNoteDialog(null));

        loadNotes();
    }

    private void loadNotes() {
        Cursor cursor = database.query(DatabaseHelper.TABLE_NOTES, null, null, null, null, null, null);
        String[] from = {DatabaseHelper.COLUMN_NOTE};
        int[] to = {R.id.noteTextView};

        adapter = new SimpleCursorAdapter(this, R.layout.note_item, cursor, from, to, 0) {
            @Override
            public void bindView(View view, final Context context, Cursor cursor) {
                super.bindView(view, context, cursor);
                final long id = cursor.getLong(cursor.getColumnIndex(DatabaseHelper.COLUMN_ID));

                Button editButton = view.findViewById(R.id.editButton);
                Button deleteButton = view.findViewById(R.id.deleteButton);

                editButton.setOnClickListener(v -> showAddEditNoteDialog(id));
                deleteButton.setOnClickListener(v -> deleteNoteWithConfirmation(id));
            }
        };

        ((ListView) findViewById(R.id.notesListView)).setAdapter(adapter);
    }

    private ContentValues createNoteContentValues(String note) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_NOTE, note);
        return values;
    }

    private void showAddEditNoteDialog(final Long noteId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_edit_note, null);
        builder.setView(dialogView);

        final EditText noteEditText = dialogView.findViewById(R.id.noteEditText);
        Button saveButton = dialogView.findViewById(R.id.saveButton);
        final AlertDialog dialog = builder.create();

        if (noteId != null) {
            Cursor cursor = database.query(DatabaseHelper.TABLE_NOTES, new String[]{DatabaseHelper.COLUMN_NOTE},
                    DatabaseHelper.COLUMN_ID + " = ?", new String[]{String.valueOf(noteId)}, null, null, null);
            if (cursor != null) {
                cursor.moveToFirst();
                noteEditText.setText(cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_NOTE)));
                cursor.close();
            }
        }

        saveButton.setOnClickListener(v -> {
            String note = noteEditText.getText().toString();
            if (noteId == null) {
                database.insert(DatabaseHelper.TABLE_NOTES, null, createNoteContentValues(note));
            } else {
                database.update(DatabaseHelper.TABLE_NOTES, createNoteContentValues(note),
                        DatabaseHelper.COLUMN_ID + " = ?", new String[]{String.valueOf(noteId)});
            }
            loadNotes();
            dialog.dismiss();
        });

        dialog.show();
    }

    private void deleteNoteWithConfirmation(final long noteId) {
        new AlertDialog.Builder(this)
                .setMessage("Are you sure you want to delete this note?")
                .setPositiveButton("Yes", (dialog, which) -> deleteNote(noteId))
                .setNegativeButton("No", null)
                .show();
    }

    private void deleteNote(long noteId) {
        database.delete(DatabaseHelper.TABLE_NOTES, DatabaseHelper.COLUMN_ID + " = ?", new String[]{String.valueOf(noteId)});
        loadNotes();
    }
}
