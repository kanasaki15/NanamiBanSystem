package xyz.n7mn.dev.nanamibansystem.util;

import java.util.HashMap;
import java.util.UUID;

public class VPNAlertData {

private static HashMap<UUID, Boolean> vpnAlertList = null;

public static void set (UUID uuid, boolean flag){
    if (vpnAlertList == null){
        vpnAlertList = new HashMap<>();
    }

    if (vpnAlertList.get(uuid) != null){
        vpnAlertList.remove(uuid);
    }

    vpnAlertList.put(uuid, flag);
}

public static boolean get(UUID uuid){

    try {
        return vpnAlertList.get(uuid);
    } catch (Exception e){
        return false;
    }

}

}
