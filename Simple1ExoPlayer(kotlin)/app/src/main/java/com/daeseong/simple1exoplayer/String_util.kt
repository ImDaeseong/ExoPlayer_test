package com.daeseong.simple1exoplayer

object String_util  {

    //파일 확장자
    fun getFileExt(url: String): String {
        val index = url.lastIndexOf(".")
        return if (index >= 0) url.substring(index + 1) else ""
    }

    //파일 이름앞 폴더명
    fun getLastFolderName(url: String): String {
        if (url.isEmpty()) return ""

        val lastSlash = url.lastIndexOf("/")
        if (lastSlash >= 0) {
            val secondLastSlash = url.lastIndexOf("/", lastSlash - 1)
            return if (secondLastSlash >= 0) url.substring(secondLastSlash + 1, lastSlash) else ""
        }
        return ""
    }

    //파일 이름
    fun getFileName(url: String): String {
        val lastSlash = url.lastIndexOf("/")
        return if (lastSlash >= 0) url.substring(lastSlash + 1) else ""
    }
}