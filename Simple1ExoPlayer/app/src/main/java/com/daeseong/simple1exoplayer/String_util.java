package com.daeseong.simple1exoplayer;

import android.text.TextUtils;

public class String_util {

    private String_util() {
        throw new UnsupportedOperationException("String_util");
    }

    //파일 확장자
    public static String getFileExt(String url){
        String sResult = "";
        int nIndex = url.lastIndexOf(".");
        if(nIndex >= 0){
            sResult = url.substring(nIndex + 1);
        }
        return sResult;
    }

    //파일 이름앞 폴더명
    public static String getLastFolderName(String url){
        String sResult = "";
        if(TextUtils.isEmpty(url)) return sResult;

        int nIndex = url.lastIndexOf("/");
        if(nIndex >= 0){
            String sTemp = url.substring(0, nIndex);

            nIndex = sTemp.lastIndexOf("/");
            if(nIndex >= 0){
                sResult = sTemp.substring(nIndex + 1);
            }
        }
        return sResult;
    }

    //파일 이름
    public static String getFileName(String url){
        String sResult = "";
        int nIndex = url.lastIndexOf("/");
        if(nIndex >= 0){
            sResult = url.substring(nIndex + 1);
        }
        return sResult;
    }
}
