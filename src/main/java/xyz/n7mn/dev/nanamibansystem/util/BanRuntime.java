package xyz.n7mn.dev.nanamibansystem.util;

import com.google.gson.Gson;
import okhttp3.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.sql.*;
import java.sql.Connection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BanRuntime {
    public static void ban(Plugin plugin, String Reason, String Area, UUID TargetPlayer, UUID FromPlayer){
        // BAN処理
        if (TargetPlayer == null){
            return;
        }

        new Thread(()->{
            boolean isOnline = false;
            String ip = "";
            String banUserName = "";
            for (Player player : Bukkit.getOnlinePlayers()){
                if (player.getUniqueId().equals(TargetPlayer)){
                    isOnline = true;
                    ip = player.getAddress().getHostName();
                    banUserName = player.getName();
                    break;
                }
            }

            if (isOnline){
                // オンライン
                Bukkit.getScheduler().runTask(plugin, ()->{
                    plugin.getServer().getPlayer(TargetPlayer).kickPlayer(Reason);
                });
            } else {

                OfflinePlayer player = plugin.getServer().getOfflinePlayer(TargetPlayer);
                if (player != null){
                    banUserName = player.getName();
                }

                if (player == null || banUserName == null) {
                    //System.out.println("aiue");
                    try {
                        OkHttpClient client = new OkHttpClient();
                        Request request = new Request.Builder().url("https://sessionserver.mojang.com/session/minecraft/profile/"+TargetPlayer.toString().replaceAll("-","")+"").build();
                        Response response = client.newCall(request).execute();
                        UserProfile json = new Gson().fromJson(response.body().string(), UserProfile.class);

                        banUserName = json.getUserName();

                    } catch (Exception ex){
                        ex.printStackTrace();
                    }
                }
            }

            try {
                boolean found = false;
                Enumeration<Driver> drivers = DriverManager.getDrivers();

                while (drivers.hasMoreElements()){
                    Driver driver = drivers.nextElement();
                    if (driver.equals(new com.mysql.cj.jdbc.Driver())){
                        found = true;
                        break;
                    }
                }

                if (!found){
                    DriverManager.registerDriver(new com.mysql.cj.jdbc.Driver());
                }
            } catch (SQLException e){
                e.printStackTrace();
            }

            Date endDate = null;
            try {
                endDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("9999-12-31 23:59:59");
            } catch (ParseException ex) {
                ex.printStackTrace();
            }
            try {
                int banId = 0;

                Connection con = DriverManager.getConnection("jdbc:mysql://" + plugin.getConfig().getString("MySQLServer") + ":"+plugin.getConfig().getInt("MySQLPort") + "/" + plugin.getConfig().getString("MySQLDatabase") + plugin.getConfig().getString("MySQLOption"), plugin.getConfig().getString("MySQLUsername"), plugin.getConfig().getString("MySQLPassword"));
                con.setAutoCommit(true);
                PreparedStatement statement1 = con.prepareStatement("SELECT BanID FROM BanList ORDER BY BanID DESC");
                ResultSet set1 = statement1.executeQuery();
                if (set1.next()){
                    banId = set1.getInt("BanID");
                }
                set1.close();
                statement1.close();

                PreparedStatement statement2 = con.prepareStatement("INSERT INTO `BanList`(`BanID`, `UserUUID`, `Reason`, `Area`, `IP`, `EndDate`, `ExecuteDate`, `ExecuteUserUUID`, `Active`) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)");
                statement2.setInt(1, banId + 1);
                statement2.setString(2, TargetPlayer.toString());
                statement2.setString(3, Reason);
                statement2.setString(4, Area);
                statement2.setString(5, ip);
                statement2.setTimestamp(6, new Timestamp(endDate.getTime()));
                statement2.setTimestamp(7, new Timestamp((new java.util.Date()).getTime()));
                if (FromPlayer != null){
                    statement2.setString(8, FromPlayer.toString());
                } else {
                    statement2.setString(8, plugin.getConfig().getString("AdminUUID"));
                }
                statement2.setBoolean(9, true);
                statement2.execute();
                statement2.close();
                con.close();
            } catch (SQLException ex){
                ex.printStackTrace();
            }

            String content = "" +
                    "新たにBANされました。\n" +
                    "MinecraftID: "+banUserName+" ("+TargetPlayer+")\n" +
                    "オンライン: " + isOnline + "\n" +
                    "理由: "+Reason+"\n" +
                    "有効期限: "+new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(endDate)+"\n" +
                    "BAN実行者: " + plugin.getServer().getPlayer(FromPlayer).getName() + "(" + FromPlayer + ")";
            String username = "NanamiBanSystem Ver "+plugin.getDescription().getVersion();
            String avatar_url = "https://n7mn.xyz/nanamibot.png";

            try {
                DiscordWebhookData data = new DiscordWebhookData(content, username, avatar_url, false);
                OkHttpClient client = new OkHttpClient();
                RequestBody body = RequestBody.create(MediaType.get("application/json; charset=utf-8"), new Gson().toJson(data));
                Request request = new Request.Builder()
                        .url(plugin.getConfig().getString("DiscordWebHook"))
                        .post(body)
                        .build();
                Response response = client.newCall(request).execute();
                response.close();
            } catch (Exception ex){
                ex.printStackTrace();
            }

            plugin.getServer().getConsoleSender().sendMessage(""+
                    ChatColor.YELLOW + "[ななみ鯖] " + ChatColor.RESET + banUserName+"さんが以下の理由でBANされました。\n" +
                    "理由 : " + Reason+"\n" +
                    "実行者 : " + FromPlayer);
            for (Player player : plugin.getServer().getOnlinePlayers()){
                if (player.getUniqueId().equals(FromPlayer)){
                    player.sendMessage(ChatColor.YELLOW + "[ななみ鯖] " + ChatColor.RESET + banUserName+"さんをBANしました。");
                    continue;
                }

                if (player.isOp()){
                    player.sendMessage(""+
                            ChatColor.YELLOW + "[ななみ鯖] " + ChatColor.RESET + banUserName+"さんが以下の理由でBANされました。\n" +
                            "理由 : " + Reason
                    );
                }
            }

        }).start();
    }

    @Deprecated
    public static void unban(Plugin plugin, UUID targetPlayer){
        if (isBan(plugin, targetPlayer)){
            getData(plugin, targetPlayer).forEach((id, data)->{
                unban(plugin, id);
            });
        }
    }

    public static boolean unban(Plugin plugin, int BanID){

        try {
            boolean found = false;
            Enumeration<Driver> drivers = DriverManager.getDrivers();

            while (drivers.hasMoreElements()) {
                Driver driver = drivers.nextElement();
                if (driver.equals(new com.mysql.cj.jdbc.Driver())) {
                    found = true;
                    break;
                }
            }

            if (!found) {
                DriverManager.registerDriver(new com.mysql.cj.jdbc.Driver());
            }

            Connection con = DriverManager.getConnection("jdbc:mysql://" + plugin.getConfig().getString("MySQLServer") + ":" + plugin.getConfig().getInt("MySQLPort") + "/" + plugin.getConfig().getString("MySQLDatabase") + plugin.getConfig().getString("MySQLOption"), plugin.getConfig().getString("MySQLUsername"), plugin.getConfig().getString("MySQLPassword"));
            con.setAutoCommit(true);

            PreparedStatement statement1 = con.prepareStatement("UPDATE `BanList` SET Active = 0 WHERE BanID = ?");
            statement1.setInt(1, BanID);
            statement1.execute();

            statement1.close();
            con.close();

            return true;
        } catch (SQLException ex){
            ex.printStackTrace();
        }

        return false;
    }


    @Deprecated
    public static boolean isBan(Plugin plugin, UUID targetPlayer){
        try {
            boolean found = false;
            Enumeration<Driver> drivers = DriverManager.getDrivers();

            while (drivers.hasMoreElements()) {
                Driver driver = drivers.nextElement();
                if (driver.equals(new com.mysql.cj.jdbc.Driver())) {
                    found = true;
                    break;
                }
            }

            if (!found) {
                DriverManager.registerDriver(new com.mysql.cj.jdbc.Driver());
            }

            found = false;

            Connection con = DriverManager.getConnection("jdbc:mysql://" + plugin.getConfig().getString("MySQLServer") + ":" + plugin.getConfig().getInt("MySQLPort") + "/" + plugin.getConfig().getString("MySQLDatabase") + plugin.getConfig().getString("MySQLOption"), plugin.getConfig().getString("MySQLUsername"), plugin.getConfig().getString("MySQLPassword"));
            con.setAutoCommit(true);

            PreparedStatement statement1 = con.prepareStatement("SELECT BanID FROM BanList WHERE UserUUID = ? AND EndDate >= ? AND Active = 1 ORDER BY BanID DESC");
            statement1.setString(1, targetPlayer.toString());
            statement1.setTimestamp(2, new Timestamp(new Date().getTime()));

            ResultSet set1 = statement1.executeQuery();

            if (set1.next()){
                found = set1.getInt("BanID") > 0;
            }

            set1.close();
            statement1.close();
            con.close();

            return found;
        } catch (SQLException ex){
            ex.printStackTrace();
        }

        return false;
    }

    public static boolean isBan(Plugin plugin, UUID targetPlayer, String area){
        try {
            boolean found = false;
            Enumeration<Driver> drivers = DriverManager.getDrivers();

            while (drivers.hasMoreElements()) {
                Driver driver = drivers.nextElement();
                if (driver.equals(new com.mysql.cj.jdbc.Driver())) {
                    found = true;
                    break;
                }
            }

            if (!found) {
                DriverManager.registerDriver(new com.mysql.cj.jdbc.Driver());
            }

            found = false;

            Connection con = DriverManager.getConnection("jdbc:mysql://" + plugin.getConfig().getString("MySQLServer") + ":" + plugin.getConfig().getInt("MySQLPort") + "/" + plugin.getConfig().getString("MySQLDatabase") + plugin.getConfig().getString("MySQLOption"), plugin.getConfig().getString("MySQLUsername"), plugin.getConfig().getString("MySQLPassword"));
            con.setAutoCommit(true);

            PreparedStatement statement1 = con.prepareStatement("SELECT BanID FROM BanList WHERE UserUUID = ? AND Area = ? AND EndDate >= ? AND Active = 1 ORDER BY BanID DESC");
            statement1.setString(1, targetPlayer.toString());
            statement1.setString(2, area);
            statement1.setTimestamp(3, new Timestamp(new Date().getTime()));

            ResultSet set1 = statement1.executeQuery();

            if (set1.next()){
                found = set1.getInt("BanID") > 0;
            }

            set1.close();
            statement1.close();
            con.close();

            return found;
        } catch (SQLException ex){
            ex.printStackTrace();
        }

        return false;
    }

    @Deprecated
    public static Map<Integer, String> getReason(Plugin plugin, UUID targetPlayer){

        HashMap<Integer, String> map = new HashMap<>();

        try {
            boolean found = false;
            Enumeration<Driver> drivers = DriverManager.getDrivers();

            while (drivers.hasMoreElements()) {
                Driver driver = drivers.nextElement();
                if (driver.equals(new com.mysql.cj.jdbc.Driver())) {
                    found = true;
                    break;
                }
            }

            if (!found) {
                DriverManager.registerDriver(new com.mysql.cj.jdbc.Driver());
            }

            Connection con = DriverManager.getConnection("jdbc:mysql://" + plugin.getConfig().getString("MySQLServer") + ":" + plugin.getConfig().getInt("MySQLPort") + "/" + plugin.getConfig().getString("MySQLDatabase") + plugin.getConfig().getString("MySQLOption"), plugin.getConfig().getString("MySQLUsername"), plugin.getConfig().getString("MySQLPassword"));
            con.setAutoCommit(true);

            PreparedStatement statement1 = con.prepareStatement("SELECT * FROM BanList WHERE UserUUID = ? AND EndDate >= ? AND Active = 1 ORDER BY BanID DESC");
            statement1.setString(1, targetPlayer.toString());
            statement1.setTimestamp(2, new Timestamp(new Date().getTime()));

            ResultSet set1 = statement1.executeQuery();

            while (set1.next()){
                map.put(set1.getInt("BanID"), set1.getString("Reason"));
            }

            set1.close();
            statement1.close();
            con.close();

            return map;
        } catch (SQLException ex){
            ex.printStackTrace();
        }

        return map;
    }

    public static Map<Integer, String> getReason(Plugin plugin, UUID targetPlayer, String Area){

        HashMap<Integer, String> map = new HashMap<>();

        try {
            boolean found = false;
            Enumeration<Driver> drivers = DriverManager.getDrivers();

            while (drivers.hasMoreElements()) {
                Driver driver = drivers.nextElement();
                if (driver.equals(new com.mysql.cj.jdbc.Driver())) {
                    found = true;
                    break;
                }
            }

            if (!found) {
                DriverManager.registerDriver(new com.mysql.cj.jdbc.Driver());
            }

            Connection con = DriverManager.getConnection("jdbc:mysql://" + plugin.getConfig().getString("MySQLServer") + ":" + plugin.getConfig().getInt("MySQLPort") + "/" + plugin.getConfig().getString("MySQLDatabase") + plugin.getConfig().getString("MySQLOption"), plugin.getConfig().getString("MySQLUsername"), plugin.getConfig().getString("MySQLPassword"));
            con.setAutoCommit(true);

            PreparedStatement statement1 = con.prepareStatement("SELECT * FROM BanList WHERE UserUUID = ? AND Area = ? AND EndDate >= ? AND Active = 1 ORDER BY BanID DESC");
            statement1.setString(1, targetPlayer.toString());
            statement1.setString(2, Area);
            statement1.setTimestamp(3, new Timestamp(new Date().getTime()));

            ResultSet set1 = statement1.executeQuery();

            while (set1.next()){
                map.put(set1.getInt("BanID"), set1.getString("Reason"));
            }

            set1.close();
            statement1.close();
            con.close();

            return map;
        } catch (SQLException ex){
            ex.printStackTrace();
        }

        return map;
    }


    public static Map<Integer, BanData> getData(Plugin plugin, UUID targetPlayer){

        HashMap<Integer, BanData> map = new HashMap<>();

        try {
            boolean found = false;
            Enumeration<Driver> drivers = DriverManager.getDrivers();

            while (drivers.hasMoreElements()) {
                Driver driver = drivers.nextElement();
                if (driver.equals(new com.mysql.cj.jdbc.Driver())) {
                    found = true;
                    break;
                }
            }

            if (!found) {
                DriverManager.registerDriver(new com.mysql.cj.jdbc.Driver());
            }

            Connection con = DriverManager.getConnection("jdbc:mysql://" + plugin.getConfig().getString("MySQLServer") + ":" + plugin.getConfig().getInt("MySQLPort") + "/" + plugin.getConfig().getString("MySQLDatabase") + plugin.getConfig().getString("MySQLOption"), plugin.getConfig().getString("MySQLUsername"), plugin.getConfig().getString("MySQLPassword"));
            con.setAutoCommit(true);

            PreparedStatement statement1 = con.prepareStatement("SELECT * FROM BanList WHERE UserUUID = ? ORDER BY BanID DESC");
            statement1.setString(1, targetPlayer.toString());

            ResultSet set1 = statement1.executeQuery();

            while (set1.next()){
                map.put(
                        set1.getInt("BanID"),
                        new BanData(
                                set1.getInt("BanID"),
                                UUID.fromString(set1.getString("UserUUID")),
                                set1.getString("Reason"),
                                set1.getString("Area"),
                                set1.getString("IP"),
                                new Date(set1.getTimestamp("EndDate").getTime()),
                                new Date(set1.getTimestamp("ExecuteDate").getTime()),
                                UUID.fromString(set1.getString("ExecuteUserUUID")),
                                set1.getBoolean("Active")
                        )
                );
            }

            set1.close();
            statement1.close();
            con.close();

            return map;
        } catch (SQLException ex){
            ex.printStackTrace();
        }

        return map;

    }


}
