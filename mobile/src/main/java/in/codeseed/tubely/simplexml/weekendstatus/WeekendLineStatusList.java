package in.codeseed.tubely.simplexml.weekendstatus;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.List;

/**
 * Created by bala on 5/11/14.
 */
@Root(name = "tubeToday", strict = false)
public class WeekendLineStatusList {

    @ElementList(name = "Lines")
    private List<Line> lines;

    public List<Line> getLines() {
        return lines;
    }
}
