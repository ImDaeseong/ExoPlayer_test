package com.daeseong.simple1exoplayer

import android.text.TextUtils





class String_util  {

    //파일 확장자
    fun getFileExt(url: String): String? {
        var sResult: String? = ""
        val nIndex = url.lastIndexOf(".")
        if (nIndex >= 0) {
            sResult = url.substring(nIndex + 1)
        }
        return sResult
    }

    //파일 이름앞 폴더명
    fun getLastFolderName(url: String): String? {
        var sResult = ""
        if (TextUtils.isEmpty(url)) return sResult
        var nIndex = url.lastIndexOf("/")
        if (nIndex >= 0) {
            val sTemp = url.substring(0, nIndex)
            nIndex = sTemp.lastIndexOf("/")
            if (nIndex >= 0) {
                sResult = sTemp.substring(nIndex + 1)
            }
        }
        return sResult
    }

    //파일 이름
    fun getFileName(url: String): String? {
        var sResult: String? = ""
        val nIndex = url.lastIndexOf("/")
        if (nIndex >= 0) {
            sResult = url.substring(nIndex + 1)
        }
        return sResult
    }
}