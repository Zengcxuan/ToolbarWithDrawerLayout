package com.tcl.easybill.ui.adapter;

import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import com.tcl.easybill.ui.fragment.bill_Fragment;


public class ViewPagerAdapter extends FragmentStatePagerAdapter {

    private List<Fragment> mFragmentList;
    private int mChildCount = 0;

    @Override
    public void notifyDataSetChanged() {
        mChildCount = getCount();
        super.notifyDataSetChanged();
    }

    @Override
    public int getItemPosition(@NonNull Object object) {
        /**
         * resolve adapter.notifyDataSetChanged() adiaphoria
         * just reload bill_fragment
         */
        if (object.getClass().getName().equals(bill_Fragment.class.getName())
                || object.getClass().getName().equals(bill_Fragment.class.getName())) {
            return POSITION_NONE;
        }

        return super.getItemPosition(object);
    }

    public ViewPagerAdapter(FragmentManager manager) {
        super(manager);
        mFragmentList = new ArrayList<>();
    }

    @Override
    public Fragment getItem(int position) {
        return mFragmentList.get(position);
    }

    @Override
    public int getCount() {
        return mFragmentList.size();
    }

    public void addFragment(Fragment fragment) {
        mFragmentList.add(fragment);
    }


}
