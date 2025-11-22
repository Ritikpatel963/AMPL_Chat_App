package com.agromarket.ampl_chat.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.agromarket.ampl_chat.fragments.SplashFragment1;
import com.agromarket.ampl_chat.fragments.SplashFragment2;
import com.agromarket.ampl_chat.fragments.SplashFragment3;

public class OnboardingAdapter extends FragmentStateAdapter {

    public OnboardingAdapter(@NonNull FragmentActivity fa) {
        super(fa);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0: return new SplashFragment1();
            case 1: return new SplashFragment2();
            case 2: return new SplashFragment3();
        }
        return new SplashFragment1();
    }

    @Override
    public int getItemCount() {
        return 3;
    }
}