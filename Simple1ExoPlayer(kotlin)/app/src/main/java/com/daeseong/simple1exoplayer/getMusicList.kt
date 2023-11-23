package com.daeseong.simple1exoplayer

import android.database.Cursor
import android.provider.MediaStore
import android.util.Log
import com.mpatric.mp3agic.ID3v1
import com.mpatric.mp3agic.ID3v2
import com.mpatric.mp3agic.ID3v24Tag
import com.mpatric.mp3agic.Mp3File

class getMusicList {

    private val tag = getMusicList::class.java.simpleName

    private val musicList = ArrayList<MusicInfo>()

    private var mp3File: Mp3File? = null
    private var id3v2Tag: ID3v2? = null

    fun getData(): ArrayList<MusicInfo> {

        try {

            //음악 폴더 선택
            musicList.clear()
            val cursor: Cursor? = MusicApplication.getAppContext().contentResolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                arrayOf(MediaStore.Audio.AudioColumns.ARTIST, MediaStore.Audio.AudioColumns.TITLE, MediaStore.Audio.AudioColumns.DATA),
                "${MediaStore.Audio.AudioColumns.IS_MUSIC} > 0",
                null,
                null
            )

            val nIndex: Int = cursor?.getColumnIndex(MediaStore.Audio.AudioColumns.DATA) ?: -1

            cursor?.use {

                while (it.moveToNext()) {

                    if (nIndex == -1) {
                        continue
                    }

                    val sMusicPath: String = it.getString(nIndex)

                    //music3 폴더만 가져온다
                    if (String_util.getLastFolderName(sMusicPath) != "music2") {
                        continue
                    }

                    try {
                        var sTitle = ""
                        var sArtist = ""
                        var sSing = ""

                        mp3File = Mp3File(sMusicPath)
                        if (mp3File != null) {

                            if (mp3File?.hasId3v2Tag() == true) {
                                id3v2Tag = mp3File?.id3v2Tag
                                sTitle = id3v2Tag?.title ?: ""
                                sArtist = id3v2Tag?.artist ?: ""

                                Log.e(tag, "${id3v2Tag?.track}${id3v2Tag?.album}${id3v2Tag?.year}${id3v2Tag?.genre}${id3v2Tag?.comment}")

                            } else {
                                id3v2Tag = ID3v24Tag()
                                if (mp3File?.hasId3v1Tag() == true) {
                                    val id3v1Tag: ID3v1? = mp3File?.id3v1Tag
                                    sTitle = id3v1Tag?.title ?: ""
                                    sArtist = id3v1Tag?.artist ?: ""

                                    Log.e(tag, "${id3v1Tag?.track}${id3v1Tag?.album}${id3v1Tag?.year}${id3v1Tag?.genre}${id3v1Tag?.comment}")
                                }
                            }

                            sSing = if (sTitle == "" && sArtist == "") {
                                String.format("%s", String_util.getFileName(sMusicPath))
                            } else {
                                String.format("%s - %s", sTitle, sArtist)
                            }

                            val info = MusicInfo()
                            info.musicPath = sMusicPath
                            info.musicName = sSing
                            musicList.add(info)
                        }

                    } catch (ex: Exception) {
                        Log.e(tag, ex.message.toString())
                    }
                }
            }

        } catch (ex: Exception) {
            Log.e(tag, ex.message.toString())
        }

        return musicList
    }
}
