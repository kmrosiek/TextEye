package com.chris.texteye.activities;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import com.chris.texteye.Dictionary;
import com.chris.texteye.FlashcardFields;
import com.chris.texteye.InternetBroadcastReceiver;
import com.chris.texteye.R;
import com.chris.texteye.adapters.DeckSettingsListView;
import com.chris.texteye.database.Entities.Flashcard;
import com.chris.texteye.modelViews.VMDeckSettings;

import java.util.HashSet;
import java.util.Set;

public class DeckSettings extends AppCompatActivity {

    private static final String TAG = "DeckSettingstyDD";
    public static final int EDIT_FLASHCARD_REQUEST = 1;
    public static final String EXTRA_FLASHCARD_TEXT = "com.chris.texteye.activities.EXTRA_FLASHCARD_TEXT";
    public static final String EXTRA_FLASHCARD_ID = "com.chris.texteye.activities.EXTRA_FLASHCARD_ID";
    private Set<Integer> selectedItems = new HashSet<>();
    private Menu toolbar_menu;
    private int deckId;
    private boolean selectingModeOn = false;
    VMDeckSettings vmDeckSettings;
    private ListView listView;
    private DeckSettingsListView listViewAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deck_settings);

        setupToolbar();

        getIntentInfo();

        setupListView();

        setupAddWordListeners();
    }

    private void setupAddWordListeners() {
        ImageButton addWordButton = findViewById(R.id.add_word_button);

        final EditText userNewWordEditText = findViewById(R.id.user_insert_new_word);
        userNewWordEditText.setOnKeyListener((View.OnKeyListener) (v, keyCode, event) -> {
            if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                performDictionaryLookupIfNotEmpty(userNewWordEditText);
                //todo do not lose focus when performing this click.
                // listView.setFocusable(false);
                // Works but the keyboard hides.
                return true;
            }
            return false;
        });
        addWordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: size: " + selectedItems.size() + " selectingMode: " + selectingModeOn);
                performDictionaryLookupIfNotEmpty(userNewWordEditText);
            }
        });
    }

    private void performDictionaryLookupIfNotEmpty(EditText editText) {
        final String userNewWord = editText.getText().toString().trim();
        if (userNewWord.isEmpty()) {
            Toast.makeText(DeckSettings.this, "Enter new word.", Toast.LENGTH_LONG).show();
        } else {
            if (vmDeckSettings.checkIfExists(userNewWord)) {
                Toast.makeText(DeckSettings.this, userNewWord +
                        " is already in the deck.", Toast.LENGTH_LONG).show();
            } else {
                dictionaryCheckUp(userNewWord);
                editText.setText("");
            }
        }
    }

    private void setupListView() {
        listView = findViewById(R.id.settings_list_of_words);

        listViewAdapter = new DeckSettingsListView(this, null);
        listView.setAdapter(listViewAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Log.d(TAG, "onItemClick: position: " + position + "  id: " + listViewAdapter.getItemId(position));
                Log.d(TAG, "onItemClick: string:" + listViewAdapter.getIdByPosition(position));
                final int flashcardId = listViewAdapter.getIdByPosition(position);

                if (selectingModeOn) {
                    if (selectedItems.contains(flashcardId)) {
                        Log.d(TAG, "onItemClick: Contains");
                        view.setBackgroundColor(Color.TRANSPARENT);
                        selectedItems.remove(flashcardId);
                    } else {
                        Log.d(TAG, "onItemClick: Does not contain.");
                        view.setBackgroundResource(R.color.colorPrimary);
                        selectedItems.add(flashcardId);
                    }

                    if (selectedItems.isEmpty()) {
                        toolbar_menu.findItem(R.id.bin).setVisible(false);
                        getSupportActionBar().setSubtitle("");
                        selectingModeOn = false;
                    } else {
                        toolbar_menu.findItem(R.id.bin).setVisible(true);
                        getSupportActionBar().setSubtitle(Integer.toString(selectedItems.size()) + " words selected");
                    }
                } else {
                    Log.d(TAG, "onItemClick: Start activity for word: " + listViewAdapter.getNameByPosition(position));
                    goToFlashcardEditActivity(listViewAdapter.getNameByPosition(position),
                            listViewAdapter.getIdByPosition(position));
                }

            }
        });

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                final int flashcardId = listViewAdapter.getIdByPosition(position);

                if (!selectedItems.contains(flashcardId)) {
                    view.setBackgroundResource(R.color.colorPrimary);
                    selectedItems.add(flashcardId);
                }

                // Start the selection.
                if (!selectingModeOn) {
                    selectingModeOn = true;
                    toolbar_menu.findItem(R.id.bin).setVisible(true);
                    getSupportActionBar().setSubtitle(Integer.toString(selectedItems.size()) + " words selected");
                    hideKeyboard();
                    Log.d(TAG, "onItemLongClick: size: " + selectedItems.size());
                }

                Log.d(TAG, "onItemLongClick: Long click on item: " + flashcardId);
                return true;
            }
        });


        //todo repair bug for empty deck.
        vmDeckSettings = ViewModelProviders.of(this).get(VMDeckSettings.class);
        vmDeckSettings.init(deckId);
        vmDeckSettings.getFronts().observe(this, flashcards -> {
            Log.d(TAG, "onChanged: size: " + flashcards.size());
            listViewAdapter.setDeck(flashcards);
        });
    }

    private void goToFlashcardEditActivity(final String flashcardText, final int id) {
        Intent intent = new Intent(this, FlashcardEdit.class);
        intent.putExtra(EXTRA_FLASHCARD_TEXT, flashcardText);
        intent.putExtra(EXTRA_FLASHCARD_ID, id);
        startActivityForResult(intent, EDIT_FLASHCARD_REQUEST);
    }


    private void dictionaryCheckUp(final String extractedText) {
        if (InternetBroadcastReceiver.CONNECTED_TO_INTERNET) {
            new Thread(() -> {
                Dictionary dictionary = new Dictionary();
                FlashcardFields flashcardFields = dictionary.getTranslation(extractedText);
                Flashcard flashcard = FlashcardFields.createFlashcardFromFF(extractedText, deckId, flashcardFields);
                vmDeckSettings.insertFlashcard(flashcard);
                if (flashcardFields != null) {
                    Log.d(TAG, "run: Dictionary for word \"" + extractedText + "\".done: "
                            + flashcardFields.getEnglishSentence());
                }
            }).start();

        } else {
            Log.d(TAG, "run: Connected to the Internet: " + InternetBroadcastReceiver.CONNECTED_TO_INTERNET);
            Flashcard flashcard = FlashcardFields.createFlashcardFromFF(extractedText, deckId,
                    new FlashcardFields(FlashcardFields.UNTRANSLATED));
            vmDeckSettings.insertFlashcard(flashcard);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch(requestCode)
        {
            case EDIT_FLASHCARD_REQUEST:
                if(resultCode == RESULT_OK) {
                    String newFlashcardText = data.getStringExtra(EXTRA_FLASHCARD_TEXT);
                    final int ID_NOT_PROVIDED = -1;
                    int editedFlashcardId = data.getIntExtra(EXTRA_FLASHCARD_ID, ID_NOT_PROVIDED);
                    
                    if(editedFlashcardId != ID_NOT_PROVIDED) {
                        vmDeckSettings.removeFlashcard(editedFlashcardId);
                        dictionaryCheckUp(newFlashcardText);
                        Toast.makeText(this, "Flashcard updated.", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        Log.d(TAG, "onActivityResult: Flashcard ID not provided.");
                        Toast.makeText(this, "Flashcard ID not provided.", Toast.LENGTH_SHORT).show();
                    }

                } else {
                    Toast.makeText(this, "Flashcard was not changed.", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    private void setupToolbar() {

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null)
            //todo change to actual title.
            getSupportActionBar().setTitle("Benjamin Franklin");
        else
            Log.e(TAG, "onCreate: ActionBar equals null.");
    }

    private void hideKeyboard() {
        View view = DeckSettings.this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.general_menu, menu);
        toolbar_menu = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.bin: {
                for (int i : selectedItems) {
                    Log.d(TAG, "onOptionsItemSelected: removing id: " + i);
                    vmDeckSettings.removeFlashcard(i);
                }
                Toast.makeText(this, selectedItems.size() + " flashcards removed.",
                        Toast.LENGTH_SHORT).show();
                leaveSelectingMode();
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void leaveSelectingMode() {
        selectedItems.clear();
        toolbar_menu.findItem(R.id.bin).setVisible(false);
        getSupportActionBar().setSubtitle("");
        selectingModeOn = false;
        listViewAdapter.resetItemsBackgrounds();
    }

    private void getIntentInfo() {
        Intent intent = getIntent();
        final int DECK_ID_NOT_GIVEN = -1;
        deckId = intent.getIntExtra("list_id", DECK_ID_NOT_GIVEN);
        if (deckId == DECK_ID_NOT_GIVEN) {
            Toast.makeText(this, "List not provided!", Toast.LENGTH_LONG).show();
            Log.d(TAG, "Starting deck setting activity, deckId was not provided.");
        } else {
            Log.d(TAG, "Starting deck setting activity deckId: " + deckId);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        Log.d(TAG, "onSupportNavigateUp: ");
        if (selectingModeOn) {
            leaveSelectingMode();
        } else
            onBackPressed();

        return true;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Log.d(TAG, "onKeyDown: ");
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Log.d(TAG, "back button pressed");
            if (selectingModeOn) {
                leaveSelectingMode();
                return false;
            }
        }

        return super.onKeyDown(keyCode, event);
    }

}
