package in.codeseed.tubely.fragments;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.List;

import in.codeseed.tubely.R;
import in.codeseed.tubely.activities.StationActivity;
import in.codeseed.tubely.simplexml.allstations.Station;
import in.codeseed.tubely.util.Util;

/**
 * Created by bala on 10/10/14.
 */
public class TubeStationsFragment extends Fragment {

    private ListView listView;
    private RelativeLayout lineError;

    private ArrayList<String> stations;
    private ArrayAdapter<String> stationsAdapter;
    private String lineName;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_tubestations, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        lineName = getArguments().getString("line_name");

        lineError = (RelativeLayout) view.findViewById(R.id.line_error);
        listView = (ListView) view.findViewById(R.id.list_view);

        if(lineName.equalsIgnoreCase("Overground") || lineName.equalsIgnoreCase("DLR")){
            listView.setVisibility(View.GONE);
        }else {
            lineError.setVisibility(View.GONE);
            stations = new ArrayList<>();
            stationsAdapter = new ArrayAdapter<>(getActivity().getApplicationContext(), R.layout.listitem_tube_station, stations);
            listView.setAdapter(stationsAdapter);

            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {

                    String stationName = stations.get(position);
                    String stationCode = "";
                    String stationLine = "";
                    for(Station station : Util.getAllStations().getStations()){
                        if(station.getName().equalsIgnoreCase(stationName)){
                            stationCode = station.getCode();
                            stationLine = station.getLine();
                        }
                    }

                    //Passdata to activity to open the next fragment
                    Intent intent = new Intent(getActivity().getApplicationContext(), StationActivity.class);
                    intent.putExtra("station", stationName);
                    intent.putExtra("code", stationCode);
                    intent.putExtra("line", stationLine);
                    startActivity(intent);
                }
            });

            loadStationsData();
        }

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

    }

    public void loadStationsData(){
        List<String> stationsList = new ArrayList<>();
        for(Station station : Util.getAllStations().getStations()){
            if(station.getLine().contains(lineName)){
                stationsList.add(station.getName());
            }
        }
        if(stationsList.isEmpty())
            stationsList.add("Sorry! We don't support " + lineName + " stations.");
        stations.addAll(stationsList);
        stationsAdapter.notifyDataSetChanged();
    }
}
