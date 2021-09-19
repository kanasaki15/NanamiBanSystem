package xyz.n7mn.dev.nanamibansystem;

import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;
import xyz.n7mn.dev.nanamibansystem.command.*;
import xyz.n7mn.dev.nanamibansystem.tab.PlayerListForTab;

public final class NanamiBanSystem extends JavaPlugin {

    @Override
    public void onEnable() {
        // Plugin startup logic
        saveDefaultConfig();

        PlayerListForTab tab = new PlayerListForTab(1);
        PluginCommand gban = getCommand("gban");
        PluginCommand ban = getCommand("lban");
        PluginCommand ipban = getCommand("ipban");
        PluginCommand unban = getCommand("unban");
        PluginCommand baninfo = getCommand("baninfo");
        PluginCommand antiProxy = getCommand("antiproxy");
        PluginCommand antiVPN = getCommand("antivpn");

        gban.setExecutor(new GlobalBanCommand(this));
        gban.setTabCompleter(tab);
        ban.setExecutor(new BanCommand(this));
        ban.setTabCompleter(tab);
        ipban.setExecutor(new IPBanCommand(this));
        ipban.setTabCompleter(tab);
        unban.setExecutor(new UnBanCommand());
        unban.setTabCompleter(tab);
        baninfo.setExecutor(new BanInfoCommand());
        baninfo.setTabCompleter(tab);

        antiProxy.setExecutor(new AntiProxyCommand(this));
        antiVPN.setExecutor(new AntiVPNCommand(this));

        getServer().getPluginManager().registerEvents(new EventListener(this), this);
        getLogger().info(getName() + " Ver "+getDescription().getVersion()+" 起動");
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic

        getLogger().info(getName() + " Ver "+getDescription().getVersion()+" 終了");
    }
}
