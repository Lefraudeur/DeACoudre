package io.github.Lefraudeur.DeACoudre.commands;

import io.github.Lefraudeur.DeACoudre.Utils.Utilities;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DACAdmin implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) return false;
        if (args[0].equals("temparenas")) {
            return handleTempArenas(sender);
        }
        if (!(sender instanceof Player)) {
            sender.sendMessage("The command must be executed by a Player");
            return true;
        }
        Player player = (Player) sender;
        switch (args[0]) {
            case "wand":
                return handleWand(player);
            case "setuparena":
                return handleSetupArena(player, args);
            case "setwaitingspawn":
                return handleSetWaitingSpawn(player, args);
        }
        return false;
    }

    private boolean handleTempArenas(CommandSender sender) {
        String arenas = "";
        for (World world : Bukkit.getWorlds()) {
            arenas = arenas.concat("\n - " + world.getName());
        }
        sender.sendMessage("Temp Arenas: " + arenas);
        return true;
    }
    private boolean handleSetWaitingSpawn(Player player, String[] args) {
        List<String> worlds = new ArrayList<>();
        System.out.println(Utilities.config.getList("arenas"));
        return true;
    }

    private boolean handleSetupArena(Player player, String[] args) {
        if(args.length != 2) {
            player.sendMessage("§cUsage: /dacadmin setuparena <worldname>");
            return true;
        }
        World world = Bukkit.getWorld(args[1]);
        if(world == null) world=Bukkit.createWorld(new WorldCreator(args[1]));
        player.teleport(world.getSpawnLocation());
        if (!Utilities.config.isConfigurationSection("arenas." + args[1])) {
            Utilities.config.createSection("arenas." + args[1]);
            Utilities.saveConfig();
        }
        player.sendMessage("Arena " + args[1] + " created !");
        return true;
    }
    private boolean handleWand(Player player){
        PlayerInventory inventory = player.getInventory();
        ItemStack item = new ItemStack(Material.BLAZE_ROD);
        Map<Enchantment, Integer> enchants = new HashMap<Enchantment, Integer>();
        enchants.put(Enchantment.FIRE_ASPECT, 666);
        enchants.put(Enchantment.KNOCKBACK, 666);
        enchants.put(Enchantment.DAMAGE_ALL, 666);
        enchants.put(Enchantment.DURABILITY, 666);
        item.addUnsafeEnchantments(enchants);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§cDAC Wand");
        List<String> lore = new ArrayList<>();
        lore.add("§6Use this to configure the DAC arena");
        lore.add("§6Used to select regions");
        meta.setLore(lore);
        item.setItemMeta(meta);
        inventory.addItem(item);
        return true;
    }
}