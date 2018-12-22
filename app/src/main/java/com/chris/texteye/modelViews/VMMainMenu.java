package com.chris.texteye.modelViews;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;

import com.chris.texteye.AppRepository;
import com.chris.texteye.database.Entities.Deck;

import java.util.List;

public class VMMainMenu extends AndroidViewModel {

    private AppRepository repository;
    private LiveData<List<Deck>> allDecks;

    public VMMainMenu(@NonNull Application application) {
        super(application);
        repository = new AppRepository(application);
        allDecks = repository.getAllDecks();
    }

    public void insert(Deck deck) {
        repository.insertDeck(deck);
    }

    public LiveData<List<Deck>> getAllDecks() {
        return allDecks;
    }
}
