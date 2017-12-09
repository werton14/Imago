package com.imago.imago;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.ListFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by werton on 09.12.17.
 */

public class CustomFragmentPageAdapter extends FragmentPagerAdapter {

    private List<Fragment> fragments = new ArrayList<>();

    public CustomFragmentPageAdapter(FragmentManager fragmentManager) {
        super(fragmentManager);

        fragments.add(ImagesFragment.newInstance());
        fragments.add(TaskFragment.newInstance());
        fragments.add(LeadersFragment.newInstance());
    }

    @Override
    public Fragment getItem(int position) {
        return fragments.get(position);
    }

    @Override
    public int getCount() {
        return fragments.size();
    }

}
