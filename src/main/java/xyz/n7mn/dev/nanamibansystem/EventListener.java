package xyz.n7mn.dev.nanamibansystem;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import xyz.n7mn.dev.api.Ban;
import xyz.n7mn.dev.api.data.BanData;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class EventListener implements Listener {

    private final Ban banSystem;
    public EventListener(Ban banSystem){
        this.banSystem = banSystem;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void AsyncPlayerPreLoginEvent (AsyncPlayerPreLoginEvent e){

        UUID targetUUID = e.getUniqueId();
        StringBuilder sb = new StringBuilder("" +
                "--- ななみ鯖 ---\n" +
                "\n" +
                "あなたは以下の理由でBANされています。\n"
        );

        List<BanData> list;
        try {
            list = banSystem.getList(targetUUID, true);
            Date nowDate = new Date();
            List<BanData> copyList = new ArrayList<>(list);

            for (BanData data : copyList){
                if (data.getEndDate().getTime() < nowDate.getTime()){
                    list.remove(data);
                }
            }

            boolean isFound = list.size() > 0;

            for (BanData data : list){
                if (data.getBanUser().equals(targetUUID)){
                    sb.append("ID : ");
                    sb.append(data.getBanID());
                    sb.append("\n");
                    sb.append("理由 : ");
                    sb.append(data.getReason());
                    sb.append("\n");
                    sb.append("解除日時 : ");
                    sb.append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(data.getEndDate()));
                    sb.append("\n");
                }
            }

            if (isFound){
                e.disallow(AsyncPlayerPreLoginEvent.Result.KICK_BANNED, sb.toString());
            }
        } catch (SQLException ex){
            ex.printStackTrace();
        }
    }

}
