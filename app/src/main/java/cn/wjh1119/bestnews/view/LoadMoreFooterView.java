package cn.wjh1119.bestnews.view;

import android.content.Context;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;

import com.aspsine.swipetoloadlayout.SwipeLoadMoreTrigger;
import com.aspsine.swipetoloadlayout.SwipeTrigger;

import cn.wjh1119.bestnews.R;


/**
 * RecyclerView的FooterView
 * Created by Mr.King on 2017/4/11 0011.
 */

public class LoadMoreFooterView extends AppCompatTextView implements SwipeTrigger, SwipeLoadMoreTrigger {
    public LoadMoreFooterView(Context context) {
        super(context);
    }

    public LoadMoreFooterView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void onLoadMore() {
        setText(R.string.load_more_footer_view_onloadmore);
    }

    @Override
    public void onPrepare() {
        setText("");
    }

    @Override
    public void onMove(int yScrolled, boolean isComplete, boolean automatic) {
        if (!isComplete) {
            if (yScrolled <= -getHeight()) {
                setText(R.string.load_more_footer_view_release);
            } else {
                setText(R.string.load_more_footer_view_swipe);
            }
        } else {
            setText(R.string.load_more_footer_view_return);
        }
    }

    @Override
    public void onRelease() {
        setText(R.string.load_more_footer_view_onrelease);
    }

    @Override
    public void onComplete() {
        setText(R.string.load_more_footer_view_complete);
    }

    @Override
    public void onReset() {
        setText("");
    }
}
