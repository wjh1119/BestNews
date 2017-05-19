package cn.wjh1119.bestnews.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class BestNewsSyncService extends Service {
    private static final Object sSyncAdapterLock = new Object();
    private static BestNewsSyncAdapter sBestNewsSyncAdapter = null;

    @Override
    public void onCreate() {
        synchronized (sSyncAdapterLock) {
            if (sBestNewsSyncAdapter == null) {
                sBestNewsSyncAdapter = new BestNewsSyncAdapter(getApplicationContext(), true);
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return sBestNewsSyncAdapter.getSyncAdapterBinder();
    }
}