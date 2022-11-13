package xyz.n7mn.dev.nanamibansystem;

import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public final class NanamiBanSystem extends JavaPlugin {

    @Override
    public void onEnable() {
        // Plugin startup logic
        saveDefaultConfig();

        PluginCommand gban = getCommand("gban");
        PluginCommand ban = getCommand("lban");
        PluginCommand ipban = getCommand("ipban");
        PluginCommand unban = getCommand("unban");
        PluginCommand baninfo = getCommand("baninfo");
        PluginCommand antiProxy = getCommand("antiproxy");
        PluginCommand antiVPN = getCommand("antivpn");
        PluginCommand alert = getCommand("ban-alert");
        PluginCommand alert2 = getCommand("vpn-alert");
        PluginCommand report = getCommand("report");




        //getServer().getPluginManager().registerEvents(new EventListener(this), this);
        getLogger().info(getName() + " Ver "+getDescription().getVersion()+" 起動");
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic

        getLogger().info(getName() + " Ver "+getDescription().getVersion()+" 終了");
    }
}
