package xyz.n7mn.dev.nanamibansystem;

import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;
import xyz.n7mn.dev.api.Ban;
import xyz.n7mn.dev.nanamibansystem.command.*;
import xyz.n7mn.dev.nanamibansystem.tab.BanPlayerList;
import xyz.n7mn.dev.nanamibansystem.tab.PlayerListForTab;

public final class NanamiBanSystem extends JavaPlugin {

    @Override
    public void onEnable() {
        // Plugin startup logic
        saveDefaultConfig();

        Ban banSystem = new Ban(
                getConfig().getString("MySQLServer"),
                getConfig().getInt("MySQLPort"),
                getConfig().getString("MySQLDatabase"),
                getConfig().getString("MySQLOption"),
                getConfig().getString("MySQLUsername"),
                getConfig().getString("MySQLPassword")
        );

        PlayerListForTab tab = new PlayerListForTab(1);
        BanPlayerList tab2 = new BanPlayerList(banSystem);
        PluginCommand gban = getCommand("gban");
        PluginCommand ban = getCommand("lban");
        PluginCommand ipban = getCommand("ipban");
        PluginCommand unban = getCommand("unban");
        PluginCommand baninfo = getCommand("baninfo");

        gban.setExecutor(new GlobalBanCommand(banSystem, this));
        gban.setTabCompleter(tab);
        ban.setExecutor(new BanCommand(banSystem, this));
        ban.setTabCompleter(tab);
        ipban.setExecutor(new IPBanCommand(banSystem, this));
        ipban.setTabCompleter(tab);
        unban.setExecutor(new UnBanCommand(banSystem));
        unban.setTabCompleter(tab2);
        baninfo.setExecutor(new BanInfoCommand(banSystem));
        baninfo.setTabCompleter(tab2);

        getLogger().info(getName() + " Ver "+getDescription().getVersion()+" 起動");
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic

        getLogger().info(getName() + " Ver "+getDescription().getVersion()+" 終了");
    }
}
