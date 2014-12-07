package in.codeseed.tubely.pojos;

/**
 * Created by bala on 4/12/14.
 */
public class NearByStation {

    private String name;
    private String lines;
    private float distanceFromCurrentLocation;
    private double latitude;
    private double longitude;

    public NearByStation(String name, String lines, float distanceFromCurrentLocation, double latitude, double longitude) {
        this.name = name;
        this.lines = lines;
        this.distanceFromCurrentLocation = distanceFromCurrentLocation;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getName() {
        return name;
    }

    public String getLines() {
        return lines;
    }

    public float getDistanceFromCurrentLocation() {
        return distanceFromCurrentLocation;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }
}
