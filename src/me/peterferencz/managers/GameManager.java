package me.peterferencz.managers;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import me.peterferencz.Main;
import me.peterferencz.utils.ChatUtils;
import me.peterferencz.utils.PositionsConfiguration;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

public class GameManager {
	
    public enum GameState {
        WAITINGFORPLAYERS,
        COUNTDOWN,
        RUNNINGFORBLOCK, //Players got the block in hand
        BLOCKSFALLING, //If a player is not standing on a selected block, the'll fall
        GRACEBETWEENRUNNING, //Some grace period between selecting blocks
        END
    }
    static GameState state;
    public static GameState getState() {
        return state;
    }
    static int subGameCount = 0;
    
	static ArrayList<Player> players = new ArrayList<>();
	static ArrayList<Player> spectators = new ArrayList<>();
	static int playerCountToStart;
	static boolean forced = false; //Only set to true, when the /start command was used
	static int ticksUntilStart = 0; 
	
	static ArrayList<ItemStack> floorBlocks = new ArrayList<>();
	static ItemStack selectedBlock = null;
	static Random random = new Random();
	
	public static void init() {
		playerCountToStart = Main.getInstance().getConfig().getInt("playerCountToStart");
		state = GameState.WAITINGFORPLAYERS;
		subGameCount = 0;
		
		resetMap();
		
		//Start main loop
		new BukkitRunnable() {
		    @Override public void run() {
		        timerTick();
		    }
		}.runTaskTimer(Main.getInstance(), 0, 1);
	}
	
	public static void playerJoin(Player p) {
	    switch (state) {
        case WAITINGFORPLAYERS:
        case COUNTDOWN:
            joinPlayers(p);
            break;
        default:
            joinSpectators(p);
            break;
        }
	}
	
	public static void playerLeave(Player p) {
	    if (spectators.contains(p)) {
            spectators.remove(p);
        }
	    if (players.contains(p)) {
	        players.remove(p);
	    }
	    checkPlayerCount();
	}
	
	private static void joinSpectators(Player p) {
	    p.getInventory().clear();
        p.teleport(Bukkit.getWorlds().get(0).getSpawnLocation());
        p.setGameMode(GameMode.SPECTATOR);
        
	    //players contains
	    if (players.contains(p)) {
            players.remove(p);
        }
	    if (!spectators.contains(p)) {
	        spectators.add(p);
	    }
	    
	    checkGameEnd();
	}
	
	private static void joinPlayers(Player p) {
	    p.getInventory().clear();
	    ChatUtils.log("Teleported Player");
        p.teleport(Bukkit.getWorlds().get(0).getSpawnLocation());
        p.setGameMode(GameMode.SURVIVAL);
	    
        if (spectators.contains(p)) {
            spectators.remove(p);
        }
        if (!players.contains(p)) {
            players.add(p);
        }
	    
	    checkPlayerCount();
    }
	
	
	
	private static void timerTick() {
	    switch (state) {
        case WAITINGFORPLAYERS:
            printActionBar("&c" + players.size() + " out of " + playerCountToStart);
            break;
        case COUNTDOWN:
            ticksUntilStart--;
            printActionBar("&6Starting in " + getRemainingSeconds() + " seconds");
            if (ticksUntilStart <= 0) {
                startGame();
                return;
            }
            break;
        case GRACEBETWEENRUNNING:
            ticksUntilStart--;
            printActionBar("&6New block in " + getRemainingSeconds() + " seconds");
            if(ticksUntilStart <= 0) {
                selectRandomBlock();
            }
            break;
        case RUNNINGFORBLOCK:
            ticksUntilStart--;
            printActionBar("&6Stand on the right block! (" + getRemainingSeconds() + "s)");
            if(ticksUntilStart <= 0) {
                deleteSelectedBlock();
            }
            break;
        case BLOCKSFALLING:
            ticksUntilStart--;
            if(ticksUntilStart <= 0) {
                state = GameState.GRACEBETWEENRUNNING;
                ticksUntilStart = (int) calculateGracePeriod();
                subGameCount++;
                
                for (int i = 0; i < players.size(); i++) {
                    players.get(i).getInventory().clear();
                }
                
                resetMap();
            }
            break;
        case END:
            ticksUntilStart--;
            ChatUtils.log(ticksUntilStart);
            if (ticksUntilStart <= 0) {
                for (int i = 0; i < players.size(); i++) {
                    joinPlayers(players.get(i));
                }
                for (int i = 0; i < spectators.size(); i++) {
                    joinPlayers(spectators.get(i));
                }
            }
            break;
        default:
            break;
        }
	}
	
