package io.github.Lefraudeur.DeACoudre;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import io.github.Lefraudeur.DeACoudre.Utils.CustomMaterial;
import io.github.Lefraudeur.DeACoudre.Utils.Utilities;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ArenaPlayer {
    public static List<ArenaPlayer> global_players = new ArrayList<>();
    public Player player;
    public boolean isTurn = false;
    public Objective objective;
    private final Scoreboard scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
    public final Arena arena;
    private List<String> savedScores = new ArrayList<>();
    private int score = 0;
    private int lives = 1;
    public CustomMaterial cmaterial = new CustomMaterial(Material.WOOD, (byte) 0);
    private BukkitTask scoreboardTask;
    public final Runnable scoreboardFunction;
    public ArenaPlayer(Player aplayer, Arena oarena) {
        this.player = aplayer;
        this.player.setScoreboard(scoreboard);
        this.arena = oarena;
        global_players.add(this);
        scoreboardFunction = ()->{
            String path = (arena.playing ? "scoreboards.playing" : "scoreboards.waiting");
            List<String> scores = new ArrayList<>();
            scores.add(Utilities.config.getString(path + ".name"));
            List<String> configScores = Utilities.config.getStringList(path + ".scores");
            for (String line : configScores) {
                if (line.equals("%score%")) {
                    List<String> playerScores = new ArrayList<>();
                    for (ArenaPlayer arenaPlayer : arena.players) {
                        playerScores.add(arenaPlayer.player.getDisplayName() + ": §6⭐" + arenaPlayer.score + " §4❤" + arenaPlayer.lives);
                    }
                    scores.addAll(playerScores);
                    continue;
                }
                String temp_line = line;
                if (arena.currentTurn != null) temp_line = temp_line.replace("%current_turn%", arena.currentTurn.player.getDisplayName());
                if (arena.nextTurn != null) temp_line = temp_line.replace("%next_turn%", arena.nextTurn.player.getDisplayName());
                temp_line = temp_line.replace("%timer%", (arena.players.size() >= arena.minPlayers ? String.valueOf(arena.timer) : Utilities.config.getString("messages.not-enough-players")));
                scores.add(Utilities.parsePlaceholders(player, temp_line));
            }
            if (!scores.equals(savedScores)) {
                savedScores = scores;
                if (objective != null) {
                    try {
                        objective.unregister();
                    }
                    catch (IllegalStateException ignore){}
                }
                objective = scoreboard.registerNewObjective("a", "dummy");
                objective.setDisplayName(scores.get(0));
                List<ChatColor> random = new ArrayList<>(Arrays.asList(ChatColor.values()));
                random.remove(16);
                for (int i = 1; i < scores.size(); i++) {
                    objective.getScore(random.get(i-1) + String.valueOf(ChatColor.RESET) + scores.get(i)).setScore(scores.size()-i-1);
                }
                objective.setDisplaySlot(DisplaySlot.SIDEBAR);
            }
        };
        startScoreboardTask();
        Arena.hideOtherWorldPlayers();
    }
    public void addScore(){
        score++;
        Utilities.economy.depositPlayer(player, arena.rewardOnScore);
        player.sendMessage(Utilities.config.getString("messages.onscore").replace("%money%", String.valueOf(arena.rewardOnScore)));
    }
    public void addLife(){
        lives++;
        Utilities.economy.depositPlayer(player, arena.rewardOnLife);
        player.sendMessage(Utilities.config.getString("messages.onlife").replace("%money%", String.valueOf(arena.rewardOnLife)));
        player.playSound(player.getLocation(), Sound.LEVEL_UP, 1, 1);
    }
    public void removeLife(){
        lives--;
        player.sendMessage(Utilities.config.getString("messages.lifeloss"));
        if (lives == 0) {
            for (Player p : player.getWorld().getPlayers()) {
                p.sendMessage(Utilities.parsePlaceholders(player, Utilities.config.getString("messages.eliminated")));
            }
            remove();
        }
    }
    public void setTurn(boolean state) {
        isTurn = state;
        if (state) {
            arena.currentTurn = this;
            for (int i = 0; i < arena.players.size(); ++i) {
                if (arena.players.get(i).player.getUniqueId() == this.player.getUniqueId()) {
                    int index = i + 1;
                    if (index == arena.players.size()) index = 0;
                    arena.nextTurn = arena.players.get(index);
                    break;
                }
            }
            player.setGameMode(GameMode.SURVIVAL);
            for (Player p : player.getWorld().getPlayers()) {
                p.sendMessage(Utilities.parsePlaceholders(player, Utilities.config.getString("messages.playerturn")));
            }
            int savedScore = score;
            int savedLives = lives;
            Bukkit.getScheduler().runTaskLater(JavaPlugin.getPlugin(Main.class), ()->{
                if (isTurn && score == savedScore && savedLives == lives) {
                    player.sendMessage(Utilities.config.getString("messages.afk"));
                    removeLife();
                }
            }, 600);
            player.teleport(arena.startingLocation);
        }
        else {
            player.setGameMode(GameMode.SPECTATOR);
            player.teleport(arena.spectatorLocation);
        }
    }
    public void endGame(ArenaPlayer winner){
        Utilities.economy.depositPlayer(winner.player, arena.rewardOnWin);
        winner.player.sendMessage(Utilities.config.getString("messages.onwin-winner").replace("%money%", String.valueOf(arena.rewardOnWin)));
        List<Player> players = player.getWorld().getPlayers();
        for (ArenaPlayer arenaPlayer : arena.players) {
            arenaPlayer.setTurn(false);
        }
        for (Player wplayer : players){
            wplayer.sendMessage(Utilities.parsePlaceholders(winner.player, Utilities.config.getString("messages.onwin-others")));
        }
        Bukkit.getScheduler().runTaskLater(JavaPlugin.getPlugin(Main.class), ()->{
            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeUTF("Connect");
            out.writeUTF(Utilities.config.getString("lobby-server"));
            for (Player wplayer : players) {
                if (wplayer.isOnline()) {
                    wplayer.sendPluginMessage(JavaPlugin.getPlugin(Main.class), "BungeeCord", out.toByteArray());
                }
            }
        }, 200);
    }
    public void nextTurn(){
        if (isTurn){
            setTurn(false);
            arena.nextTurn.setTurn(true);
        }
    }
    public static ArenaPlayer get(Player player) {
        for (ArenaPlayer aplayer : global_players) {
            if (aplayer.player.getUniqueId() == player.getUniqueId()) return aplayer;
        }
        return null;
    }
    public void remove(){
        nextTurn();
        arena.players.remove(this);
        if (arena.players.size() == 1 && !arena.waiting) endGame(arena.players.get(0));
        Arena.hideOtherWorldPlayers();
    }
    public void fullremove() {
        stopScoreboardTask();
        global_players.remove(this);
    }
    public void startScoreboardTask(){
        if (scoreboardTask == null) scoreboardTask = Bukkit.getScheduler().runTaskTimerAsynchronously(JavaPlugin.getPlugin(Main.class), scoreboardFunction, 0, 4);
    }
    public void stopScoreboardTask(){
        if (scoreboardTask != null) {
            scoreboardTask.cancel();
            savedScores.clear();
            try {
                objective.unregister();
            }
            catch (IllegalStateException ignore){}
            scoreboardTask = null;
        }
    }
}
