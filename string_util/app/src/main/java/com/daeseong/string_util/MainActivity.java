package com.daeseong.string_util;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private Button Button1, Button2, Button3;

    private HashMap<String, String> gameMap = new HashMap<>();

    private HashMap<String, gameinfo> gameinfoMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.e(TAG, "onCreate");

        //테스트용 데이타
        initData();

        Button1 = findViewById(R.id.Button1);
        Button2 = findViewById(R.id.Button2);
        Button3 = findViewById(R.id.Button3);

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

                List<String> splitFolder = String_util.getSplitFolder(sFileFullPath, "/");
                for (int i=0; i < splitFolder.size(); i++){
                    Log.e(TAG, "폴더 경로 분리: " + splitFolder.get(i));
                }

                String sReplaceFilename = sFileFullPath.replace("png", "jpg");
                Log.e(TAG, "파일명 변경: " + sReplaceFilename);

            }
        });

        Button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //전체 리스트 조회
                Iterator<String> keys = gameMap.keySet().iterator();
                while (keys.hasNext()){
                    String key = keys.next();
                    Log.e(TAG, "게임 패키지:" +  gameMap.get(key) );
                }


                //특정 데이타 존재 여부 확인
                if(gameMap.containsKey("com.kakaogames.moonlight")){
                    Log.e(TAG, "게임 패키지명 존재");
                }else {
                    Log.e(TAG, "게임 패키지명 미존재");
                }


                //특정 데이타 제거
                gameMap.remove("com.kakaogames.moonlight");


                //특정 데이타 존재 여부 확인
                if(gameMap.containsKey("com.kakaogames.moonlight")){
                    Log.e(TAG, "게임 패키지명 존재");
                }else {
                    Log.e(TAG, "게임 패키지명 미존재");
                }


                //전체 리스트 조회
                for( Iterator keys1 = gameMap.values().iterator(); keys1.hasNext(); ) {
                    Log.e(TAG, "게임 패키지:" +  keys1.next() );
                }

                //전체 데이타 초기화
                gameMap.clear();
            }
        });

        Button3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //전체 리스트 조회
                for (String sKey : gameinfoMap.keySet()) {
                    Log.e(TAG, gameMap.get(sKey)  + "- 시작 시간: "  + gameinfoMap.get(sKey).getStarttm() + " 종료 시간: " +  gameinfoMap.get(sKey).getEndtm() + " 패키지명: " +  gameinfoMap.get(sKey).getPackagename());
                }

                //데이타 수정
                if(gameMap.containsKey("net.gameply.android.moonlight")){
                    gameinfoMap.put("net.gameply.android.moonlight", new gameinfo("net.gameply.android.moonlight", gameinfoMap.get("net.gameply.android.moonlight").getStarttm(), getTimeDate()) );
                }

                if(gameMap.containsKey("com.kakaogames.moonlight")) {

                    gameinfoMap.put("com.kakaogames.moonlight", new gameinfo("com.kakaogames.moonlight", gameinfoMap.get("com.kakaogames.moonlight").getStarttm(), getTimeDate()) );
                }


                //전체 리스트 조회
                for (String sKey : gameinfoMap.keySet()) {
                    Log.e(TAG, gameMap.get(sKey)  + "- 시작 시간: "  + gameinfoMap.get(sKey).getStarttm() + " 종료 시간: " +  gameinfoMap.get(sKey).getEndtm() + " 패키지명: " +  gameinfoMap.get(sKey).getPackagename());
                }
            }
        });
    }

    private void initData(){

        gameMap.put("net.gameply.android.moonlight", "net.gameply.android.moonlight");
        gameMap.put("com.kakaogames.moonlight", "com.kakaogames.moonlight");
        gameMap.put("com.ncsoft.lineagem19", "com.ncsoft.lineagem19");
        gameMap.put("com.lilithgames.rok.gpkr", "com.lilithgames.rok.gpkr");
        gameMap.put("com.netmarble.lineageII", "com.netmarble.lineageII");
        gameMap.put("com.bluepotiongames.eosm", "com.bluepotiongames.eosm");
        gameMap.put("com.qjzj4399kr.google", "com.qjzj4399kr.google");
        gameMap.put("com.netmarble.bnsmkr", "com.netmarble.bnsmkr");
        gameMap.put("com.zlongame.kr.langrisser", "com.zlongame.kr.langrisser");
        gameMap.put("com.pearlabyss.blackdesertm", "com.pearlabyss.blackdesertm");

        gameinfoMap.put("net.gameply.android.moonlight", new gameinfo("net.gameply.android.moonlight", getTimeDate(), getTimeDate()) );
        gameinfoMap.put("com.kakaogames.moonlight", new gameinfo("com.kakaogames.moonlight", getTimeDate(), getTimeDate()) );
    }

    private static String getTimeDate() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
        return dateFormat.format(new Date());
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.e(TAG, "onStart");
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        Log.e(TAG, "onRestoreInstanceState");
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
