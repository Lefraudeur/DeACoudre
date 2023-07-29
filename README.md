# DeACoudre
Dé A Coudre plugin for spigot 1.8.8, but also tested on 1.19.
Designed to be used in a BungeeCord network since it makes you join a game when you login

If you are here, you already know what this minigame is about.
Very simple : Each player jump need to jump into water to survive and not die from fall damage. When a player lands on water, a block is placed where he landed, which makes the game harder.
When a player lands in a single block of water, surrounded by only solid blocks, it gains one life.
The last player alive wins

Dependencies:
 - PlaceholderAPI + player extension : /papi ecloud download player
 - Vault
 - Economy Plugin compatible with Vault

Cool features :
 - Block selector (select the block placed when you fall into water)
 - Multi Arena, multiple games can be started at the same time on the same server, no need to create multiple servers

Bad things:
 - My first plugin
 - I don't like java, I've always focused on C++
 - Code is shit

Put your map in a folder named for example "crystal", and make sure the name of the folder corresponds to one of the arena names in config.yml.
You have to edit all the positions manually in the config.yml. There isn't any setup system

This is a draft plugin, the code is not well structured, there are surely lot of bugs, and the plugin creates useless threads which may have a negative performance impact.

Download: https://github.com/Lefraudeur/DeACoudre/releases/download/v1.0-snapshot/DeACoudre-1.0-SNAPSHOT.jar
