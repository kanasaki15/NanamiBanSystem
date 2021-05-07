package xyz.n7mn.dev.nanamibansystem.command;

import com.google.gson.Gson;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import xyz.n7mn.dev.api.Ban;
import xyz.n7mn.dev.api.data.BanData;

import java.io.IOException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.UUID;

public class BanInfoCommand implements CommandExecutor {
    private final Ban banSystem;
    public BanInfoCommand(Ban banSystem){
        this.banSystem = banSystem;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (args.length == 0){
            new Thread(()->{
                List<BanData> list;
                List<BanData> list2;
                try {
                    list = banSystem.getList(false);
                    list2 = banSystem.getList(true);
                } catch (SQLException e){
                    e.printStackTrace();
                    sender.sendMessage(ChatColor.YELLOW + "[ななみ鯖] "+ChatColor.RED+"取得に失敗しました。");
                    return;
                }

                sender.sendMessage("" +
                        "---- ななみ鯖 BanSystem ----\n" +
                        "BAN件数 : " + list.size() + "\n" +
                        "(そのうちの有効な件数 : " + list2.size() + ")\n" +
                        "BAN詳細は「/baninfo <mcid>」と入力してください。"
                );
            }).start();

            return true;
        }

        if (args.length != 1){
            return true;
        }

        new Thread(()->{

            UUID targetUUID = null;

            try {
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder()
                        .url("https://api.mojang.com/users/profiles/minecraft/"+args[0])
                        .build();
                Response response = client.newCall(request).execute();
                String json = response.body().string();
                ApiData data = new Gson().fromJson(json, ApiData.class);
                targetUUID = UUID.fromString(data.getId().replaceFirst("([0-9a-fA-F]{8})([0-9a-fA-F]{4})([0-9a-fA-F]{4})([0-9a-fA-F]{4})([0-9a-fA-F]+)", "$1-$2-$3-$4-$5"));
                response.close();
            } catch (IOException e){
                e.printStackTrace();
                sender.sendMessage(ChatColor.YELLOW + "[ななみ鯖] "+ChatColor.RED+"存在しないユーザーのようです。");
                return;
            }

            List<BanData> list;
            try {
                list = banSystem.getList(targetUUID, true);
            } catch (SQLException e){
                e.printStackTrace();
                sender.sendMessage(ChatColor.YELLOW + "[ななみ鯖] "+ChatColor.RED+"取得に失敗しました。");
                return;
            }

            StringBuffer sb = new StringBuffer("--- " + args[0] + "さんの情報 ---\n");
            if (list.size() == 0){
                sb.append("BAN情報なし");
            }
            for (BanData data : list){
                if (data.isActive()){
                    sb.append(ChatColor.GREEN);
                } else {
                    sb.append(ChatColor.RED);
                }
                sb.append("---- ID : ");
                sb.append(data.getBanID());
                sb.append(" ----\n");
                sb.append(ChatColor.RESET);
                sb.append("理由 : ");
                sb.append(data.getReason());
                sb.append("\n");
                sb.append("解除日時 : ");
                sb.append(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(data.getEndDate()));
                sb.append("\n");
            }

            sender.sendMessage(sb.toString());
        }).start();

        return true;
    }
}
