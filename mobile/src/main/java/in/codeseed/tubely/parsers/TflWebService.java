package in.codeseed.tubely.parsers;

import in.codeseed.tubely.simplexml.currentstatus.LineStatusList;
import in.codeseed.tubely.simplexml.facilities.Root;
import in.codeseed.tubely.simplexml.platform.TrainPrediction;
import in.codeseed.tubely.simplexml.weekendstatus.WeekendLineStatusList;
import retrofit.Callback;
import retrofit.http.GET;
import retrofit.http.Path;

/**
 * Created by bala on 29/10/14.
 */
public interface TflWebService {

    @GET("/tfl/syndication/feeds/stations_facilities.xml?app_id=91795102&app_key=09149734c72d21e58a5b332965a12e82")
    Root getStationsData();

    @GET("/TrackerNet/PredictionDetailed/{line}/{station}")
    void getTrainPrediction(@Path("line") String line, @Path("station") String station, Callback<TrainPrediction> trainPrediction);

    @GET("/TrackerNet/LineStatus")
    LineStatusList getCurrentLineStatusList();

    @GET("/tfl/syndication/feeds/TubeThisWeekend_v1.xml?app_id=91795102&app_key=09149734c72d21e58a5b332965a12e82")
    WeekendLineStatusList getWeekendLineStatusList();

}
