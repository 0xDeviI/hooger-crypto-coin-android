package com.hoogercoin.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.hoogercoin.fragments.splash.FirstFragment;
import com.hoogercoin.fragments.splash.SecondFragment;
import com.hoogercoin.fragments.splash.ThirdFragment;

public class SplashViewPagerAdapter extends FragmentStateAdapter {
    public SplashViewPagerAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle) {
        super(fragmentManager, lifecycle);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position){
            case 0:
                return new FirstFragment();
            case 1:
                return new SecondFragment();
            case 2:
                return new ThirdFragment();
        }
        return null;
    }

    @Override
    public int getItemCount() {
        return 3;
    }
}
