package cn.wjh1119.bestnews.asynctask;

import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;

import java.util.ArrayList;

import cn.wjh1119.bestnews.bean.ReviewBean;
import cn.wjh1119.bestnews.util.NewDataUtil;

import static cn.wjh1119.bestnews.data.BestNewsContract.NewsEntry.COLUMN_REVIEW;


/**
 * 从数据库中获取新闻的评论信息
 * Created by Mr.King on 2017/2/22 0022.
 */

public class FetchReviewDataFromSqlTask extends AsyncTask<Cursor, Void, ArrayList<ReviewBean>> {
    //private final String LOG_TAG = FetchReviewDataFromSqlTask.class.getSimpleName();

    private final Context mContext;

    public FetchReviewDataFromSqlTask(Context context) {
        mContext = context;
    }

    //数据监听器
    private FetchReviewDataFromSqlTask.OnDataFinishedListener onDataFinishedListener;

    /**
     * 设置监听器
     * @param onDataFinishedListener 监听器
     */
    public void setOnDataFinishedListener(
            FetchReviewDataFromSqlTask.OnDataFinishedListener onDataFinishedListener) {
        this.onDataFinishedListener = onDataFinishedListener;
    }

    @Override
    protected ArrayList<ReviewBean> doInBackground(Cursor... params) {

        ArrayList<ReviewBean> reviewsData = new ArrayList<>();
        Cursor mData = params[0];
        try {

            String reviewsJsonStr = mData.getString(mData.getColumnIndex(COLUMN_REVIEW));

            if (reviewsJsonStr != null) {
                reviewsData = NewDataUtil.getReviewDataFromJsonStr(mContext,reviewsJsonStr);

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return reviewsData;
    }

    @Override
    protected void onPostExecute(ArrayList<ReviewBean> reviewsData) {
        if(reviewsData!=null){
            onDataFinishedListener.onDataSuccessfully(reviewsData);
        }else{
            onDataFinishedListener.onDataFailed();
        }
    }

    /**
     * 监听器，数据获取成功或失败的回调
     */
    public interface OnDataFinishedListener {

        void onDataSuccessfully(ArrayList<ReviewBean> data);
        void onDataFailed();

    }
}
