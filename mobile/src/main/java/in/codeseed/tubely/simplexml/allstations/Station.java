package in.codeseed.tubely.simplexml.allstations;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;

/**
 * Created by bala on 6/11/14.
 */
@Root(name = "Station", strict = false)
public class Station{

    @Attribute(name = "Name")
    private String name;

    @Attribute(name = "Code")
    private String code;

    @Attribute(name = "Lines")
    private String line;

    public String getName() {
        return name;
    }

    public String getCode() {
        return code;
    }

    public String getLine() {
        return line;
    }
}

