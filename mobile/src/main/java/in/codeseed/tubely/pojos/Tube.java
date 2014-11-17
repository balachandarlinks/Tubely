package in.codeseed.tubely.pojos;

import java.io.Serializable;

/**
 * Created by bala on 24/9/14.
 */
public class Tube implements Serializable {
    private String name;
    private String status;
    private String extraInformation;

    public Tube() {
        this.name = "";
        this.status = "";
        this.extraInformation = "";
    }

    public Tube(String name, String status, String extraInformation) {
        // Fix for Hammersmith and Waterloo line name inconsistencies in tfl feeds
        if(name.equalsIgnoreCase("H'smith & City")){
            this.name = "Hammersmith and City";
        }else if(name.equalsIgnoreCase("Waterloo & City")){
            this.name = "Waterloo and City";
        }else {
            this.name = name;
        }
        this.status = status;
        this.extraInformation = extraInformation;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        // Fix for Hammersmith and Waterloo line name inconsistencies in tfl feeds
        if(name.equalsIgnoreCase("H'smith & City")){
            this.name = "Hammersmith and City";
            return;
        }else if(name.equalsIgnoreCase("Waterloo & City")){
            this.name = "Waterloo and City";
            return;
        }
        this.name = name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getExtraInformation() {
        return extraInformation;
    }

    public void setExtraInformation(String extraInformation) {
        this.extraInformation = extraInformation;
    }
}
