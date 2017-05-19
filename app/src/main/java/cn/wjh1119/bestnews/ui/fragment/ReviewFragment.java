package cn.wjh1119.bestnews.ui.fragment;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.wjh1119.bestnews.R;
import cn.wjh1119.bestnews.asynctask.FetchReviewDataFromSqlTask;
import cn.wjh1119.bestnews.bean.ReviewBean;
import cn.wjh1119.bestnews.data.BestNewsContract;
import cn.wjh1119.bestnews.view.UnScrollListView;

/**
 * ReviewFragment
 * Created by Mr.King on 2017/4/12 0012.
 */

public class ReviewFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{

    static final String REVIEW_URI = "URI";
    private static final int REVIEW_LOADER = 0;
    private Uri mUri;
    private ReviewListViewAdapter mAdapter;

    @BindView(R.id.tv_review_name)
    TextView mNameView;

    @BindView(R.id.uslv_review)
    UnScrollListView mListView;

    //ListFragment中使用的列
    private static final String[] REVIEW_COLUMNS = {
            BestNewsContract.NewsEntry.TABLE_NAME + "." +
            BestNewsContract.NewsEntry.COLUMN_REVIEW,

    };
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_review, container, false);

        Bundle arguments = getArguments();
        if (arguments != null) {
            mUri = arguments.getParcelable(REVIEW_URI);
        }

        ButterKnife.bind(this, rootView);

        mAdapter = new ReviewListViewAdapter(getActivity());
        mListView.setAdapter(mAdapter);

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        //加载Loader
        getLoaderManager().initLoader(REVIEW_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // Now create and return a CursorLoader that will take care of
        // creating a Cursor for the data being displayed.
        if ( null != mUri ) {
            return new CursorLoader(
                    getActivity(),
                    mUri,
                    REVIEW_COLUMNS,
                    null,
                    null,
                    null
            );
        }
        return null;
    }


    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        if (!data.moveToFirst()) { return; }

        FetchReviewDataFromSqlTask fetchReviewDataFromSqlTask = new FetchReviewDataFromSqlTask(getContext());
        fetchReviewDataFromSqlTask.setOnDataFinishedListener(new FetchReviewDataFromSqlTask.OnDataFinishedListener(){
            @Override
            public void onDataSuccessfully(ArrayList<ReviewBean> data) {
                mAdapter.setData(data);
                mNameView.setText("评论");
            }

            @Override
            public void onDataFailed() {

            }
        });
        fetchReviewDataFromSqlTask.execute(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
