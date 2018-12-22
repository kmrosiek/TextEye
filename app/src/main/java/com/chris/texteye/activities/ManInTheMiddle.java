package com.chris.texteye.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.chris.texteye.R;

import java.io.File;

public class ManInTheMiddle extends AppCompatActivity {
    private static final String TAG = "ManInTheMiddletyDD";

    private static final int BUILT_IN_CAMERA_REQUEST = 1;
    private static final int APP_CAMERA_REQUEST = 199;
    private static final int CAMERA_BUILT_IN_SELECTION_REQUEST = 222;
    private static final int MANDELA_PICTURE_SELECTION_REQUEST = 444;
    private File imageFromBuiltInCamera;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_man_in_the_middle);

        prepareButtons();
    }


    private void prepareButtons() {
        Button builtInCameraButton = findViewById(R.id.built_in_camera_button);
        Button appCameraButton = findViewById(R.id.in_app_camera_button);
        Button imgFromStorageButton = findViewById(R.id.image_from_storage_button);

        builtInCameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                prepareFileForResult();
                Log.d("onActivityResult", "Starting built-in camera...");
                Intent camera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                camera.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(imageFromBuiltInCamera));
                camera.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
                startActivityForResult(camera, BUILT_IN_CAMERA_REQUEST);
            }
        });

        appCameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ManInTheMiddle.this, Camera.class);
                startActivityForResult(intent, APP_CAMERA_REQUEST);
            }
        });

        imgFromStorageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ManInTheMiddle.this, WordsSelection.class);
                intent.putExtra(WordsSelection.IMAGE_PATH_EXTRAS, "/storage/emulated/0/TextEyePictures/mandela.jpg");
                startActivityForResult(intent, MANDELA_PICTURE_SELECTION_REQUEST);
            }
        });
    }

    private void prepareFileForResult() {
        final String imageFolder = "/storage/emulated/0/TextEyePictures";
        File folder = new File(imageFolder);
        if(!folder.exists())
        {
            folder.mkdir();
        }

        File imageFile = new File(folder, "built_in_camera_picture.jpg");
        imageFromBuiltInCamera = imageFile;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d("onActivityResult", "Result code: " + Integer.toString(resultCode) + "  RequestCode" + Integer.toString(requestCode) );
        
        switch(requestCode) {
            case BUILT_IN_CAMERA_REQUEST:
                if (resultCode == RESULT_OK) {
                    if(imageFromBuiltInCamera == null)
                        prepareFileForResult();
                    Toast.makeText(this, "Image saved: " + imageFromBuiltInCamera.getAbsolutePath(), Toast.LENGTH_LONG).show();
                    Log.d("onActivityResult", "Image saved: " + imageFromBuiltInCamera.getAbsolutePath());
                    Intent intent = new Intent(this, WordsSelection.class);
                    intent.putExtra(WordsSelection.IMAGE_PATH_EXTRAS, imageFromBuiltInCamera.getAbsolutePath());
                    startActivityForResult(intent, CAMERA_BUILT_IN_SELECTION_REQUEST);
                } else {
                    Log.d(TAG, "onActivityResult: Built-in camera capture cancelled.");
                    Toast.makeText(this, "Cancelled.", Toast.LENGTH_SHORT).show();
                }
                break;
            case CAMERA_BUILT_IN_SELECTION_REQUEST:
                if (resultCode == RESULT_OK) {
                    Intent intent = new Intent(this, Study.class);
                    setResult(RESULT_OK, intent);
                    finish();
                } else {
                    Log.d(TAG, "onActivityResult: selecting words cancelled.");
                    Toast.makeText(this, "Cancelled.", Toast.LENGTH_SHORT).show();
                }

                break;
            case MANDELA_PICTURE_SELECTION_REQUEST:
                if (resultCode == RESULT_OK) {
                    Intent intent = new Intent(this, Study.class);
                    setResult(RESULT_OK, intent);
                    finish();
                } else {
                        Log.d(TAG, "onActivityResult: selecting words cancelled.");
                        Toast.makeText(this, "Cancelled.", Toast.LENGTH_SHORT).show();
                    }
                break;
            case APP_CAMERA_REQUEST :
                if(resultCode == RESULT_OK) {
                    Intent intent = new Intent(this, Study.class);
                    setResult(RESULT_OK, intent);
                    finish();
                } else {
                    Log.d(TAG, "onActivityResult: selecting words cancelled.");
                    Toast.makeText(this, "Cancelled.", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }
}
