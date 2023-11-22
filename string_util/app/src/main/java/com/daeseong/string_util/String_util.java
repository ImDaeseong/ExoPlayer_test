package com.daeseong.string_util;

import android.text.TextUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class String_util {

    //파일 확장자
    public static String getFileExt(String url) {
        String result = "";
        int lastIndex = url.lastIndexOf(".");
        if (lastIndex >= 0) {
            result = url.substring(lastIndex + 1);
        }
        return result;
    }

    //파일 이름만
    public static String getOnlyFileName(String url) {
        String result = "";
        int lastSlashIndex = url.lastIndexOf("/");
        int lastIndex;

        if (lastSlashIndex >= 0) {
            String temp = url.substring(lastSlashIndex + 1);
            lastIndex = temp.lastIndexOf(".");
            if (lastIndex >= 0) {
                result = temp.substring(0, lastIndex);
            }
        } else {
            lastIndex = url.lastIndexOf(".");
            if (lastIndex >= 0) {
                result = url.substring(0, lastIndex);
            }
        }
        return result;
    }

    //파일 이름
    public static String getFileName(String url) {
        String result = "";
        int lastSlashIndex = url.lastIndexOf("/");
        if (lastSlashIndex >= 0) {
            result = url.substring(lastSlashIndex + 1);
        }
        return result;
    }

    //파일 경로
    public static String getFilePath(String url) {
        String result = "";
        int lastSlashIndex = url.lastIndexOf("/");
        if (lastSlashIndex >= 0) {
            result = url.substring(0, lastSlashIndex);
        }
        return result;
    }

    //파일 경로 / 포함
    public static String getFilePathSlash(String url) {
        String result = "";
        if (TextUtils.isEmpty(url)) return result;

        if (url.charAt(url.length() - 1) != '/') {
            result = url.substring(0, url.lastIndexOf('/') + 1);
        } else {
            result = url.substring(0, url.lastIndexOf('/', url.lastIndexOf('/') - 1) + 1);
        }
        return result;
    }

    //파일 이름앞 폴더명
    public static String getLastFolderName(String url) {
        String result = "";
        if (TextUtils.isEmpty(url)) return result;

        int lastSlashIndex = url.lastIndexOf("/");
        if (lastSlashIndex >= 0) {
            String temp = url.substring(0, lastSlashIndex);

            lastSlashIndex = temp.lastIndexOf("/");
            if (lastSlashIndex >= 0) {
                result = temp.substring(lastSlashIndex + 1);
            }
        }
        return result;
    }

    //폴더 경로 분리
    public static List<String> getSplitFolder(String url, String split) {
        List<String> arrayList = new ArrayList<>();

        if (isEmpty(url)) {
            return arrayList;
        }

        if (isEmpty(split)) {
            arrayList.add(url);
            return arrayList;
        }

        StringTokenizer stringTokenizer = new StringTokenizer(url, split);
        try {
            while (stringTokenizer.hasMoreTokens()) {
                arrayList.add(stringTokenizer.nextToken());
            }
        } catch (Exception ex) {
        }
        return arrayList;
    }

    public static boolean isEmpty(String url) {
        return url == null || url.trim().length() == 0;
    }
}
