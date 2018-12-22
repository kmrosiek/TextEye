package com.chris.texteye.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Switch;

import com.chris.texteye.R;

public class GlobalSettings extends AppCompatActivity {

    private static final String TAG = "GlobalSettingstyDD";
    private final String GLOBAL_APP_PREFS = "GLOBAL_APP_PREFS";
    private final String AUTOPLAY_PREF = "autoplay";
    private final String LEVELS_4_PREF = "4_levels";
    private Switch autoplay;
    private Switch levels_4;
    private boolean autoplay_value;
    private boolean levels_4_value;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_global_settings);

        setupToolbar();

        autoplay = findViewById(R.id.autoplay);
        levels_4 = findViewById(R.id.levels);

        SharedPreferences settings = getSharedPreferences(GLOBAL_APP_PREFS, 0);
        autoplay_value = settings.getBoolean(AUTOPLAY_PREF, true);
        levels_4_value = settings.getBoolean(LEVELS_4_PREF, false);

        autoplay.setChecked(autoplay_value);
        levels_4.setChecked(levels_4_value);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if(getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        } else
            Log.e(TAG, "onCreate: getSupportActionBar equals NULL");
    }

    // Allows to display items on toolbar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.general_menu, menu);
        menu.findItem(R.id.confirm).setVisible(true);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.confirm:
                SharedPreferences settings = getSharedPreferences(GLOBAL_APP_PREFS, 0);
                SharedPreferences.Editor editor = settings.edit();
                editor.putBoolean(AUTOPLAY_PREF, autoplay.isChecked());
                editor.putBoolean(LEVELS_4_PREF, levels_4.isChecked());
                editor.apply();

                Intent intent = new Intent();
                setResult(RESULT_OK, intent);
                finish();
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
