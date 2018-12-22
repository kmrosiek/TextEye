package com.chris.texteye.activities;

import android.Manifest;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.PointF;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.chris.texteye.AppConstant;
import com.chris.texteye.Dictionary;
import com.chris.texteye.FlashcardData;
import com.chris.texteye.FlashcardFields;
import com.chris.texteye.InternetBroadcastReceiver;
import com.chris.texteye.R;
import com.chris.texteye.WordExtractor;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.googlecode.tesseract.android.TessBaseAPI;

import org.opencv.android.OpenCVLoader;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

public class WordsSelection extends AppCompatActivity {

    private static final String TAG = "SelectionActivityDD";

    static {
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "static initializer: OpenCV Failed!");
        } else {
            Log.d(TAG, "instance initializer: OpenCV loaded successfully.");
        }
    }


    public static final String IMAGE_PATH_EXTRAS = "IMAGE_PATH";
    private TessBaseAPI tessBaseApi;
    private FlashcardData flashcardData = FlashcardData.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_words_selection);

        setUpToolbar();

        flashcardData.clear();

        if (checkExternalStoragePermission())
            initialiseOCRAndImageView();

    }

    private boolean checkIfImageExists(final String imagePath) {
        if (imagePath != null) {
            Log.d(TAG, "checkIfImageExists: path: " + imagePath);
            File file = new File(imagePath);
            if (file.exists()) {
                Log.d(TAG, "checkIfImageExists: The image exists.");
                return true;
            } else
                Log.d(TAG, "checkIfImageExists: The image does not exist.");
        } else
            Log.d(TAG, "checkIfImageExists: Path to the image was not provided.");

        return false;
    }

    private void initialiseOCRAndImageView() {
        OCR_init();

        final String imagePath = retrieveDataFromPreviousActivity();
        Log.d(TAG, "Image path:" + imagePath);
        if (checkIfImageExists(imagePath)) {
            setupImageView(imagePath);
        }
    }

    private void setupImageView(final String imagePath) {
        final SubsamplingScaleImageView imageView = findViewById(R.id.selection_image_view);
        final WordExtractor subImageExtractor = new WordExtractor(WordsSelection.this, imageView, imageView);
        String errorMessage = subImageExtractor.loadImage(imagePath);
        if (errorMessage != null) {
            Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
            return;
        }
        subImageExtractor.displayImage();

        final GestureDetector gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {

            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                if (imageView.isReady()) {
                    final PointF sCoord = imageView.viewToSourceCoord(e.getX(), e.getY());
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            if (sCoord == null) {
                                Log.e(TAG, "run: sCoord equals null.");
                                return;
                            }
                            final Bitmap extractedWordImage = subImageExtractor.extract((int) sCoord.x, (int) sCoord.y);
                            String preprocessedText = (extractedWordImage != null ? extractText(extractedWordImage) : "");
                            preprocessedText = preprocessedText.replaceAll("[^a-zA-Z0-9 '-]", "");
                            final String extractedText = preprocessedText.trim();
                            if (!extractedText.equals("")) {
                                if (flashcardData.addOrRemoveIfAlreadyExists(extractedText))
                                    dictionaryCheckUp(extractedText);
                            }

                            WordsSelection.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    TextView tesseractResult = findViewById(R.id.tesseract_result_text);
                                    tesseractResult.setText("Result: " + extractedText);
                                    ImageView tesseractImageView = findViewById(R.id.tesseract_result_image);
                                    if (extractedWordImage != null)
                                        tesseractImageView.setImageBitmap(extractedWordImage);
                                }
                            });
                        }
                    }).start();
                }
                return true;
            }
        });

        imageView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return gestureDetector.onTouchEvent(event);
            }
        });
    }

    private void dictionaryCheckUp(final String extractedText) {
        if (InternetBroadcastReceiver.CONNECTED_TO_INTERNET) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Dictionary dictionary = new Dictionary();
                    FlashcardFields flashcardFields = dictionary.getTranslation(extractedText);
                    flashcardData.put(extractedText, flashcardFields);
                    if (flashcardFields != null) {
                        Log.d(TAG, "run: Dictionary for word \"" + extractedText + "\".done: "
                                + flashcardFields.getEnglishSentence());
                    }
                }
            }).start();

        } else {
            Log.d(TAG, "run: Connected to the Internet: " + InternetBroadcastReceiver.CONNECTED_TO_INTERNET);
            flashcardData.put(extractedText, new FlashcardFields(FlashcardFields.UNTRANSLATED));
        }
    }


    private int getNoTranslationCounter() {
        return flashcardData.getNoTranslationsCounter();
    }

    class DialogManager {

        class MyDialog {

            MyDialog(final String word, final List<String> suggestions) {
                this.word = word;
                this.suggestions = suggestions;
                dialogType = SINGLE_CHOICE_DIALOG;
            }

            MyDialog(final String word) {
                this.word = word;
                dialogType = EDIT_TEXT_DIALOG;
            }


            String word;
            List<String> suggestions;
            int dialogType;
        }

        private void popAndDisplayDialog() {
            Log.d(TAG, "popAndDisplayDialog: queue size:" + dialogsQueue.size());
            MyDialog myDialog = dialogsQueue.poll();
            if (myDialog != null) {
                if (myDialog.dialogType == SINGLE_CHOICE_DIALOG) {
                    List<String> items = new ArrayList<>();
                    items.clear(); // Needed if dialog box appear again, then adding will be duplicated.
                    items = myDialog.suggestions;
                    items.add("Other:");
                    dialogBoxSingleChoice(myDialog.word, items.toArray(new String[0]));
                } else {
                    dialogBoxEditText(myDialog.word);
                }
            } else {
                Log.d(TAG, "popAndDisplayDialog: " + flashcardData.flashcardDataMap.size() + " Map: " + flashcardData.flashcardDataMap.toString());
                int counter = getNoTranslationCounter();
                Log.d(TAG, "popAndDisplayDialog: Counter of no translations = " + counter);
                if (counter > 0)
                    informAboutMissingTranslationsIfAny(counter);
                else
                    saveAndGoToStudyActivity();
            }
        }

        private void createQueue(Map<String, FlashcardFields> flashcardFieldsMap) {
            for (Map.Entry<String, FlashcardFields> entry : flashcardFieldsMap.entrySet()) {
                if (entry.getValue().isMisspelled()) {
                    List<String> suggestions = entry.getValue().getSuggestions();
                    if (suggestions.size() > 0)
                        dialogsQueue.add(new MyDialog(entry.getKey(), suggestions));
                    else
                        dialogsQueue.add(new MyDialog(entry.getKey()));
                }
            }
        }

        private int SINGLE_CHOICE_DIALOG = 0;
        private int EDIT_TEXT_DIALOG = 1;

        Queue<MyDialog> dialogsQueue = new LinkedList<>();

        int selectedPosition = 0;

        private void dialogBoxSingleChoice(final String wordToCheck, final String[] items) {
            final Dialog dialog;
            AlertDialog.Builder builder = new AlertDialog.Builder(WordsSelection.this);
            final EditText input = new EditText(WordsSelection.this);
            input.setInputType(InputType.TYPE_CLASS_TEXT);
            builder.setView(input);
            builder.setTitle("Word: \"" + wordToCheck + "\" could not be found." +
                    " Select correct word:");
            builder.setSingleChoiceItems(items, 0, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    Log.d(TAG, "onClick: Position");
                    selectedPosition = ((AlertDialog) dialog).getListView().getCheckedItemPosition();
                }
            })
                    .setPositiveButton("Done!", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            Log.d(TAG, "onClick: On Button");
                            flashcardData.removeEntry(wordToCheck);
                            boolean userEnteredWord = selectedPosition == items.length - 1;
                            String word = userEnteredWord ? input.getText().toString() : items[selectedPosition];
                            word = word.trim();
                            if (word.isEmpty()) {
                                Log.d(TAG, "onClick: Dismissed even though ok button clicked.");
                                Toast.makeText(WordsSelection.this, "word \"" +
                                        wordToCheck + "\" dismissed due to empty input.", Toast.LENGTH_SHORT).show();
                            } else {  // User selected word from the list.
                                if (!flashcardData.isAlreadyInMap(word))
                                    dictionaryCheckUp(word);
                            }

                            popAndDisplayDialog();
                        }
                    })
                    .setNegativeButton("Dismiss", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            Log.d(TAG, "onClick: Dismiss");
                            flashcardData.removeEntry(wordToCheck);
                            popAndDisplayDialog();
                        }
                    });

            selectedPosition = 0;
            dialog = builder.create();
            dialog.show();
        }

        private void dialogBoxEditText(final String wordToCheck) {
            AlertDialog.Builder builder = new AlertDialog.Builder(WordsSelection.this);
            builder.setTitle("No suggestions for \"" + wordToCheck + "\". Enter correct version please.");

            final EditText input = new EditText(WordsSelection.this);
            input.setInputType(InputType.TYPE_CLASS_TEXT);
            builder.setView(input);

            builder.setPositiveButton("OK", null);
            builder.setNegativeButton("Dismiss", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    flashcardData.removeEntry(wordToCheck);
                    dialog.cancel();
                    popAndDisplayDialog();
                }
            });

            final AlertDialog dialog = builder.create();
            dialog.show();
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String userProvidedWord = input.getText().toString();
                    if (input.getText().toString().trim().isEmpty()) {
                        Toast.makeText(WordsSelection.this, "The input field cannot be empty." +
                                " You can dismiss the word if you don't want it.", Toast.LENGTH_LONG).show();
                    } else {
                        flashcardData.removeEntry(wordToCheck);
                        if (!flashcardData.isAlreadyInMap(userProvidedWord))
                            dictionaryCheckUp(userProvidedWord);
                        dialog.cancel();
                        popAndDisplayDialog();
                    }
                }
            });
        }

        private void informAboutMissingTranslationsIfAny(final int numberOfLackingTranslations) {
            AlertDialog.Builder builder = new AlertDialog.Builder(WordsSelection.this);
            builder.setMessage("For " + numberOfLackingTranslations +
                    " words translations were not downloaded yet.")
                    .setCancelable(false)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            saveAndGoToStudyActivity();
                        }
                    });
            AlertDialog alert = builder.create();
            alert.show();
        }
    }

    private void informAboutMisspelledWords() {
        DialogManager dialogManager = new DialogManager();
        dialogManager.createQueue(flashcardData.getMisspelledEntries());
        dialogManager.popAndDisplayDialog();
    }


    private void saveAndGoToStudyActivity() {
        //todo Wait for other threads to finish, for dictionary to download translations and add them to map.
        // remember that dialogs again have to be repeat as word may not be found in dictionary.
        Log.d(TAG, "saveAndGoToStudyActivity: Going to Study!");
        Intent intent = new Intent(this, ManInTheMiddle.class);
        setResult(RESULT_OK, intent);
        Log.d(TAG, "saveAndGoToStudyActivity: number of new items: " + flashcardData.flashcardDataMap.size());
        finish();
    }

    private void OCR_init() {

        tessBaseApi = new TessBaseAPI();
        if (!tessBaseApi.init(AppConstant.TESSERACT_DATA_PATH, "eng")) {
            Log.d(TAG, "OCR_init: Could not initialise Tesseract-ocr.");

            // Check if tessdata file exists.
            File folder = new File(AppConstant.TESSERACT_DATA_PATH +
                    "tessdata");
            if (folder.isDirectory() && folder.exists())
                Log.d(TAG, "OCR_init: The tessdata file exists.");
            else
                Log.d(TAG, "OCR_init: The tessdata file does not exist.");
        } else
            Log.d(TAG, "OCR_init: Tesseract-OCR initialised successfully.");
    }

    private boolean checkExternalStoragePermission() {
        // Check permission.
        if (ContextCompat.checkSelfPermission(WordsSelection.this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted.
            if (ActivityCompat.shouldShowRequestPermissionRationale(WordsSelection.this,
                    Manifest.permission.READ_EXTERNAL_STORAGE)) {
                Toast.makeText(this, "The app needs external storage for OCR engine " +
                        "to work correctly.", Toast.LENGTH_LONG).show();
                // Display dialog with ok button saying why the app needs this permission.
                // Do this asynchronously.
            } else {
                // No explanation needed; request the permission
                ActivityCompat.requestPermissions(WordsSelection.this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        AppConstant.READ_EXTERNAL_STORAGE_REQUEST_CODE);
            }
            return false;
        } else {
            Log.d(TAG, "checkExternalStoragePermission: Granted.");
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case AppConstant.READ_EXTERNAL_STORAGE_REQUEST_CODE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay!
                    Toast.makeText(this, "Permission granted.", Toast.LENGTH_LONG).show();
                    initialiseOCRAndImageView();
                } else {
                    Toast.makeText(this, "No external storage permission!"
                            , Toast.LENGTH_LONG).show();
                    // permission denied, boo! Disable the functionality.
                }
            }
            break;
        }
    }

    private String extractText(Bitmap bitmap) {
        tessBaseApi.setImage(bitmap);
        return tessBaseApi.getUTF8Text();
    }

    private String retrieveDataFromPreviousActivity() {
        return getIntent().getStringExtra(IMAGE_PATH_EXTRAS);
    }

    private void setUpToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);

            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        } else
            Log.e(TAG, "setUpToolbar: getSupportActionBar equals NULL");
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.confirm: {
                informAboutMisspelledWords();
            }
            return true;
            case R.id.settings: {
                StringBuilder stringBuilder = new StringBuilder();
                for (Map.Entry<String, FlashcardFields> word : flashcardData.flashcardDataMap.entrySet()) {
                    stringBuilder.append(word.getKey()).append(" ");
                }
                Log.d(TAG, "onOptionsItemSelected: " + flashcardData.flashcardDataMap.size() +
                        " ... " + stringBuilder.toString());
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.general_menu, menu);
        menu.findItem(R.id.confirm).setVisible(true);
        menu.findItem(R.id.settings).setVisible(true);

        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        flashcardData.clear();
        onBackPressed();
        Intent intent = new Intent(this, Camera.class);
        setResult(RESULT_CANCELED, intent);
        finish();
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: Ending tessBaseApi.");
        tessBaseApi.end();
    }
}
