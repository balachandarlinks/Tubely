package in.codeseed.tubely.adapters;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import in.codeseed.tubely.R;
import in.codeseed.tubely.activities.StatusAndStationsActivity;
import in.codeseed.tubely.pojos.Tube;

/**
 * Created by bala on 23/9/14.
 */
public class TubeStatusAdapter extends RecyclerView.Adapter<TubeStatusAdapter.ViewHolder> {

    private static Context appContext;
    private static List<Tube> tubeList;
    private int cardLayout;
    private static String[] twitterHandles;

    public TubeStatusAdapter(Context appContext, List<Tube> tubeList, int cardLayout) {
        TubeStatusAdapter.appContext = appContext;
        TubeStatusAdapter.tubeList = tubeList;
        this.cardLayout = cardLayout;
        this.twitterHandles = appContext.getResources().getStringArray(R.array.twitterHandles);
    }

    @Override
    public TubeStatusAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(cardLayout, viewGroup, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(TubeStatusAdapter.ViewHolder viewHolder, int i) {

        Tube tube = tubeList.get(i);
        viewHolder.tubeName.setText(tube.getName());
        viewHolder.tubeStatus.setText(tube.getStatus());

        switch (tube.getName()) {
            case "Bakerloo":

                viewHolder.tubeName.setBackgroundResource(R.color.bakerloo_bg);
                viewHolder.tubeName.setTextColor(appContext.getResources().getColor(R.color.bakerloo_fg));
                break;

            case "Central":

                viewHolder.tubeName.setBackgroundResource(R.color.central_bg);
                viewHolder.tubeName.setTextColor(appContext.getResources().getColor(R.color.central_fg));
                break;

            case "Circle":

                viewHolder.tubeName.setBackgroundResource(R.color.circle_bg);
                viewHolder.tubeName.setTextColor(appContext.getResources().getColor(R.color.circle_fg));
                break;

            case "District":

                viewHolder.tubeName.setBackgroundResource(R.color.district_bg);
                viewHolder.tubeName.setTextColor(appContext.getResources().getColor(R.color.district_fg));
                break;

            case "DLR":

                viewHolder.tubeName.setBackgroundResource(R.color.dlr_bg);
                viewHolder.tubeName.setTextColor(appContext.getResources().getColor(R.color.dlr_fg));
                break;

            case "Hammersmith and City":

                viewHolder.tubeName.setBackgroundResource(R.color.hsmithandcity_bg);
                viewHolder.tubeName.setTextColor(appContext.getResources().getColor(R.color.hsmithandcity_fg));
                break;

            case "Jubilee":

                viewHolder.tubeName.setBackgroundResource(R.color.jubilee_bg);
                viewHolder.tubeName.setTextColor(appContext.getResources().getColor(R.color.jubilee_fg));
                break;

            case "Metropolitan":

                viewHolder.tubeName.setBackgroundResource(R.color.metropoliton_bg);
                viewHolder.tubeName.setTextColor(appContext.getResources().getColor(R.color.metropoliton_fg));
                break;

            case "Northern":

                viewHolder.tubeName.setBackgroundResource(R.color.northern_bg);
                viewHolder.tubeName.setTextColor(appContext.getResources().getColor(R.color.northern_fg));
                break;

            case "Overground":

                viewHolder.tubeName.setBackgroundResource(R.color.overground_bg);
                viewHolder.tubeName.setTextColor(appContext.getResources().getColor(R.color.overground_fg));
                break;

            case "Piccadilly":

                viewHolder.tubeName.setBackgroundResource(R.color.piccadilly_bg);
                viewHolder.tubeName.setTextColor(appContext.getResources().getColor(R.color.piccadilly_fg));
                break;

            case "Victoria":

                viewHolder.tubeName.setBackgroundResource(R.color.victoria_bg);
                viewHolder.tubeName.setTextColor(appContext.getResources().getColor(R.color.victoria_fg));
                break;

            case "Waterloo and City":

                viewHolder.tubeName.setBackgroundResource(R.color.waterlooandcity_bg);
                viewHolder.tubeName.setTextColor(appContext.getResources().getColor(R.color.waterlooandcity_fg));
                break;
        }


    }

    public void updateAdapter(List<Tube> tubeList) {

        TubeStatusAdapter.tubeList = tubeList;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return (tubeList == null || tubeList.isEmpty()) ? 0 : tubeList.size();
    }


    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView tubeName, tubeStatus;

        public ViewHolder(View itemView) {

            super(itemView);
            tubeName = (TextView) itemView.findViewById(R.id.tubeName);
            tubeStatus = (TextView) itemView.findViewById(R.id.tubeStatus);
            itemView.setOnClickListener(this);
        }


        @Override
        public void onClick(View view) {

            Bundle bundle = new Bundle();
            bundle.putSerializable("tube_data", tubeList.get(getPosition()));

            Intent intent = new Intent(appContext, StatusAndStationsActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("data", bundle);
            intent.putExtra("twitter_handle", twitterHandles[getPosition()]);
            appContext.startActivity(intent);

        }

    }
}