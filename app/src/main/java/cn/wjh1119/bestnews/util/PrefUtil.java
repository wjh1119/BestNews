package cn.wjh1119.bestnews.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;

import cn.wjh1119.bestnews.R;

import static cn.wjh1119.bestnews.util.NewDataUtil.NEWDATA_STATUS_UNKNOWN;

/**
 * pref工具包，储存及获取SharedPreferences的数据
 * Created by Mr.King on 2017/2/15 0015.
 */

public class PrefUtil {

    private static volatile PrefUtil instance;

    private Context mContext;

    private PrefUtil(Context context) {
        this.mContext = context;
    }

    public static PrefUtil getInstance(Context context) {
        if (instance == null) {
            synchronized (PrefUtil.class) {
                if (instance == null) {
                    instance = new PrefUtil(context.getApplicationContext());
                }
            }
        }
        return instance;
    }

//    /**
//     * 用于获取后台数据更新的频率
//     * @return
//     */
//    public String getPreferredSyncInterval() {
//        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
//        return prefs.getString(mContext.getString(R.string.pref_sync_interval_key), mContext.getString(R.string.pref_sync_interval_default));
//    }

    /**
     * @param isTwoPane 是否双屏
     */
    public void setIsTwoPane(boolean isTwoPane){
        String key = mContext.getString(R.string.pref_istwopane_key);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(key, isTwoPane);
        editor.apply();
    }

    /**
     * 获取是否双屏
     * @return 是否双屏
     */
    public boolean getIsTwoPane() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        return prefs.getBoolean(mContext.getString(R.string.pref_istwopane_key), false);
    }

    /**
     * 储存包含新闻频道的JsonStr
     * @param newChannelJsonStr 新闻频道的JsonStr
     */
    public void setBestNewsChannels(String newChannelJsonStr){

        final String LOG_TAG = "setBestNewsChannels";
        String key = mContext.getString(R.string.pref_channels_key);
        ArrayList<String> channels = new ArrayList<>();

        try{
            String OWM_BODY = "showapi_res_body";
            String OWM_ERROR = "showapi_res_error";
            String OWM_CODE = "showapi_res_code";
            String OWM_LIST = "channelList";
            String OWM_CHANNEL_NAME = "name";

            JSONObject newChannelJson = new JSONObject(newChannelJsonStr);
            int resCode = newChannelJson.getInt(OWM_CODE);

            if (resCode != 0){
                Logger.d(LOG_TAG,newChannelJson.getString(OWM_ERROR));
                return;
            }
            JSONObject resBody = newChannelJson.getJSONObject(OWM_BODY);
            JSONArray channelsArray = resBody.getJSONArray(OWM_LIST);

            int numberOfChannel = channelsArray.length();

            for(int i = 0; i < numberOfChannel; i++) {
                JSONObject channelInfo = channelsArray.getJSONObject(i);
                String channelName = channelInfo.getString(OWM_CHANNEL_NAME);
                channels.add(channelName);
            }
        }catch (JSONException e){
            Logger.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(key, channels.toString());
        editor.apply();
    }

    /**
     * 获取新闻频道列表
     * @return 新闻频道列表
     */
    public ArrayList<String> getBestNewsChannels() {

        String channelsKey = mContext.getString(R.string.pref_channels_key);
        String initializedKey = mContext.getString(R.string.pref_channel_initialized_key);
        String[] defaultChannelsList = mContext.getResources().getStringArray(R.array.default_channels);

        ArrayList<String> defaultChannels = new ArrayList<>(Arrays.asList(defaultChannelsList));
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);

        boolean initialized = prefs.getBoolean(initializedKey, false);

        if (!initialized) {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean(initializedKey, true);
            editor.putString(channelsKey, defaultChannels.toString());
            editor.apply();
            return defaultChannels;
        }

        String channelsArrayStr = prefs.getString(channelsKey, defaultChannels.toString());
        return new ArrayList<>(Arrays.asList(channelsArrayStr
                .replace("[","")
                .replace("]","")
                .split(", ")));
    }

    /**
     * 存储数据库状态
     * @param NewDataStatus 数据库状态
     */
    void setNewDataStatus(@NewDataUtil.NewDataStatus int NewDataStatus){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(mContext);
        SharedPreferences.Editor spe = sp.edit();
        spe.putInt(mContext.getString(R.string.pref_newdata_status_key), NewDataStatus);
        spe.apply();
        Logger.d("setNewDataStatus ", NewDataStatus+"");
    }

    /**
     * 获取数据库状态
     * @return 数据库状态
     */
    @SuppressWarnings("ResourceType")
    public @NewDataUtil.NewDataStatus
    int getNewDataStatus(){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(mContext);
        return sp.getInt(mContext.getString(R.string.pref_newdata_status_key), NEWDATA_STATUS_UNKNOWN);
    }

    /**
     * 重置数据库状态
     */
    void resetNewDataStatus(){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(mContext);
        SharedPreferences.Editor spe = sp.edit();
        spe.putInt(mContext.getString(R.string.pref_newdata_status_key), NEWDATA_STATUS_UNKNOWN);
        spe.apply();
        Logger.d("setNewDataStatus: ", NEWDATA_STATUS_UNKNOWN+"");
    }
}
