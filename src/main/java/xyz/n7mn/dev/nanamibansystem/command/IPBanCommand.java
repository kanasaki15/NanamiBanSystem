package xyz.n7mn.dev.nanamibansystem.command;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import xyz.n7mn.dev.api.Ban;

public class IPBanCommand implements CommandExecutor {

    private final Ban banSystem;
    private final Plugin plugin;
    public IPBanCommand(Ban banSystem, Plugin plugin){
        this.banSystem = banSystem;
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        String area = "all";

        new BukkitRunnable() {
            @Override
            public void run() {

                Player player = Bukkit.getServer().getPlayer(args[0]);
                if (player == null){
                    sender.sendMessage(ChatColor.YELLOW + "[ななみ鯖] " + ChatColor.RED + "そのユーザーはオフラインです。");
                    return;
                }

                String targetHost = player.getAddress().getHostName();

                for (Player player1 : Bukkit.getServer().getOnlinePlayers()){
                    if (player1.getAddress().getHostName().equals(targetHost)){
                        args[0] = player1.getName();
                        BanRuntime.run(plugin, banSystem, area, sender, args);
                    }
                }
            }
        }.runTaskAsynchronously(plugin);

        return true;
    }
}
