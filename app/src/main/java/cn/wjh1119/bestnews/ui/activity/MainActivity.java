package cn.wjh1119.bestnews.ui.activity;

import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import java.util.ArrayList;
import java.util.List;

import cn.wjh1119.bestnews.R;
import cn.wjh1119.bestnews.sync.BestNewsSyncAdapter;
import cn.wjh1119.bestnews.ui.fragment.DetailFragment;
import cn.wjh1119.bestnews.ui.fragment.ListFragment;
import cn.wjh1119.bestnews.util.DatabaseUtil;
import cn.wjh1119.bestnews.util.FileUtil;
import cn.wjh1119.bestnews.util.Logger;
import cn.wjh1119.bestnews.util.PrefUtil;
import cn.wjh1119.bestnews.util.SnackbarUtil;

/**
 * 主界面
 *@author WJH
 *created at 2017/4/16 0016
 */


public class MainActivity extends AppCompatActivity {

    final String LOG_TAG = getClass().getSimpleName();
    public static final String DETAILFRAGMENT_TAG = "DFTAG";
    private ViewPager mViewPager;
    private MainViewPagerFragmentAdapter mMainViewPagerFragmentAdapter;
    private PrefUtil mPrefUtil;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Uri contentUri = getIntent() != null ? getIntent().getData() : null;

        mPrefUtil = new PrefUtil(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_detail);
        setSupportActionBar(toolbar);

        boolean isTwoPane;
        if (findViewById(R.id.container_detail) != null) {
            // The detail container view will be present only in the large-screen layouts
            // (res/layout-sw600dp). If this view is present, then the activity should be
            // in two-pane mode.
            isTwoPane = true;
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.
            if (savedInstanceState == null) {
                DetailFragment fragment = new DetailFragment();
                if (contentUri != null) {
                    Bundle args = new Bundle();
                    args.putParcelable(DetailFragment.DETAIL_URI, contentUri);
                    fragment.setArguments(args);
                }
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.container_detail, fragment, DETAILFRAGMENT_TAG)
                        .commit();
            }
        } else {
            isTwoPane = false;
            getSupportActionBar().setElevation(0f);
        }
        //将是否双屏储存值SharedPreference中
        mPrefUtil.setIsTwoPane(isTwoPane);

        //加载mViewPager
        mViewPager = (ViewPager) findViewById(R.id.container_list);
        initViewPager();

        //获取当前viewPager中的Fragment
        ListFragment currentFragment = (ListFragment) mMainViewPagerFragmentAdapter.getCurrentFragment();

        //若存在contentUri，则点击该item
        if (contentUri != null) {
            currentFragment.setInitialSelectedId(
                    DatabaseUtil.getIdFromUri(contentUri));
        }

        BestNewsSyncAdapter.initializeSyncAdapter(this);

        mPrefUtil = new PrefUtil(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.action_delete_cache:
                if (FileUtil.getInstance(this).deleteFile()){
                    SnackbarUtil.show(mViewPager, this.getString(R.string.action_delete_cache_hint_success));
                }else{
                    SnackbarUtil.show(mViewPager, this.getString(R.string.action_delete_cache_hint_fail));
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * 初始化ViewPager
     */
    private void initViewPager() {
        TabLayout mTabLayout = (TabLayout) findViewById(R.id.tabs_main);
        List<String> titles = new ArrayList<>();
        ArrayList<String> channels = mPrefUtil.getBestNewsChannels();
        Logger.d(LOG_TAG,"channelsArray is " + channels.toString());

        int numberOfChannel = channels.size();

        if (numberOfChannel == Integer.parseInt(this.getString(R.string.pref_channel_initialized_count))){
            SnackbarUtil.show(mViewPager, this.getString(R.string.pref_channel_initialized_hint));
        }
        for(int i = 0; i < numberOfChannel; i++) {
            String channelName = channels.get(i);
            titles.add(channelName);
            mTabLayout.addTab(mTabLayout.newTab().setText(channelName));
        }

        List<Fragment> fragments = new ArrayList<>();
        for(int i=0;i<titles.size();i++){
            fragments.add(ListFragment.newInstance(i));
        }
        mMainViewPagerFragmentAdapter =
                new MainViewPagerFragmentAdapter(getSupportFragmentManager(), fragments, titles);
        //给ViewPager设置适配器
        mViewPager.setAdapter(mMainViewPagerFragmentAdapter);
        //将TabLayout和ViewPager关联起来。
        mTabLayout.setupWithViewPager(mViewPager);
        //给TabLayout设置适配器
        mTabLayout.setTabsFromPagerAdapter(mMainViewPagerFragmentAdapter);
    }
}
