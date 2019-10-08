package com.daeseong.string_util;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.PersistableBundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private Button Button1, Button2, Button3, Button4, Button5, Button6;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.e(TAG, "onCreate");

        Button1 = findViewById(R.id.Button1);
        Button2 = findViewById(R.id.Button2);
        Button3 = findViewById(R.id.Button3);
        Button4 = findViewById(R.id.Button4);
        Button5 = findViewById(R.id.Button5);
        Button6 = findViewById(R.id.Button6);

        Button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String sFileFullPath = "/storage/emulated/0/DCIM/Camera/test.png";

                String sOnlyFileName = String_util.getOnlyFileName(sFileFullPath);
                Log.e(TAG, "파일이름만: " + sOnlyFileName);

                String sFileName = String_util.getFileName(sFileFullPath);
                Log.e(TAG, "파일이름: " + sFileName);

                String sFileExt = String_util.getFileExt(sFileFullPath);
                Log.e(TAG, "파일확장자: " + sFileExt);

                String sFilePath = String_util.getFilePath(sFileFullPath);
                Log.e(TAG, "파일경로: " + sFilePath);

                String sFilePathSlash = String_util.getFilePathSlash(sFileFullPath);
                Log.e(TAG, "파일경로('/'): " + sFilePathSlash);

                String sFolderName = String_util.getLastFolderName(sFileFullPath);
                Log.e(TAG, "파일 이름앞 폴더명: " + sFolderName);
            }
        });

        Button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        Button3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        Button4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        Button5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        Button6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        Log.e(TAG, "onStart");
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        Log.i(TAG, "onRestoreInstanceState");
    }

    @Override
    protected void onResume() {
        super.onResume();

        Log.e(TAG, "onResume");
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState, @NonNull PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);

        Log.e(TAG, "onSaveInstanceState");
    }

    @Override
    protected void onPause() {
        super.onPause();

        Log.e(TAG, "onPause");
    }

    @Override
    protected void onStop() {
        super.onStop();

        Log.e(TAG, "onStop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        Log.e(TAG, "onDestroy");
    }

    @Override
    protected void onRestart() {
        super.onRestart();

        Log.e(TAG, "onRestart");
    }
}
