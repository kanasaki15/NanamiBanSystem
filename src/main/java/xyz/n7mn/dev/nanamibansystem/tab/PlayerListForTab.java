package xyz.n7mn.dev.nanamibansystem.tab;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class PlayerListForTab implements TabExecutor {

    private final int maxArgs;

    public PlayerListForTab(int maxArgs){
        this.maxArgs = maxArgs;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (args.length <= maxArgs){
            return true;
        }

        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {

        List<String> list = new ArrayList<>();

        if (args.length == 0){
            for (Player player : Bukkit.getServer().getOnlinePlayers()){
                list.add(player.getName());
            }

            return list;
        }

        String arg = args[args.length - 1];

        for (Player player : Bukkit.getServer().getOnlinePlayers()){
            if (player.getName().toLowerCase().startsWith(arg)){
                list.add(player.getName());
            }
        }

        return list;
    }
}
