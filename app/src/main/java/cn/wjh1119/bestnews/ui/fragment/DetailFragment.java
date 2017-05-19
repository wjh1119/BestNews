/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cn.wjh1119.bestnews.ui.fragment;

import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ForegroundColorSpan;
import android.text.style.URLSpan;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.wjh1119.bestnews.R;
import cn.wjh1119.bestnews.asynctask.FetchDetailDataFromSqlTask;
import cn.wjh1119.bestnews.data.BestNewsContract;
import cn.wjh1119.bestnews.util.ImageManager;
import cn.wjh1119.bestnews.util.Logger;

/**
 * DetailFragment
 *
 * @author WJH
 *         created at 2017/4/16 0016
 */

public class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String LOG_TAG = DetailFragment.class.getSimpleName();

    public static final String REVIEWFRAGMENT_TAG = "RFTAG";
    public static final String DETAIL_URI = "URI";
    public static final String DETAIL_TRANSITION_ANIMATION = "DTA";

    private Uri mUri;
    private boolean mTransitionAnimation;
    private ImageManager imageManager;

    private static final int DETAIL_LOADER = 0;


    @BindView(R.id.tv_detail_title)
    TextView mTitleView;

    @BindView(R.id.tv_detail_source)
    TextView mSourceView;

    @BindView(R.id.tv_detail_pubdate)
    TextView mPubDateView;

    @BindView(R.id.iv_detail_image)
    ImageView mImageView;

    @BindView(R.id.tv_detail_content)
    TextView mContentView;

    @BindView(R.id.tv_detail_link)
    TextView mLinkView;

    //Detail中使用的列
    private static final String[] DETAIL_COLUMNS = {
            BestNewsContract.NewsEntry.TABLE_NAME + "." + BestNewsContract.NewsEntry.COLUMN_TITLE,
            BestNewsContract.NewsEntry.COLUMN_SOURCE,
            BestNewsContract.NewsEntry.COLUMN_PUBDATE,
            BestNewsContract.NewsEntry.COLUMN_CONTENT,
            BestNewsContract.NewsEntry.COLUMN_LINK,
            BestNewsContract.NewsEntry.COLUMN_IMAGEURL,
    };

    public DetailFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        //获取参数
        Bundle arguments = getArguments();
        if (arguments != null) {
            mUri = arguments.getParcelable(DetailFragment.DETAIL_URI);
            mTransitionAnimation = arguments.getBoolean(DetailFragment.DETAIL_TRANSITION_ANIMATION, false);
        }

        View rootView = inflater.inflate(R.layout.fragment_detail_start, container, false);

        //查看是否有reviewFragment，没有则初始化
        Fragment reviewFragment = getChildFragmentManager().findFragmentByTag(REVIEWFRAGMENT_TAG);
        if (reviewFragment == null) {
            Logger.i(LOG_TAG, "add new reviewFragment !!");
            Bundle args = new Bundle();
            args.putParcelable(ReviewFragment.REVIEW_URI, mUri);

            reviewFragment = new ReviewFragment();
            reviewFragment.setArguments(args);

            FragmentTransaction fragmentTransaction = getChildFragmentManager().beginTransaction();
            fragmentTransaction.add(R.id.container_review, reviewFragment, REVIEWFRAGMENT_TAG).commit();
        } else {
            Logger.i(LOG_TAG, "found existing reviewFragment, no need to add it again !!");
        }

        //加载其他view
        ButterKnife.bind(this, rootView);

        imageManager = ImageManager.getSingleton(getContext());

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        //初始化Loader
        getLoaderManager().initLoader(DETAIL_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (null != mUri) {
            // Now create and return a CursorLoader that will take care of
            // creating a Cursor for the data being displayed.
            return new CursorLoader(
                    getActivity(),
                    mUri,
                    DETAIL_COLUMNS,
                    null,
                    null,
                    null
            );
        }

        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data != null && data.moveToFirst()) {

            //异步获取新闻的详细数据，当获取成功时将数据载入至view
            FetchDetailDataFromSqlTask fetchDetailDataFromSqlTask = new FetchDetailDataFromSqlTask();
            fetchDetailDataFromSqlTask.setOnDataFinishedListener(new FetchDetailDataFromSqlTask.OnDataFinishedListener() {
                @Override
                public void onDataSuccessfully(HashMap data) {

                    String title = data.get("title").toString();
                    String source = data.get("source").toString();
                    String pubDate = data.get("pubDate").toString();
                    String content = data.get("content").toString();
                    String link = data.get("link").toString();
//                    BitmapDrawable drawable = (BitmapDrawable) data.get("drawable");
                    Object imageTextFromSql = data.get("imageUrl");
                    String imageUrl = null;
                    if (imageTextFromSql != null){
                        imageUrl = data.get("imageUrl").toString();
                    }

                    mTitleView.setText(title);
                    mTitleView.setFocusable(true);
                    mTitleView.setFocusableInTouchMode(true);
                    mTitleView.requestFocus();
                    TextPaint paint = mTitleView.getPaint();
                    paint.setFakeBoldText(true);
                    mSourceView.setText(source);
                    mPubDateView.setText(pubDate);
                    mContentView.setText(content);

                    if (imageUrl != null){
                        Bitmap bitmap = imageManager.showCacheBitmap(imageUrl);
                        if (bitmap != null){
                            mImageView.setImageBitmap(bitmap);

                        }else{
                            imageManager.downloadImage(imageUrl, new ImageManager.onImageLoaderListener() {

                                @Override
                                public void onImageLoader(Bitmap bitmap, String url) {
                                    if(bitmap != null){
                                        mImageView.setImageBitmap(bitmap);
                                    }else{
                                        mImageView.setImageResource(R.mipmap.picture_fail_loading);
                                    }
                                }
                            });
                        }
                    }

                    SpannableStringBuilder ssb = new SpannableStringBuilder("新闻来源");
                    ssb.setSpan(new URLSpan(link), 0, ssb.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);

                    ForegroundColorSpan span = new ForegroundColorSpan(Color.BLUE);
                    ssb.setSpan(span, 0, ssb.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                    mLinkView.setText(ssb);
                    // 在单击链接时凡是有要执行的动作，都必须设置MovementMethod对象
                    mLinkView.setMovementMethod(LinkMovementMethod.getInstance());
                    // 设置点击后的颜色，这里涉及到ClickableSpan的点击背景
                    mLinkView.setHighlightColor(0xff8FABCC);
                }

                @Override
                public void onDataFailed() {

                }
            });
            fetchDetailDataFromSqlTask.execute(data);
        }
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        Toolbar toolbarView = (Toolbar) getView().findViewById(R.id.toolbar_detail);

        // We need to start the enter transition after the data has loaded
        if (mTransitionAnimation) {
            activity.supportStartPostponedEnterTransition();

            if (null != toolbarView) {
                activity.setSupportActionBar(toolbarView);
                activity.getSupportActionBar().setDisplayShowTitleEnabled(false);
                activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }
        } else {
            if (null != toolbarView) {
                Menu menu = toolbarView.getMenu();
                if (null != menu) menu.clear();
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }
}