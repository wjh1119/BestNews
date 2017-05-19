package cn.wjh1119.bestnews.ui.fragment;

import com.aspsine.swipetoloadlayout.SwipeToLoadLayout;

import cn.wjh1119.bestnews.util.Logger;

/**
 * 用于管理List上拉与下拉状态
 * Created by Mr.King on 2017/5/13 0013.
 */

class ListModeManager {

    private final String LOG_TAG = getClass().getSimpleName();

    private int mCurrentPage;
    //主要用来存储上一个totalItemCount
    private int mPreviousTotal;

    private SwipeToLoadLayout mSwipeToLoadLayout;

    private int mSwipeToLoadLayoutMode = 0;
    private final static int SWIPETOLOADLAYOUT_NONE = 0;
    final static int SWIPETOLOADLAYOUT_REFRESH = 1;
    final static int SWIPETOLOADLAYOUT_LOAD = 2;

    ListModeManager(SwipeToLoadLayout swipeToLoadLayout) {
        //当前页数
        mCurrentPage = 1;
        //已经加载出来的Item的数量
        //主要用来存储上一个totalItemCount
        mPreviousTotal = 0;

        mSwipeToLoadLayout = swipeToLoadLayout;
    }

    void onUpdateDataFinished(int totalItemCount) {

        //更改SwipeToLoadLayoutMode的加载状态，并记录当前的页码
        if (mSwipeToLoadLayoutMode == SWIPETOLOADLAYOUT_REFRESH) {
            mSwipeToLoadLayout.setRefreshing(false);
            mSwipeToLoadLayoutMode = SWIPETOLOADLAYOUT_NONE;
            mCurrentPage = 1;
            Logger.d(LOG_TAG, "currentPage is " + mCurrentPage);
        }
        if (mSwipeToLoadLayoutMode == SWIPETOLOADLAYOUT_LOAD) {
            mSwipeToLoadLayout.setLoadingMore(false);
            mSwipeToLoadLayoutMode = SWIPETOLOADLAYOUT_NONE;
            if (totalItemCount > mPreviousTotal) {
                mCurrentPage++;
                mPreviousTotal = totalItemCount;
                Logger.d(LOG_TAG, "currentPage is " + mCurrentPage);
            }
        }
    }

    void onDataStatusError() {
        if (mSwipeToLoadLayoutMode == SWIPETOLOADLAYOUT_REFRESH) {
            mSwipeToLoadLayout.setRefreshing(false);
            mSwipeToLoadLayoutMode = SWIPETOLOADLAYOUT_NONE;
        }
        if (mSwipeToLoadLayoutMode == SWIPETOLOADLAYOUT_LOAD) {
            mSwipeToLoadLayout.setLoadingMore(false);
            mSwipeToLoadLayoutMode = SWIPETOLOADLAYOUT_NONE;
        }
    }

    void setMode(int mode) {
        mSwipeToLoadLayoutMode = mode;
    }

    int getCurrentPage() {
        return mCurrentPage;
    }
}
