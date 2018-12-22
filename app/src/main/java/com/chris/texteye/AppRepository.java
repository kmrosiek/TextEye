package com.chris.texteye;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.os.AsyncTask;
import android.util.Log;

import com.chris.texteye.database.AppDatabase;
import com.chris.texteye.database.DAOs.DeckDao;
import com.chris.texteye.database.DAOs.FlashcardDao;
import com.chris.texteye.database.Entities.Flashcard;
import com.chris.texteye.database.Entities.Deck;

import java.util.List;

public class AppRepository implements AsyncResult{
    private static final String TAG = "AppRepositorytyDD";

    private DeckDao deckDao;
    private LiveData<List<Deck>> allDecks;

    private FlashcardDao flashcardDao;
    private LiveData<List<Flashcard>> cardsForList;
    private LiveData<List<Flashcard>> allFlashcards;

    public AppRepository(Application application) {
        AppDatabase database = AppDatabase.getInstance(application);
        deckDao = database.deckDao();
        allDecks = deckDao.getAllDecks();
        flashcardDao = database.flashcardDao();
        cardsForList = flashcardDao.getCardsByListId(3);
        allFlashcards = flashcardDao.getAllFlashcards();
    }

    public void insertFlashcard(Flashcard flashcard){
        new InsertFlashcardAsyncTask(flashcardDao).execute(flashcard);
    }

    public void insertDeck(Deck words_list) {
        new InsertWordsListAsyncTask(deckDao).execute(words_list);
    }

    public void updateDeck(Deck words_list) {
        new UpdateWordsListAsyncTask(deckDao).execute(words_list);
    }

    public void delete(Deck words_list) {
        new DeleteWordsListAsyncTask(deckDao).execute(words_list);
    }

    public LiveData<List<Flashcard>> getFlashcardsByDeckId(Integer id) {
        return flashcardDao.getCardsByListId(id);
    }

    public void deleteFlashcard(final int deckId, final int flashcardId) {
        new DeleteFlashcardAsyncTask(flashcardDao).execute(deckId, flashcardId);
    }

    public LiveData<List<Deck>> getAllDecks(){
       return allDecks;
    }

    public LiveData<List<Flashcard>> getAllFlashcards(){ return allFlashcards; }

    public void fetchFlashcardsByListId(final int id) {
        GetCardsByListIdAsyncTask task = new GetCardsByListIdAsyncTask(flashcardDao);
        task.delegate = this;
        task.execute(id);
    }

    public LiveData<List<Flashcard>> getFlashcardsByListId() {
        return cardsForList;
    }

    public void updateFlashcard(Flashcard flashcard) {
        new UpdateFlashcardAsyncTask(flashcardDao).execute(flashcard);
    }

    public void fetchUntranslatedFlashcards() {
        Log.d(TAG, "fetchUntranslatedFlashcards: ");
        FetchUntranslatedFlashcardsAsyncTask task = new FetchUntranslatedFlashcardsAsyncTask(flashcardDao);
        task.delegate = this;
        task.execute();
    }
    
    @Override
    public void cardsSelectionAsyncFinished(LiveData<List<Flashcard>> results) {
//       cardsForList.setValue(results);
    }

    @Override
    public void untranslatedFlashcardsAsyncFinished(List<Flashcard> results) {
        if(results != null)
            Log.d(TAG, "untranslatedFlashcardsAsyncFinished: " + results.size());
        else
            Log.d(TAG, "untranslatedFlashcardsAsyncFinished: ");
        InternetBroadcastReceiver.updateFlashcards(results);
    }



    // --------- ASYNC TASKS ---------



    private static class InsertWordsListAsyncTask extends AsyncTask<Deck, Void, Void> {

        private DeckDao words_list_dao;

        private InsertWordsListAsyncTask(DeckDao words_list_dao) {
            this.words_list_dao = words_list_dao;
        }

        @Override
        protected Void doInBackground(Deck... decks) {
            words_list_dao.insert(decks[0]);
            return null;
        }
    }

    private static class UpdateWordsListAsyncTask extends AsyncTask<Deck, Void, Void> {

        private DeckDao words_list_dao;

        private UpdateWordsListAsyncTask(DeckDao words_list_dao) {
            this.words_list_dao = words_list_dao;
        }

        @Override
        protected Void doInBackground(Deck... decks) {
            words_list_dao.update(decks[0]);
            return null;
        }
    }

    private static class DeleteWordsListAsyncTask extends AsyncTask<Deck, Void, Void> {

        private DeckDao words_list_dao;

        private DeleteWordsListAsyncTask(DeckDao words_list_dao) {
            this.words_list_dao = words_list_dao;
        }

        @Override
        protected Void doInBackground(Deck... decks) {
            words_list_dao.delete(decks[0]);
            return null;
        }
    }


    private static class GetCardsByListIdAsyncTask extends AsyncTask<Integer, Void, LiveData<List<Flashcard>>> {

        private FlashcardDao flashcardDao;
        private AppRepository delegate = null;

        @Override
        protected void onPostExecute(LiveData<List<Flashcard>> flashcards) {
            delegate.cardsSelectionAsyncFinished(flashcards);
        }

        private GetCardsByListIdAsyncTask(FlashcardDao flashcardDao) {
            this.flashcardDao = flashcardDao;
        }

        @Override
        protected LiveData<List<Flashcard>> doInBackground(Integer... ints) {
            return flashcardDao.getCardsByListId(ints[0]);
        }
    }

    // ---------------- ASYNC FLASHCARD TASKS -------------------------------

    private static class InsertFlashcardAsyncTask extends AsyncTask<Flashcard, Void, Void> {

        private FlashcardDao flashcardDao;

        private InsertFlashcardAsyncTask(FlashcardDao flashcardDao) {
            this.flashcardDao = flashcardDao;
        }

        @Override
        protected Void doInBackground(Flashcard... flashcards) {
            flashcardDao.insert(flashcards[0]);
            return null;
        }
    }

    private static class UpdateFlashcardAsyncTask extends AsyncTask<Flashcard, Void, Void> {

        private FlashcardDao flashcardDao;

        private UpdateFlashcardAsyncTask(FlashcardDao flashcardDao) {
            this.flashcardDao = flashcardDao;
        }

        @Override
        protected Void doInBackground(Flashcard... flashcards) {
            flashcardDao.update(flashcards[0]);
            return null;
        }
    }

    private static class FetchUntranslatedFlashcardsAsyncTask extends AsyncTask<Void, Void, List<Flashcard>> {

        private FlashcardDao flashcardDao;
        private AppRepository delegate = null;

        @Override
        protected void onPostExecute(List<Flashcard> flashcards) {
            delegate.untranslatedFlashcardsAsyncFinished(flashcards);
        }

        private FetchUntranslatedFlashcardsAsyncTask(FlashcardDao flashcardDao) {
            this.flashcardDao = flashcardDao;
        }

        @Override
        protected List<Flashcard> doInBackground(Void... voids) {
            return flashcardDao.getUntranslatedFlashcards();
        }
    }

    private static class DeleteFlashcardAsyncTask extends AsyncTask<Integer, Void, Void> {

        private FlashcardDao flashcardDao;

        private DeleteFlashcardAsyncTask(FlashcardDao flashcardDao) {
            this.flashcardDao = flashcardDao;
        }

        @Override
        protected Void doInBackground(Integer... ints) {
            flashcardDao.deleteFlashcard(ints[0], ints[1]);
            return null;
        }
    }
}
