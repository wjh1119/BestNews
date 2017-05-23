package cn.wjh1119.bestnews.ui.fragment;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.wjh1119.bestnews.R;
import cn.wjh1119.bestnews.data.BestNewsContract;
import cn.wjh1119.bestnews.util.ImageManager;
import cn.wjh1119.bestnews.util.Logger;
import cn.wjh1119.bestnews.util.PrefUtil;

/**
 * RecyclerView的适配器
 */

class ListRecyclerViewAdapter extends RecyclerView.Adapter<ListRecyclerViewAdapter.ViewHolder> {

    private final String LOG_TAG = getClass().getSimpleName();

    private static final int VIEW_TYPE_FIRST = 0;
    private static final int VIEW_TYPE_OTHER = 1;
    private static final int VIEW_TYPE_FIRST_NO_PICTURE = 2;
    private static final int VIEW_TYPE_OTHER_NO_PICTURE = 3;

    private Context mContext;
    private Cursor mCursor;
    private final NewAdapterOnClickHandler clickHandler;
    final private ListItemChoiceManager mICM;
    private ImageManager mImageManager;
    private PrefUtil mPrefUtil;

    ListRecyclerViewAdapter(Context mContext, NewAdapterOnClickHandler clickHandler, int choiceMode) {
        this.mContext = mContext;
        this.clickHandler = clickHandler;
        mICM = new ListItemChoiceManager(this);
        mICM.setChoiceMode(choiceMode);
        mImageManager = ImageManager.getSingleton(mContext);
        mPrefUtil = PrefUtil.getInstance(mContext);
    }

