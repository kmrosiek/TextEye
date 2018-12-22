package com.chris.texteye.activities;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.chris.texteye.AppConstant;
import com.chris.texteye.InternetBroadcastReceiver;
import com.chris.texteye.R;
import com.chris.texteye.TextEye;
import com.chris.texteye.Utils;
import com.chris.texteye.adapters.MainMenuRecycViewAdapter;
import com.chris.texteye.database.Entities.Deck;
import com.chris.texteye.modelViews.VMMainMenu;

import java.util.List;

public class MainMenu extends AppCompatActivity {
    private static final String TAG = "MainMenutyDD";

    public static final int ADD_DECK_REQUEST = 1;
    public static final int CHANGE_GENERAL_SETTINGS_REQUEST = 2;
    private VMMainMenu decksVMMainMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        setUpToolbar();

        setupRecyclerViewAndViewModel();

        setConnectivityStatus();

        TextEye.getContext();

        //todo Check if settings are saved. If not, then load initial settings.
        // PreferenceManager.setDefaultValues(this, R.xml.preference, false);
        // https://stackoverflow.com/questions/2691772/android-preferences-how-to-load-the-default-values-when-the-user-hasnt-used-th
    }

    private void setConnectivityStatus() {
        InternetBroadcastReceiver.CONNECTED_TO_INTERNET =  Utils.isNetworkAvailable(this);
    }

    private void setupRecyclerViewAndViewModel() {
        RecyclerView recyclerView = findViewById(R.id.main_menu_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);

        final MainMenuRecycViewAdapter adapter = new MainMenuRecycViewAdapter();
        recyclerView.setAdapter(adapter);

        decksVMMainMenu = ViewModelProviders.of(this).get(VMMainMenu.class);
        decksVMMainMenu.getAllDecks().observe(this, new Observer<List<Deck>>() {
            @Override
            public void onChanged(@Nullable List<Deck> deck) {
                adapter.setDecks(deck);
            }
        });

        adapter.setOnDeckClickListener(new MainMenuRecycViewAdapter.OnDeckClickListener() {
            @Override
            public void onDeckClick(Deck deck) {
                switchToStudyActivity(deck.getId());
            }
        });
    }

    private void setUpToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if(getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        } else
            Log.e(TAG, "setUpToolbar: getSupportActionBar equals NULL");
    }

    private void switchToStudyActivity(final int deckId){
        Intent intent = new Intent(this, Study.class);
        intent.putExtra("list_id", deckId);
        startActivity(intent);
    }

    /** Each element of toolbar menu that will be used in this activity has to be made visible*/
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.general_menu, menu);
        menu.findItem(R.id.settings).setVisible(true);
        menu.findItem(R.id.add).setVisible(true);
        return true;
    }

    /** Options on the toolbar menu. Event handler*/
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId())
        {
            case R.id.settings: {
                Intent intent = new Intent(this, GlobalSettings.class);
                startActivityForResult(intent, CHANGE_GENERAL_SETTINGS_REQUEST);
            }
                return true;
            case R.id.add: {
                Intent intent = new Intent(this, AddDeck.class);
                startActivityForResult(intent, ADD_DECK_REQUEST);
            }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch(requestCode)
        {
            case ADD_DECK_REQUEST:
                if(resultCode == RESULT_OK) {
                    String deckName = data.getStringExtra(AddDeck.EXTRA_DECK_NAME);

                    Deck deck = new Deck(deckName);
                    decksVMMainMenu.insert(deck);
                    Toast.makeText(this, "New deck added.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "A deck was not added.", Toast.LENGTH_SHORT).show();
                }
                break;
            case CHANGE_GENERAL_SETTINGS_REQUEST:
                if(resultCode == RESULT_OK) {
                    Toast.makeText(this, "Settings were saved.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Settings were not saved.", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    /** Required for back button on the toolbar to work correctly.*/
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)  {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case AppConstant.CAMERA_PERMISSION_REQUEST_CODE: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Permission granted.", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(this, "The camera cannot be started, due to lack" +
                            " of permission for camera to run.", Toast.LENGTH_LONG).show();
                }
            }
        }
    }
}
