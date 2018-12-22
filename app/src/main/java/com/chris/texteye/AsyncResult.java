package com.chris.texteye;

import android.arch.lifecycle.LiveData;

import com.chris.texteye.database.Entities.Flashcard;

import java.util.List;

public interface AsyncResult {
    void cardsSelectionAsyncFinished(LiveData<List<Flashcard>> results);
    void untranslatedFlashcardsAsyncFinished(List<Flashcard> results);
}
