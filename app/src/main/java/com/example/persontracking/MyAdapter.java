package com.example.persontracking;

import android.content.Context;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.fragment.app.FragmentManager;

class MyAdapter extends FragmentPagerAdapter {

    Context context;
    int totalTabs;
    public MyAdapter(Context c, FragmentManager fm, int totalTabs) {
        super(fm);
        context = c;
        this.totalTabs = totalTabs;
    }
    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                PersonImage personImageFragment = new PersonImage();
                return personImageFragment;
            case 1:
                TrackingPerson trackingPersonFragment = new TrackingPerson();
                return trackingPersonFragment;

            default:
                return null;
        }
    }
    @Override
    public int getCount() {
        return totalTabs;
    }

}