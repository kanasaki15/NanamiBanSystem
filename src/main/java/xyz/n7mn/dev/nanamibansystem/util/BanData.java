package xyz.n7mn.dev.nanamibansystem.util;

import java.util.Date;
import java.util.UUID;

public class BanData {

    private long BanID;
    private UUID UserUUID;
    private String Reason;
    private String Area;
    private String IP;
    private Date EndDate;
    private Date ExecuteDate;
    private UUID ExecuteUserUUID;
    private boolean Active;

    public BanData(long banID, UUID userUUID, String reason, String area, String ip, Date endDate, Date executeDate, UUID executeUserUUID, boolean active){
        this.BanID = banID;
        this.UserUUID = userUUID;
        this.Reason = reason;
        this.Area = area;
        this.IP = ip;
        this.EndDate = endDate;
        this.ExecuteDate = executeDate;
        this.ExecuteUserUUID = executeUserUUID;
        this.Active = active;
    }

    public long getBanID() {
        return BanID;
    }

    public UUID getUserUUID() {
        return UserUUID;
    }

    public String getReason() {
        return Reason;
    }

    public String getArea() {
        return Area;
    }

    public String getIP() {
        return IP;
    }

    public Date getEndDate() {
        return EndDate;
    }

    public Date getExecuteDate() {
        return ExecuteDate;
    }

    public UUID getExecuteUserUUID() {
        return ExecuteUserUUID;
    }

    public boolean isActive() {
        return Active;
    }
}
