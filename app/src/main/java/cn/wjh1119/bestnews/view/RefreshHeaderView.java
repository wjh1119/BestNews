package cn.wjh1119.bestnews.view;

import android.content.Context;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;

import com.aspsine.swipetoloadlayout.SwipeRefreshTrigger;
import com.aspsine.swipetoloadlayout.SwipeTrigger;

import cn.wjh1119.bestnews.R;

/**
 * RecyclerView的head
 * Created by Mr.King on 2017/4/11 0011.
 */

public class RefreshHeaderView extends AppCompatTextView implements SwipeRefreshTrigger, SwipeTrigger {

    public RefreshHeaderView(Context context) {
        super(context);
    }

    public RefreshHeaderView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void onRefresh() {
        setText(R.string.refresh_header_view_onrefresh);
    }

    @Override
    public void onPrepare() {
        setText("");
    }

    @Override
    public void onMove(int yScrolled, boolean isComplete, boolean automatic) {
        if (!isComplete) {
            if (yScrolled >= getHeight()) {
                setText(R.string.refresh_header_view_release);
            } else {
                setText(R.string.refresh_header_view_swipe);
            }
        } else {
            setText(R.string.refresh_header_view_return);
        }
    }

    @Override
    public void onRelease() {
    }

    @Override
    public void onComplete() {
        setText(R.string.refresh_header_view_complete);
    }

    @Override
    public void onReset() {
        setText("");
    }
}