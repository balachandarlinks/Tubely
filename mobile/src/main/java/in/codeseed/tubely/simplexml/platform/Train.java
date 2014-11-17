package in.codeseed.tubely.simplexml.platform;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;

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

    public String getSeconds() {
        return seconds;
    }

    public String getLocation() {
        return location;
    }

    public String getDestination() {
        return destination;
    }
}
