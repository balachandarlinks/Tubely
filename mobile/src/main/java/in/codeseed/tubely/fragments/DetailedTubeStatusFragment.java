package in.codeseed.tubely.fragments;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import in.codeseed.tubely.R;
import in.codeseed.tubely.adapters.DetailedStatusAdapter;
import in.codeseed.tubely.pojos.Tube;
import in.codeseed.tubely.pojos.Tweet;
import in.codeseed.tubely.util.Util;
import twitter4j.Paging;
import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

/**
 * Created by bala on 6/10/14.
 */
public class DetailedTubeStatusFragment extends Fragment {

    private static final String CONSUMER_KEY = "u7zBTno1vdfYCMZq83FIjjHul";
    private static final String CONSUMER_SECRET = "2r4rjbCICIYwSirikTKaGRhuIRC9F5v3atdIxjMEfuJwq4LC8u";
    private static final String ACCESS_TOKEN = "58185250-SAI4FcCe38RbYGGeBq5fhl5CJxqlZ9nc0F4JUM17g";
    private static final String ACCESS_TOKEN_SECRET = "SclsQbfPMyqiZhYP2a3mhl79Kx2VHuZwQQagH9fLNm55C";

    @Bind(R.id.detailed_status_root_layout) FrameLayout mDetailedStatusRootLayout;
    @Bind(R.id.tweetLoader) RelativeLayout mTweetLoader;
    @Bind(R.id.tweetLoaderImageView) ImageView mTweetLoaderImageView;
    @Bind(R.id.twitter_stream_recyclerview) RecyclerView mRecyclerView;

    private DownloadTwitterStatus mDownloadTwitterStatus;
    private List<Tweet> mTweetList = new ArrayList<>();
    private LinearLayoutManager mLinearLayoutManager;

    private DetailedStatusAdapter mDetailedStatusAdapter;
    private Tube mTube;
    private int mPageStart = 1;
    private int mPageEnd = 20;
    private String mTwitterHandle;

    private ObjectAnimator mLoaderAnimator;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_detailed_status, container, false);
        ButterKnife.bind(this, rootView);
        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //Read Input data
        mTube = (Tube)getArguments().getSerializable("data");
        mTwitterHandle = getArguments().getString("twitter_handle");

        //Layout manager for RecyclerView
        mLinearLayoutManager = new LinearLayoutManager(getActivity().getApplicationContext());
        mLinearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);

        mDetailedStatusAdapter = new DetailedStatusAdapter(getActivity().getApplicationContext(), mTube, mTweetList);

        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setLayoutManager(mLinearLayoutManager);
        mRecyclerView.setAdapter(mDetailedStatusAdapter);

        mLoaderAnimator = ObjectAnimator.ofFloat(mTweetLoaderImageView, "rotation", 360);
        mLoaderAnimator.setDuration(600);
        mLoaderAnimator.setInterpolator(new LinearInterpolator());
        mLoaderAnimator.setRepeatCount(ObjectAnimator.INFINITE);

        refreshTweetStatus(mTwitterHandle);
    }

    @OnClick(R.id.tweetLoaderImageView)
    public void reloadTweets(View view){
        refreshTweetStatus(mTwitterHandle);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("tweets_list", (Serializable) mTweetList);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mDownloadTwitterStatus = null;
    }

    private void refreshTweetStatus(String twitterHandle) {
        mLoaderAnimator.start();
        ConnectivityManager connMgr = (ConnectivityManager) getActivity().getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = connMgr.getActiveNetworkInfo();

        if (netInfo != null && netInfo.isConnected()) {
            //Call Async Task
            if (mDownloadTwitterStatus == null || mDownloadTwitterStatus.getStatus() == AsyncTask.Status.FINISHED) {
                mDownloadTwitterStatus = new DownloadTwitterStatus();
                mDownloadTwitterStatus.execute(twitterHandle);
            }
        } else {
            setNetworkError();
            mLoaderAnimator.cancel();
        }
    }

    private void setNetworkError() {
        Snackbar.make(mDetailedStatusRootLayout, R.string.network_error, Snackbar.LENGTH_SHORT)
                .setAction(R.string.retry, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        refreshTweetStatus(mTwitterHandle);
                    }
                })
                .show();
    }

    private class DownloadTwitterStatus extends AsyncTask<String, Void, List<Tweet>> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected List<Tweet> doInBackground(String... users) {
            String tweetHomeUser = users[0];
            Tweet tweet;
            List<Tweet> updatedTweetsList = new ArrayList<>();
            Paging page = new Paging(mPageStart, mPageEnd);
            ConfigurationBuilder cb = new ConfigurationBuilder();
            cb.setDebugEnabled(true)
                    .setOAuthConsumerKey(CONSUMER_KEY)
                    .setOAuthConsumerSecret(CONSUMER_SECRET)
                    .setOAuthAccessToken(ACCESS_TOKEN)
                    .setOAuthAccessTokenSecret(ACCESS_TOKEN_SECRET);
            try {
                TwitterFactory tFactory = new TwitterFactory(cb.build());
                Twitter twitter = tFactory.getInstance();

                for (twitter4j.Status status : twitter.getUserTimeline(tweetHomeUser, page)) {
                    //Log.d("Tweet "+ tweetHomeUser, tweet.getText());
                    tweet = new Tweet();
                    tweet.setTweet(status.getText());
                    tweet.setLine(tweetHomeUser);
                    tweet.setTime(Util.calculateTweetTime(status.getCreatedAt()));
                    updatedTweetsList.add(tweet);
                }
            } catch (Exception e) {
            }

            return updatedTweetsList;
        }

        @Override
        protected void onPostExecute(List<Tweet> tweets) {

            if(getActivity() != null) {
                if (tweets == null || tweets.isEmpty()) {
                    mLoaderAnimator.cancel();
                    setNetworkError();
                } else {
                    mTweetList = tweets;
                    mDetailedStatusAdapter.updateData(mTube, mTweetList);
                    mTweetLoader.setVisibility(View.GONE);
                    mLoaderAnimator.cancel();
                }
            }
        }
    }
}
