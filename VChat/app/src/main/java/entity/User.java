package entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Created by POPVUK on 9/3/2017.
 */
@JsonIgnoreProperties({"contacts"})
public class User {

    private String username;
    private String phone;
    private String token;
    private String photo;
    private Contact[] contacts;

    public User(){};

    public User(String username, String phone, String token)
    {
        this.username = username;
        this.phone = phone;
        this.token = token;
    }

    public String getUsername() {
        return username;
    }

    public String getPhone() {
        return phone;
    }

    public String getToken(){return token;}

    public String getPhoto(){return photo;}

}
