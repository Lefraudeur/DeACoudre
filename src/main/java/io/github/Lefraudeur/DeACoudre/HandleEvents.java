package io.github.Lefraudeur.DeACoudre;

import io.github.Lefraudeur.DeACoudre.Utils.CustomMaterial;
import io.github.Lefraudeur.DeACoudre.Utils.Utilities;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HandleEvents implements Listener {
    private String itemname = Utilities.config.getString("messages.block-selector");
    private String spectatorItem = Utilities.config.getString("messages.spectator");
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event){
        Player player = event.getPlayer();
        event.setJoinMessage("");
        Arena arena = null;
        while (arena == null) {
            arena = Arena.getMaxPlayersArena();
        }
        arena.addPlayer(player);
        Arena.hideOtherWorldPlayers();
    }
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        event.setQuitMessage("");
        Player player = event.getPlayer();
        World world = player.getWorld();
        List<Player> playerList = world.getPlayers();
        for (Player player1 : playerList) {
            player1.sendMessage(Utilities.parsePlaceholders(player, Utilities.config.getString("messages.onleave")));
        }
        ArenaPlayer arenaPlayer = ArenaPlayer.get(player);
        if (arenaPlayer != null){
            arenaPlayer.remove();
            arenaPlayer.fullremove();
        }
        Arena.hideOtherWorldPlayers();
        if (playerList.size() == 1) {
            for (Arena arena : Arena.arenas) {
                if (arena.world == world) {
                    Bukkit.getScheduler().runTaskLater(JavaPlugin.getPlugin(Main.class), arena::remove, 20);
                    return;
                }
            }
        }
    }
    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        event.setCancelled(true);
        if (event.getEntity() instanceof Player && event.getCause() == EntityDamageEvent.DamageCause.FALL) {
            ArenaPlayer arenaPlayer = ArenaPlayer.get((Player)event.getEntity());
            if (arenaPlayer == null || !arenaPlayer.isTurn) return;
            arenaPlayer.removeLife();
            arenaPlayer.nextTurn();
        }
    }
    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        event.setCancelled(true);
    }
    @EventHandler
    public void onBlockDestroy(BlockBreakEvent event) {
        event.setCancelled(true);
    }
    @EventHandler
    public void onPlayerLoseFood(FoodLevelChangeEvent event) {
        event.setCancelled(true);
    }
    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        event.setCancelled(true);
    }
    @EventHandler
    public void onPlayerMoveEvent(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        ArenaPlayer arenaPlayer = ArenaPlayer.get(player);
        Block block = event.getTo().getBlock();
        if (arenaPlayer == null || !arenaPlayer.isTurn || !block.isLiquid()) return;
        if (blockNearby(block.getLocation())) arenaPlayer.addLife();
        arenaPlayer.addScore();
        block.setType(arenaPlayer.cmaterial.material);
        block.setData(arenaPlayer.cmaterial.data);
        arenaPlayer.nextTurn();
    }
    @EventHandler
    public void onItemRightClick(PlayerInteractEvent event){
        if (!(event.getAction().equals(Action.RIGHT_CLICK_AIR) || event.getAction().equals(Action.RIGHT_CLICK_AIR))) return;
        ItemStack item = event.getItem();
        if (item!= null) {
            Player player = event.getPlayer();
            ArenaPlayer arenaPlayer = ArenaPlayer.get(player);
            if (arenaPlayer != null) {
                if (item.getItemMeta().getDisplayName().equals(itemname)) {
                    event.setCancelled(true);
                    Inventory gui = Bukkit.createInventory(null, 36, itemname);
                    ItemStack[] other = {
                            new ItemStack(Material.WOOD),
                            new ItemStack(Material.SAND),
                            new ItemStack(Material.SANDSTONE)
                    };
                    ItemStack[] wools = getItemVariants(Material.WOOL);
                    ItemStack[] stained_glass = getItemVariants(Material.STAINED_GLASS);
                    ItemStack[] items = concatenateArrays(other, wools);
                    items = concatenateArrays(items, stained_glass);
                    gui.setContents(items);
                    player.openInventory(gui);
                }
                else if (item.getType() == Material.COMPASS) {
                    event.setCancelled(true);
                    List<Arena> playingArenas = new ArrayList<>();
                    for (Arena arena : Arena.arenas) {
                        if (!arena.playing) continue;
                        playingArenas.add(arena);
                    }
                    int size = playingArenas.size();
                    if (size == 0) {
                        player.sendMessage("No game to spectate");
                        return;
                    }
                    int lines = 0;
                    while (size > 0) {
                        size -= 9;
                        lines++;
                    }
                    Inventory gui = Bukkit.createInventory(null, lines * 9, spectatorItem);
                    for (Arena arena : playingArenas) {
                        ItemStack aselector = new ItemStack(Material.WOOL);
                        ItemMeta meta = aselector.getItemMeta();
                        meta.setDisplayName(arena.world.getName());
                        List<String> lore = new ArrayList<>();
                        lore.add("Map: " + arena.templateName);
                        lore.add("Players alive:");
                        for (ArenaPlayer aplayer : arena.players) {
                            lore.add("- " + aplayer.player.getDisplayName());
                        }
                        meta.setLore(lore);
                        aselector.setItemMeta(meta);
                        gui.addItem(aselector);
                    }
                    player.openInventory(gui);
                }
            }
        }
    }
    @EventHandler
    public void onInventoryClick(final InventoryClickEvent e) {
        if (e.getInventory().getName().equals(itemname)) {
            e.setCancelled(true);
            final ItemStack clickedItem = e.getCurrentItem();
            if (clickedItem == null || clickedItem.getType() == Material.AIR) return;
            final Player p = (Player) e.getWhoClicked();
            ArenaPlayer arenaPlayer = ArenaPlayer.get(p);
            if (arenaPlayer != null) {
                arenaPlayer.cmaterial = new CustomMaterial(clickedItem.getType(), clickedItem.getData().getData());
                ItemStack item = new ItemStack(clickedItem);
                ItemMeta meta = item.getItemMeta();
                meta.setDisplayName(itemname);
                item.setItemMeta(meta);
                p.getInventory().setItemInHand(item);
            }
        }
        else if (e.getInventory().getName().equals(spectatorItem)) {
            e.setCancelled(true);
            ItemStack clickedItem = e.getCurrentItem();
            if (clickedItem == null || clickedItem.getType() == Material.AIR) return;
            String arenaName = clickedItem.getItemMeta().getDisplayName();
            Arena arena = null;
            for (Arena a : Arena.arenas) {
                if (a.world.getName() == arenaName) {
                    arena = a;
                }
            }
            if (arena != null) {
                Player player = (Player)e.getWhoClicked();
                ArenaPlayer arenaPlayer = ArenaPlayer.get(player);
                if (arenaPlayer != null) {
                    arenaPlayer.remove();
                    arenaPlayer.fullremove();
                    arena.addSpectator(player);
                }
            }
        }
    }
    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        event.setCancelled(true);
        Player messenger = event.getPlayer();
        List<Player> players = messenger.getWorld().getPlayers();
        for (Player player : players) {
            player.sendMessage(Utilities.parsePlaceholders(messenger, Utilities.config.getString("messages.chat-format")
                    .replace("%message%", event.getMessage())
            ));
        }
    }
    @EventHandler
    public void onPlayerWorldChange(PlayerChangedWorldEvent event) {
        Arena.hideOtherWorldPlayers();
    }

    private ItemStack[] getItemVariants(Material material) {
        ItemStack[] itemStacks = new ItemStack[16];
        for (short i = 0; i < itemStacks.length; ++i) {
            itemStacks[i] = new ItemStack(material, 1, i);
        }
        return itemStacks;
    }
    private static <T> T[] concatenateArrays(T[] array1, T[] array2){
        T[] result = Arrays.copyOf(array1, array1.length + array2.length);
        System.arraycopy(array2, 0, result, array1.length, array2.length);
        return result;
    }
    private boolean blockNearby(Location blocklocation){
        Material block1 = new Location(blocklocation.getWorld(), blocklocation.getX()+1, blocklocation.getY(), blocklocation.getZ()).getBlock().getType();
        Material block2 = new Location(blocklocation.getWorld(), blocklocation.getX()-1, blocklocation.getY(), blocklocation.getZ()).getBlock().getType();
        Material block3 = new Location(blocklocation.getWorld(), blocklocation.getX(), blocklocation.getY(), blocklocation.getZ()+1).getBlock().getType();
        Material block4 = new Location(blocklocation.getWorld(), blocklocation.getX(), blocklocation.getY(), blocklocation.getZ()-1).getBlock().getType();
        return block1.isSolid() && block2.isSolid() && block3.isSolid() && block4.isSolid();
    }
}