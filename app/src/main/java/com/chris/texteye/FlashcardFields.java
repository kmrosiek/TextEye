package com.chris.texteye;

import android.util.Log;

import com.chris.texteye.database.Entities.Flashcard;

import java.util.ArrayList;
import java.util.List;

public class FlashcardFields {
    private static final String TAG = "FlashcardFieldstyDD";

    public static final boolean UNTRANSLATED = false;
    public static final boolean MISSPELLED = true;

    public FlashcardFields(final boolean translated) {
        this.translatedAlready = translated;
    }

    FlashcardFields(List<String> suggestions, boolean misspelled) {
        this.misspelled = misspelled;
        this.suggestions = suggestions;
        translatedAlready = true;
    }

    FlashcardFields(final String pT, final String eS, final String pS) {
        polishText = pT;
        englishSentence = eS;
        polishSentence = pS;
        translatedAlready = true;
    }

    public boolean isMisspelled() {
        return misspelled;
    }

    public boolean isTranslatedAlready() {
        return translatedAlready;
    }

    public String getPolishText() {
        return polishText;
    }

    public String getEnglishSentence() {
        return englishSentence;
    }

    public String getPolishSentence() {
        return polishSentence;
    }

    public List<String> getSuggestions() {
        return suggestions;
    }

    private String polishText;
    private String englishSentence;
    private String polishSentence;
    /** Misspelled when word could not be found in the dictionary. translatedAlready when the answer
     * from dictionary was receiver. If word has translatedAlready = false it cannot be misspelled,
     * because it was not checked whether it was misspelled or not.
     */
    private boolean misspelled = false;
    private boolean translatedAlready;
    /** Strings are added to list when the word was misspelled and suggestions were found in
     * the dictionary
     */
    private List<String> suggestions = new ArrayList<>();

    public static Flashcard createFlashcardFromFF(final String frontText, final int deckId,
                                                  final FlashcardFields flashcardFields) {
        Flashcard flashcard;
        if (flashcardFields == null) {
            Log.d(TAG, "createFlashcardFromFF: text: " + frontText +
                    ". Error. FlashcardFields equals null.");
            flashcard = new Flashcard(deckId, frontText);
        } else if (!flashcardFields.isTranslatedAlready()) {
            Log.d(TAG, "createFlashcardFromFF: text: " + frontText +
                    ". Not checked in dictionary yet.");
            flashcard = new Flashcard(deckId, frontText);
        } else if(flashcardFields.isMisspelled()) {
            Log.d(TAG, "createFlashcardFromFF: text: " + frontText + " is misspelled.");
            flashcard = new Flashcard(deckId, frontText,
                    Flashcard.ALREADY_CHECKED_IN_DICTIONARY,
                    Flashcard.TEXT_IS_MISSPELLED);
        }
        else { // Everything is fine.
            Log.d(TAG, "createFlashcardFromFF: text: " + frontText + ". Correct.");
            flashcard = new Flashcard(deckId, frontText, flashcardFields.getPolishText(),
                    flashcardFields.getEnglishSentence(), flashcardFields.getPolishSentence());
        }
        return flashcard;
    }
}
