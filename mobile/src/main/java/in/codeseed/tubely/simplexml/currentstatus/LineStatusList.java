package in.codeseed.tubely.simplexml.currentstatus;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.List;

/**
 * Created by bala on 5/11/14.
 */
@Root(name = "ArrayOfLineStatus", strict = false)
public class LineStatusList {

    @ElementList(name = "LineStatus", inline = true)
    private List<LineStatus> lineStatuses;

    public List<LineStatus> getLineStatuses() {
        return lineStatuses;
    }
}
