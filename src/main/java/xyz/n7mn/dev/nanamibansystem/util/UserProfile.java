package xyz.n7mn.dev.nanamibansystem.util;

public class UserProfile {

    private String id;
    private String name;

    public UserProfile(String id, String name){
        this.id = id;
        this.name = name;
    }

    public String getUserName() {
        return name;
    }
    public String getId(){
        return id;
    }
}
