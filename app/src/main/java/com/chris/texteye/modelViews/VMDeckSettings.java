package com.chris.texteye.modelViews;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;

import com.chris.texteye.AppRepository;
import com.chris.texteye.database.Entities.Flashcard;

import java.util.List;

public class VMDeckSettings extends AndroidViewModel {

    private AppRepository repository;
    private LiveData<List<Flashcard>> flashcards;
    private int deckId;

    public VMDeckSettings(@NonNull Application application) {
        super(application);
        repository = new AppRepository(application);
    }

    public void init(final int deckId) {
        flashcards = repository.getFlashcardsByDeckId(deckId);
        this.deckId = deckId;
    }

    public LiveData<List<Flashcard>> getFronts() {
        return flashcards;
    }

    public void removeFlashcard(final int flashcardId){
        repository.deleteFlashcard(deckId, flashcardId);
    }

    public void insertFlashcard(final Flashcard flashcard){
        repository.insertFlashcard(flashcard);
    }

    public boolean checkIfExists(final String front) {
        for(Flashcard flashcard : flashcards.getValue()) {
            if(flashcard.getListId() == deckId && flashcard.getEngText().equals(front))
                return true;
        }
        return false;
    }
}
