package in.codeseed.tubely.simplexml.facilities;

import org.simpleframework.xml.ElementList;

import java.util.List;

/**
 * Created by bala on 29/10/14.
 */


@org.simpleframework.xml.Root(strict = false)
public class Root {

    @ElementList
    private List<Station> stations;

    public List<Station> getStations() {
        return stations;
    }

    public void setStations(List<Station> stations) {
        this.stations = stations;
    }
}
