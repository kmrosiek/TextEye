package com.chris.texteye.modelViews;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Transformations;
import android.support.annotation.NonNull;
import android.util.Log;

import com.chris.texteye.AppRepository;
import com.chris.texteye.database.Entities.Deck;
import com.chris.texteye.database.Entities.Flashcard;

import java.util.List;

public class VMStudy extends AndroidViewModel {

    private AppRepository repository;
    private LiveData<List<Flashcard>> flashcardsForSelectedList;
    private MutableLiveData<Integer> filterFlashcards = new MutableLiveData<Integer>();

    public VMStudy(@NonNull Application application) {
        super(application);
        repository = new AppRepository(application);
        flashcardsForSelectedList = Transformations.switchMap(filterFlashcards,
                id -> repository.getFlashcardsByDeckId(id));
    }

    public void setFilter(Integer filter) { filterFlashcards.setValue(filter);}

    public LiveData<List<Flashcard>> getFlashcardsForSelectedList() {
        return flashcardsForSelectedList;
    }

    public void fetchCardsByListId(final int id){
        repository.fetchFlashcardsByListId(id);
    }

    public void insert(Flashcard flashcard){
        repository.insertFlashcard(flashcard);
    }

}
