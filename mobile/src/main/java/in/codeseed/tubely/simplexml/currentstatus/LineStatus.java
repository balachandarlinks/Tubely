package in.codeseed.tubely.simplexml.currentstatus;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

/**
 * Created by bala on 5/11/14.
 */

@Root(name = "LineStatus", strict = false)
public class LineStatus {

    @Attribute(name = "StatusDetails")
    private String extraInformation;

    @Element(name="Line")
    private Line line;

    @Element(name = "Status")
    private Status status;

    public String getExtraInformation() {
        return extraInformation;
    }

    public String getName(){
        return line.getName();
    }

    public String getDescription() {
        return status.getDescription();
    }

    @Root(name = "Line", strict = false)
    private static class Line{

        @Attribute(name = "Name")
        private String name;

        public String getName() {
            return name;
        }
    }

    @Root(name="Status", strict = false)
    private static class Status{

        @Attribute(name = "Description")
        private String description;

        public String getDescription() {
            return description;
        }
    }

}
