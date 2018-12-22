package com.chris.texteye.activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.chris.texteye.R;

public class FlashcardEdit extends AppCompatActivity {
    private static final String TAG = "FlashcardEdityDD";

    private String flashcardText;
    private int flashcardId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flashcard_edit);


        setupToolbar();
        setupSaveButton();
        setupSpinner();

        retrieveDataFromIntent();
        setupEditText(flashcardText);
    }

    private void setupEditText(final String flashcardName) {
        final EditText editText = findViewById(R.id.f_edit_edit_flashcard_name_edit_text);
        editText.setText(flashcardName);
        //todo move carret to the end of edittext.
    }

    private void retrieveDataFromIntent() {
        Intent intent = getIntent();
        flashcardText = intent.getStringExtra(DeckSettings.EXTRA_FLASHCARD_TEXT);
        final int FLASHCARD_ID_NOT_PROVIDED = -1;
        flashcardId = intent.getIntExtra(DeckSettings.EXTRA_FLASHCARD_ID, FLASHCARD_ID_NOT_PROVIDED);
        if(flashcardId == FLASHCARD_ID_NOT_PROVIDED) {
            Log.d(TAG, "retrieveDataFromIntent: Flashcard ID not provided.");
            Toast.makeText(this, "Flashcard ID not provided.", Toast.LENGTH_SHORT).show();
            setResult(RESULT_CANCELED, intent);
            finish();
        }
    }

    private void setupSaveButton() {
        Button addDeckButton = findViewById(R.id.f_edit_save_flashcard_changes_button);

        addDeckButton.setOnClickListener(v ->saveFlashcardChanges());
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if(getSupportActionBar() != null) {
            setTitle("Edit flashcard");
        } else
            Log.e(TAG, "setUpToolbar: getSupportActionBar equals NULL");
    }

    private void setupSpinner() {
        Spinner dropdown = findViewById(R.id.f_edit_deck_selection_spinner);
        String[] items = new String[]{"Android book.", "This is it.", "Walk to freedom."};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, items);
        dropdown.setAdapter(adapter);
    }

    private void saveFlashcardChanges() {
        final EditText editFlashcardName = findViewById(R.id.f_edit_edit_flashcard_name_edit_text);
        String newFlashcardText = editFlashcardName.getText().toString();

        if(newFlashcardText.trim().isEmpty()) {
            Toast.makeText(this, "Please enter the flashcard name.", Toast.LENGTH_SHORT).show();
            return;
        }

        if(newFlashcardText.compareTo(flashcardText) == 0) {
            Toast.makeText(this, "No changes.", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "No changes to flashcard_name");
            Intent intent = new Intent();
            setResult(RESULT_CANCELED, intent);
            finish();
        } else {
            Intent intent = new Intent();
            intent.putExtra(DeckSettings.EXTRA_FLASHCARD_TEXT, newFlashcardText);
            intent.putExtra(DeckSettings.EXTRA_FLASHCARD_ID, flashcardId);
            setResult(RESULT_OK, intent);
            finish();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
