package in.codeseed.tubely.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import butterknife.Bind;
import butterknife.ButterKnife;
import in.codeseed.tubely.R;
import in.codeseed.tubely.fragments.DetailedTubeStatusFragment;
import in.codeseed.tubely.fragments.TubeStationsFragment;
import in.codeseed.tubely.pojos.Tube;

public class StatusAndStationsActivity extends BaseActivity implements ActionBar.TabListener {

    private static Tube tube;
    private static String twitterHandle;
    @Bind(R.id.status_stations_viewpager)
    ViewPager mViewPager;
    private SectionsPagerAdapter mSectionsPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status_and_stations);
        ButterKnife.bind(this);
        //Get data
        Bundle bundle = getIntent().getBundleExtra("data");
        tube = (Tube) bundle.getSerializable("tube_data");
        twitterHandle = getIntent().getStringExtra("twitter_handle");

        if(tube == null)
            Log.d("NULL DATA", "tube is null");

        // Set up the action bar.
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(tube.getName());

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                actionBar.setSelectedNavigationItem(position);
            }
        });

        // For each of the sections in the app, add a tab to the action bar.
        for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
            actionBar.addTab(
                    actionBar.newTab()
                            .setText(mSectionsPagerAdapter.getPageTitle(i))
                            .setTabListener(this));
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.status_and_stations, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){
            case R.id.action_tweet:
                createTweet();
                return true;
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);

        }
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        // When the given tab is selected, switch to the corresponding page in
        // the ViewPager.
        mViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    public void createTweet(){
        Intent tweetIntent = new Intent();
        tweetIntent.setAction(Intent.ACTION_SEND);
        tweetIntent.setType("text/plain");
        tweetIntent.putExtra(Intent.EXTRA_TEXT, "@"+twitterHandle + " ");
        startActivity(Intent.createChooser(tweetIntent, "Choose your twitter app.."));
    }

    public static class SectionsPagerAdapter extends android.support.v4.app.FragmentPagerAdapter {

        private TubeStationsFragment tubeStationsFragment;
        private DetailedTubeStatusFragment twitterStreamFragment;

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    twitterStreamFragment = new DetailedTubeStatusFragment();
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("data", tube);
                    bundle.putString("twitter_handle", twitterHandle);
                    twitterStreamFragment.setArguments(bundle);
                    return twitterStreamFragment;
                case 1:
                    tubeStationsFragment = new TubeStationsFragment();
                    Bundle bundle1 = new Bundle();
                    bundle1.putString("line_name", tube.getName());
                    tubeStationsFragment.setArguments(bundle1);
                    return tubeStationsFragment;
            }
            return null;
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {

            switch (position) {
                case 0:
                    return "STATUS";
                case 1:
                    return "STATIONS";
            }
            return null;
        }
    }

}
