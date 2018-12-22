package com.chris.texteye.activities;

import android.Manifest;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.chris.texteye.AppConstant;
import com.chris.texteye.FlashcardData;
import com.chris.texteye.FlashcardFields;
import com.chris.texteye.R;
import com.chris.texteye.TextEye;
import com.chris.texteye.adapters.StudySwipeCardsAdapter;
import com.chris.texteye.database.Entities.Flashcard;
import com.chris.texteye.modelViews.VMStudy;
import com.lorentzos.flingswipe.SwipeFlingAdapterView;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class Study extends AppCompatActivity {

    private int selectedListIndex;

    private static final String TAG = "StudtyDD";
    private SwipeFlingAdapterView swipeCardsContainer;

    private Flashcard flashcard;
    private boolean frontIsShown = true;
    private VMStudy vmStudy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_study);

        setUpToolbar();

        retrieveDataFromPreviousIntent();

        swipeCardsContainer = findViewById(R.id.frame);

        setUpAdapterAndViewModel();

    }

    private void setUpAdapterAndViewModel() {
        final StudySwipeCardsAdapter swipeCardsAdapter = new StudySwipeCardsAdapter(Study.this);
        swipeCardsContainer.setAdapter(swipeCardsAdapter);
        vmStudy = ViewModelProviders.of(this).get(VMStudy.class);
        vmStudy.setFilter(selectedListIndex);

        // Layouts imitate flashcards stack. They will be hidden when there're 2 or 1 cards in the stack.
        final LinearLayout farther_card_imitator = findViewById(R.id.farthest_card_imitator);
        final LinearLayout middle_card_imitator = findViewById(R.id.middle_card_imitator);

        vmStudy.getFlashcardsForSelectedList().observe(this, new Observer<List<Flashcard>>() {
            @Override
            public void onChanged(@Nullable List<Flashcard> flashcards) {
                swipeCardsAdapter.setFlashcards(flashcards);
                Log.d(TAG, "onChanged: " + swipeCardsAdapter.getCount());
                flashcard = swipeCardsAdapter.getCurrentFlashcard();
                if (swipeCardsAdapter.getCount() < 3)
                    farther_card_imitator.setVisibility(View.GONE);
                else
                    farther_card_imitator.setVisibility(View.VISIBLE);
                if (swipeCardsAdapter.getCount() < 2)
                    middle_card_imitator.setVisibility(View.GONE);
                else
                    middle_card_imitator.setVisibility(View.VISIBLE);
            }
        });


        swipeCardsContainer.setFlingListener(new SwipeFlingAdapterView.onFlingListener() {
            // Occurs when card id swipe on left or right,
            // but before onLeftCardExit or onRightCardExit events
            @Override
            public void removeFirstObjectInAdapter() {
                if (swipeCardsAdapter.getCount() <= 3)
                    farther_card_imitator.setVisibility(View.GONE);
                if (swipeCardsAdapter.getCount() <= 2)
                    middle_card_imitator.setVisibility(View.GONE);
                frontIsShown = true;
                Log.d(TAG, "removeFirstObjectInAdapter: ");
            }

            @Override
            public void onLeftCardExit(Object dataObject) {
                swipeCardsAdapter.pop_front();
                swipeCardsAdapter.notifyDataSetChanged();
            }

            @Override
            public void onRightCardExit(Object dataObject) {
                swipeCardsAdapter.pop_front();
                swipeCardsAdapter.notifyDataSetChanged();

            }

            @Override
            public void onAdapterAboutToEmpty(int itemsInAdapter) {
                flashcard = swipeCardsAdapter.getCurrentFlashcard();
            }

            // Event occurs when cards are dragged - moved.
            @Override
            public void onScroll(float scrollProgressPercent) {

                View view = swipeCardsContainer.getSelectedView();
                if (null != view) {
                    view.findViewById(R.id.background).setAlpha(0);
                    view.findViewById(R.id.item_swipe_right_indicator).setAlpha(scrollProgressPercent < 0 ? -scrollProgressPercent : 0);
                    view.findViewById(R.id.item_swipe_left_indicator).setAlpha(scrollProgressPercent > 0 ? scrollProgressPercent : 0);
                }
            }
        });

        // Event when flashcard is clicked.
        swipeCardsContainer.setOnItemClickListener(new SwipeFlingAdapterView.OnItemClickListener() {
            @Override
            public void onItemClicked(int itemPosition, Object dataObject) {

                View view = swipeCardsContainer.getSelectedView();
                if (null != view) {
                    view.findViewById(R.id.background).setAlpha(0);
                    if (null != flashcard) {
                        TextView sentence = view.findViewById(R.id.flashcard_sentence);
                        TextView text = view.findViewById(R.id.flashcard_text);
                        if (sentence == null || text == null) {
                            Log.e(TAG, "onItemClicked: TextView is null");
                            return;
                        }
                        if (frontIsShown) {
                            text.setText(flashcard.getPolText());
                            sentence.setText(flashcard.getPolSentence());
                            Log.d(TAG, "onItemClicked: " + flashcard.getPolText() + "  " + flashcard.getPolSentence());
                        } else {
                            text.setText(flashcard.getEngText());
                            sentence.setText(flashcard.getEngSentence());
                        }
                        text.requestLayout();
                        sentence.requestLayout();
                        frontIsShown = !frontIsShown;
                    }
                } else
                    Log.e(TAG, "onItemClicked: view equals null.");
                Log.d(TAG, "onItemClicked: ");
            }
        });
    }

    private void setUpToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        } else
            Log.e(TAG, "setUpToolbar: getSupportActionBar equals NULL.");
    }


    private void retrieveDataFromPreviousIntent() {
        Intent intent = getIntent();
        final int LIST_NOT_GIVEN = -1;
        selectedListIndex = intent.getIntExtra("list_id", LIST_NOT_GIVEN);
        if (selectedListIndex == LIST_NOT_GIVEN) {
            Toast.makeText(this, "List not provided!", Toast.LENGTH_LONG).show();
            Log.d("Bug", "Starting study activity, list id was not provided.");
        } else {
            Log.d(TAG, "Starting study activity, list id: " + Integer.toString((selectedListIndex)));
        }
    }

    private void goToCameraActivity() {
        Intent intent = new Intent(this, ManInTheMiddle.class);
        startActivityForResult(intent, AppConstant.SELECTING_WORDS_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult: requestCode: " + requestCode + " resultCode : " +
                resultCode);
        switch (requestCode) {
            case AppConstant.SELECTING_WORDS_REQUEST: {
                if (resultCode == RESULT_OK) {
                    FlashcardData flashcardData = FlashcardData.getInstance();
                    Log.d(TAG, "onActivityResult: map size: " + flashcardData.flashcardDataMap.size());

                    for (Map.Entry<String, FlashcardFields> entry : flashcardData.flashcardDataMap.entrySet()) {
                        Flashcard flashcard = FlashcardFields.createFlashcardFromFF(entry.getKey(),
                                selectedListIndex, entry.getValue());

                        vmStudy.insert(flashcard);
                    }

                    vmStudy.fetchCardsByListId(selectedListIndex);
                    flashcardData.clear();
                }
            }
            break;
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case AppConstant.CAMERA_PERMISSION_REQUEST_CODE: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Permission granted.", Toast.LENGTH_LONG).show();
                    goToCameraActivity();
                } else {
                    Toast.makeText(this, "The camera cannot be started, due to lack" +
                            " of permission for camera to run.", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.general_menu, menu);
        menu.findItem(R.id.settings).setVisible(true);
        menu.findItem(R.id.camera).setVisible(true);
        menu.findItem(R.id.translation).setVisible(true);
        menu.findItem(R.id.play_audio).setVisible(true);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.settings: {
                Log.d("Item select", "Settings");
                Intent intent = new Intent(this, DeckSettings.class);
                intent.putExtra("list_id", selectedListIndex);
                startActivity(intent);
            }
            return true;
            case R.id.camera:
                // Check permission for using a camera.
                if (ContextCompat.checkSelfPermission(Study.this, Manifest.permission.CAMERA)
                        != PackageManager.PERMISSION_GRANTED) {
                    // Permission is not granted.
                    if (ActivityCompat.shouldShowRequestPermissionRationale(Study.this,
                            Manifest.permission.CAMERA)) {
                        Toast.makeText(this, "You have to allow the app to use " +
                                "camera in order to take pictures.", Toast.LENGTH_SHORT).show();
                        //todo display asynchronously message here.
                        // Display dialog with ok button saying why the app needs this permission.
                        // Do this asynchronously.
                        ActivityCompat.requestPermissions(Study.this,
                                new String[]{Manifest.permission.CAMERA},
                                AppConstant.CAMERA_PERMISSION_REQUEST_CODE);
                    } else {
                        // No explanation needed; request the permission
                        ActivityCompat.requestPermissions(Study.this,
                                new String[]{Manifest.permission.CAMERA},
                                AppConstant.CAMERA_PERMISSION_REQUEST_CODE);
                    }
                } else {
                    // Open camera intent.
                    goToCameraActivity();
                }
                return true;
            case R.id.translation:
                Intent intent = new Intent(this, Meanings.class);
                intent.putExtra("list_id", selectedListIndex);
                //intent.putExtra("word_id", word_studied_now_index);
                startActivity(intent);
                return true;
            case R.id.play_audio:
                if(flashcard == null)
                    return true;
                try {
                    final String filePath = TextEye.getContext().getExternalFilesDir(null) + File.separator +
                            "Audios" + File.separator + flashcard.getEngText() + ".mp3";
                    File file = new File(filePath);
                    if(file.exists()) {
                        MediaPlayer mPlayer = new MediaPlayer();
                        Uri myUri = Uri.parse(filePath);
                        mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                        mPlayer.setDataSource(TextEye.getContext(), myUri);
                        mPlayer.prepare();
                        mPlayer.start();
                    } else {
                        Log.d(TAG, "onOptionsItemSelected: Audio file does not exist.");
                        Toast.makeText(this, "No audio for this word.", Toast.LENGTH_SHORT).show();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.d(TAG, "onOptionsItemSelected: Error playing audio: " + e.toString());
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
