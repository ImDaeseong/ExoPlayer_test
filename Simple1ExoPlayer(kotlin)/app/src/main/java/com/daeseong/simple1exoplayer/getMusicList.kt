package com.daeseong.simple1exoplayer

import android.database.Cursor
import android.provider.MediaStore
import android.util.Log
import com.mpatric.mp3agic.ID3v2
import com.mpatric.mp3agic.ID3v24Tag
import com.mpatric.mp3agic.Mp3File


class getMusicList {

    private val tag = getMusicList::class.java.simpleName

    val musicList = ArrayList<MusicInfo>()

    private var mp3File: Mp3File? = null
    private var id3v2Tag: ID3v2? = null

    fun getData(): Boolean {

        try {

            //음악 폴더 선택
            musicList.clear()
            val cursor: Cursor? =  MusicApplication.getContext().contentResolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, arrayOf(
                    MediaStore.Audio.AudioColumns.ARTIST,
                    MediaStore.Audio.AudioColumns.TITLE,
                    MediaStore.Audio.AudioColumns.DATA
                ), MediaStore.Audio.AudioColumns.IS_MUSIC + " > 0", null, null
            )

            while (cursor!!.moveToNext()) {

                val sMusicPath = cursor!!.getString(cursor!!.getColumnIndex(MediaStore.Audio.AudioColumns.DATA))

                //music3 폴더만 가져온다
                //if (String_util().getLastFolderName(sMusicPath) != "music3") continue

                try {

                    var sTitle: String? = ""
                    var sArtist: String? = ""
                    var sSing = ""
                    mp3File = Mp3File(sMusicPath)

                    if (mp3File != null) {

                        if (mp3File!!.hasId3v2Tag()) {

                            val id3v2Tag = mp3File!!.id3v2Tag
                            sTitle = id3v2Tag.title
                            sArtist = id3v2Tag.artist
                            //id3v2Tag.track
                            //id3v2Tag.album
                            //id3v2Tag.year
                            //id3v2Tag.genre
                            //id3v2Tag.comment

                        } else {

                            id3v2Tag = ID3v24Tag()

                            if (mp3File!!.hasId3v1Tag()) {

                                val id3v1Tag = mp3File!!.id3v1Tag
                                sTitle = id3v1Tag.title
                                sArtist = id3v1Tag.artist
                                //id3v1Tag.track
                                //id3v1Tag.album
                                //id3v1Tag.year
                                //id3v1Tag.genre
                                //id3v1Tag.comment
                            }
                        }

                        /*
                        val secs = mp3File!!.lengthInSeconds.toInt() % 60
                        val mins = mp3File!!.lengthInSeconds.toInt() / 60
                        val sPlaytime = String.format("%02d:%02d", mins, secs)
                        Log.e(tag, "Playtime: $sPlaytime")
                        */

                        sSing = if (sTitle == null && sArtist == null) {
                            String.format("%s", String_util().getFileName(sMusicPath))
                        } else {
                            String.format("%s - %s", sTitle, sArtist)
                        }
                        //Log.e(tag, "sSing: $sSing")

                        val info = MusicInfo()
                        info.musicPath = sMusicPath
                        info.musicName = sSing
                        musicList.add(info)
                    }
                } catch (ex: Exception) {
                    Log.e(tag, ex.message.toString())
                }
            }

        }catch (ex: java.lang.Exception) {
            Log.e(tag, ex.message.toString())
            return false
        }

        return true
    }
}

