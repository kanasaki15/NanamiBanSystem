package xyz.n7mn.dev.nanamibansystem.util;

import java.util.HashMap;
import java.util.UUID;

public class VPNAlertData {

private static HashMap<UUID, Boolean> alertList = null;

public static void set (UUID uuid, boolean flag){
    if (alertList == null){
        alertList = new HashMap<>();
    }

    if (alertList.get(uuid) != null){
        alertList.remove(uuid);
    }

    alertList.put(uuid, flag);
}

public static boolean get(UUID uuid){

    try {
        return alertList.get(uuid);
    } catch (Exception e){
        return false;
    }

}

}
