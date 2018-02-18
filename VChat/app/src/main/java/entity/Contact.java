package entity;

/**
 * Created by MiljanaMilena on 12/20/2017.
 */

public class Contact {

    private String id_contact;
    private String readed;

    public Contact()
    {}

    public Contact(String id, String read)
    {
        this.id_contact = id;
        this.readed = read;
    }

    public String getId_contact(){return id_contact;}

    public String getReaded(){return readed;}
}
