package cn.wjh1119.bestnews.asynctask;

import android.database.Cursor;
import android.os.AsyncTask;

import java.util.HashMap;

import cn.wjh1119.bestnews.data.BestNewsContract;

/**
 * 从数据库中获取新闻的详细信息
 * Created by Mr.King on 2017/2/22 0022.
 */

public class FetchDetailDataFromSqlTask extends AsyncTask<Cursor, Void, HashMap> {
    //private final String LOG_TAG = FetchDetailDataFromSqlTask.class.getSimpleName();

    public FetchDetailDataFromSqlTask() {

    }

    //数据监听器
    private FetchDetailDataFromSqlTask.OnDataFinishedListener onDataFinishedListener;

    /**
     * 设置监听器
     * @param onDataFinishedListener 监听器
     */
    public void setOnDataFinishedListener(
            FetchDetailDataFromSqlTask.OnDataFinishedListener onDataFinishedListener) {
        this.onDataFinishedListener = onDataFinishedListener;
    }

    @Override
    protected HashMap doInBackground(Cursor... params) {

        HashMap<String, Object> detailHashMap = new HashMap<>();
        Cursor mData = params[0];

        String title;
        String source;
        String pubDate;
        String content;
        String link;
        String imageUrl;

        try {

            title = mData.getString(mData.getColumnIndex(BestNewsContract.NewsEntry.COLUMN_TITLE));
            source = mData.getString(mData.getColumnIndex(BestNewsContract.NewsEntry.COLUMN_SOURCE));
            pubDate = mData.getString(mData.getColumnIndex(BestNewsContract.NewsEntry.COLUMN_PUBDATE));
            content = mData.getString(mData.getColumnIndex(BestNewsContract.NewsEntry.COLUMN_CONTENT));
            link = mData.getString(mData.getColumnIndex(BestNewsContract.NewsEntry.COLUMN_LINK));
            imageUrl = mData.getString(mData.getColumnIndex(BestNewsContract.NewsEntry.COLUMN_IMAGEURL));

            detailHashMap.put("title",title);
            detailHashMap.put("source",source);
            detailHashMap.put("pubDate",pubDate);
            detailHashMap.put("content",content);
            detailHashMap.put("link",link);
            detailHashMap.put("imageUrl",imageUrl);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return detailHashMap;
    }

    @Override
    protected void onPostExecute(HashMap hashMap) {
        if(hashMap!=null){
            onDataFinishedListener.onDataSuccessfully(hashMap);
        }else{
            onDataFinishedListener.onDataFailed();
        }
    }

    /**
     * 监听器，数据获取成功或失败的回调
     */
    public interface OnDataFinishedListener {

        void onDataSuccessfully(HashMap mData);
        void onDataFailed();

    }
}

