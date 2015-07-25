package in.codeseed.tubely.activities;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnPageChange;
import in.codeseed.tubely.R;
import in.codeseed.tubely.util.Util;

public class WelcomeActivity extends BaseActivity {

    @Bind(R.id.indicator_one) TextView indicatorOne;
    @Bind(R.id.indicator_two) TextView indicatorTwo;
    @Bind(R.id.indicator_three) TextView indicatorThree;
    @Bind(R.id.indicator_four) TextView indicatorFour;
    @Bind(R.id.welcome_viewpager) ViewPager mViewPager;
    SectionsPagerAdapter mSectionsPagerAdapter;
    private SharedPreferences mSharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        ButterKnife.bind(this);

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setPageTransformer(true, new DepthPageTransformer());
    }

    @OnClick(R.id.lets_start_button)
    public void closeWelcomeActivity(View view){
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putBoolean(Util.SHARED_PREF_FIRST_TIME_WELCOME, false);
        editor.apply();
        finish();
    }

    @OnPageChange(R.id.welcome_viewpager)
    public void onPageSelected(int position) {
        indicatorOne.setBackgroundColor(getResources().getColor(R.color.colorAccent));
        indicatorTwo.setBackgroundColor(getResources().getColor(R.color.colorAccent));
        indicatorThree.setBackgroundColor(getResources().getColor(R.color.colorAccent));
        indicatorFour.setBackgroundColor(getResources().getColor(R.color.colorAccent));
        switch (position){
            case 0:
                indicatorOne.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                break;
            case 1:
                indicatorTwo.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                break;
            case 2:
                indicatorThree.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                break;
            case 3:
                indicatorFour.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                break;
        }
    }

    public static class PlaceholderFragment extends Fragment {
        private static final String ARG_SECTION_NUMBER = "section_number";

        public PlaceholderFragment() {
        }

        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstancmeState) {

            int temp = getArguments().getInt(ARG_SECTION_NUMBER);
            View rootView;
            if (temp == 1)
                rootView = inflater.inflate(R.layout.fragment_welcome_page1, container, false);
            else if (temp == 2)
                rootView = inflater.inflate(R.layout.fragment_welcome_page2, container, false);
            else if (temp == 3)
                rootView = inflater.inflate(R.layout.fragment_welcome_page3, container, false);
            else
                rootView = inflater.inflate(R.layout.fragment_welcome_page4, container, false);

            return rootView;
        }
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return PlaceholderFragment.newInstance(position + 1);
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 4;
        }
    }

    public class DepthPageTransformer implements ViewPager.PageTransformer {
        private static final float MIN_SCALE = 0.75f;

        public void transformPage(View view, float position) {
            int pageWidth = view.getWidth();

            if (position < -1) { // [-Infinity,-1)
                // This page is way off-screen to the left.
                view.setAlpha(0);

            } else if (position <= 0) { // [-1,0]
                // Use the default slide transition when moving to the left page
                view.setAlpha(1);
                view.setTranslationX(0);
                view.setScaleX(1);
                view.setScaleY(1);

            } else if (position <= 1) { // (0,1]
                // Fade the page out.
                view.setAlpha(1 - position);

                // Counteract the default slide transition
                view.setTranslationX(pageWidth * -position);

                // Scale the page down (between MIN_SCALE and 1)
                float scaleFactor = MIN_SCALE
                        + (1 - MIN_SCALE) * (1 - Math.abs(position));
                view.setScaleX(scaleFactor);
                view.setScaleY(scaleFactor);

            } else { // (1,+Infinity]
                // This page is way off-screen to the right.
                view.setAlpha(0);
            }
        }
    }
}
