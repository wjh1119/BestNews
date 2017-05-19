package cn.wjh1119.bestnews.util;

import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.IntDef;

import com.show.api.ShowApiRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Vector;

import cn.wjh1119.bestnews.BuildConfig;
import cn.wjh1119.bestnews.asynctask.FetchNewDataTask;
import cn.wjh1119.bestnews.bean.ReviewBean;
import cn.wjh1119.bestnews.data.BestNewsContract;


/**
 * 网络数据工具包，用于从网络上获取新闻数据
 * Created by Mr.King on 2017/4/7 0007.
 */

public class NewDataUtil {

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({NEWDATA_STATUS_OK, NEWDATA_STATUS_SERVER_DOWN, NEWDATA_STATUS_SERVER_INVALID, NEWDATA_STATUS_UNKNOWN, NEWDATA_STATUS_INVALID})
    public @interface NewDataStatus {
    }

    public static int NORMAL = 0;
    public static int REFRESH = 1;
    public static int UPDATE = 2;

    public static final int NEWDATA_STATUS_OK = 0;
    public static final int NEWDATA_STATUS_SERVER_DOWN = 1;
    public static final int NEWDATA_STATUS_SERVER_INVALID = 2;
    public static final int NEWDATA_STATUS_UNKNOWN = 3;
    public static final int NEWDATA_STATUS_INVALID = 4;

    /**
     * 从网络上获取包含新闻频道的JsonStr
     *
     * @return 包含新闻频道的JsonStr
     */
    public static String getChannelJsonStr() {
        // If there's no zip code, there's nothing to look up.  Verify size of params.

        final String LOG_TAG = "getChannelJsonStr";

        String BASE_URL = "http://route.showapi.com/109-34";
        final String appid = "34508";//要替换成自己的
        final String secret = "7eabc294c59643959a5fb19ebba2b454";//要替换成自己的


        final String newChannelJson = new ShowApiRequest(BASE_URL, appid, secret)
                .post();

        Logger.d(LOG_TAG, "NewChannelJson is " + newChannelJson);

        return newChannelJson;

    }

    /**
     * 获取包含评论数据的Json
     *
     * @return 包含评论数据的Json
     */
    private static JSONObject getReviewsDataJson() {
        JSONObject reviewsDataJson = new JSONObject();
        JSONArray jsonMembers = new JSONArray();
        try {
            for (int i = 0; i < 10; i++) {
                JSONObject item = new JSONObject();
                item.put("date", "2017-1-1 00:00:0" + i);
                item.put("author", "作者" + i);
                item.put("site", "地点" + i);
                item.put("content", "评论内容评论内容评论内容评论内容" + i);
                jsonMembers.put(item);
            }
            reviewsDataJson.put("review", jsonMembers);
        } catch (JSONException exception) {
            exception.printStackTrace();
        }

        return reviewsDataJson;
    }

    /**
     * 获取该新闻频道下的某个页数下的新闻Jsonstr
     *
     * @param channelName 新闻频道
     * @param page        页数
     * @return 该新闻频道下的某个页数下的新闻JsonStr
     */
    public static String getNewsDataJsonStr(String channelName, int page) {
        // If there's no zip code, there's nothing to look up.  Verify size of params.

        final String LOG_TAG = "getNewsDataJsonStr";

        String BASE_URL = "http://route.showapi.com/109-35";
        final String appid = "34508";//要替换成自己的
        final String secret = BuildConfig.MY_SHOWAPI_NEWS_API_KEY;

        final String newsJsonStr = new ShowApiRequest(BASE_URL, appid, secret)
                .addTextPara("channelId", "")
                .addTextPara("channelName", channelName)
                .addTextPara("title", "")
                .addTextPara("page", Integer.toString(page))
                .addTextPara("needContent", "1")
                .addTextPara("needHtml", "")
                .addTextPara("needAllList", "")
                .addTextPara("maxResult", "20")
                .post();

        Logger.d(LOG_TAG, "NewJsonStr is " + newsJsonStr);

        return newsJsonStr;

    }

