package xyz.n7mn.dev.nanamibansystem.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import xyz.n7mn.dev.api.Ban;


public class BanCommand implements CommandExecutor {

    private final Ban banSystem;
    private final Plugin plugin;
    public BanCommand(Ban banSystem, Plugin plugin){
        this.banSystem = banSystem;
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String area = plugin.getConfig().getString("Area");

        new BukkitRunnable() {
            @Override
            public void run() {
                BanRuntime.run(plugin, banSystem, area, sender, args);
            }
        }.runTaskAsynchronously(plugin);

        return true;
    }
}
