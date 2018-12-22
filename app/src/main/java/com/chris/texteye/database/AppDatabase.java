package com.chris.texteye.database;

import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;

import com.chris.texteye.database.DAOs.DeckDao;
import com.chris.texteye.database.DAOs.FlashcardDao;
import com.chris.texteye.database.Entities.Flashcard;
import com.chris.texteye.database.Entities.Deck;


@Database(entities = {Deck.class, Flashcard.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    private static final Object LOCK = new Object();
    private static AppDatabase instance;

    public abstract DeckDao deckDao();

    public abstract FlashcardDao flashcardDao();

    public static AppDatabase getInstance(Context context) {
        if(instance == null) {
            synchronized (LOCK) {
                instance = Room.databaseBuilder(context.getApplicationContext(),
                        AppDatabase.class, "diki_database").fallbackToDestructiveMigration()
                        .addCallback(roomCallback)
                        .build();
            }
        }

        return instance;
    }

    private static RoomDatabase.Callback roomCallback = new RoomDatabase.Callback(){
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);
            new PopulateDbAsyncTask(instance).execute();
        }
    };

    private static class PopulateDbAsyncTask extends AsyncTask<Void, Void, Void> {
        private DeckDao deckDao;
        private FlashcardDao flashcardDao;

        private PopulateDbAsyncTask(AppDatabase db) {
            deckDao = db.deckDao();
            flashcardDao = db.flashcardDao();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            deckDao.insert(new Deck("Benjamin Franklin"));
            deckDao.insert(new Deck("Do Androids Dream of Electric Sheep?"));
            deckDao.insert(new Deck("Amazing story of our country"));
            flashcardDao.insert(new Flashcard(1, "Amazing", "Niesamowity", "It was a truly amazing performance.", "To było naprawdę niezwykłe przedstawienie."));
            flashcardDao.insert(new Flashcard(1, "Force", "Sila, moc (np.uderzenia)", "The car hit the tree with great force.", "Samochód uderzył w drzewo z wielką siłą."));
            flashcardDao.insert(new Flashcard(1, "Stop", "zatrzymac", "Could you stop here, please?", "Czy mógłbyś się, proszę, tutaj zatrzymać?"));
            flashcardDao.insert(new Flashcard(1, "Car", "Samochod", "He got struck by a car and is in hospital now.", "On został potrącony przez samochód i jest teraz w szpitalu."));
            flashcardDao.insert(new Flashcard(1, "Bike", "Rower", "You can keep a bicycle here if you want.","Możesz tutaj trzymać swój rower, jeśli chcesz."));
            flashcardDao.insert(new Flashcard(1, "Coffee", "Kawa","We were talking and drinking coffee.", "Rozmawialiśmy i piliśmy kawę."));
            flashcardDao.insert(new Flashcard(2, "Rope", "Lina", "I used the rope to get down into the cave.", "Użyłem liny żeby dostać się do jaskini."));
            flashcardDao.insert(new Flashcard(2, "Hammer"));
            flashcardDao.insert(new Flashcard(3, "Tram", "Tramwaj", "Which tram goes to the bus station?","Który tramwaj jedzie na dworzec autobusowy?"));
            return null;
        }
    }
}
