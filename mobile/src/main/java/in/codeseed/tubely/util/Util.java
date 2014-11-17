package in.codeseed.tubely.util;

import java.util.Date;

import in.codeseed.tubely.simplexml.allstations.AllStations;

/**
 * Created by bala on 18/10/14.
 */
public class Util {

    public static final String STATION_TABLE_SPLITTER = "#123#";
    public static final String SHARED_PREF_TUBESTATUS_CURRENT = "tubestatus_current_lastupdate";
    public static final String SHARED_PREF_TUBESTATUS_WEEKEND = "tubestatus_weekend_lastupdate";
    public static final String SHARED_PREF_STATION_FACILITIES = "stations_facilities_lastupdate";
    public static final String SHARED_PREF_FIRST_TIME_WELCOME = "tubely_first_time_welcome";

    private static AllStations allStations;

    public Util(){

    }

    public static long calculateHours(Date date){

        long diff = new Date().getTime() - date.getTime();
        long diffHours = diff / (60 * 60 * 1000) % 24;
        return diffHours;
    }

    public static String calculateTweetTime(Date tweetTime) {

        long diff = new Date().getTime() - tweetTime.getTime();

        long diffSeconds = diff / 1000 % 60;
        long diffMinutes = diff / (60 * 1000) % 60;
        long diffHours = diff / (60 * 60 * 1000) % 24;
        long diffDays = diff / (24 * 60 * 60 * 1000);

        if (diffDays > 1) {
            return diffDays + " days";
        } else if (diffDays == 1) {
            return diffDays + " day";
        } else if (diffHours > 1) {
            return diffHours + " hrs";
        } else if (diffHours == 1) {
            return diffHours + " hr";
        } else if (diffMinutes > 1) {
            return diffMinutes + " mns";
        } else if (diffMinutes == 1) {
            return diffMinutes + " mn";
        } else if (diffSeconds > 1) {
            return diffSeconds + " secs";
        } else if (diffSeconds >= 0) {
            return diffSeconds + " sec";
        }
        return "";
    }

    public static AllStations getAllStations() {
        return allStations;
    }

    public static void setAllStations(AllStations allStations) {
        Util.allStations = allStations;
    }
}
