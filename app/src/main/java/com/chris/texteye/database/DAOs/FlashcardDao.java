package com.chris.texteye.database.DAOs;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import com.chris.texteye.database.Entities.Flashcard;

import java.util.List;

@Dao
public interface FlashcardDao {

    @Insert
    void insert(Flashcard flashcard);

    @Update
    void update(Flashcard flashcard);

    @Delete
    void delete(Flashcard flashcard);

    @Query("SELECT * FROM flashcard_table WHERE list_id = :id")
    LiveData<List<Flashcard>> getCardsByListId(int id);

    @Query("SELECT * FROM flashcard_table WHERE dictionaryChecked=0")
    List<Flashcard> getUntranslatedFlashcards();

    @Query("SELECT * FROM flashcard_table")
    LiveData<List<Flashcard>> getAllFlashcards();

    @Query("DELETE FROM flashcard_table WHERE list_id= :deckId AND id= :flashcardId")
    void deleteFlashcard(int deckId, int flashcardId);
}
