package com.tcl.easybill.ui.activity;




import android.app.Dialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.oguzdev.circularfloatingactionmenu.library.FloatingActionButton;
import com.oguzdev.circularfloatingactionmenu.library.FloatingActionMenu;
import com.oguzdev.circularfloatingactionmenu.library.SubActionButton;

import java.util.List;

import butterknife.BindView;
import com.tcl.easybill.R;
import com.tcl.easybill.Utils.DateUtils;
import com.tcl.easybill.Utils.ProgressUtils;
import com.tcl.easybill.Utils.SharedPUtils;
import com.tcl.easybill.Utils.ToastUtils;
import com.tcl.easybill.base.BmobRepository;
import com.tcl.easybill.base.Constants;
import com.tcl.easybill.base.LocalRepository;
import com.tcl.easybill.pojo.AllSortBill;
import com.tcl.easybill.pojo.SortBill;
import com.tcl.easybill.ui.MyViewPager;
import com.tcl.easybill.ui.adapter.ViewPagerAdapter;
import com.tcl.easybill.ui.fragment.bill_Fragment;
import com.tcl.easybill.ui.fragment.chart_Fragment;
import com.tcl.easybill.ui.fragment.mine_Fragment;

import static android.view.Gravity.CENTER;
import static com.tcl.easybill.Utils.DateUtils.FORMAT_M;
import static com.tcl.easybill.Utils.DateUtils.FORMAT_Y;


public class HomeActivity extends BaseActivity  {

   /* private MyViewPager viewPager;*/
    private MenuItem menuItem;
    private static final int RESULTCODE =0;
    /*private BottomNavigationView bottomNavigationView;*/
    private bill_Fragment bill_fFragment;
    private chart_Fragment chart_fragment;
    private mine_Fragment mine_fragment;
    private long mExitTime;
    ViewPagerAdapter adapter;
    @BindView(R.id.viewpager)
    MyViewPager viewPager ;
    @BindView(R.id.bottom_navigation)
    BottomNavigationView bottomNavigationView;

    @Override
    protected int getLayout() {
        return R.layout.activity_home;
    }

    @Override
    protected void initEventAndData() {
        Log.e(TAG, "initEventAndData: " );
        TAG = "meng111";
        /*FloatingActionButton*/
        initFab();
        // it is the first the user open this APP
        if(SharedPUtils.isFirstStart(mContext)){
            Log.i(TAG,"第一次进入将默认账单分类添加到数据库");
            AllSortBill note= new Gson().fromJson(Constants.BILL_NOTE, AllSortBill.class);
            List<SortBill> sorts=note.getOutSortList();
            sorts.addAll(note.getInSortList());
            LocalRepository.getInstance().saveBsorts(sorts);
            LocalRepository.getInstance().saveBPays(note.getPayinfo());
        }
        viewPager.setOffscreenPageLimit(5);
        bottomNavigationView.setOnNavigationItemSelectedListener(
                new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.item_bill:
                                viewPager.setCurrentItem(0);
                                break;
                            case R.id.item_chart:
                                viewPager.setCurrentItem(1);
                                break;
                            case R.id.item_mine:
                                viewPager.setCurrentItem(2);
                                break;
                        }
                        return false;
                    }
                });


        bottomNavigationView.setItemIconTintList(null);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }
            @Override
            public void onPageSelected(int position) {
                menuItem = bottomNavigationView.getMenu().getItem(position);
                menuItem.setChecked(true);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }


        });

        setupViewPager(viewPager);
    }



    //fragment change
    private void setupViewPager(ViewPager viewPager) {
        Log.e(TAG, "setupViewPager: " );
        adapter= new ViewPagerAdapter(getSupportFragmentManager());
        bill_fFragment = new bill_Fragment();
        chart_fragment = new chart_Fragment();
        mine_fragment = new mine_Fragment();
        adapter.addFragment(bill_fFragment);
        adapter.addFragment(chart_fragment);
        adapter.addFragment(mine_fragment);
        viewPager.setAdapter(adapter);
    }

    /**
     * add suspend button
     */
    private void initFab(){
        /*parent button*/
        FloatingActionButton floatingActionButton = new FloatingActionButton.Builder(this).build();
        floatingActionButton.setBackground(mContext.getDrawable(R.drawable.add_bill3));
        /*item button*/
        SubActionButton.Builder itemBuilder = new SubActionButton.Builder(this);
        SubActionButton addbill = itemBuilder.build();
        addbill.setBackground(mContext.getDrawable(R.drawable.add_icon));
        SubActionButton refreshbill = itemBuilder.build();
        refreshbill.setBackground(mContext.getDrawable(R.drawable.refresh_icon));

        /*set FloatingActionMenu*/
        final FloatingActionMenu actionMenu = new FloatingActionMenu.Builder(this)
                .addSubActionView(addbill)
                .addSubActionView(refreshbill)
                .attachTo(floatingActionButton)
                .build();
        /*size and locate*/
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(180, 180);
        params.setMargins(0, 0, 0, 100);
        floatingActionButton.setPosition(4, params);

        /*onClick react*/
        addbill.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ProgressUtils.show(HomeActivity.this, "正在加载...");
                /*open BillAddActivity*/
                Intent intent = new Intent(HomeActivity.this, BillAddActivity.class);
                startActivityForResult(intent,RESULTCODE);
                actionMenu.close(true);

            }
        });
        refreshbill.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*refresh the data*/
                Log.e(TAG, "onClick: " );
                BmobRepository.getInstance().syncBill(currentUser.getObjectId());
                actionMenu.close(true);
            }
        });
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.e(TAG, "onActivityResult: " );
        super.onActivityResult(requestCode, resultCode, data);
        adapter.notifyDataSetChanged();
        ProgressUtils.dismiss();

    }

    /**
     * call the exit() when press the back twice
     * @param keyCode
     * @param event
     * @return
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            exit();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * finish the app
     */
    private void exit() {
        if ((System.currentTimeMillis() - mExitTime) > 2000) {
            ToastUtils.show(mContext, "再按一次退出应用");
            mExitTime = System.currentTimeMillis();
        } else {
            //用户退出处理
            finish();
            System.exit(0);
        }
    }
}
