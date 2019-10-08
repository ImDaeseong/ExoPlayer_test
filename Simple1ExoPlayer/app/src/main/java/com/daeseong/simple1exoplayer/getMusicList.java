package com.daeseong.simple1exoplayer;

import android.database.Cursor;
import android.provider.MediaStore;
import android.util.Log;

import com.mpatric.mp3agic.ID3v2;
import com.mpatric.mp3agic.Mp3File;
import com.mpatric.mp3agic.ID3v1;
import com.mpatric.mp3agic.ID3v24Tag;

import java.util.ArrayList;

public class getMusicList {

    private static final String TAG = getMusicList.class.getSimpleName();

    private ArrayList<MusicInfo> musicList  = new ArrayList<MusicInfo>();

    private Mp3File mp3File;
    private  ID3v2 id3v2Tag;

    public ArrayList<MusicInfo> getData(){

        try {

            //음악 폴더 선택
            musicList.clear();
            Cursor cursor = MusicApplication.getInstance().getAppContext().getContentResolver().query(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    new String[]
                            {
                                    MediaStore.Audio.AudioColumns.ARTIST,
                                    MediaStore.Audio.AudioColumns.TITLE,
                                    MediaStore.Audio.AudioColumns.DATA
                            },
                    MediaStore.Audio.AudioColumns.IS_MUSIC + " > 0",
                    null,
                    null
            );

            while(cursor.moveToNext())
            {
                String sMusicPath = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.AudioColumns.DATA));

                //music3 폴더만 가져온다
                if(!String_util.getLastFolderName(sMusicPath).equals("music3")) continue;

                try {

                    String sTitle = "";
                    String sArtist = "";
                    String sSing = "";

                    mp3File = new Mp3File(sMusicPath);
                    if (mp3File != null){

                        if (mp3File.hasId3v2Tag()) {
                            id3v2Tag = mp3File.getId3v2Tag();
                            sTitle = id3v2Tag.getTitle();
                            sArtist = id3v2Tag.getArtist();;

                            //id3v2Tag.getTrack();
                            //id3v2Tag.getAlbum();
                            //id3v2Tag.getYear();
                            //id3v2Tag.getGenre();
                            //id3v2Tag.getComment();

                        }else {
                            id3v2Tag = new ID3v24Tag();
                            if (mp3File.hasId3v1Tag()) {
                                ID3v1 id3v1Tag = mp3File.getId3v1Tag();
                                sTitle = id3v1Tag.getTitle();
                                sArtist = id3v1Tag.getArtist();

                                //id3v1Tag.getTrack();
                                //id3v1Tag.getAlbum();
                                //id3v1Tag.getYear();
                                //id3v1Tag.getGenre();
                                //id3v1Tag.getComment();
                            }
                        }

                        /*
                        int secs = (int)mp3File.getLengthInSeconds() % 60;
                        int mins = (int)mp3File.getLengthInSeconds() / 60;
                        String sPlaytime = String.format("%02d:%02d", mins, secs);
                        Log.e(TAG, "Playtime: " + sPlaytime);
                        */

                        if(sTitle == null && sArtist == null){
                            sSing = String.format("%s", String_util.getFileName(sMusicPath));
                        }else {
                            sSing = String.format("%s - %s", sTitle, sArtist);
                        }
                        //Log.e(TAG, "sSing: " + sSing);

                        MusicInfo info = new MusicInfo();
                        info.setMusicPath(sMusicPath);
                        info.setMusicName(sSing);
                        musicList.add(info);

                    }
                }  catch (Exception ex){
                    Log.e(TAG, ex.getMessage().toString());
                }
            }
        }catch (Exception ex){
            Log.e(TAG, ex.getMessage().toString());
        }
        return musicList;
    }

}
