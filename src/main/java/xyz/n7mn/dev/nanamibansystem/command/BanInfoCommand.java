package xyz.n7mn.dev.nanamibansystem.command;

import com.google.gson.Gson;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.plugin.Plugin;
import xyz.n7mn.dev.nanamibansystem.util.BanData;
import xyz.n7mn.dev.nanamibansystem.util.BanRuntime;
import xyz.n7mn.dev.nanamibansystem.util.UUID2Username;
import xyz.n7mn.dev.nanamibansystem.util.Username2UUID;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Map;
import java.util.UUID;

public class BanInfoCommand implements CommandExecutor {

    private final Plugin plugin;
    public BanInfoCommand(Plugin plugin){
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (sender.isOp()){
            new Thread(()->{
                UUID targetUUID = null;
                String username = args[0];
                // Username ----> UUID
                Player player = Bukkit.getServer().getPlayer(username);
                if (player != null){
                    targetUUID = player.getUniqueId();
                }

                if (targetUUID == null){
                    try {
                        OkHttpClient client = new OkHttpClient();
                        Request request = new Request.Builder().url("https://api.mojang.com/users/profiles/minecraft/"+username).build();
                        Response response = client.newCall(request).execute();
                        UUID2Username json = new Gson().fromJson(response.body().string(), UUID2Username.class);
                        targetUUID = json.getUUID();

                    } catch (Exception ex){
                        ex.fillInStackTrace();
                    }
                }
                Map<Integer, BanData> list = BanRuntime.getData(plugin, targetUUID);

                if (list.size() > 0){
                    if (sender instanceof Player){
                        ItemStack stack = new ItemStack(Material.WRITTEN_BOOK);
                        BookMeta meta = (BookMeta) stack.getItemMeta().clone();
                        meta.setTitle(args[0] + "さんのBAN情報");
                        meta.setAuthor("NanamiBanSystem");

                        list.forEach((id, data)->{
                            String areaName = "不明";
                            try {
                                Connection con = DriverManager.getConnection("jdbc:mysql://" + plugin.getConfig().getString("MySQLServer") + ":" + plugin.getConfig().getInt("MySQLPort") + "/" + plugin.getConfig().getString("MySQLDatabase") + plugin.getConfig().getString("MySQLOption"), plugin.getConfig().getString("MySQLUsername"), plugin.getConfig().getString("MySQLPassword"));
                                con.setAutoCommit(true);

                                PreparedStatement statement = con.prepareStatement("SELECT * FROM ServerList WHERE ServerCode = ?");
                                statement.setString(1, data.getArea());
                                ResultSet set = statement.executeQuery();
                                if (set.next()){
                                    areaName = set.getString("ServerName");
                                }
                                set.close();
                                statement.close();
                                con.close();
                            } catch (SQLException ex){
                                ex.printStackTrace();
                            }

                            SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

                            String userName = "";
                            try {
                                OkHttpClient client = new OkHttpClient();
                                Request request = new Request.Builder().url("https://api.mojang.com/user/profiles/"+data.getExecuteUserUUID().toString().replaceAll("-","")+"/names").build();
                                Response response = client.newCall(request).execute();
                                Username2UUID[] json = new Gson().fromJson(response.body().string(), Username2UUID[].class);

                                userName = json[json.length - 1].getName();

                            } catch (Exception ex){
                                ex.printStackTrace();
                            }

                            meta.addPage("" +
                                    "ID : \n" +
                                    data.getBanID()+"\n" +
                                    "理由 :\n" +
                                    data.getReason()+"\n" +
                                    "範囲 :\n" +
                                    areaName + "\n" +
                                    "有効期限 :\n" +
                                    format.format(data.getEndDate()) + "\n" +
                                    "実行日時 :\n" +
                                    format.format(data.getExecuteDate()) + "\n" +
                                    "実行者 :\n" +
                                    userName+" (" + data.getExecuteUserUUID()+")"
                            );
                        });

                        stack.setItemMeta(meta);

                        Player p = (Player) sender;
                        p.getInventory().addItem(stack);
                        p.sendMessage(ChatColor.YELLOW + "[ななみ鯖] "+ChatColor.RESET + args[0] + " さんのBAN情報を本にしました。");
                    } else {
                        list.forEach((id, data)->{
                            String areaName = "不明";
                            try {
                                Connection con = DriverManager.getConnection("jdbc:mysql://" + plugin.getConfig().getString("MySQLServer") + ":" + plugin.getConfig().getInt("MySQLPort") + "/" + plugin.getConfig().getString("MySQLDatabase") + plugin.getConfig().getString("MySQLOption"), plugin.getConfig().getString("MySQLUsername"), plugin.getConfig().getString("MySQLPassword"));
                                con.setAutoCommit(true);

                                PreparedStatement statement = con.prepareStatement("SELECT * FROM ServerList WHERE ServerCode = ?");
                                statement.setString(1, data.getArea());
                                ResultSet set = statement.executeQuery();
                                if (set.next()){
                                    areaName = set.getString("ServerName");
                                }
                                set.close();
                                statement.close();
                                con.close();
                            } catch (SQLException ex){
                                ex.printStackTrace();
                            }

                            SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

                            String userName = "";
                            try {
                                OkHttpClient client = new OkHttpClient();
                                Request request = new Request.Builder().url("https://api.mojang.com/user/profiles/"+data.getExecuteUserUUID().toString().replaceAll("-","")+"/names").build();
                                Response response = client.newCall(request).execute();
                                Username2UUID[] json = new Gson().fromJson(response.body().string(), Username2UUID[].class);

                                userName = json[json.length - 1].getName();

                            } catch (Exception ex){
                                ex.printStackTrace();
                            }

                            sender.sendMessage("" +
                                    "ID : "+data.getBanID()+"\n" +
                                    "理由 : "+data.getReason()+"\n" +
                                    "範囲 : " + areaName + "\n" +
                                    "有効期限 : " + format.format(data.getEndDate()) + "\n" +
                                    "実行日時 : " + format.format(data.getExecuteDate()) + "\n" +
                                    "実行者 : "+userName+" (" + data.getExecuteUserUUID()+")"
                            );
                        });
                    }
                } else {
                    sender.sendMessage(ChatColor.YELLOW + "[ななみ鯖] "+ChatColor.RESET + args[0] + " さんは一度もBANされていません。");
                }


            }).start();
        }

        return true;
    }
}
