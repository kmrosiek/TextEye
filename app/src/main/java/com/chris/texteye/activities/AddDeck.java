package com.chris.texteye.activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.chris.texteye.R;

public class AddDeck extends AppCompatActivity {
    private static final String TAG = "AddDecktyDD";

    public static final String EXTRA_DECK_NAME = "com.chris.texteye.activities.EXTRA_DECK_NAME";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_deck);

        setUpToolbar();
        setupAddButton();
    }

    private void setupAddButton() {

        Button addDeckButton = findViewById(R.id.add_deck_button);

        addDeckButton.setOnClickListener(v -> addDeck());
    }

    private void addDeck() {
        EditText deckNameEditText = findViewById(R.id.edit_text_deck_name);
        String deckName = deckNameEditText.getText().toString();

        if(deckName.trim().isEmpty()) {
            Toast.makeText(this, "Please enter the deck name.", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent();
        intent.putExtra(EXTRA_DECK_NAME, deckName);

        setResult(RESULT_OK, intent);
        finish();

    }
    /** Each element of toolbar menu that will be used in this activity has to be made visible*/
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.general_menu, menu);
        return true;
    }

    private void setUpToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if(getSupportActionBar() != null) {
            setTitle("Add Deck");
        } else
            Log.e(TAG, "setUpToolbar: getSupportActionBar equals NULL");
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
