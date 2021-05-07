package xyz.n7mn.dev.nanamibansystem.command;

import com.google.gson.Gson;
import okhttp3.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import xyz.n7mn.dev.api.Ban;

import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class BanRuntime {

    public static void run(Plugin plugin, Ban banSystem, String area, CommandSender sender, String[] args){
        if (area.length() == 0){
            area = "all";
        }

        if (sender instanceof Player){
            Player player = (Player) sender;

            if (!player.hasPermission("bansystem.ban") && !player.isOp()){
                sender.sendMessage(ChatColor.YELLOW + "[ななみ鯖] " + ChatColor.RED + "権限がないっすよ。");
                return;
            }
        }

        if (args.length == 0){
            sender.sendMessage(ChatColor.YELLOW + "[ななみ鯖] " + ChatColor.RED + "「/lban <MCID>」でお願いしますよ。");
            return;
        }

        if (args.length == 1){
            String targetName = args[0];
            UUID targetUUID = null;
            boolean isOnline = false;
            String ip = "";

            for (Player player : plugin.getServer().getOnlinePlayers()){
                if (player.getName().equals(targetName)){
                    isOnline = true;
                    targetName = player.getName();
                    targetUUID = player.getUniqueId();
                    ip = player.getAddress().getHostName();
                    break;
                }
            }

            if (!isOnline){
                try {
                    OkHttpClient client = new OkHttpClient();
                    Request request = new Request.Builder()
                            .url("https://api.mojang.com/users/profiles/minecraft/"+targetName)
                            .build();
                    Response response = client.newCall(request).execute();
                    String json = response.body().string();
                    ApiData data = new Gson().fromJson(json, ApiData.class);
                    targetUUID = UUID.fromString(data.getId().replaceFirst("([0-9a-fA-F]{8})([0-9a-fA-F]{4})([0-9a-fA-F]{4})([0-9a-fA-F]{4})([0-9a-fA-F]+)", "$1-$2-$3-$4-$5"));
                    response.close();
                } catch (IOException e){
                    e.printStackTrace();
                    sender.sendMessage(ChatColor.YELLOW + "[ななみ鯖] " + ChatColor.RED + "エラーが発生しました。 Mojang APIへのアクセスに失敗したようです。");
                    return;
                } catch (IllegalArgumentException e){
                    targetUUID = null;
                }
            }

            if (targetUUID == null){
                sender.sendMessage(ChatColor.YELLOW + "[ななみ鯖] " + ChatColor.RED + "そのユーザー名は存在しないようです。");
                return;
            }

            if (sender instanceof Player){
                Player player = (Player) sender;
                try {
                    banSystem.addList(targetUUID, "(未記入)", area, ip, player.getUniqueId(), new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("9999-12-31 23:59:59"));
                } catch (SQLException | ParseException e) {
                    e.printStackTrace();
                    sender.sendMessage(ChatColor.YELLOW + "[ななみ鯖] " + ChatColor.RED + "エラーが発生しました。 DB接続に失敗したようです。");
                }
            } else {
                try {
                    banSystem.addList(targetUUID, "(未記入)", area, ip, UUID.fromString(plugin.getConfig().getString("AdminUUID")), new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("9999-12-31 23:59:59"));
                } catch (SQLException | ParseException e) {
                    e.printStackTrace();
                    sender.sendMessage(ChatColor.YELLOW + "[ななみ鯖] " + ChatColor.RED + "エラーが発生しました。 DB接続に失敗したようです。");
                }
            }

            if (isOnline){
                UUID finalTargetUUID = targetUUID;
                Bukkit.getScheduler().runTask(plugin, new Runnable() {
                    @Override
                    public void run() {
                        plugin.getServer().getPlayer(finalTargetUUID).kickPlayer("\n" +
                                "--- ななみ鯖 ---\n" +
                                "以下の理由でBANされました。\n" +
                                "理由 : (未記入)"
                        );
                    }
                });
            }

            for (Player player : Bukkit.getServer().getOnlinePlayers()){
                if (player.hasPermission("bansystem.ban") || player.isOp()){
                    player.sendMessage(ChatColor.YELLOW + "[ななみ鯖] " + ChatColor.GREEN + targetName+"さんがBANされました。\n"+ChatColor.RESET+"理由 : (未記入)\n期間 : 9999/12/31 23:59:59");
                }
            }
            plugin.getLogger().info(targetName + " ("+targetUUID+")をBANしました。");

            try {
                DiscordWebhookData data;
                if (isOnline){
                    data = new DiscordWebhookData("" +
                            "新たにBANされました。\n" +
                            "MinecraftID : "+targetName+" ("+targetUUID.toString()+")\n" +
                            "理由 : 未記入",
                            "NanamiBanSystem",
                            "https://n7mn.xyz/nanamibot.png",
                            false
                    );
                } else {
                    data = new DiscordWebhookData("" +
                            "新たにBANされました。\n" +
                            "MinecraftID : "+targetName+" (オフライン)\n" +
                            "理由 : 未記入",
                            "NanamiBanSystem",
                            "https://n7mn.xyz/nanamibot.png",
                            false
                    );
                }

                OkHttpClient client = new OkHttpClient();
                RequestBody body = RequestBody.create(MediaType.get("application/json; charset=utf-8"), new Gson().toJson(data));
                Request request = new Request.Builder()
                        .url(plugin.getConfig().getString("DiscordWebHook"))
                        .post(body)
                        .build();
                Response response = client.newCall(request).execute();
                response.close();
            } catch (IOException e){
                e.printStackTrace();
                sender.sendMessage(ChatColor.YELLOW + "[ななみ鯖] " + ChatColor.RED + "エラーが発生しました。 Mojang APIへのアクセスに失敗したようです。");
                return;
            }
        }


        String timeText = "";
        String Reason = "";
        String targetName = "";
        UUID targetUUID = null;
        String targetIp = "";
        Date endDate = null;
        boolean isOnline = false;
        try {
            endDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("9999-12-31 23:59:59");
        } catch (ParseException e){
            e.printStackTrace();
        }

        StringBuilder sb = new StringBuilder();
        int i = 0;
        for (String arg : args){
            if (i == 0){
                targetName = arg;
                i++;
                continue;
            }
            if (i > 0 && arg.toLowerCase(Locale.ROOT).startsWith("t:") || arg.toLowerCase(Locale.ROOT).startsWith("time:")){
                timeText = arg;
                i++;
                continue;
            }

            sb.append(arg);
            sb.append(" ");
            i++;
        }
        if (sb.length() > 0){
            Reason = sb.substring(0, sb.length() - 1);
        }

        for (Player player : Bukkit.getServer().getOnlinePlayers()){
            if (player.getName().toLowerCase().equals(targetName)){
                targetName = player.getName();
                targetUUID = player.getUniqueId();
                targetIp = player.getAddress().getHostName();
                isOnline = true;
                break;
            }
        }

        if (!isOnline){
            try {
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder()
                        .url("https://api.mojang.com/users/profiles/minecraft/"+targetName)
                        .build();
                Response response = client.newCall(request).execute();
                String json = response.body().string();
                ApiData data = new Gson().fromJson(json, ApiData.class);
                targetUUID = UUID.fromString(data.getId().replaceFirst("([0-9a-fA-F]{8})([0-9a-fA-F]{4})([0-9a-fA-F]{4})([0-9a-fA-F]{4})([0-9a-fA-F]+)", "$1-$2-$3-$4-$5"));
                response.close();
            } catch (IOException e){
                e.printStackTrace();
                sender.sendMessage(ChatColor.YELLOW + "[ななみ鯖] " + ChatColor.RED + "エラーが発生しました。 Mojang APIへのアクセスに失敗したようです。");
                return;
            } catch (IllegalArgumentException e){
                targetUUID = null;
            }
        }

        if (targetUUID == null){
            sender.sendMessage(ChatColor.YELLOW + "[ななみ鯖] " + ChatColor.RED + "そのユーザー名は存在しないようです。");
            return;
        }

        Player player = null;
        if (sender instanceof Player){
            player = (Player) sender;
        }

        if (timeText.length() > 0){
            Matcher matcher = Pattern.compile("(\\d+)").matcher(timeText);
            // Integer.parseInt(matcher.group(1)
            if (matcher.find()){
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(new Date());
                if (timeText.toLowerCase().endsWith("s")){
                    calendar.add(Calendar.SECOND, Integer.parseInt(matcher.group(1)));
                } else if (timeText.toLowerCase().endsWith("m")){
                    calendar.add(Calendar.MINUTE, Integer.parseInt(matcher.group(1)));
                } else if (timeText.toLowerCase().endsWith("h")){
                    calendar.add(Calendar.HOUR, Integer.parseInt(matcher.group(1)));
                } else if (timeText.toLowerCase().endsWith("d")){
                    calendar.add(Calendar.DAY_OF_MONTH, Integer.parseInt(matcher.group(1)));
                } else {
                    calendar.add(Calendar.SECOND, Integer.parseInt(matcher.group(1)));
                }

                endDate = calendar.getTime();
            }
        }

        try {
            if (isOnline) {
                banSystem.addList(targetUUID, Reason, area, targetIp, player.getUniqueId(), endDate);
            } else {
                banSystem.addList(targetUUID, Reason, area, targetIp, UUID.fromString(plugin.getConfig().getString("AdminUUID")), endDate);
            }
        } catch (Exception e){
            e.printStackTrace();
            sender.sendMessage(ChatColor.YELLOW + "[ななみ鯖] " + ChatColor.RED + "エラーが発生しました。 DB接続に失敗したようです。");
        }

        if (isOnline){
            UUID finalTargetUUID = targetUUID;
            Bukkit.getScheduler().runTask(plugin, new Runnable() {
                @Override
                public void run() {
                    plugin.getServer().getPlayer(finalTargetUUID).kickPlayer("\n" +
                            "--- ななみ鯖 ---\n" +
                            "以下の理由でBANされました。\n" +
                            "理由 : (未記入)"
                    );
                }
            });
        }

        for (Player player1 : Bukkit.getServer().getOnlinePlayers()){
            if (player1.hasPermission("bansystem.ban") || player1.isOp()){
                player1.sendMessage(ChatColor.YELLOW + "[ななみ鯖] " + ChatColor.GREEN + targetName+"さんがBANされました。\n"+ChatColor.RESET+"理由 : "+Reason+"\n期間 : "+new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(endDate));
            }
        }
        plugin.getLogger().info(targetName + " ("+targetUUID+")をBANしました。");

        DiscordWebhookData data;
        if (isOnline){
            data = new DiscordWebhookData("" +
                    "新たにBANされました。\n" +
                    "MinecraftID : "+targetName+" ("+targetUUID.toString()+")\n" +
                    "理由 : "+Reason+"\n" +
                    "有効期限 : "+new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(endDate),
                    "NanamiBanSystem",
                    "https://n7mn.xyz/nanamibot.png",
                    false
            );
        } else {
            data = new DiscordWebhookData("" +
                    "新たにBANされました。\n" +
                    "MinecraftID : "+targetName+" (オフライン)\n" +
                    "理由 : "+Reason+"\n" +
                    "有効期限 : "+new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(endDate),
                    "NanamiBanSystem",
                    "https://n7mn.xyz/nanamibot.png",
                    false
            );
        }

        try {
            OkHttpClient client = new OkHttpClient();
            RequestBody body = RequestBody.create(MediaType.get("application/json; charset=utf-8"), new Gson().toJson(data));
            Request request = new Request.Builder()
                    .url(plugin.getConfig().getString("DiscordWebHook"))
                    .post(body)
                    .build();
            Response response = client.newCall(request).execute();
            response.close();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

}
