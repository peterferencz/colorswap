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
    public static GameState state;
    static int subGameCount = 0;
    
	static ArrayList<Player> players = new ArrayList<>();
	static ArrayList<Player> spectators = new ArrayList<>();
	static int playerCountToStart;
	static boolean forced = false; //Only set to true, when the /start command was used
	static int ticksUntilStart = 0; 
	
	static ArrayList<ItemStack> floorBlocks = new ArrayList<>();
	static ItemStack selectedBlock = null;
	static Random random = new Random();
	
	public static void Init() {
		playerCountToStart = Main.i.getConfig().getInt("playerCountToStart");
		state = GameState.WAITINGFORPLAYERS;
		subGameCount = 0;
		
		ResetMap();
		
		//Start main loop
		new BukkitRunnable() {
		    @Override public void run() {
		        TimerTick();
		    }
		}.runTaskTimer(Main.i, 0, 1);
	}
	
	public static void PlayerJoin(Player p) {
	    switch (state) {
        case WAITINGFORPLAYERS:
        case COUNTDOWN:
            JoinPlayers(p);
            break;
        default:
            JoinSpectators(p);
            break;
        }
	}
	
	public static void PlayerLeave(Player p) {
	    if (spectators.contains(p)) {
            spectators.remove(p);
        }
	    if (players.contains(p)) {
	        players.remove(p);
	    }
	    CheckPlayerCount();
	}
	
	private static void JoinSpectators(Player p) {
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
	    
	    CheckGameEnd();
	}
	
	private static void JoinPlayers(Player p) {
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
	    
	    CheckPlayerCount();
    }
	
	
	
	private static void TimerTick() {
	    switch (state) {
        case WAITINGFORPLAYERS:
            PrintActionBar("&c" + players.size() + " out of " + playerCountToStart);
            break;
        case COUNTDOWN:
            ticksUntilStart--;
            PrintActionBar("&6Starting in " + getRemainingSeconds() + " seconds");
            if (ticksUntilStart <= 0) {
                StartGame();
                return;
            }
            break;
        case GRACEBETWEENRUNNING:
            ticksUntilStart--;
            PrintActionBar("&6New block in " + getRemainingSeconds() + " seconds");
            if(ticksUntilStart <= 0) {
                SelectRandomBlock();
            }
            break;
        case RUNNINGFORBLOCK:
            ticksUntilStart--;
            PrintActionBar("&6Stand on the right block! (" + getRemainingSeconds() + "s)");
            if(ticksUntilStart <= 0) {
                DeleteSelectedBlock();
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
                
                ResetMap();
            }
            break;
        case END:
            ticksUntilStart--;
            ChatUtils.log(ticksUntilStart);
            if (ticksUntilStart <= 0) {
                for (int i = 0; i < players.size(); i++) {
                    JoinPlayers(players.get(i));
                }
                for (int i = 0; i < spectators.size(); i++) {
                    JoinPlayers(spectators.get(i));
                }
            }
            break;
        default:
            break;
        }
	}
	
	private static void CheckPlayerCount() {
	    if(players.size() >= playerCountToStart) {
            StartCountDown();
        } else if (players.size() < playerCountToStart && state == GameState.COUNTDOWN && !forced) {
            state = GameState.WAITINGFORPLAYERS;
        } else if(state == GameState.END) {
            state = GameState.WAITINGFORPLAYERS;
        }
	    CheckGameEnd();
	}
	
	private static void StartCountDown() {
	    state = GameState.COUNTDOWN;
        ticksUntilStart = Main.i.getConfig().getInt("startCountdownPeriod");
    }
	
	private static void DeleteSelectedBlock() {
	    state = GameState.BLOCKSFALLING;
        ticksUntilStart = Main.i.getConfig().getInt("blockFallPeriod");
        
	    for (Iterator<Location> templateBlocks = PositionsConfiguration.IterateThroughBox(PositionsConfiguration.getPlayAreaMinimum(), PositionsConfiguration.getPlayAreaMaximum()); templateBlocks.hasNext();) {
            Block templateBlock = templateBlocks.next().getBlock();
            
            if (templateBlock.getType() != selectedBlock.getType()) {                
                templateBlock.getWorld().spawnFallingBlock(templateBlock.getLocation(), templateBlock.getBlockData());
                templateBlock.setType(Material.AIR);
            }
        }
	}
	
	private static void SelectRandomBlock() {
	    state = GameState.RUNNINGFORBLOCK;
        ticksUntilStart = (int) calculateRunningTime();
	    
	    if (floorBlocks.size() == 0) {
            LoadFloorBlocks();
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
	
	private static void LoadFloorBlocks() {
	    if(!ArePositionsCorrect()) { 
	        ChatUtils.error("Tried to update floor blocks, while the areas were not set! (user /arenasetter)");
	        return;
	    }
	    floorBlocks.clear();
	    
	    for (Iterator<Location> templateBlocks = PositionsConfiguration.IterateThroughBox(PositionsConfiguration.getTemplateMinimum(), PositionsConfiguration.getTemplateMaximum()); templateBlocks.hasNext();) {
            Location templateBlock = templateBlocks.next();
            
            ItemStack item = new ItemStack(templateBlock.getBlock().getType());
            if (!floorBlocks.contains(item)) {
                floorBlocks.add(item);                        
            }
        }
	}
	
	private static void StartGame() {
	    PrintActionBar("&6Starting...");
	    if(!ArePositionsCorrect()) { return; }
	    SelectRandomBlock();
	    
	    state = GameState.RUNNINGFORBLOCK;
        ticksUntilStart = (int) calculateRunningTime();
	    
	    for (int i = 0; i < spectators.size(); i++) {
            spectators.get(i).setGameMode(GameMode.SURVIVAL);
            players.add(spectators.get(i));
            spectators.remove(i);
        }
	    
	    subGameCount = 0;
        ResetMap();
    }
	
	private static int getRemainingSeconds() {
	    return ticksUntilStart / 20; //FIXME relies on the assumption that the server runs on 20tps
	}
	
	private static double calculateGracePeriod() {
	    return Math.max(Main.i.getConfig().getInt("gracePeriod") - subGameCount * Main.i.getConfig().getInt("graceperiodDecrement"), Main.i.getConfig().getInt("graceperiodDecrementLimit"));
	}
	
	private static double calculateRunningTime() {
        return Math.max(Main.i.getConfig().getInt("runPeriod") - subGameCount * Main.i.getConfig().getInt("runPeriodDecrement"), Main.i.getConfig().getInt("runPeriodDecrementLimit"));
    }
	
	private static boolean ArePositionsCorrect() {
	    try {
            PositionsConfiguration.isAllSet();
            return true;
        } catch (Exception e) {
            if (forced) {
                for (int i = 0; i < players.size(); i++) {
                    players.get(i).sendMessage(ChatUtils.Color("&c"+e.getMessage()));
                }
            }else {
                ChatUtils.error(e.getMessage());
            }
            return false;
        }
	}
	
	private static void ResetMap() {
	    if(!ArePositionsCorrect()) { return; }
	    Location tMin = PositionsConfiguration.getTemplateMinimum();
	    Location aMin = PositionsConfiguration.getPlayAreaMinimum();
	    Location offset = new Location(tMin.getWorld(),
	            tMin.getBlockX() - aMin.getBlockX(),
	            tMin.getBlockY() - aMin.getBlockY(),
	            tMin.getBlockZ() - aMin.getBlockZ());
	    
	    
	    for (Iterator<Location> templateBlocks = PositionsConfiguration.IterateThroughBox(PositionsConfiguration.getTemplateMinimum(), PositionsConfiguration.getTemplateMaximum()); templateBlocks.hasNext();) {
            Location from = templateBlocks.next();
            Location to = subtractLocations(from, offset);
            to.getBlock().setBlockData(from.getBlock().getBlockData());
        }
	}
	
	private static Location subtractLocations(Location loc1, Location loc2) {
	    return new Location(loc1.getWorld(), loc1.getX() - loc2.getX(), loc1.getY() - loc2.getY(), loc1.getZ() - loc2.getZ());
	}
	
	private static void PrintActionBar(String msg) {
	    for (int i = 0; i < players.size(); i++) {
            Player p = players.get(i);
            p.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ChatUtils.Color(msg)));
        }
    }
	
	public static void ForceStart() {
		forced = true;
		StartCountDown();
	}
	
	private static void GameEnd() {
	    ChatUtils.log("Game end at " + subGameCount + " rounds");
        state = GameState.END;
        ticksUntilStart = Main.i.getConfig().getInt("endPeriod");
        ResetMap();
        
        for (int i = 0; i < spectators.size(); i++) {
            spectators.get(i).sendTitle(ChatUtils.Color("&cGame Over"), null, 5, 40, 5);
        }
        for (int i = 0; i < players.size(); i++) {
            players.get(i).sendTitle(ChatUtils.Color("&6You Won!"), ChatUtils.Color("&2You survived &6" + subGameCount + "&2 rounds"), 5, 40, 5);
            players.get(i).getInventory().clear();
        }
        subGameCount = 0;
        
	}
	
	private static void CheckGameEnd() {
	    if (!(state == GameState.BLOCKSFALLING || state == GameState.BLOCKSFALLING || state == GameState.RUNNINGFORBLOCK)) {
            return;
        }
	    if (players.size() < 2) {
            GameEnd();
        }
	}

    public static void PlayerDie(Player player) {
        if (!(state == GameState.BLOCKSFALLING || state == GameState.GRACEBETWEENRUNNING || state == GameState.RUNNINGFORBLOCK)) {
            return;
        }
        
        JoinSpectators(player);
    }
}
