package cn.wjh1119.bestnews.ui.fragment;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.util.Pair;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AbsListView;

import com.aspsine.swipetoloadlayout.OnLoadMoreListener;
import com.aspsine.swipetoloadlayout.OnRefreshListener;
import com.aspsine.swipetoloadlayout.SwipeToLoadLayout;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.wjh1119.bestnews.R;
import cn.wjh1119.bestnews.asynctask.FetchNewDataTask;
import cn.wjh1119.bestnews.data.BestNewsContract;
import cn.wjh1119.bestnews.ui.activity.DetailActivity;
import cn.wjh1119.bestnews.ui.activity.MainActivity;
import cn.wjh1119.bestnews.util.DatabaseUtil;
import cn.wjh1119.bestnews.util.Logger;
import cn.wjh1119.bestnews.util.NetworkUtil;
import cn.wjh1119.bestnews.util.NewDataUtil;
import cn.wjh1119.bestnews.util.PrefUtil;
import cn.wjh1119.bestnews.util.SnackbarUtil;
import cn.wjh1119.bestnews.view.DividerItemDecoration;

import static cn.wjh1119.bestnews.ui.fragment.ListModeManager.SWIPETOLOADLAYOUT_LOAD;
import static cn.wjh1119.bestnews.ui.fragment.ListModeManager.SWIPETOLOADLAYOUT_REFRESH;
import static cn.wjh1119.bestnews.util.NewDataUtil.NEWDATA_STATUS_INVALID;
import static cn.wjh1119.bestnews.util.NewDataUtil.NEWDATA_STATUS_OK;
import static cn.wjh1119.bestnews.util.NewDataUtil.NEWDATA_STATUS_SERVER_DOWN;
import static cn.wjh1119.bestnews.util.NewDataUtil.NEWDATA_STATUS_SERVER_INVALID;
import static cn.wjh1119.bestnews.util.NewDataUtil.NEWDATA_STATUS_UNKNOWN;

/**
 * ListFragment:装载每个channel的新闻。
 */

