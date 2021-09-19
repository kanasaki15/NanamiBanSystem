package xyz.n7mn.dev.nanamibansystem.command;

import com.google.gson.Gson;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import xyz.n7mn.dev.nanamibansystem.util.BanRuntime;
import xyz.n7mn.dev.nanamibansystem.util.UUID2Username;

import java.util.UUID;

public class IPBanCommand implements CommandExecutor {

    private final Plugin plugin;
    public IPBanCommand(Plugin plugin){
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        // コンソールからは実行できないようにする
        if (!(sender instanceof Player)){
            return true;
        }

        new Thread(()->{

            String area = "all";
            String username = args[0];
            String reason;
            UUID targetUUID = null;

            StringBuilder sb = new StringBuilder();
            for (int i = 1; i < args.length; i++){
                if (i != 1){
                    sb.append(" ");
                }
                sb.append(args[i]);
            }
            reason = sb.toString();

            try {
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder().url("https://api.mojang.com/users/profiles/minecraft/"+username).build();
                Response response = client.newCall(request).execute();
                UUID2Username json = new Gson().fromJson(response.body().string(), UUID2Username.class);
                targetUUID = json.getUUID();

            } catch (Exception ex){
                ex.fillInStackTrace();
            }

            if (targetUUID != null){
                String ip = "";
                Player player = plugin.getServer().getPlayer(targetUUID);
                if (player != null && player.isOnline()){
                    ip = player.getAddress().getHostName();
                }

                for (Player player1 : plugin.getServer().getOnlinePlayers()){
                    if (player1.getAddress().getHostName().equals(ip)){
                        BanRuntime.ban(plugin, reason, area, player1.getUniqueId(), ((Player) sender).getUniqueId());
                    }
                }
            }
        }).start();

        return true;
    }
}
