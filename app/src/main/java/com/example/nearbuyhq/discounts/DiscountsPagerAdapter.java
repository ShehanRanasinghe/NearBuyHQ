package com.example.nearbuyhq.discounts;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

// ViewPager2 adapter for DiscountsActivity – position 0 returns PromotionsFragment, position 1 returns DealsFragment.
public class DiscountsPagerAdapter extends FragmentStateAdapter {

    public DiscountsPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (position == 0) {
            return new PromotionsFragment();
        } else {
            return new DealsFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}

