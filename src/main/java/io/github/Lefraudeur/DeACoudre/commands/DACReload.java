package io.github.Lefraudeur.DeACoudre.commands;

import io.github.Lefraudeur.DeACoudre.Utils.Utilities;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class DACReload implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        Utilities.loadConfig();
        commandSender.sendMessage("Plugin Reloaded");
        return true;
    }
}
