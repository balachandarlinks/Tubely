package in.codeseed.tubely.pojos;

import java.io.Serializable;

/**
 * Created by bala on 7/10/14.
 */
public class Tweet implements Serializable {

    private String tweet;
    private String time;
    private String line;

    public Tweet() {

    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getLine() {
        return line;
    }

    public void setLine(String line) {
        this.line = line;
    }

    public String getTweet() {
        return tweet;
    }

    public void setTweet(String tweet) {
        this.tweet = tweet;
    }
}
