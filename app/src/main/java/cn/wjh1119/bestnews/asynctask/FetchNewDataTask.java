package cn.wjh1119.bestnews.asynctask;

import android.content.Context;
import android.os.AsyncTask;

import cn.wjh1119.bestnews.util.Logger;
import cn.wjh1119.bestnews.util.NewDataUtil;

/**
 * 从网络上获取新闻的数据
 * Created by Mr.King on 2017/2/22 0022.
 */

public class FetchNewDataTask extends AsyncTask<String, Void, Void> {
    private final String LOG_TAG = getClass().getSimpleName();

    private final Context mContext;

    public FetchNewDataTask(Context context) {
        mContext = context;
    }

    @Override
    protected Void doInBackground(String... params) {

        Logger.d(LOG_TAG,"doInBackground");

        String mChannelName = params[0];
        int currentPage = Integer.parseInt(params[1]);
        String mode = params[2];
        switch (mode) {
            case "refresh":
                NewDataUtil.getNewsDataFromJsonStr(mContext, NewDataUtil.getNewsDataJsonStr(mChannelName, 1), NewDataUtil.REFRESH, this);
                break;
            case "load":
                NewDataUtil.getNewsDataFromJsonStr(mContext, NewDataUtil.getNewsDataJsonStr(mChannelName, currentPage + 1), NewDataUtil.NORMAL);
                break;
            default:
                throw new IllegalArgumentException("Mode isn't refresh or load!");
        }

        return null;
    }
}
