package in.codeseed.tubely.simplexml.platform;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by bala on 4/11/14.
 */
@Root(name = "T", strict = false)
public class Train {

    @Attribute(name = "SecondsTo", required = false)
    private String seconds;

    @Attribute(name = "Location", required = false)
    private String location;

    @Attribute(name = "Destination", required = false)
    private String destination;

    private List<String> nextTrainTimeInSeconds = new ArrayList<>();

    public String getSeconds() {
        return seconds;
    }

    public String getLocation() {
        return location;
    }

    public String getDestination() {
        return destination;
    }

    public void addTrainTime(String time){
        nextTrainTimeInSeconds.add(time);
    }

    public List<String> getNextTrainTimeInSeconds() {
        return nextTrainTimeInSeconds;
    }
}
