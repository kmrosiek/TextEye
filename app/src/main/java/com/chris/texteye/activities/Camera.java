package com.chris.texteye.activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.chris.texteye.AppConstant;
import com.chris.texteye.CameraFragment;
import com.chris.texteye.R;

public class Camera extends AppCompatActivity {

    private static final String TAG = "CameratyDD";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        if (null == savedInstanceState) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, CameraFragment.newInstance())
                    .commit();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult: requestCode: " + requestCode + " resultCode : " +
                resultCode);
        switch (requestCode) {
            case AppConstant.SELECTING_WORDS_REQUEST:
                if(resultCode == RESULT_OK) {

                    Intent intent = new Intent(this, ManInTheMiddle.class);
                    setResult(RESULT_OK, intent);
                    finish();
                } else {
                    Intent intent = new Intent(this, ManInTheMiddle.class);
                    setResult(RESULT_CANCELED, intent);
                    finish();
                }
                break;
        }
    }
}
