package in.codeseed.tubely.fragments;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

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

    private DownloadTwitterStatus downloadTwitterStatus;
    private RelativeLayout tweetLoader;
    private ImageView tweetRefreshImageView;

    private RecyclerView recyclerView;
    private List<Tweet> tweetList = new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;
    private DetailedStatusAdapter detailedStatusAdapter;

    private Tube tube;
    private int pageStart = 1;
    private int pageEnd = 20;
    private String twitterHandle;
    private ObjectAnimator animator;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_detailed_status, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //Read Input data
        tube = (Tube)getArguments().getSerializable("data");
        twitterHandle = getArguments().getString("twitter_handle");

        injectViews(view);

        //Layout manager for RecyclerView
        linearLayoutManager = new LinearLayoutManager(getActivity().getApplicationContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);

        detailedStatusAdapter = new DetailedStatusAdapter(getActivity().getApplicationContext(), tube, tweetList);

        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(detailedStatusAdapter);

        tweetRefreshImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refreshTweetStatus(twitterHandle);
            }
        });

        animator = ObjectAnimator.ofFloat(tweetRefreshImageView, "rotation", 360);
        animator.setDuration(600);
        animator.setInterpolator(new LinearInterpolator());
        animator.setRepeatCount(ObjectAnimator.INFINITE);

        refreshTweetStatus(twitterHandle);
    }

    void injectViews(View view){
        tweetLoader = (RelativeLayout) view.findViewById(R.id.tweetLoader);
        tweetRefreshImageView = (ImageView) tweetLoader.findViewById(R.id.tweetLoaderImageView);
        recyclerView = (RecyclerView) view.findViewById(R.id.twitter_stream_recyclerview);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("tweets_list", (Serializable) tweetList);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        downloadTwitterStatus = null;
    }

    private void refreshTweetStatus(String twitterHandle) {

        animator.start();
        ConnectivityManager connMgr = (ConnectivityManager) getActivity().getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = connMgr.getActiveNetworkInfo();

        if (netInfo != null && netInfo.isConnected()) {
            //Call Async Task
            if (downloadTwitterStatus == null || downloadTwitterStatus.getStatus() == AsyncTask.Status.FINISHED) {
                downloadTwitterStatus = new DownloadTwitterStatus();
                downloadTwitterStatus.execute(twitterHandle);
            }
        } else {
            setNetworkError();
            animator.cancel();
        }
    }

    void setNetworkError() {
        Toast.makeText(getActivity().getApplicationContext(), "There is some problem with your data network :(", Toast.LENGTH_SHORT).show();
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
            Paging page = new Paging(pageStart, pageEnd);
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
                Log.e("TwitterException", e.getMessage());
            }

            return updatedTweetsList;
        }

        @Override
        protected void onPostExecute(List<Tweet> tweets) {

            if(getActivity() != null) {
                if (tweets == null || tweets.isEmpty()) {
                    Log.d("Tweets", "Failure");
                    animator.cancel();
                    setNetworkError();
                } else {
                    Log.d("Tweets", "Success");
                    tweetList = tweets;
                    detailedStatusAdapter.updateData(tube, tweetList);
                    tweetLoader.setVisibility(View.INVISIBLE);
                    animator.cancel();
                }
            }
        }
    }
}
