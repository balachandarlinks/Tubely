package in.codeseed.tubely.simplexml.facilities;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.List;

/**
 * Created by bala on 29/10/14.
 */
@Root(strict = false)
public class Station {

    @Attribute
    private String type;

    @Element
    private String name;

    @Element
    private ContactDetails contactDetails;

    @ElementList
    private List<String> servingLines;

    @ElementList
    private List<String> zones;

    @ElementList
    private List<String> facilities;

    @Element
    private Placemark Placemark;

    public Placemark getPlacemark() {
        return Placemark;
    }

    public List<String> getFacilities() {
        return facilities;
    }

    public void setFacilities(List<String> facilities) {
        this.facilities = facilities;
    }

    public List<String> getZones() {
        return zones;
    }

    public void setZones(List<String> zones) {
        this.zones = zones;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<String> getServingLines() {
        return servingLines;
    }

    public void setServingLines(List<String> servingLines) {
        this.servingLines = servingLines;
    }

    public ContactDetails getContactDetails() {
        return contactDetails;
    }

    public void setContactDetails(ContactDetails contactDetails) {
        this.contactDetails = contactDetails;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
