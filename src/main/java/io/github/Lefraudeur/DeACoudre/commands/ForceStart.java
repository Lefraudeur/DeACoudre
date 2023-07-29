package io.github.Lefraudeur.DeACoudre.commands;

import io.github.Lefraudeur.DeACoudre.ArenaPlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ForceStart implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length != 0 || !(sender instanceof Player)) return false;
        ArenaPlayer arenaPlayer = ArenaPlayer.get((Player) sender);
        if (arenaPlayer != null && arenaPlayer.arena.waiting && (arenaPlayer.arena.players.size() >= arenaPlayer.arena.minPlayers) && arenaPlayer.arena.timer > 5) {
            arenaPlayer.arena.timer = 5;
            sender.sendMessage("Countdown Shortenned");
        }
        return true;
    }
}