    @Override
    public ListRecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (parent instanceof RecyclerView) {
            int layoutId = -1;
            switch (viewType) {
                case VIEW_TYPE_FIRST: {
                    layoutId = R.layout.list_item_new_first;
                    break;
                }
                case VIEW_TYPE_OTHER: {
                    layoutId = R.layout.list_item_new_other;
                    break;
                }
                case VIEW_TYPE_FIRST_NO_PICTURE: {
                    layoutId = R.layout.list_item_new_first_no_picture;
                    break;
                }
                case VIEW_TYPE_OTHER_NO_PICTURE: {
                    layoutId = R.layout.list_item_new_other_no_picture;
                }
            }
            View view = LayoutInflater.from(parent.getContext()).inflate(layoutId, parent, false);
            view.setFocusable(true);
            return new ViewHolder(view);
        } else {
            throw new RuntimeException("Not bound to RecyclerView");
        }
    }

    @Override
    public int getItemViewType(int position) {
        mCursor.moveToPosition(position);
        String imageUrl = mCursor.getString(mCursor.getColumnIndex(BestNewsContract.NewsEntry.COLUMN_IMAGEURL));
        if (imageUrl != null && imageUrl.length() != 0) {
            return (position == 0 && !mPrefUtil.getIsTwoPane()) ?
                    VIEW_TYPE_FIRST : VIEW_TYPE_OTHER;
        } else {
            return (position == 0 && !mPrefUtil.getIsTwoPane()) ?
                    VIEW_TYPE_FIRST_NO_PICTURE : VIEW_TYPE_OTHER_NO_PICTURE;
        }
    }

    void setCursor(Cursor mCursor) {
        this.mCursor = mCursor;

        notifyDataSetChanged();
    }

    Cursor getCursor() {
        return mCursor;
    }

    @Override
    public void onBindViewHolder(final ListRecyclerViewAdapter.ViewHolder holder, int position) {

        mCursor.moveToPosition(position);
        holder.mTitle.setText(mCursor.getString
                (mCursor.getColumnIndex(BestNewsContract.NewsEntry.COLUMN_TITLE)));
        holder.mSource.setText
                (mCursor.getString(mCursor.getColumnIndex(BestNewsContract.NewsEntry.COLUMN_SOURCE)));
        holder.mPubDate.setText
                (mCursor.getString(mCursor.getColumnIndex(BestNewsContract.NewsEntry.COLUMN_PUBDATE)));
        holder.mReviewImage.setImageResource(R.mipmap.ic_review);

        String reviewsDataJsonStr = mCursor
                .getString(mCursor.getColumnIndex(BestNewsContract.NewsEntry.COLUMN_REVIEW));
        try {
            JSONObject reviewsDataJson = new JSONObject(reviewsDataJsonStr);
            JSONArray listArray = reviewsDataJson.getJSONArray("review");
            int numOfReviews = listArray.length();
            holder.mReview.setText(String.valueOf(numOfReviews));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) mContext).getWindowManager()
                .getDefaultDisplay()
                .getMetrics(displayMetrics);
        int width = displayMetrics.widthPixels;// 屏幕宽度（像素）

        String imageUrl = mCursor.getString(mCursor.getColumnIndex(BestNewsContract.NewsEntry.COLUMN_IMAGEURL));
        Logger.d(getClass().getSimpleName(),"imageUrl is "+ imageUrl);

        if (holder.mImage != null) {
            holder.mImage.setImageResource(R.mipmap.picture_loading);
            holder.mImage.setTag(imageUrl);
            ViewGroup.LayoutParams layoutParams = holder.mImage.getLayoutParams();
            if (layoutParams.width == -1) {
                layoutParams.width = width;
                if (mPrefUtil.getIsTwoPane()) {
                    layoutParams.width = width / 2;
                } else {
                    layoutParams.width = width;
                }
                layoutParams.height = layoutParams.width * 2 / 3;
                holder.mImage.setLayoutParams(layoutParams);
            }

            if (imageUrl != null){
                Bitmap bitmap = mImageManager.showCacheBitmap(imageUrl);
                if (bitmap != null){
                    holder.mImage.setImageBitmap(bitmap);

                }else{
                    mImageManager.downloadImage(imageUrl, new ImageManager.onImageLoaderListener() {

                        @Override
                        public void onImageLoader(Bitmap bitmap, String url) {
                            if(bitmap != null){
                                holder.mImage.setImageBitmap(bitmap);
                            }else{
                                holder.mImage.setImageResource(R.mipmap.picture_fail_loading);
                            }
                        }
                    });
                }
            }
        }


        mICM.onBindViewHolder(holder, position);
    }

    @Override
    public int getItemCount() {
        int count = 0;
        if (mCursor != null) {
            count = mCursor.getCount();
        }
        return count;
    }

    void onRestoreInstanceState(Bundle savedInstanceState) {
        mICM.onRestoreInstanceState(savedInstanceState);
    }

    void onSaveInstanceState(Bundle outState) {
        mICM.onSaveInstanceState(outState);
    }

    int getSelectedItemPosition() {
        return mICM.getSelectedItemPosition();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        @BindView(R.id.tv_list_item_title)
        TextView mTitle;

        @BindView(R.id.tv_list_item_pubdate)
        TextView mPubDate;

        @BindView(R.id.tv_list_item_source)
        TextView mSource;

        @BindView(R.id.tv_list_item_content)
        TextView mReview;

        @BindView(R.id.iv_list_item_review_image)
        ImageView mReviewImage;

        ImageView mImage;

        ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
            mImage = (ImageView) view.findViewById(R.id.iv_list_item_image);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int adapterPosition = getAdapterPosition();
            mCursor.moveToPosition(adapterPosition);
            long id = mCursor.getLong(mCursor.getColumnIndex(BestNewsContract.NewsEntry._ID));
            clickHandler.onClick(id, this);
            mICM.onClick(this);
        }
    }

    void selectView(RecyclerView.ViewHolder viewHolder) {
        if (viewHolder instanceof ViewHolder) {
            ViewHolder vfh = (ViewHolder) viewHolder;
            vfh.onClick(vfh.itemView);
            Logger.d(LOG_TAG, "onClick item");
        }
    }

    interface NewAdapterOnClickHandler {
        void onClick(long id, ViewHolder vh);
    }
}