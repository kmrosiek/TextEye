package com.chris.texteye;

import java.util.HashMap;
import java.util.Map;

public class FlashcardData {

    private static final FlashcardData instance = new FlashcardData();

    public Map<String, FlashcardFields> flashcardDataMap = new HashMap<>();

    private int noTranslationsCounter = 0;

    private FlashcardData(){}

    public static FlashcardData getInstance(){
        return instance;
    }

    public Map<String, FlashcardFields> getMisspelledEntries() {
        Map<String, FlashcardFields> misspelled = new HashMap<>();
        for(Map.Entry<String, FlashcardFields> entry : flashcardDataMap.entrySet()) {
            if(entry.getValue().isTranslatedAlready() && entry.getValue().isMisspelled()){
                misspelled.put(entry.getKey(), entry.getValue());
            }
        }

        return misspelled;
    }

    public int getNoTranslationsCounter() {
        return noTranslationsCounter;
    }

    /**
     * Returns true if word was added. False if removed.
     */
    public boolean addOrRemoveIfAlreadyExists(final String extractedText) {
        if (flashcardDataMap.containsKey(extractedText)) {
            flashcardDataMap.remove(extractedText);
            return false;
        } else {
            flashcardDataMap.put(extractedText, new FlashcardFields(FlashcardFields.UNTRANSLATED));
            return true;
        }
    }

    public void put(String word, FlashcardFields fields) {
        if(!fields.isTranslatedAlready())
            noTranslationsCounter++;
       flashcardDataMap.put(word,fields);
    }

    public void clear() {
        flashcardDataMap.clear();
        noTranslationsCounter = 0;
    }

    public boolean isAlreadyInMap(final String word) {
        return flashcardDataMap.containsKey(word);
    }

    //todo check what happens when word is not there.
    public void removeEntry(final String word) {
        flashcardDataMap.remove(word);
    }
}
