package cn.wjh1119.bestnews.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.support.v4.util.LruCache;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 图片管理类，使用三级缓存
 * reference http://blog.csdn.net/xiaanming/article/details/9825113
 */

public class ImageManager {
    /**
     * 缓存Image的类，当存储Image的大小大于LruCache设定的值，系统自动释放内存
     */
    private LruCache<String, Bitmap> mMemoryCache;
    /**
     * 操作文件相关类对象的引用
     */
    private FileUtil fileUtil;
    /**
     * 下载Image的线程池
     */
    private ExecutorService mImageThreadPool = null;

    private static volatile ImageManager singleton = null;
    private static final int MSG_DOWNLOAD = 0;

    private ImageManager(Context context) {
        //获取系统分配给每个应用程序的最大内存，每个应用系统分配32M
        if (mMemoryCache == null) {
            int maxMemory = (int) Runtime.getRuntime().maxMemory();
            int mCacheSize = maxMemory / 64;
            Logger.d(getClass().getSimpleName(), "init cachesize is " +
                    mCacheSize + "\n" +
                    "maxMemory is " + maxMemory);
            //给LruCache分配1/8 4M
            mMemoryCache = new LruCache<String, Bitmap>(mCacheSize) {

                //必须重写此方法，来测量Bitmap的大小
                @Override
                protected int sizeOf(String key, Bitmap value) {
                    return value.getByteCount();
                }

            };
        }

        fileUtil = FileUtil.getInstance(context.getApplicationContext());
    }

    public static ImageManager getSingleton(Context context) {
        if (singleton == null) {
            synchronized (ImageManager.class) {
                if (singleton == null) {
                    singleton = new ImageManager(context);
                }
            }
        }
        return singleton;
    }


    /**
     * 获取线程池的方法，因为涉及到并发的问题，我们加上同步锁
     *
     * @return 线程池
     */
    private ExecutorService getThreadPool() {
        if (mImageThreadPool == null) {
            synchronized (ExecutorService.class) {
                if (mImageThreadPool == null) {
                    //为了下载图片更加的流畅，我们用了2个线程来下载图片
                    mImageThreadPool = Executors.newFixedThreadPool(2);
                }
            }
        }

        return mImageThreadPool;

    }

    /**
     * 添加Bitmap到内存缓存
     *
     * @param url    图片地址
     * @param bitmap bitmap
     */
    private void addBitmapToMemoryCache(String url, Bitmap bitmap) {
        String key = MD5Util.getMD5String(url);
        Logger.d(getClass().getSimpleName(), "addBitmapToMemoryCache, \nkey is " + key);
        if (getBitmapFromMemCache(key) == null && bitmap != null) {
            mMemoryCache.put(key, bitmap);
            Logger.d(getClass().getSimpleName(), "addBitmapToMemoryCache, successed to add." +
                    "\nkey is " + key);
        }
    }

    /**
     * 从内存缓存中获取一个Bitmap
     *
     * @param key LruCache 的key
     * @return 对应的Bitmap
     */
    private Bitmap getBitmapFromMemCache(String key) {
        return mMemoryCache.get(key);
    }

    /**
     * 先从内存缓存中获取Bitmap,如果没有就从SD卡或者手机缓存中获取，SD卡或者手机缓存
     * 没有就去下载
     *
     * @param url      图片地址
     * @param listener 下载监听器, 当为null的时候，仅仅保存数据到本地
     */
    public void downloadImage(final String url, final onImageLoaderListener listener) {
        Logger.d(getClass().getSimpleName(), "downloadImage start");

        if (listener != null) {
            final Handler handler = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    super.handleMessage(msg);
                    //当有监听器的时候
                    listener.onImageLoader((Bitmap) msg.obj, url);
                }
            };

