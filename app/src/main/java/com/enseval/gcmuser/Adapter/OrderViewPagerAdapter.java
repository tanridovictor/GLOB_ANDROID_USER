package com.enseval.gcmuser.Adapter;

import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.Log;

import java.util.ArrayList;

public class OrderViewPagerAdapter extends FragmentPagerAdapter {
    private ArrayList<Fragment> fragmentList = new ArrayList<>();
    private ArrayList<String> FragmentListTitles = new ArrayList<>();

    public OrderViewPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int i) {
        return fragmentList.get(i);
    }

    @Override
    public int getCount() {
        return FragmentListTitles.size();
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return FragmentListTitles.get(position);
    }

    /**Method untuk menambahkan sub-fragment*/
    public void addFragment(Fragment fragment, String title){
        fragmentList.add(fragment);
        FragmentListTitles.add(title);
        Log.d("", "OrderViewPagerAdapter: "+fragmentList.size());
    }
}
