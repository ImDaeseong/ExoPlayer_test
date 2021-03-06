package com.daeseong.string_util;

import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

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

    //파일 이름만
    public static String getOnlyFileName(String url){
        String sResult = "";
        int nIndex = url.lastIndexOf("/");
        if(nIndex >= 0){
            String sTemp = url.substring(nIndex +1 );
            nIndex = sTemp.lastIndexOf(".");
            if(nIndex >= 0){
                sResult = sTemp.substring(0, nIndex);
            }
        }else {
            nIndex = url.lastIndexOf(".");
            if(nIndex >= 0){
                sResult = url.substring(0, nIndex);
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

    //파일 경로
    public static String getFilePath(String url){
        String sResult = "";
        int nIndex = url.lastIndexOf("/");
        if(nIndex >= 0){
            sResult = url.substring(0, nIndex);
        }
        return sResult;
    }

    //파일 경로 / 포함
    public static String getFilePathSlash(String url) {
        String sResult = "";
        if(TextUtils.isEmpty(url)) return sResult;

        if (url.charAt(url.length() - 1) != '/') {
            sResult = url.substring(0, url.lastIndexOf('/') + 1);
        } else {
            sResult = url.substring(0, url.lastIndexOf('/', url.lastIndexOf('/') - 1) + 1);
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

    //폴더 경로 분리
    public static List<String> getSplitFolder(String url, String split){

        List<String> arrayList = new ArrayList<>();

        if (isEmpty(url)) {
            return arrayList;
        }

        if (isEmpty(split)) {
            arrayList.add(url);
            return arrayList;
        }

        StringTokenizer stringTokenizer = new StringTokenizer(url, split);
        try{
            while (stringTokenizer.hasMoreTokens()){
                arrayList.add(stringTokenizer.nextToken());
            }
        }catch (Exception ex){
        }
        return arrayList;
    }

    public static boolean isEmpty(String url) {
        return url == null || url.trim().length() == 0;
    }

}
