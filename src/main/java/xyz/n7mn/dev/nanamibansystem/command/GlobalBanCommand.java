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

public class GlobalBanCommand implements CommandExecutor {

    private final Plugin plugin;
    public GlobalBanCommand(Plugin plugin){
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        // コンソールからは実行できないようにする
        if (!(sender instanceof Player)){
            return true;
        }

        new Thread(()->{
            String username = args[0];
            UUID targetUUID = null;

            // 理由空白対策
            StringBuilder sb = new StringBuilder();
            for (int i = 1; i < args.length; i++){
                if (i != 1){
                    sb.append(" ");
                }
                sb.append(args[i]);
            }

            // Username ----> UUID
            try {
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder().url("https://api.mojang.com/users/profiles/minecraft/"+username).build();
                Response response = client.newCall(request).execute();
                UUID2Username json = new Gson().fromJson(response.body().string(), UUID2Username.class);
                targetUUID = json.getUUID();

            } catch (Exception ex){
                ex.fillInStackTrace();
            }

            BanRuntime.ban(plugin, sb.toString(), "all", targetUUID, ((Player) sender).getUniqueId());

        }).start();

        return true;
    }
}
