package in.codeseed.tubely.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import in.codeseed.tubely.R;
import in.codeseed.tubely.pojos.Tube;
import in.codeseed.tubely.pojos.Tweet;

/**
 * Created by bala on 2/11/14.
 */
public class DetailedStatusAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int CARD_STATUS = 0;
    private static final int CARD_TWEET = 1;

    private Context mContext;
    private Tube tube;
    private List<Tweet> tweetList;

    public DetailedStatusAdapter(Context mContext, Tube tube, List<Tweet> tweetList) {
        this.mContext = mContext;
        this.tube = tube;
        this.tweetList = tweetList;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {

           if(viewType ==  CARD_STATUS) {
               View statusCardView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.detailed_status_card, viewGroup, false);
               return new StatusViewHolder(statusCardView);
           }else {
               View tweetCardView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.twitter_status_card, viewGroup, false);
               return new TweetViewHolder(tweetCardView);
           }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        if(position == 0){
            StatusViewHolder statusViewHolder = (StatusViewHolder) viewHolder;
            statusViewHolder.titleCard.setText(tube.getStatus());
            if(tube.getExtraInformation().equalsIgnoreCase(""))
                statusViewHolder.titleCardExtra.setText(mContext.getResources().getString(R.string.good_service_extra_message));
            else
                statusViewHolder.titleCardExtra.setText(tube.getExtraInformation());
            statusViewHolder.twitterHeader.setText("Tweets from @" + tube.getName());
        }else{
            TweetViewHolder tweetViewHolder = (TweetViewHolder) viewHolder;
            Tweet tweet = tweetList.get(position - 1);
            tweetViewHolder.tweetStatus.setText(tweet.getTweet());
            tweetViewHolder.tweetTime.setText(tweet.getTime());
        }
    }

    @Override
    public int getItemCount() {
        return tweetList.size() + 1;
    }

    public void updateData(Tube tube, List<Tweet> tweetList){
        this.tube = tube;
        this.tweetList = tweetList;
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {

        if(position == 0)
            return CARD_STATUS;
        else
            return CARD_TWEET;
    }

    public static class StatusViewHolder extends RecyclerView.ViewHolder{

        private TextView titleCard, titleCardExtra, twitterHeader;
        public StatusViewHolder(View itemView) {
            super(itemView);
            titleCard = (TextView) itemView.findViewById(R.id.titleCardText);
            titleCardExtra = (TextView) itemView.findViewById(R.id.detailCardExtra);
            twitterHeader = (TextView) itemView.findViewById(R.id.twitter_header);
        }
    }

    public static class TweetViewHolder extends RecyclerView.ViewHolder{

        private TextView tweetStatus, tweetTime;
        private ImageView tweetIcon;

        public TweetViewHolder(View itemView) {
            super(itemView);
            tweetStatus = (TextView) itemView.findViewById(R.id.tweet);
            tweetTime = (TextView) itemView.findViewById(R.id.tweetCardTime);
            tweetIcon = (ImageView) itemView.findViewById(R.id.tweetCardIcon);
        }
    }
}