    /**
     * 从新闻JsonStr中获取新闻数据
     *
     * @param c                上下文环境
     * @param newsJsonStr      包含新闻数据的JsonStr
     * @param mode             是否删除原来的数据
     * @param fetchNewDataTask 异步，方便调用isCancelled
     */
    public static void getNewsDataFromJsonStr(Context c, String newsJsonStr, int mode,
                                              FetchNewDataTask fetchNewDataTask) {

        String mChannelName = "";

        final String LOG_TAG = "getNewsDataFromJsonStr";

        final String OWM_ERROR = "showapi_res_error";
        final String OWM_BODY = "showapi_res_body";
        final String OWM_CODE = "showapi_res_code";

        final String OWM_RET_CODE = "ret_code";
        final String OWM_REMARK = "remark";
        final String OWM_PAGEBEAN = "pagebean";

        final String OWM_LIST = "contentlist";

        final String OWM_PUBDATE = "pubDate";
        final String OWM_HAVEPIC = "havePic";
        final String OWM_TITLE = "title";
        final String OWM_CHANNELNAME = "channelName";
        final String OWM_IMAGEULRS = "imageurls";
        final String OWM_ULR = "url";
        final String OWM_DESC = "desc";
        final String OWM_SOURCE = "source";
        final String OWM_CHANNELID = "channelId";
        final String OWM_LINK = "link";
        final String OWM_CONTENT = "content";

        Context context = c.getApplicationContext();
        ImageManager imageManager = null;
        if (mode == UPDATE){
            imageManager = ImageManager.getSingleton(context);
        }

        PrefUtil prefUtil = new PrefUtil(context);
        //重置服务器状态
        prefUtil.resetNewDataStatus();

        try {
            String pubDate;
            boolean havePic;
            String title;
            String channelName;
            String desc;
            String source;
            String channelId;
            String link;
            String content;
            Bitmap image = null;
            String reviewsJsonStr;

            JSONObject newContent;
            ContentValues contentValue;

            JSONObject newsJson = new JSONObject(newsJsonStr);

            // do we have an error?
            if (newsJson.getInt(OWM_CODE) != 0) {
                Logger.d(LOG_TAG, newsJson.getString(OWM_ERROR));
                prefUtil.setNewDataStatus(NEWDATA_STATUS_SERVER_DOWN);
                return;
            }

            JSONObject bodyJson = newsJson.getJSONObject(OWM_BODY);

            if (bodyJson.getInt(OWM_RET_CODE) != 0) {
                Logger.d(LOG_TAG, bodyJson.getString(OWM_REMARK));
                prefUtil.setNewDataStatus(NEWDATA_STATUS_SERVER_DOWN);
                Logger.d(LOG_TAG, "asynctask is cancelled");
                return;
            }

            JSONObject pageBeanJson = bodyJson.getJSONObject(OWM_PAGEBEAN);
            JSONArray listArray = pageBeanJson.getJSONArray(OWM_LIST);

            Vector<ContentValues> cVVector = new Vector<>(listArray.length());

            int listItemCount = listArray.length();

            if (listItemCount == 0) {
                //服务器无显示错误信息，但返回空值，原因是channelName出错
                prefUtil.setNewDataStatus(NEWDATA_STATUS_INVALID);
                return;
            }

            for (int i = 0; i < listItemCount; i++) {
                if (fetchNewDataTask != null && fetchNewDataTask.isCancelled()) {
                    prefUtil.setNewDataStatus(NEWDATA_STATUS_OK);
                    return;
                }

                newContent = listArray.getJSONObject(i);

                pubDate = newContent.getString(OWM_PUBDATE);
                havePic = newContent.getBoolean(OWM_HAVEPIC);
                title = newContent.getString(OWM_TITLE);
                channelName = newContent.getString(OWM_CHANNELNAME);
                if (i == 0) {
                    mChannelName = channelName;
                }
                desc = newContent.getString(OWM_DESC);
                source = newContent.getString(OWM_SOURCE);
                channelId = newContent.getString(OWM_CHANNELID);
                link = newContent.getString(OWM_LINK);
                content = newContent.getString(OWM_CONTENT);

                contentValue = new ContentValues();

                contentValue.put(BestNewsContract.NewsEntry.COLUMN_PUBDATE, pubDate);
                contentValue.put(BestNewsContract.NewsEntry.COLUMN_HAVEPIC, havePic ? "1" : "0");

                if (havePic) {
                    JSONArray imagesArray = newContent.getJSONArray(OWM_IMAGEULRS);
                    String firstImageUrl = imagesArray.getJSONObject(0).getString(OWM_ULR);
                    contentValue.put(BestNewsContract.NewsEntry.COLUMN_IMAGEURL, firstImageUrl);
                    if (mode == UPDATE){
                        imageManager.downloadImage(firstImageUrl);
                    }
                }

                reviewsJsonStr = getReviewsDataJson().toString();

                contentValue.put(BestNewsContract.NewsEntry.COLUMN_TITLE, title);
                contentValue.put(BestNewsContract.NewsEntry.COLUMN_CHANNELNAME, channelName);
                contentValue.put(BestNewsContract.NewsEntry.COLUMN_DESC, desc);
                contentValue.put(BestNewsContract.NewsEntry.COLUMN_SOURCE, source);
                contentValue.put(BestNewsContract.NewsEntry.COLUMN_CHANNELID, channelId);
                contentValue.put(BestNewsContract.NewsEntry.COLUMN_LINK, link);
                contentValue.put(BestNewsContract.NewsEntry.COLUMN_CONTENT, content);
                contentValue.put(BestNewsContract.NewsEntry.COLUMN_REVIEW, reviewsJsonStr);
                Logger.d(LOG_TAG, "insert value, value is " + contentValue.toString());

                cVVector.add(contentValue);
            }

            //回收image
            if (null != image) {
                image.recycle();
            }

            //int inserted = 0;
            // add to database
            if (cVVector.size() > 0) {
                ContentValues[] cvArray = new ContentValues[cVVector.size()];
                cVVector.toArray(cvArray);

                if (mode != NORMAL) {
                    context.getContentResolver().delete(BestNewsContract.NewsEntry.CONTENT_URI,
                            BestNewsContract.NewsEntry.COLUMN_CHANNELNAME + " = ?",
                            new String[]{mChannelName});
                    Logger.d(LOG_TAG, "delete,channelname is " + mChannelName);
                }

                context.getContentResolver().bulkInsert(BestNewsContract.NewsEntry.CONTENT_URI, cvArray);

            }
            Logger.d(LOG_TAG, "Sync Complete. " + cVVector.size() + " Inserted");
            prefUtil.setNewDataStatus(NEWDATA_STATUS_OK);

        } catch (JSONException e) {
            Logger.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
            prefUtil.setNewDataStatus(NEWDATA_STATUS_SERVER_INVALID);
        }
    }

