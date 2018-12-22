package com.chris.texteye;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.util.Log;
import android.widget.Toast;

import com.chris.texteye.database.Entities.Flashcard;

import java.util.List;

public class InternetBroadcastReceiver extends BroadcastReceiver {
    private static final String TAG = "InternetBroadcastRetyDD";

    private static AppRepository repository = null;
    public static boolean CONNECTED_TO_INTERNET = true;

    public InternetBroadcastReceiver() {

    }

    public static void updateFlashcards(final List<Flashcard> flashcards) {
        Log.d(TAG, "updateFlashcards: ");
        if (flashcards != null) {
            Log.d(TAG, "updateFlashcards: flash size: " + flashcards.size());
            if (flashcards.size() > 0)
                Log.d(TAG, "updateFlashcards: " + flashcards.get(0).getEngText());

            //todo create thread, ask for translations, save to database.

            new Thread(new Runnable() {
                public void run() {
                    Log.d(TAG, "run: ");
                    final Dictionary dictionary = new Dictionary();
//                    Handler handler = new Handler();
                    int counter = 0;
                    for (final Flashcard flashcard : flashcards) {

                        Log.d(TAG, "run: Translating : " + flashcard.getEngText());
                        FlashcardFields flashcardFields = dictionary.getTranslation(flashcard.getEngText());
                        if (flashcardFields.isMisspelled()) {
                            Log.d(TAG, "run: Flashcard misspelled.");
                            Flashcard newFlashcard = new Flashcard(flashcard.getListId(),
                                    flashcard.getEngText(),
                                    Flashcard.ALREADY_CHECKED_IN_DICTIONARY,
                                    Flashcard.TEXT_IS_MISSPELLED);
                            newFlashcard.setId(flashcard.getId());
                            repository.updateFlashcard(newFlashcard);
                            continue;
                        }
                        if (!flashcardFields.isTranslatedAlready()) {
                            Log.d(TAG, "run: Error while translating word: " +
                                    flashcard.getEngText() + ". Stopping translations.");
                            break;
                        }

                        Flashcard newFlashcard = new Flashcard(flashcard.getListId(),
                                flashcard.getEngText(),
                                flashcardFields.getPolishText(),
                                flashcardFields.getEnglishSentence(),
                                flashcardFields.getPolishSentence());
                        newFlashcard.setId(flashcard.getId());

                        repository.updateFlashcard(newFlashcard);
                        counter++;

                        Log.d(TAG, "run: Inserted: " + flashcard.getListId() + " " +
                                flashcard.getEngText() + " " +
                                flashcardFields.getPolishText() + " " +
                                flashcardFields.getEnglishSentence() + " " +
                                flashcardFields.getPolishSentence());
                    }

                    if(counter > 0) {
                        Log.d(TAG, "run: " + counter + " words were updated.");
                    }

                }
            }).start();
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {
            boolean noConnectivity = intent.getBooleanExtra(
                    ConnectivityManager.EXTRA_NO_CONNECTIVITY, false);

            if (noConnectivity) {
                Toast.makeText(context, "Disconnected", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "onReceive: Disconnected");
                CONNECTED_TO_INTERNET = false;
            } else {
                CONNECTED_TO_INTERNET = true;
                Toast.makeText(context, "Connected", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "onReceive: Connected");

                if (repository == null) {
                    Application application = (Application) context.getApplicationContext();
                    repository = new AppRepository(application);
                }

                repository.fetchUntranslatedFlashcards();
            }
        }
    }
}
