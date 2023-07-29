package io.github.Lefraudeur.DeACoudre;

import io.github.Lefraudeur.DeACoudre.Utils.ArenaTemplate;
import io.github.Lefraudeur.DeACoudre.Utils.Utilities;

import java.util.Set;

public class ArenaLoader {
    public static void loadAllArenas() {
        Set<String> keys = Utilities.config.getConfigurationSection("arenas").getKeys(false);
        System.out.println(keys);
        for (String arena : keys) {
            Arena.arenas.add(new Arena(arena, ArenaTemplate.get(arena)));
        }
        Arena.hideOtherWorldPlayers();
    }
    public static void loadArena(String name){
        Arena.arenas.add(new Arena(name, ArenaTemplate.get(name)));
        Arena.hideOtherWorldPlayers();
    }
}
