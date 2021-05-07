package xyz.n7mn.dev.nanamibansystem.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import xyz.n7mn.dev.api.Ban;

public class UnBanCommand implements CommandExecutor {

    private final Ban banSystem;
    public UnBanCommand(Ban banSystem){
        this.banSystem = banSystem;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {



        return true;
    }
}
