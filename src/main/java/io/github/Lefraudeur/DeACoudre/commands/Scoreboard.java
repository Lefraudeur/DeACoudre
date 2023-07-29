package io.github.Lefraudeur.DeACoudre.commands;

import io.github.Lefraudeur.DeACoudre.ArenaPlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Scoreboard implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player) || args.length != 1) return false;
        ArenaPlayer arenaPlayer = ArenaPlayer.get((Player) sender);
        if (arenaPlayer == null) return true;
        switch (args[0]) {
            case "off":
                arenaPlayer.stopScoreboardTask();
                sender.sendMessage("Your scoreboard has been hidden");
                return true;
            case "on":
                arenaPlayer.startScoreboardTask();
                sender.sendMessage("Your scoreboard has been enbaled");
                return true;
        }
        return false;
    }
}