    public static void getNewsDataFromJsonStr(Context context, String newsJsonStr, int mode) {
        getNewsDataFromJsonStr(context, newsJsonStr, mode, null);
    }

    /**
     * 从JosnStr中获取评论数据
     *
     * @param context           上下文环境
     * @param reviewDataJsonStr 包含评论数据的JsonStr
     * @return 评论数据
     */
    public static ArrayList<ReviewBean> getReviewDataFromJsonStr(Context context, String reviewDataJsonStr) {

        final String LOG_TAG = "getReviewDataFromJsonStr";

        final String OWM_REVIEW = "review";

        final String OWM_DATE = "date";
        final String OWM_AUTHOR = "author";
        final String OWM_SITE = "site";
        final String OWM_CONTENT = "content";

        PrefUtil prefUtil = new PrefUtil(context);

        ArrayList<ReviewBean> reviewsData
                = new ArrayList<>();

        try {

            String date;
            String author;
            String site;
            String content;

            JSONObject reviewContent;
            ReviewBean review;

            JSONObject reviewsJson = new JSONObject(reviewDataJsonStr);
            JSONArray listArray = reviewsJson.getJSONArray(OWM_REVIEW);

            int listItemCount = listArray.length();

            if (listItemCount == 0) {
                return null;
            }

            for (int i = 0; i < listItemCount; i++) {

                // Get the JSON object representing the review
                reviewContent = listArray.getJSONObject(i);

                date = reviewContent.getString(OWM_DATE);
                author = reviewContent.getString(OWM_AUTHOR);
                site = reviewContent.getString(OWM_SITE);
                content = reviewContent.getString(OWM_CONTENT);

                review = new ReviewBean();

                review.author = author;
                review.date = date;
                review.site = site;
                review.content = content;

                reviewsData.add(review);
            }
        } catch (JSONException e) {
            Logger.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
            prefUtil.setNewDataStatus(NEWDATA_STATUS_SERVER_INVALID);
        }

        return reviewsData;
    }
}
