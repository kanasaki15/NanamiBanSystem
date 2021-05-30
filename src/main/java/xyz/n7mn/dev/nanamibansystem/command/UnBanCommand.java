package xyz.n7mn.dev.nanamibansystem.command;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import xyz.n7mn.dev.api.Ban;

import java.sql.SQLException;

public class UnBanCommand implements CommandExecutor {

    private final Ban banSystem;
    public UnBanCommand(Ban banSystem){
        this.banSystem = banSystem;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (sender instanceof Player){
            Player player = (Player) sender;

            if (!player.hasPermission("bansystem.unban") && !player.isOp()){
                sender.sendMessage(ChatColor.YELLOW + "[ななみ鯖] " + ChatColor.RED + "権限がないっすよ。");
                return true;
            }
        }

        if (args.length != 1){
            sender.sendMessage(ChatColor.YELLOW + "[ななみ鯖] " + ChatColor.RESET + "/unban <BanID>");
            return true;
        }

        try {
            int id = Integer.parseInt(args[0]);

            new Thread(()->{
                try {
                    banSystem.delList(id);
                    sender.sendMessage(ChatColor.YELLOW + "[ななみ鯖] " + ChatColor.RESET + "ID : "+id+" をBAN解除しました。");
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }).start();
        } catch (Exception e){
            sender.sendMessage(ChatColor.YELLOW + "[ななみ鯖] " + ChatColor.RESET + "/unban <BanID>");
            e.printStackTrace();
        }

        return true;
    }
}
