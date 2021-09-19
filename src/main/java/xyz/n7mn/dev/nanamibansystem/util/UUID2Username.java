package xyz.n7mn.dev.nanamibansystem.util;

import java.util.UUID;

public class UUID2Username {

    private String name;
    private String id;

    public UUID2Username(String name, String id){
        this.name = name;
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    public UUID getUUID(){
        return UUID.fromString(id.replaceFirst("([0-9a-fA-F]{8})([0-9a-fA-F]{4})([0-9a-fA-F]{4})([0-9a-fA-F]{4})([0-9a-fA-F]+)", "$1-$2-$3-$4-$5"));
    }
}
