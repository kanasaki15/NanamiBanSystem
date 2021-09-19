package xyz.n7mn.dev.nanamibansystem.util;

class Username2UUID {

    private String name;
    private long changedToAt;

    public Username2UUID(String name, long changedToAt){
        this.name = name;
        this.changedToAt = changedToAt;
    }

    public String getName() {
        return name;
    }

    public long getChangedToAt() {
        return changedToAt;
    }
}
