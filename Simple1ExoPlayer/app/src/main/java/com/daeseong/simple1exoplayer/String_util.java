package com.daeseong.simple1exoplayer;

import android.text.TextUtils;

public class String_util {

    private String_util() {
    }

    //파일 확장자
    public static String getFileExt(String url){
        int index = url.lastIndexOf(".");
        return index >= 0 ? url.substring(index + 1) : "";
    }

    //파일 이름앞 폴더명
    public static String getLastFolderName(String url){
        if (TextUtils.isEmpty(url)) return "";

        int lastSlash = url.lastIndexOf("/");
        if (lastSlash >= 0) {
            int secondLastSlash = url.lastIndexOf("/", lastSlash - 1);
            return secondLastSlash >= 0 ? url.substring(secondLastSlash + 1, lastSlash) : "";
        }
        return "";
    }

    //파일 이름
    public static String getFileName(String url){
        int lastSlash = url.lastIndexOf("/");
        return lastSlash >= 0 ? url.substring(lastSlash + 1) : "";
    }
}