	private static void checkPlayerCount() {
	    if(players.size() >= playerCountToStart) {
            startCountDown();
        } else if (players.size() < playerCountToStart && state == GameState.COUNTDOWN && !forced) {
            state = GameState.WAITINGFORPLAYERS;
        } else if(state == GameState.END) {
            state = GameState.WAITINGFORPLAYERS;
        }
	    checkGameEnd();
	}
	
	private static void startCountDown() {
	    state = GameState.COUNTDOWN;
        ticksUntilStart = Main.getInstance().getConfig().getInt("startCountdownPeriod");
    }
	
	private static void deleteSelectedBlock() {
	    state = GameState.BLOCKSFALLING;
        ticksUntilStart = Main.getInstance().getConfig().getInt("blockFallPeriod");
        
	    for (Iterator<Location> templateBlocks = PositionsConfiguration.iterateThroughBox(PositionsConfiguration.getPlayAreaMinimum(), PositionsConfiguration.getPlayAreaMaximum()); templateBlocks.hasNext();) {
            Block templateBlock = templateBlocks.next().getBlock();
            
            if (templateBlock.getType() != selectedBlock.getType()) {                
                templateBlock.getWorld().spawnFallingBlock(templateBlock.getLocation(), templateBlock.getBlockData());
                templateBlock.setType(Material.AIR);
            }
        }
	}
	
	private static void selectRandomBlock() {
	    state = GameState.RUNNINGFORBLOCK;
        ticksUntilStart = (int) calculateRunningTime();
	    
	    if (floorBlocks.size() == 0) {
            loadFloorBlocks();
        }
	    
	    selectedBlock = floorBlocks.get(random.nextInt(floorBlocks.size()));
	    for (int i = 0; i < players.size(); i++) {
            Inventory inventory = players.get(i).getInventory();
            inventory.clear();
            for (int j = 0; j < 9; j++) {
                inventory.setItem(j, selectedBlock);
            }
        }
	}
	
	private static void loadFloorBlocks() {
	    if(!arePositionsCorrect()) { 
	        ChatUtils.error("Tried to update floor blocks, while the areas were not set! (user /arenasetter)");
	        return;
	    }
	    floorBlocks.clear();
	    
	    for (Iterator<Location> templateBlocks = PositionsConfiguration.iterateThroughBox(PositionsConfiguration.getTemplateMinimum(), PositionsConfiguration.getTemplateMaximum()); templateBlocks.hasNext();) {
            Location templateBlock = templateBlocks.next();
            
            ItemStack item = new ItemStack(templateBlock.getBlock().getType());
            if (!floorBlocks.contains(item)) {
                floorBlocks.add(item);                        
            }
        }
	}
	
	private static void startGame() {
	    printActionBar("&6Starting...");
	    if(!arePositionsCorrect()) { return; }
	    selectRandomBlock();
	    
	    state = GameState.RUNNINGFORBLOCK;
        ticksUntilStart = (int) calculateRunningTime();
	    
	    for (int i = 0; i < spectators.size(); i++) {
            spectators.get(i).setGameMode(GameMode.SURVIVAL);
            players.add(spectators.get(i));
            spectators.remove(i);
        }
	    
	    subGameCount = 0;
        resetMap();
    }
	
