package io.github.Lefraudeur.DeACoudre;

import io.github.Lefraudeur.DeACoudre.Utils.ArenaTemplate;
import io.github.Lefraudeur.DeACoudre.Utils.Utilities;
import org.apache.commons.io.FileUtils;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Arena implements Listener {
    public static List<Arena> arenas = new ArrayList<>();
    public List<ArenaPlayer> players = new ArrayList<>();
    public String templateName;
    public Location waitingSpawn;
    public Location startingLocation;
    public Location spectatorLocation;
    public World world;
    public int maxPlayers;
    public int minPlayers;
    public int rewardOnWin;
    public int rewardOnLife;
    public int rewardOnScore;
    public boolean waiting = true;
    public boolean playing = false;
    public ArenaPlayer currentTurn = null;
    public ArenaPlayer nextTurn = null;
    private static int arenaCount = 0;
    public int timer = 30;
    public Arena(String world_template, ArenaTemplate template){
        templateName = world_template;
        String arenaName = "temp_" + arenaCount;
        arenaCount++;
        try {
            FileUtils.copyDirectory(new File(world_template), new File(arenaName), false);
            Files.deleteIfExists(Paths.get(arenaName + "/uid.dat"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        this.world = Bukkit.createWorld(new WorldCreator(arenaName));
        this.world.setTime(6000);
        this.world.setGameRuleValue("doDaylightCycle", "false");
        this.world.setGameRuleValue("doMobSpawning", "false");
        this.world.setGameRuleValue("doWeatherCycle", "false");
        this.world.setGameRuleValue("randomTickSpeed", "0");
        this.world.setStorm(false);
        this.world.setWeatherDuration(999999999);
        this.world.setAutoSave(false);
        this.waitingSpawn = new Location(world, template.waitingSpawn.x, template.waitingSpawn.y, template.waitingSpawn.z, template.waitingSpawn.yaw, template.waitingSpawn.pitch);
        this.startingLocation = new Location(world, template.startingLocation.x, template.startingLocation.y, template.startingLocation.z, template.startingLocation.yaw, template.startingLocation.pitch);
        this.spectatorLocation = new Location(world, template.spectatorLocation.x, template.spectatorLocation.y, template.spectatorLocation.z, template.spectatorLocation.yaw, template.spectatorLocation.pitch);
        this.minPlayers = template.minPlayers;
        this.maxPlayers = template.maxPlayers;
        this.rewardOnWin = template.rewardOnWin;
        this.rewardOnLife = template.rewardOnLife;
        this.rewardOnScore = template.rewardOnScore;
    }

    public void addSpectator(Player player) {
        player.teleport(spectatorLocation);
        player.setGameMode(GameMode.SPECTATOR);
        Arena.hideOtherWorldPlayers();
        new ArenaPlayer(player, this);
    }
    public void addPlayer(Player player) {
        player.setGameMode(GameMode.SURVIVAL);
        player.teleport(waitingSpawn);
        ItemStack item = new ItemStack(Material.WOOD);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(Utilities.config.getString("messages.block-selector"));
        item.setItemMeta(meta);
        ItemStack[] itemStacks = {item};
        player.getInventory().setContents(itemStacks);
        if (player.hasPermission("dac.spectate")) {
            ItemStack spectate = new ItemStack(Material.COMPASS);
            ItemMeta spectateMeta = spectate.getItemMeta();
            spectateMeta.setDisplayName(Utilities.config.getString("messages.spectate"));
            spectate.setItemMeta(spectateMeta);
            player.getInventory().setItem(4, spectate);
        }
        player.setFoodLevel(20);
        players.add(new ArenaPlayer(player, this));
        for (ArenaPlayer p : players) {
            p.player.sendMessage(Utilities.parsePlaceholders(player, Utilities.config.getString("messages.onjoin")
                    .replace("%player_count%", String.valueOf(players.size()))
                    .replace("%player_min%", String.valueOf(minPlayers))
                    .replace("%player_max%", String.valueOf(maxPlayers))
            ));
        }
        Arena.hideOtherWorldPlayers();
        if (players.size() == minPlayers) {
            Bukkit.getScheduler().runTaskAsynchronously(JavaPlugin.getPlugin(Main.class), ()->{
                timer = 30;
                while (players.size() >= minPlayers) {
                    if (players.size() == maxPlayers) {
                        waiting = false;
                        for (ArenaPlayer p : players) {
                            p.player.sendMessage(Utilities.config.getString("messages.gamefull"));
                        }
                        timer = 5;
                    }
                    else {
                        waiting = true;
                    }
                    if (timer == 0) {
                        waiting = false;
                        playing = true;
                        Bukkit.getScheduler().runTask(JavaPlugin.getPlugin(Main.class), ()->{
                            if (players.size() < minPlayers) {
                                waiting = true;
                                playing = false;
                                for (ArenaPlayer p : players) {
                                    p.player.sendMessage(Utilities.config.getString("messages.startcancelled"));
                                }
                                return;
                            }
                            for (ArenaPlayer p : players) {
                                p.player.getInventory().clear();
                                p.player.sendTitle(" ", null);
                                p.player.sendMessage(Utilities.config.getString("messages.starting-game"));
                                p.setTurn(false);
                            }
                            players.get(0).setTurn(true);
                            ArenaLoader.loadArena(templateName);
                        });
                        return;
                    }
                    if (timer % 10 == 0) {
                        for (ArenaPlayer p : players) {
                            p.player.sendMessage(Utilities.config.getString("messages.starting-in").replace("%timer%", String.valueOf(timer)));
                        }
                    }
                    else if (timer <= 5) {
                        for (ArenaPlayer p : players) {
                            p.player.sendTitle(String.valueOf(timer), null);
                        }
                    }
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    timer--;
                }
                for (ArenaPlayer p : players) {
                    p.player.sendMessage(Utilities.config.getString("messages.startcancelled"));
                }
            });
        }
    }
    public void remove(){
        if (!waiting) {
            arenas.remove(this);
            String directory = world.getName();
            if (Bukkit.unloadWorld(world, false)) {
                try {
                    FileUtils.deleteDirectory(new File(directory));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
    public static Arena getMaxPlayersArena(){
        Arena maxPlayersArena = null;
        for (Arena arena : arenas) {
            if (arena.waiting) {
                if (maxPlayersArena == null || arena.players.size() > maxPlayersArena.players.size()) {
                    maxPlayersArena = arena;
                }
            }
        }
        return maxPlayersArena;
    }

    public static void hideOtherWorldPlayers() {
        for (World world : Bukkit.getWorlds()) {
            List<Player> tohide = new ArrayList<>();
            List<Player> toshow = new ArrayList<>();
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.getWorld().getUID() != world.getUID()) tohide.add(player);
                else toshow.add(player);
            }
            for (Player wplayer : world.getPlayers()) {
                for (Player oplayer : toshow) {
                    if (wplayer == oplayer) continue;
                    wplayer.showPlayer(oplayer);
                }
                for (Player oplayer : tohide) {
                    if (wplayer == oplayer) continue;
                    wplayer.hidePlayer(oplayer);
                }
            }
        }
    }
}

