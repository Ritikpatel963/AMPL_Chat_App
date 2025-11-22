package com.agromarket.ampl_chat;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.agromarket.ampl_chat.adapters.OnboardingAdapter;

public class OnboardingActivity extends AppCompatActivity {

    private ViewPager2 viewPager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

        // Initialize ViewPager
        viewPager = findViewById(R.id.viewPager);

        // Set Adapter
        OnboardingAdapter adapter = new OnboardingAdapter(this);
        viewPager.setAdapter(adapter);

        // Enable swipe navigation
        viewPager.setUserInputEnabled(true);
    }
}
