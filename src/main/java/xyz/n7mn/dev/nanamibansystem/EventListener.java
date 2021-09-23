package xyz.n7mn.dev.nanamibansystem;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.plugin.Plugin;
import xyz.n7mn.dev.nanamibansystem.util.BanRuntime;

import java.io.IOException;
import java.util.Map;

public class EventListener implements Listener {

    private final Plugin plugin;
    public EventListener(Plugin plugin){
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void AsyncPlayerPreLoginEvent (AsyncPlayerPreLoginEvent e){

        if (BanRuntime.isBan(plugin, e.getUniqueId(), "all") || BanRuntime.isBan(plugin, e.getUniqueId(), plugin.getConfig().getString("Area"))){
            Map<Integer, String> reason = BanRuntime.getReason(plugin, e.getUniqueId(), plugin.getConfig().getString("Area"));

            if (reason.isEmpty()){
                reason = BanRuntime.getReason(plugin, e.getUniqueId(), "all");
            }

            Map<Integer, String> finalReason = reason;
            reason.forEach((id, rea)->{
                e.disallow(AsyncPlayerPreLoginEvent.Result.KICK_BANNED, "" +
                        "--- ななみ鯖 ---\n" +
                        "あなたは以下の理由でBANされています。\n" +
                        "解除申請は "+plugin.getConfig().getString("DiscordInviteURL")+" まで\n" +
                        "\n" +
                        "理由: " + finalReason.get(id)
                );

                new Thread(()->{
                    for (Player player : plugin.getServer().getOnlinePlayers()){
                        if (player.isOp()){
                            player.sendMessage(ChatColor.YELLOW + "[ななみ鯖] " + ChatColor.RESET + "BANされている " + e.getName() + "さんが入室しようとしました。(BAN理由: "+ finalReason.get(id)+" )");
                        }
                    }
                }).start();
            });
            return;
        }

        String ip = e.getAddress().getHostAddress();
        String url = "http://proxycheck.io/v2/"+ip+"?key="+plugin.getConfig().getString("ProxyCheckIoAPI")+"&risk=1&vpn=1";
        String json = "";

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

                    new Thread(()->{
                        for (Player player : plugin.getServer().getOnlinePlayers()){
                            if (player.isOp()){
                                player.sendMessage(ChatColor.YELLOW + "[ななみ鯖] " + ChatColor.RESET + e.getName() + "さんが入室しようとしました。(理由: Proxy判定のIPからの接続 )");
                            }
                        }
                    }).start();
                    return;
                }

                if (type.getAsString().equals("VPN") && plugin.getConfig().getBoolean("isAntiVPN")){
                    e.disallow(AsyncPlayerPreLoginEvent.Result.KICK_BANNED, "現在 VPNからは接続できません。");

                    new Thread(()->{
                        for (Player player : plugin.getServer().getOnlinePlayers()){
                            if (player.isOp()){
                                player.sendMessage(ChatColor.YELLOW + "[ななみ鯖] " + ChatColor.RESET + e.getName() + "さんが入室しようとしました。(理由: VPN判定のIPからの接続 )");
                            }
                        }
                    }).start();
                }

            }
        }

    }

}
