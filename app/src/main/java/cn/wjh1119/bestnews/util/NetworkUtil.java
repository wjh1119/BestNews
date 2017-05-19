package cn.wjh1119.bestnews.util;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * 网络工具包
 *@author WJH
 *created at 2017/4/16 0016
 */
 

public class NetworkUtil {

    private Context mContext;

    public NetworkUtil(Context context) {
        this.mContext = context;
    }

    //判断网络是否可用
    public boolean getConnectivityStatus() {
        ConnectivityManager connManager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = connManager.getActiveNetworkInfo();
        //这条语句的意思是，只有当info不为null，并且网络可用的情况下才返回true，其余情况返回false
        return info != null && info.isConnected();
    }
}