package entity;

/**
 * Created by MiljanaMilena on 1/15/2018.
 */

public class Message {

    private String from;
    private String message;
    private String readed;
    private String to;

    public Message(){}

    public Message(String f, String m, String r, String t)
    {
        from = f;
        message = m;
        readed = r;
        to = t;
    }

    public String getFrom(){return from;}
    public String getMessage(){return message;}
    public String getReaded() {return readed;}
    public String getTo(){return to;}
}
