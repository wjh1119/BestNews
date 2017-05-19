package cn.wjh1119.bestnews.util;

import android.support.design.widget.Snackbar;
import android.view.View;

/**
 * Snackbar工具类
 * Created by Mr.King on 2017/4/12 0012.
 */

public class SnackbarUtil {
    private SnackbarUtil() {

    }

    public static void show(View rootLayout, String message){
        Snackbar.make(rootLayout, message, Snackbar.LENGTH_LONG)
                .setDuration(4000).show();
    }
}
