package com.chris.texteye;

import android.os.Environment;

import java.io.File;

public class AppConstant {

    public static final int CAMERA_PERMISSION_REQUEST_CODE = 200;
    public static final int READ_EXTERNAL_STORAGE_REQUEST_CODE = 201;
    public static final String TESSERACT_DATA_PATH = Environment.getExternalStorageDirectory()
            + File.separator;
    public static final int SELECTING_WORDS_REQUEST = 100;
}
