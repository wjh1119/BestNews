package cn.wjh1119.bestnews.ui.fragment;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.wjh1119.bestnews.R;
import cn.wjh1119.bestnews.bean.ReviewBean;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * 评论列表的适配器
 * Created by Mr.King on 2017/2/1 0001.
 */

class ReviewListViewAdapter extends BaseAdapter {

    private Context mContext;
    private ArrayList<ReviewBean> mData = null;

    ReviewListViewAdapter(Context c) {
        super();
        this.mContext = c;
    }

    //获取评论数量
    public int getCount() {
        int count = 0;
        if (mData != null) {
            count = mData.size();
        }
        return count;
    }

    public Object getItem(int position) {
        return mData.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    public void setData(ArrayList<ReviewBean> data) {
        this.mData = data;
        notifyDataSetChanged();
    }

    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder viewHolder;

        if (convertView == null){
            LayoutInflater mInflater = LayoutInflater.from(mContext);
            convertView = mInflater.inflate(R.layout.review_item_all, null);
            viewHolder = new ViewHolder(convertView);

            convertView.setTag(viewHolder);
        }else{
            viewHolder = (ViewHolder) convertView.getTag();
        }

        ReviewBean review = mData.get(position);

        viewHolder.mImage.setImageResource(R.mipmap.review_portrait);
        viewHolder.mNumber.setText(mContext.getString(R.string.review_number_text,
                String.valueOf(position+1)));
        viewHolder.mAuthor.setText(review.author);
        viewHolder.mSite.setText(review.site);
        viewHolder.mDate.setText(review.date);
        viewHolder.mContent.setText(review.content);

        return convertView;
    }

     class ViewHolder {

         @BindView(R.id.tv_review_item_number)
         TextView mNumber;

         @BindView(R.id.tv_review_item_author)
         TextView mAuthor;

         @BindView(R.id.tv_review_item_site)
         TextView mSite;

         @BindView(R.id.tv_review_item_date)
         TextView mDate;

         @BindView(R.id.tv_review_item_content)
         TextView mContent;

         @BindView(R.id.civ_review_item_image)
         CircleImageView mImage;


        ViewHolder(View view) {
            ButterKnife.bind(this,view);
        }
    }
}
