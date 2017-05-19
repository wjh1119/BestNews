package cn.wjh1119.bestnews.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * 数据库DbHelper
 * Created by Mr.King on 2017/2/13 0013.
 */

class BestNewsDbHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 8;

    private static final String DATABASE_NAME = "movie.db";

    private static BestNewsDbHelper instance;

    private BestNewsDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    static BestNewsDbHelper getDbHelper(Context context){

        if (instance == null) {
            synchronized (BestNewsDbHelper.class) {
                if (instance == null) {
                    instance = new BestNewsDbHelper(context);
                }
            }
        }
        return instance;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        final String SQL_CREATE_MOVIE_TABLE = "CREATE TABLE " + BestNewsContract.NewsEntry.TABLE_NAME + " (" +
                BestNewsContract.NewsEntry._ID + " INTEGER PRIMARY KEY," +
                BestNewsContract.NewsEntry.COLUMN_PUBDATE + " TEXT NOT NULL, " +
                BestNewsContract.NewsEntry.COLUMN_HAVEPIC + " TEXT NOT NULL, " +
                BestNewsContract.NewsEntry.COLUMN_TITLE + " TEXT NOT NULL, " +
                BestNewsContract.NewsEntry.COLUMN_CHANNELNAME + " TEXT NOT NULL, " +
                BestNewsContract.NewsEntry.COLUMN_IMAGEURL + " TEXT, " +
                BestNewsContract.NewsEntry.COLUMN_DESC + " TEXT NOT NULL, " +
                BestNewsContract.NewsEntry.COLUMN_SOURCE + " TEXT NOT NULL, " +
                BestNewsContract.NewsEntry.COLUMN_CHANNELID + " TEXT NOT NULL, " +
                BestNewsContract.NewsEntry.COLUMN_LINK + " TEXT NOT NULL, " +
                BestNewsContract.NewsEntry.COLUMN_CONTENT + " TEXT NOT NULL, " +
                BestNewsContract.NewsEntry.COLUMN_REVIEW+ " TEXT NOT NULL, " +
                " UNIQUE (" + BestNewsContract.NewsEntry.COLUMN_TITLE + ") ON CONFLICT REPLACE);";


        sqLiteDatabase.execSQL(SQL_CREATE_MOVIE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + BestNewsContract.NewsEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}
