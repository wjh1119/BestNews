package cn.wjh1119.bestnews.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * 文件操作的工具类，提供保存图片，获取图片，判断图片是否存在，删除图片的一些方法
 *
 * @author http://blog.csdn.net/xiaanming/article/details/9825113
 */

public class FileUtil {
    private static volatile FileUtil instance;

    private Context context;

    private FileUtil(Context context) {
        this.context = context;
    }

    public static FileUtil getInstance(Context context) {
        if (instance == null) {
            synchronized (FileUtil.class) {
                if (instance == null) {
                    instance = new FileUtil(context.getApplicationContext());
                }
            }
        }
        return instance;
    }

    /**
     * 保存Image的方法，有sd卡存储到sd卡，没有就存储到手机目录
     *
     * @param url 图片地址
     * @param bitmap 图片
     * @throws IOException 错误
     */
    void saveBitmap(String url, Bitmap bitmap) throws IOException {
        Logger.d(getClass().getSimpleName(), "saveBitmap");
        if (bitmap == null) {
            return;
        }
        String md5String = MD5Util.getMD5String(url);
        FileOutputStream fos = null;
        try {
            File file = new File(getLocalCacheDir(), md5String);
            if (file.createNewFile()) {
                Logger.d(getClass().getSimpleName(), "successed to create new file, file is " +
                        file.toString());
            } else {
                Logger.d(getClass().getSimpleName(), "failed to create new file, file is " +
                        file.toString());
            }
            Logger.d(getClass().getSimpleName(), getLocalCacheDir());
            fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (fos != null) {
                    fos.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 从手机或者sd卡获取Bitmap
     *
     * @param url 图片地址
     * @return 图片
     */
    Bitmap getBitmap(String url) {
        Bitmap bitmap;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = ImageUtil.computeSampleSize(options, -1, 64 * 1024);
        options.inMutable = true;
        options.inPreferredConfig = Bitmap.Config.RGB_565;

        String path = new File(getLocalCacheDir(), MD5Util.getMD5String(url)).toString();
        Logger.d(getClass().getSimpleName(),"getBitmap, \n" +
                "path is " + path);
        bitmap = BitmapFactory.decodeFile(path,options);
        return bitmap;
    }

    /**
     * 判断文件是否存在
     *
     * @param url 图片地址
     * @return 图片是否存在
     */
    boolean isFileExists(String url) {
        return new File(getLocalCacheDir(), MD5Util.getMD5String(url)).exists();
    }

    /**
     * 获取文件的大小
     *
     * @param url 图片地址
     * @return 图片大小
     */
    long getFileSize(String url) {
        return new File(getLocalCacheDir(), MD5Util.getMD5String(url)).length();
    }


    /**
     * 删除SD卡或者手机的缓存图片和目录
     */
    public boolean deleteFile() {
        File dirFile = new File(getLocalCacheDir());
        if (!dirFile.exists()) {
            Logger.w(getClass().getSimpleName(),"File is not exist");
            return false;
        }
        if (dirFile.isDirectory()) {
            String[] children = dirFile.list();
            for (String aChildren : children) {
                if (new File(dirFile, aChildren).delete()) {
                    Logger.v(getClass().getSimpleName(), "success to delete file: " + aChildren);
                } else {
                    Logger.v(getClass().getSimpleName(), "fail to delete file: " + aChildren);
                }
            }
        }

        return dirFile.delete();
    }

    private String getLocalCacheDir() {
        Logger.d(getClass().getSimpleName(), "getLocalCacheDir");
        File dir;

        dir = context.getCacheDir();
        if (!dir.exists()) {
            if (dir.mkdirs()) {
                Logger.d(getClass().getSimpleName(), "mkdirs successed");
            } else {
                Logger.d(getClass().getSimpleName(), "mkdirs failed");
            }
        } else {
            Logger.d(getClass().getSimpleName(), "mkdirs is already existed");
        }

        return context.getCacheDir().getAbsolutePath();
    }
}
