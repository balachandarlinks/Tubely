package in.codeseed.tubely.simplexml.facilities;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;

/**
 * Created by bala on 29/10/14.
 */
@org.simpleframework.xml.Root(strict = false)
public class Facility {

    @Attribute
    private String name;

    @Element
    private String facility;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFacility() {
        return facility;
    }

    public void setFacility(String facility) {
        this.facility = facility;
    }
}
