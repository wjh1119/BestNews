package cn.wjh1119.bestnews.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SyncRequest;
import android.content.SyncResult;
import android.os.Build;
import android.os.Bundle;

import java.util.ArrayList;

import cn.wjh1119.bestnews.R;
import cn.wjh1119.bestnews.util.FileUtil;
import cn.wjh1119.bestnews.util.Logger;
import cn.wjh1119.bestnews.util.NetworkUtil;
import cn.wjh1119.bestnews.util.NewDataUtil;
import cn.wjh1119.bestnews.util.PrefUtil;

import static cn.wjh1119.bestnews.util.NewDataUtil.getNewsDataFromJsonStr;
import static cn.wjh1119.bestnews.util.NewDataUtil.getNewsDataJsonStr;

/**
 * 后台自动更新数据库
 *@author WJH
 *created at 2017/4/16 0016
 */

public class BestNewsSyncAdapter extends AbstractThreadedSyncAdapter {

    private final String LOG_TAG = BestNewsSyncAdapter.class.getSimpleName();

    // 后台更新频率
    private static int SYNC_INTERVAL = 60*60*3;  // 3 hours
    private static int SYNC_FLEXTIME = SYNC_INTERVAL/3;

    public BestNewsSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        Logger.d(LOG_TAG,"onPerformSync");
        if (!new NetworkUtil(getContext()).getConnectivityStatus()){
            return;
        }

        PrefUtil prefUtil = new PrefUtil(getContext());
        //删除所有本地缓存
        FileUtil.getInstance(getContext().getApplicationContext()).deleteFile();
        Logger.d(LOG_TAG,"delete all local cache");

        String channelJsonStr = NewDataUtil.getChannelJsonStr();
        prefUtil.setBestNewsChannels(channelJsonStr);

        ArrayList<String> channelsArray = prefUtil.getBestNewsChannels();
        Logger.d(LOG_TAG,"newChannels is " + channelsArray);

        for(int i = 0; i < channelsArray.size(); i++) {
            String channelName = channelsArray.get(i);
            int page = 1;
            getNewsDataFromJsonStr(getContext(), getNewsDataJsonStr(channelName,page), NewDataUtil.UPDATE);

            //暂停0.5s，以免频繁调用
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Helper method to schedule the sync adapter periodic execution
     */
    private static void configurePeriodicSync(Context context, int syncInterval, int flexTime) {
        Account account = getSyncAccount(context);
        String authority = context.getString(R.string.content_authority);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // we can enable inexact timers in our periodic sync
            SyncRequest request = new SyncRequest.Builder().
                    syncPeriodic(syncInterval, flexTime).
                    setSyncAdapter(account, authority).
                    setExtras(new Bundle()).build();
            ContentResolver.requestSync(request);
        } else {
            ContentResolver.addPeriodicSync(account,
                    authority, new Bundle(), syncInterval);
        }
    }

    /**
     * Helper method to have the sync adapter sync immediately
     * @param context The context used to access the account service
     */
    public static void syncImmediately(Context context) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        ContentResolver.requestSync(getSyncAccount(context),
                context.getString(R.string.content_authority), bundle);
    }

    /**
     * Helper method to get the fake account to be used with SyncAdapter, or make a new one
     * if the fake account doesn't exist yet.  If we make a new account, we call the
     * onAccountCreated method so we can initialize things.
     *
     * @param context The context used to access the account service
     * @return a fake account.
     */
    private static Account getSyncAccount(Context context) {
        // Get an instance of the Android account manager
        AccountManager accountManager =
                (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);

        // Create the account type and default account
        Account newAccount = new Account(
                context.getString(R.string.app_name), context.getString(R.string.sync_account_type));

        // If the password doesn't exist, the account doesn't exist
        if ( null == accountManager.getPassword(newAccount) ) {

        /*
         * Add the account and account type, no password or user data
         * If successful, return the Account object, otherwise report an error.
         */
            if (!accountManager.addAccountExplicitly(newAccount, "", null)) {
                return null;
            }
            /*
             * If you don't set android:syncable="true" in
             * in your <provider> element in the manifest,
             * then call ContentResolver.setIsSyncable(account, AUTHORITY, 1)
             * here.
             */

            onAccountCreated(newAccount, context);
        }
        return newAccount;
    }

    private static void onAccountCreated(Account newAccount, Context context) {
        /*
         * Since we've created an account
         */
        BestNewsSyncAdapter.configurePeriodicSync(context, SYNC_INTERVAL, SYNC_FLEXTIME);

        /*
         * Without calling setSyncAutomatically, our periodic sync will not be enabled.
         */
        ContentResolver.setSyncAutomatically(newAccount, context.getString(R.string.content_authority), true);

        /*
         * Finally, let's do a sync to get things started
         */
        syncImmediately(context);
    }

    public static void initializeSyncAdapter(Context context) {
        getSyncAccount(context);
    }

}