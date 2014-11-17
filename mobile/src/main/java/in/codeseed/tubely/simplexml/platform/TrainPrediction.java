package in.codeseed.tubely.simplexml.platform;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;

import java.util.List;

/**
 * Created by bala on 4/11/14.
 */
@org.simpleframework.xml.Root(name = "ROOT", strict = false)
public class TrainPrediction {

    @Element(name = "WhenCreated")
    private String timeStamp;

    @Element(name = "LineName")
    private String lineName;

    @ElementList(name = "S")
    private List<TrainPlatform> platforms;

    public String getTimeStamp() {
        return timeStamp;
    }

    public String getLineName() {
        return lineName;
    }

    public List<TrainPlatform> getPlatforms() {
        return platforms;
    }
}
