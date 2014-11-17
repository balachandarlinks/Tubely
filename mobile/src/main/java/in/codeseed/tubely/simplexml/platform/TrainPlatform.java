package in.codeseed.tubely.simplexml.platform;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.List;

/**
 * Created by bala on 4/11/14.
 */
@Root(name = "P",strict = false)
public class TrainPlatform {

    @Attribute(name = "N")
    private String direction;

    @ElementList(name = "T", inline = true, required = false)
    private List<Train> trainList;

    public String getDirection() {
        return direction;
    }

    public List<Train> getTrainList() {
        return trainList;
    }
}
