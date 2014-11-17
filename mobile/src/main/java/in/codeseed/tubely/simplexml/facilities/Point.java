package in.codeseed.tubely.simplexml.facilities;

import org.simpleframework.xml.Element;

/**
 * Created by bala on 29/10/14.
 */

@org.simpleframework.xml.Root(strict = false)
public class Point {

    @Element
    private String coordinates;

    public String getCoordinates() {
        return coordinates;
    }
}
