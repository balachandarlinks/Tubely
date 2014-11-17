package in.codeseed.tubely.simplexml.facilities;

import org.simpleframework.xml.Element;


/**
 * Created by bala on 29/10/14.
 */
@org.simpleframework.xml.Root(strict = false)
public class Placemark {

    @Element
    private String name;

    @Element
    private String description;

    @Element
    private Point Point;

    public Point getPoint() {
        return Point;
    }
}