            //开始下载
            getThreadPool().execute(new Runnable() {

                @Override
                public void run() {
                    Bitmap bitmap = getBitmapFormUrl(url);
                    handler.obtainMessage(MSG_DOWNLOAD, bitmap).sendToTarget();

                    if (bitmap == null){
                        Logger.d(getClass().getSimpleName(), "failed to downloadImage");
                        return;
                    }else{
                        Logger.d(getClass().getSimpleName(), "downloadImage, save image in loc cache.url is " +
                                url);
                    }
                    addBitmapToMemoryCache(url, bitmap);

                    //保存至本地
                    try {
                        //保存在SD卡或者手机目录
                        fileUtil.saveBitmap(url, bitmap);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        } else {

            //开始下载
            getThreadPool().execute(new Runnable() {

                @Override
                public void run() {
                    Bitmap bitmap = getBitmapFormUrl(url);

                    if (bitmap == null){
                        Logger.d(getClass().getSimpleName(), "failed to downloadImage");
                        return;
                    }else{
                        Logger.d(getClass().getSimpleName(), "downloadImage, save image in loc cache.url is " +
                                url);
                    }

                    //保存至本地
                    try {
                        //保存在SD卡或者手机目录
                        fileUtil.saveBitmap(url, bitmap);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    //回收bitmap
                    if (bitmap != null && !bitmap.isRecycled()){
                        bitmap.recycle();
                    }
                }
            });
        }
    }

    /**
     * 下载数据至本地磁盘中
     *
     * @param url 图片地址
     */
    void downloadImage(final String url) {
        downloadImage(url, null);
    }


    /**
     * 获取Bitmap, 内存中没有就去手机中获取，这一步在getView中会调用，比较关键的一步
     *
     * @param url 图片地址
     * @return Bitmap
     */
    public Bitmap showCacheBitmap(String url) {

        String md5String = MD5Util.getMD5String(url);
        Logger.d(getClass().getSimpleName(), "========================================================= mem start\n" +
                "try to load bitmap from memCache,\n" +
                "md5String is " + md5String);
//        if (getBitmapFromMemCache(md5String) != null) {
        Logger.d(getClass().getSimpleName(), "fail to load bitmap from memCache, \n" +
                "md5String is " + md5String + "\n" +
                "========================================================= mem fail");
        Logger.d(getClass().getSimpleName(), "========================================================= loc start\n" +
                "try to load bitmap from localCache,\n" +
                "md5String is " + md5String);
        if (fileUtil.isFileExists(url) && fileUtil.getFileSize(url) != 0) {
            //从SD卡获取手机里面获取Bitmap
            Bitmap bitmap = fileUtil.getBitmap(url);
            if (bitmap != null) {
                Logger.d(getClass().getSimpleName(), "success to load bitmap from localCache, \n" +
                        "md5String is " + md5String + "\n" +
                        "========================================================= loc success");
            } else {
                Logger.d(getClass().getSimpleName(), "fail to load bitmap from localCache, \n" +
                        "md5String is " + md5String + "\n" +
                        "========================================================= loc fail");
            }

            //将Bitmap 加入内存缓存
            addBitmapToMemoryCache(md5String, bitmap);
            return bitmap;
        }

        Logger.d(getClass().getSimpleName(), "The bitmap isn't in cache, \n" + "md5String is " + md5String);
        return null;
    }


    /**
     * 从Url中获取Bitmap
     *
     * @param urlString 图片地址
     * @return 图片
     */
    private Bitmap getBitmapFormUrl(String urlString) {
        final String LOG_TAG = "getImageFromUrl";

        HttpURLConnection urlConnection = null;

        Bitmap bitmap = null;

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = ImageUtil.computeSampleSize(options, -1, 64 * 1024);
        options.inMutable = true;
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        options.inBitmap = bitmap;


        try {
            URL url = new URL(urlString);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setConnectTimeout(2000); //超时设置
            urlConnection.setDoInput(true);
            urlConnection.setUseCaches(false); //设置不使用缓存
            InputStream inputStream = urlConnection.getInputStream();

            if (inputStream == null) {
                // Nothing to do.
                return null;
            }

            bitmap = BitmapFactory.decodeStream(inputStream, null, options);
            inputStream.close();
            Logger.v("getImageFromUrl", "url is: " + urlString);
            return bitmap;
        } catch (FileNotFoundException e) {
            Logger.e(LOG_TAG, "FileNotFoundException Error ", e);
            return null;
        } catch (IOException e) {
            Logger.e(LOG_TAG, "IOException Error ", e);
            return null;
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
    }

//    /**
//     * 取消正在下载的任务
//     */
//    public synchronized void cancelTask() {
//        if (mImageThreadPool != null) {
//            mImageThreadPool.shutdownNow();
//            mImageThreadPool = null;
//        }
//    }


    /**
     * 异步下载图片的回调接口
     *
     */
    public interface onImageLoaderListener {
        void onImageLoader(Bitmap bitmap, String url);
    }

}
