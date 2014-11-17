package in.codeseed.tubely.simplexml.allstations;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.List;

/**
 * Created by bala on 6/11/14.
 */
@Root(name = "AllStations", strict = false)
public class AllStations {

    @ElementList(name = "Stations")
    private List<Station> stations;

    public List<Station> getStations() {
        return stations;
    }

}
