package com.chris.texteye.activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.TextView;
import android.widget.Toast;

import com.chris.texteye.adapters.MeaningsExpandList;
import com.chris.texteye.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class Meanings extends AppCompatActivity {

    private ExpandableListView listView;
    private MeaningsExpandList listAdapter;
    private List<String> listDataHeader;
    private HashMap<String, List<String>> listHash;
    private int lastExpandedTab = -1;

    private int selectedDeckId;
    private int selectedFlashcardId;
    private List<Boolean> sentencesTurned;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meanings);

        setupToolbar();

        retrieveDataFromIntent();

        setupUI();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
    }

    private void setupUI() {
        listView = findViewById(R.id.list_of_meanings);
        initData();
        listAdapter = new MeaningsExpandList(this, listDataHeader, listHash);
        listView.setAdapter(listAdapter);

        listView.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {
            @Override
            public void onGroupExpand(int groupPosition) {
                if(lastExpandedTab != -1 && lastExpandedTab != groupPosition)
                    listView.collapseGroup(lastExpandedTab);

                Log.d("Expand!", "Group has : " + Integer.toString(listAdapter.getChildrenCount(groupPosition)) + " children.");
                sentencesTurned = new ArrayList<>(Collections.nCopies(listAdapter.getChildrenCount(groupPosition), false));
                lastExpandedTab = groupPosition;
            }
        });

        listView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                Log.d("Child clicked!", "Group: " + Integer.toString(groupPosition) + ". child: " + Integer.toString(childPosition) +
                        ". id: " + Long.toString(id));
                Toast.makeText(Meanings.this, "Child", Toast.LENGTH_SHORT).show();
                sentencesTurned.set(childPosition, !sentencesTurned.get(childPosition));
                Log.d("Boolean List", sentencesTurned.toString());
                TextView tx = v.findViewById(R.id.lblListItem);
                tx.setText("This is it!!!!");
                return false;
            }
        });
    }

    private void retrieveDataFromIntent() {
        Intent intent = getIntent();
        final int LIST_NOT_GIVEN = -1;
        final int WORD_NOT_GIVEN = -1;
        selectedDeckId = intent.getIntExtra("list_id", LIST_NOT_GIVEN);
        selectedFlashcardId = intent.getIntExtra("word_id", WORD_NOT_GIVEN);
        if(selectedDeckId == LIST_NOT_GIVEN) {
            Toast.makeText(this, "List not provided!", Toast.LENGTH_LONG).show();
            Log.d("Bug", "Starting WordTranslations activity, list id was not provided.");
        }
        if(selectedFlashcardId == WORD_NOT_GIVEN) {
            Toast.makeText(this, "Word not provided!", Toast.LENGTH_LONG).show();
            Log.d("Bug", "Starting WordTranslations activity, word id was not provided.");
        }
    }

    private void initData() {
        listDataHeader = new ArrayList<>();
        listHash = new HashMap<>();

        listDataHeader.add("Prawda");
        listDataHeader.add("Prawosc");
        listDataHeader.add("Prawda (Podstawa czegoś)");
        listDataHeader.add("UWP");

        List<String> edmtDev = new ArrayList<>();
        edmtDev.add("Tell me the truth.");
        edmtDev.add("They'll never know the truth about what happened that day.");
        edmtDev.add("They'll never know the truth about what happened that day. They'll never know the truth about what happened that day.");

        List<String> androidStudio = new ArrayList<>();
        androidStudio.add("Expandable ListView");
        androidStudio.add("Google Map");
        androidStudio.add("Chat Application");
        androidStudio.add("Firebase ");

        List<String> xamarin = new ArrayList<>();
        xamarin.add("Xamarin Expandable ListView");
        xamarin.add("Xamarin Google Map");
        xamarin.add("Xamarin Chat Application");
        xamarin.add("Xamarin Firebase ");

        List<String> uwp = new ArrayList<>();
        uwp.add("UWP Expandable ListView");
        uwp.add("UWP Google Map");
        uwp.add("UWP Chat Application");
        uwp.add("UWP Firebase ");

        listHash.put(listDataHeader.get(0),edmtDev);
        listHash.put(listDataHeader.get(1),androidStudio);
        listHash.put(listDataHeader.get(2),xamarin);
        listHash.put(listDataHeader.get(3),uwp);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.general_menu, menu);
        menu.findItem(R.id.settings).setVisible(true);
        menu.findItem(R.id.play_audio).setVisible(true);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.settings:
                Toast.makeText(this, "This is settings!", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.camera:
                Toast.makeText(this, "Starting Camera for list id: " + Integer.toString(selectedDeckId), Toast.LENGTH_SHORT).show();
                return true;
            case R.id.play_audio:
                Toast.makeText(this, "I am playing audio for list id: " + Integer.toString(selectedDeckId) +
                        " and word id: " + Integer.toString(selectedFlashcardId), Toast.LENGTH_SHORT).show();
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