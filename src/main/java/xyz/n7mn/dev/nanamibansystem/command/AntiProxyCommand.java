package xyz.n7mn.dev.nanamibansystem.command;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class AntiProxyCommand implements CommandExecutor {

    private final Plugin plugin;
    public AntiProxyCommand(Plugin plugin){
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (sender instanceof Player){
            Player player = (Player) sender;
            if (!player.isOp() && !player.hasPermission("bansystem.ban")){
                return true;
            }
        }

        boolean isAntiProxy = !plugin.getConfig().getBoolean("isAntiProxy");
        plugin.getConfig().set("isAntiProxy", isAntiProxy);
        sender.sendMessage(ChatColor.YELLOW + "[ななみ鯖] " + ChatColor.RESET + "AntiProxy機能を " + isAntiProxy + "にしました。");

        return true;
    }
}
