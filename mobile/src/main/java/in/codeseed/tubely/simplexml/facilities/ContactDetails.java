package in.codeseed.tubely.simplexml.facilities;

import org.simpleframework.xml.Element;

/**
 * Created by bala on 29/10/14.
 */
@org.simpleframework.xml.Root(strict = false)
public class ContactDetails {

    @Element
    private String address;

    @Element
    private String phone;

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}