	private static int getRemainingSeconds() {
	    return ticksUntilStart / 20; //FIXME relies on the assumption that the server runs on 20tps
	}
	
	private static double calculateGracePeriod() {
	    return Math.max(Main.getInstance().getConfig().getInt("gracePeriod") - subGameCount * Main.getInstance().getConfig().getInt("graceperiodDecrement"), Main.getInstance().getConfig().getInt("graceperiodDecrementLimit"));
	}
	
	private static double calculateRunningTime() {
        return Math.max(Main.getInstance().getConfig().getInt("runPeriod") - subGameCount * Main.getInstance().getConfig().getInt("runPeriodDecrement"), Main.getInstance().getConfig().getInt("runPeriodDecrementLimit"));
    }
	
	private static boolean arePositionsCorrect() {
	    try {
            PositionsConfiguration.isAllSet();
            return true;
        } catch (Exception e) {
            if (forced) {
                for (int i = 0; i < players.size(); i++) {
                    players.get(i).sendMessage(ChatUtils.color("&c"+e.getMessage()));
                }
            }else {
                ChatUtils.error(e.getMessage());
            }
            return false;
        }
	}
	
	private static void resetMap() {
	    if(!arePositionsCorrect()) { return; }
	    Location tMin = PositionsConfiguration.getTemplateMinimum();
	    Location aMin = PositionsConfiguration.getPlayAreaMinimum();
	    Location offset = new Location(tMin.getWorld(),
	            tMin.getBlockX() - aMin.getBlockX(),
	            tMin.getBlockY() - aMin.getBlockY(),
	            tMin.getBlockZ() - aMin.getBlockZ());
	    
	    
	    for (Iterator<Location> templateBlocks = PositionsConfiguration.iterateThroughBox(PositionsConfiguration.getTemplateMinimum(), PositionsConfiguration.getTemplateMaximum()); templateBlocks.hasNext();) {
            Location from = templateBlocks.next();
            Location to = subtractLocations(from, offset);
            to.getBlock().setBlockData(from.getBlock().getBlockData());
        }
	}
	
	private static Location subtractLocations(Location loc1, Location loc2) {
	    return new Location(loc1.getWorld(), loc1.getX() - loc2.getX(), loc1.getY() - loc2.getY(), loc1.getZ() - loc2.getZ());
	}
	
	private static void printActionBar(String msg) {
	    for (int i = 0; i < players.size(); i++) {
            Player p = players.get(i);
            p.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ChatUtils.color(msg)));
        }
    }
	
	public static void forceStart() {
		forced = true;
		startCountDown();
	}
	
	private static void gameEnd() {
	    ChatUtils.log("Game end at " + subGameCount + " rounds");
        state = GameState.END;
        ticksUntilStart = Main.getInstance().getConfig().getInt("endPeriod");
        resetMap();
        
        for (int i = 0; i < spectators.size(); i++) {
            spectators.get(i).sendTitle(ChatUtils.color("&cGame Over"), null, 5, 40, 5);
        }
        for (int i = 0; i < players.size(); i++) {
            players.get(i).sendTitle(ChatUtils.color("&6You Won!"), ChatUtils.color("&2You survived &6" + subGameCount + "&2 rounds"), 5, 40, 5);
            players.get(i).getInventory().clear();
        }
        subGameCount = 0;
        
	}
	
	private static void checkGameEnd() {
	    if (!(state == GameState.BLOCKSFALLING || state == GameState.BLOCKSFALLING || state == GameState.RUNNINGFORBLOCK)) {
            return;
        }
	    if (players.size() < 2) {
            gameEnd();
        }
	}

    public static void playerDie(Player player) {
        if (!(state == GameState.BLOCKSFALLING || state == GameState.GRACEBETWEENRUNNING || state == GameState.RUNNINGFORBLOCK)) {
            return;
        }
        
        joinSpectators(player);
    }
}