public class ListFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>,
        OnRefreshListener,
        OnLoadMoreListener,
        ListRecyclerViewAdapter.NewAdapterOnClickHandler,
        SharedPreferences.OnSharedPreferenceChangeListener {
    private ListRecyclerViewAdapter mAdapter;
    private SwipeToLoadLayout mSwipeToLoadLayout;

    private final String LOG_TAG = getClass().getSimpleName();
    private boolean mAutoSelectView, mHoldForTransition;
    private long mInitialSelectedId = -1;
    private Cursor mCursor;
    private boolean mIsFragmentVisable = false;
    private boolean mIsFragmentPrepared = false;
    private PrefUtil prefUtil;

    //异步任务
    FetchNewDataTask fetchNewDataTask;

    private ListModeManager mListModeManager;
//    //当前页，从1开始
//    private int currentPage = 1;
//    //已经加载出来的Item的数量
//    private int totalItemCount;
//    //主要用来存储上一个totalItemCount
//    private int previousTotal = 0;
//
//    private int mSwipeToLoadLayoutMode = 0;
//    private final int SWIPETOLOADLAYOUT_NONE = 0;
//    private final int SWIPETOLOADLAYOUT_REFRESH = 1;
//    private final int SWIPETOLOADLAYOUT_LOAD = 2;

    @BindView(R.id.swipe_target)
    RecyclerView mRecyclerView;

    String mChannelName;

    //ListFragment中使用的列
    private static final String[] LIST_COLUMNS = {
            BestNewsContract.NewsEntry.TABLE_NAME + "." + BestNewsContract.NewsEntry._ID,
            BestNewsContract.NewsEntry.COLUMN_TITLE,
            BestNewsContract.NewsEntry.COLUMN_SOURCE,
            BestNewsContract.NewsEntry.COLUMN_PUBDATE,
            BestNewsContract.NewsEntry.COLUMN_IMAGEURL,
            BestNewsContract.NewsEntry.COLUMN_REVIEW,

    };

    public static ListFragment newInstance(int num) {
        ListFragment fragment = new ListFragment();
        // Supply num input as an argument.
        Bundle args = new Bundle();
        args.putInt("number", num);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Logger.d(LOG_TAG, "onCreateView " + mChannelName);

        prefUtil = new PrefUtil(getContext());
        //根据是否双屏设置变量
        int mChoiceMode;
        if (prefUtil.getIsTwoPane()) {
            mChoiceMode = AbsListView.CHOICE_MODE_SINGLE;
            mAutoSelectView = true;
            mHoldForTransition = true;
        } else {
            mChoiceMode = AbsListView.CHOICE_MODE_NONE;
            mAutoSelectView = false;
            mHoldForTransition = false;
        }

        //加载mSwipeToLoadLayout
        mSwipeToLoadLayout
                = (SwipeToLoadLayout) inflater.inflate(R.layout.fragment_list, container, false);
        ButterKnife.bind(this, mSwipeToLoadLayout);

        mSwipeToLoadLayout.setOnRefreshListener(this);
        mSwipeToLoadLayout.setOnLoadMoreListener(this);

        LinearLayoutManager mLinearLayoutManager = new LinearLayoutManager(mRecyclerView.getContext());

        mAdapter = new ListRecyclerViewAdapter(getContext(), this, mChoiceMode);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(getContext(),
                DividerItemDecoration.VERTICAL_LIST));
        mRecyclerView.setLayoutManager(mLinearLayoutManager);

        int numberOfFragment = getArguments() != null ? getArguments().getInt("number") : 0;
        mChannelName = prefUtil.getBestNewsChannels().get(numberOfFragment);

        if (savedInstanceState != null) {
            mAdapter.onRestoreInstanceState(savedInstanceState);
        }

        //载入loader
        if (getActivity().getSupportLoaderManager().hasRunningLoaders()) {
            getActivity().getSupportLoaderManager().restartLoader(numberOfFragment, null, this);
        } else {
            getActivity().getSupportLoaderManager().initLoader(numberOfFragment, null, this);
        }

        //记录是否加载完视图
        mIsFragmentPrepared = true;

        mListModeManager = new ListModeManager(mSwipeToLoadLayout);

        prefUtil = new PrefUtil(getContext());

        return mSwipeToLoadLayout;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (mHoldForTransition) {
            getActivity().supportPostponeEnterTransition();
        }
        Logger.d(LOG_TAG, "onActivityCreated " + mChannelName);
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        //判断Fragment中的ListView时候存在，判断该Fragment时候已经正在前台显示  通过这两个判断，就可以知道什么时候去加载数据了
        mIsFragmentVisable = isVisibleToUser;
        Logger.d(LOG_TAG, "mfragmentIsVisible is " + isVisibleToUser + " " + mChannelName);
        super.setUserVisibleHint(isVisibleToUser);

        //当视图未加载完则返回
        if (!mIsFragmentPrepared) {
            return;
        }

        //当Fragment可见时
        if (mIsFragmentVisable && getContext() != null) {
            //注册pref监听器
            PreferenceManager.getDefaultSharedPreferences(getContext())
                    .registerOnSharedPreferenceChangeListener(ListFragment.this);

            //自动刷新
            autoRefresh();

            //当需要自动选择item时
            if (mAutoSelectView) {
                clickRecyclerViewItem();
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        //卸载pref监听器
        PreferenceManager.getDefaultSharedPreferences(getContext())
                .unregisterOnSharedPreferenceChangeListener(this);
        if (fetchNewDataTask != null && fetchNewDataTask.getStatus() == AsyncTask.Status.RUNNING) {
            fetchNewDataTask.cancel(true);

        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Logger.d(LOG_TAG, "onDestroyView channelName is " + mChannelName);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        // When tablets rotate, the currently selected list item needs to be saved.
        mAdapter.onSaveInstanceState(outState);
        super.onSaveInstanceState(outState);
    }

    public void setInitialSelectedId(long initialSelectedId) {
        //设置自动选择item的id
        mInitialSelectedId = initialSelectedId;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String sChannelNameSelection =
                BestNewsContract.NewsEntry.TABLE_NAME +
                        "." + BestNewsContract.NewsEntry.COLUMN_CHANNELNAME + " = ? ";

        return new CursorLoader(getContext(),
                BestNewsContract.NewsEntry.CONTENT_URI,
                LIST_COLUMNS,
                sChannelNameSelection,
                new String[]{mChannelName},
                BestNewsContract.NewsEntry.COLUMN_PUBDATE + " DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Logger.d(LOG_TAG, "onLoadFinished " + data.getCount());
        int totalItemCount = data.getCount();
        mListModeManager.onUpdateDataFinished(totalItemCount);
        mCursor = data;
        //为适配器设置游标
        mAdapter.setCursor(mCursor);

        if (totalItemCount == 0) {
            if (getActivity() != null) {
                getActivity().supportStartPostponedEnterTransition();
            }
        } else {
            mRecyclerView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    // Since we know we're going to get items, we keep the listener around until
                    // we see Children.
                    if (mRecyclerView.getChildCount() > 0) {
                        mRecyclerView.getViewTreeObserver().removeOnPreDrawListener(this);
                        //自动选择item
                        if (mAutoSelectView) {
                            clickRecyclerViewItem();
                        }
                        if (mHoldForTransition) {
                            if (getActivity() != null) {
                                getActivity().supportStartPostponedEnterTransition();
                            }
                        }
                        return true;
                    }
                    return false;
                }
            });
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.setCursor(null);
    }

    @Override
    public void onClick(long id, ListRecyclerViewAdapter.ViewHolder vh) {
        Uri contentUri = DatabaseUtil.buildNewUri(id);

        if (prefUtil.getIsTwoPane()) {
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.
            Bundle args = new Bundle();
            args.putParcelable(DetailFragment.DETAIL_URI, contentUri);

            DetailFragment fragment = new DetailFragment();
            fragment.setArguments(args);

            getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container_detail, fragment, MainActivity.DETAILFRAGMENT_TAG)
                    .commit();
        } else {
            Intent intent = new Intent(getActivity(), DetailActivity.class)
                    .setData(contentUri);

            //执行动画
            if (vh.mImage != null) {
                ActivityOptionsCompat activityOptions =
                        ActivityOptionsCompat.makeSceneTransitionAnimation(getActivity(),
                                new Pair<View, String>(vh.mImage, getString(R.string.detail_image_transition_name)));
                ActivityCompat.startActivity(getActivity(), intent, activityOptions.toBundle());
            } else {
                ActivityOptionsCompat activityOptions =
                        ActivityOptionsCompat.makeSceneTransitionAnimation(getActivity());
                ActivityCompat.startActivity(getActivity(), intent, activityOptions.toBundle());
            }
        }
    }


    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(getString(R.string.pref_newdata_status_key))) {
            @NewDataUtil.NewDataStatus int status = prefUtil.getNewDataStatus();
            Logger.d(LOG_TAG, "onSharedPrefChanged " + status + " channelName is " + mChannelName);
            //当服务器出现错误时，更新SwipeToLayout的状态
            if (status != NEWDATA_STATUS_UNKNOWN && status != NEWDATA_STATUS_OK) {
                mListModeManager.onDataStatusError();
                updateSnackbar();
            }
        }
    }

    /**
     * 更新并弹出Snackbar，以提示用户服务器的状态
     */
    private void updateSnackbar() {
        if (getActivity() != null) {
            int message = -1;

            if (mCursor.getCount() == 0) {
                message = R.string.error_list_no_item;
            }
            @NewDataUtil.NewDataStatus int status = prefUtil.getNewDataStatus();
            switch (status) {
                case NEWDATA_STATUS_SERVER_DOWN:
                    message = R.string.error_list_server_down;
                    break;
                case NEWDATA_STATUS_SERVER_INVALID:
                    message = R.string.error_list_server_error;
                    break;
                case NEWDATA_STATUS_INVALID:
                    message = R.string.error_list_invalid;
                    break;
                case NEWDATA_STATUS_OK:
                    break;
                case NEWDATA_STATUS_UNKNOWN:
                    break;
            }

            //如果是没有网络，优先显示网络错误
            if (!new NetworkUtil(getContext()).getConnectivityStatus()) {
                message = R.string.error_list_no_network;
            }
            if (message != -1) {
                SnackbarUtil.show(mSwipeToLoadLayout, getResources().getString(message));
            }
        }
    }

    /**
     * 用于RecyclerView的自动点击
     */
    private void clickRecyclerViewItem() {
        int position = mAdapter.getSelectedItemPosition();
        if (position == RecyclerView.NO_POSITION &&
                -1 != mInitialSelectedId) {
            Cursor data = mAdapter.getCursor();
            int count = data.getCount();
            int idColumn = data.getColumnIndex(BestNewsContract.NewsEntry._ID);
            for (int i = 0; i < count; i++) {
                data.moveToPosition(i);
                if (data.getLong(idColumn) == mInitialSelectedId) {
                    position = i;
                    break;
                }
            }
        }
        if (position == RecyclerView.NO_POSITION) position = 0;
        // If we don't need to restart the loader, and there's a desired position to restore
        // to, do so now.
        mRecyclerView.smoothScrollToPosition(position);
        RecyclerView.ViewHolder vh = mRecyclerView.findViewHolderForAdapterPosition(position);
        if (null != vh && mIsFragmentVisable) {
            mAdapter.selectView(vh);
        }
    }

    /**
     * 下拉刷新时执行刷新任务
     */
    @Override
    public void onRefresh() {
        mSwipeToLoadLayout.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mIsFragmentVisable) {
                    if (fetchNewDataTask != null &&
                            fetchNewDataTask.getStatus() == AsyncTask.Status.RUNNING) {
                        fetchNewDataTask.cancel(true);
                    }

                    //检查网络
                    if (!new NetworkUtil(getContext()).getConnectivityStatus()) {
                        mSwipeToLoadLayout.setRefreshing(false);
                        SnackbarUtil.show(mSwipeToLoadLayout,
                                getResources().getString(R.string.error_list_no_network));
                        return;
                    }

                    //执行下载任务
                    fetchNewDataTask = new FetchNewDataTask(getContext());
                    fetchNewDataTask.execute(mChannelName, Integer.toString(1), "refresh");
                    Logger.d(LOG_TAG, "execute fetchNewDataTask");

                    mListModeManager.setMode(SWIPETOLOADLAYOUT_REFRESH);
                }
            }
        }, 2000);
    }

    /**
     * 下拉刷新时执行加载更多数据
     */
    @Override
    public void onLoadMore() {
        mSwipeToLoadLayout.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (fetchNewDataTask != null &&
                        fetchNewDataTask.getStatus() == AsyncTask.Status.RUNNING) {
                    fetchNewDataTask.cancel(true);
                }

                //检查网络
                if (!new NetworkUtil(getContext()).getConnectivityStatus()) {
                    mSwipeToLoadLayout.setLoadingMore(false);
                    SnackbarUtil.show(mSwipeToLoadLayout,
                            getResources().getString(R.string.error_list_no_network));
                    return;
                }

                //执行下载任务
                fetchNewDataTask = new FetchNewDataTask(getContext());
                fetchNewDataTask.execute(mChannelName, Integer.toString(mListModeManager.getCurrentPage()), "load");
                Logger.d(LOG_TAG, "loading more currentPage is " + mListModeManager.getCurrentPage());

                mListModeManager.setMode(SWIPETOLOADLAYOUT_LOAD);
            }
        }, 2000);
    }

    //切换Fragment时自动刷新
    private void autoRefresh() {
        Logger.d(LOG_TAG, "onRefresh autoRefresh");
        mSwipeToLoadLayout.post(new Runnable() {
            @Override
            public void run() {
                mSwipeToLoadLayout.setRefreshing(true);
            }
        });
    }
}
