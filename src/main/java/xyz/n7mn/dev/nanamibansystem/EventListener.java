package xyz.n7mn.dev.nanamibansystem;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.plugin.Plugin;
import xyz.n7mn.dev.api.Ban;
import xyz.n7mn.dev.api.data.BanData;

import java.io.IOException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class EventListener implements Listener {

    private final Ban banSystem;
    private final Plugin plugin;
    public EventListener(Ban banSystem, Plugin plugin){
        this.banSystem = banSystem;
        this.plugin = plugin;
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

                if (!data.getArea().equals(plugin.getConfig().getString("Area")) && !data.getArea().equals("all")) {
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

        List<String> throughList = (List<String>) plugin.getConfig().getList("AntiThroughList");
        if (throughList != null && throughList.size() > 0){
            for (String through : throughList){
                if (through.length() != 36){
                    continue;
                }

                if (e.getUniqueId().equals(UUID.fromString(through))){
                    return;
                }
            }
        }

        // http://proxycheck.io/v2/<ip>?key=<api key>&risk=1&vpn=1
        String ip = e.getAddress().getHostAddress();
        String url = "http://proxycheck.io/v2/"+ip+"?key="+plugin.getConfig().getString("ProxyCheckIoAPI")+"&risk=1&vpn=1";
        
        // debug
        // ip = "219.83.125.226";
        // url = "https://proxycheck.io/v2/219.83.125.226?risk=1&vpn=1";
        
        String json = "";
        // System.out.println("debug : " + ip);
        if (plugin.getConfig().getBoolean("isAntiProxy") || plugin.getConfig().getBoolean("isAntiVPN")){
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(url)
                    .build();

            try {
                Response response = client.newCall(request).execute();
                json = response.body().string();
                response.close();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }


            JsonObject object = new Gson().fromJson(json, JsonObject.class);
            JsonElement element = object.get("status");
            // System.out.println("debug1 : " + element.getAsString());
            if (element.getAsString().equals("ok")){
                JsonElement data = object.get(ip);
                JsonElement proxy = data.getAsJsonObject().get("proxy");
                // System.out.println("debug2 : " + proxy.getAsString());
                JsonElement type = data.getAsJsonObject().get("type");
                // System.out.println("debug3 : " + type.getAsString());
                if (proxy.getAsString().equals("yes") && plugin.getConfig().getBoolean("isAntiProxy")){
                    e.disallow(AsyncPlayerPreLoginEvent.Result.KICK_BANNED, "現在 Proxyからは接続できません。");
                    return;
                }

                if (type.getAsString().equals("VPN") && plugin.getConfig().getBoolean("isAntiVPN")){
                    e.disallow(AsyncPlayerPreLoginEvent.Result.KICK_BANNED, "現在 VPNからは接続できません。");
                }

            }
        }
    }

}
