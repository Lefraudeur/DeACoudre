package io.github.Lefraudeur.DeACoudre.Utils;

import org.bukkit.configuration.ConfigurationSection;

public class ArenaTemplate {
    public Coordinates startingLocation;
    public Coordinates waitingSpawn;
    public Coordinates spectatorLocation;
    public int minPlayers;
    public int maxPlayers;
    public int rewardOnWin;
    public int rewardOnLife;
    public int rewardOnScore;
    public ArenaTemplate(Coordinates startingLocation,
                         Coordinates waitingSpawn,
                         Coordinates spectatorLocation,
                         int minPlayers,
                         int maxPlayers,
                         int rewardOnWin,
                         int rewardOnLife,
                         int rewardOnScore
        ){
        this.startingLocation = startingLocation;
        this.waitingSpawn = waitingSpawn;
        this.spectatorLocation = spectatorLocation;
        this.minPlayers = minPlayers;
        this.maxPlayers = maxPlayers;
        this.rewardOnWin = rewardOnWin;
        this.rewardOnLife = rewardOnLife;
        this.rewardOnScore = rewardOnScore;
    }
    public static ArenaTemplate get(String name) {
        ConfigurationSection arena = Utilities.config.getConfigurationSection("arenas." + name);
        ConfigurationSection waitingSpawn = arena.getConfigurationSection("waitingSpawn");
        ConfigurationSection startingLocation = arena.getConfigurationSection("startingLocation");
        ConfigurationSection spectatorLocation = arena.getConfigurationSection("spectatorLocation");
        Coordinates waitingSpawn_c = new Coordinates(
                waitingSpawn.getDouble("x"),
                waitingSpawn.getDouble("y"),
                waitingSpawn.getDouble("z"),
                (float) waitingSpawn.getDouble("yaw"),
                (float) waitingSpawn.getDouble("pitch")
        );
        Coordinates startingLocation_c = new Coordinates(
                startingLocation.getDouble("x"),
                startingLocation.getDouble("y"),
                startingLocation.getDouble("z"),
                (float) startingLocation.getDouble("yaw"),
                (float) startingLocation.getDouble("pitch")
        );
        Coordinates spectatorLocation_c = new Coordinates(
                spectatorLocation.getDouble("x"),
                spectatorLocation.getDouble("y"),
                spectatorLocation.getDouble("z"),
                (float) startingLocation.getDouble("yaw"),
                (float) startingLocation.getDouble("pitch")
        );
        return new ArenaTemplate (
                startingLocation_c,
                waitingSpawn_c,
                spectatorLocation_c,
                arena.getInt("min-players"),
                arena.getInt("max-players"),
                arena.getInt("reward-onwin"),
                arena.getInt("reward-onlife"),
                arena.getInt("reward-onscore")
        );
    }
}
