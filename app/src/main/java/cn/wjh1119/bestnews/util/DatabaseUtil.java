package cn.wjh1119.bestnews.util;

import android.content.ContentUris;
import android.net.Uri;

import cn.wjh1119.bestnews.data.BestNewsContract;

/**
 * 数据库工具类
 * Created by Mr.King on 2017/5/6 0006.
 */

public class DatabaseUtil {
    public static Uri buildNewUri(Long id) {
        return ContentUris.withAppendedId(BestNewsContract.NewsEntry.CONTENT_URI, id);
    }

    public static long getIdFromUri(Uri uri) {
        return Long.parseLong(uri.getPathSegments().get(1));
    }
}
