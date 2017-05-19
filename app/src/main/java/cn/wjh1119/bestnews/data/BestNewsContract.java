package cn.wjh1119.bestnews.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * 数据库Contract
 * Created by Mr.King on 2017/2/13 0013.
 */

public class BestNewsContract {

    static final String CONTENT_AUTHORITY = "cn.wjh1119.bestnews";
    private static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    static final String PATH_NEW= "new";

    /* Inner class that defines the table contents of the location table */
    public static final class NewsEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_NEW).build();

        static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_NEW;
        static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_NEW;

        // Table name
        public static final String TABLE_NAME = "new";

        // Human readable location string, provided by the API.  Because for styling,
        // "Mountain View" is more recognizable than 94043.
        public static final String _ID = "id";
        public static final String COLUMN_PUBDATE= "pubDate";
        public static final String COLUMN_HAVEPIC = "havePic";

        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_CHANNELNAME = "channelName";
        public static final String COLUMN_IMAGEURL = "imageUrl";
        public static final String COLUMN_DESC = "desc";
        public static final String COLUMN_SOURCE = "source";
        public static final String COLUMN_CHANNELID = "channelId";
        public static final String COLUMN_LINK = "link";
        public static final String COLUMN_CONTENT = "content";
        public static final String COLUMN_REVIEW = "review";

    }
}
