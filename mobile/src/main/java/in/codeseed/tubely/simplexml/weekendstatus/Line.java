package in.codeseed.tubely.simplexml.weekendstatus;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

/**
 * Created by bala on 5/11/14.
 */
@Root(name = "Line", strict = false)
public class Line {

    @Element(name = "Name")
    private String name;

    @Element(name = "Status")
    private Status status;

    public String getName() {
        return name;
    }

    public String getStatus(){
        return status.getStatus();
    }

    public String getExtraInformation(){
        return status.getMessage().getExtraInformation();
    }

    @Root(name = "Status", strict = false)
    private static class Status{

        @Element(name = "Text")
        private String status;

        @Element(name="Message")
        private Message message;

        public String getStatus() {
            return status;
        }

        public Message getMessage() {
            return message;
        }

        @Root(name = "Message", strict = false)
        private static class Message{

            @Element(name = "Text", required = false)
            private String extraInformation;

            public String getExtraInformation() {
                return extraInformation == null ? "":extraInformation;
            }
        }
    }

}
