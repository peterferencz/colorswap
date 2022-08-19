package me.peterferencz.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import me.peterferencz.Main;
import me.peterferencz.managers.GameManager;

public class GameBreakListener implements Listener{

    public GameBreakListener() {
        Main.getInstance().getServer().getPluginManager().registerEvents(this, Main.getInstance());
    }
    
    @EventHandler
    public void onInventoryInteract(InventoryClickEvent e) {
        if (e.getWhoClicked().hasPermission("admin") && GameManager.getState() == GameManager.GameState.WAITINGFORPLAYERS) {
            return;
        } else {
            e.setCancelled(true);
        }
    }
    
    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent e) {
        if (e.getPlayer().hasPermission("admin") && GameManager.getState() == GameManager.GameState.WAITINGFORPLAYERS) {
            return;
        } else {
            e.setCancelled(true);
        }
    }
    
    @EventHandler
    public void onPlayerPlaceBlock(BlockPlaceEvent e) {
        if (e.getPlayer().hasPermission("admin") && GameManager.getState() == GameManager.GameState.WAITINGFORPLAYERS) {
            return;
        } else {
            e.setCancelled(true);
        }
    }
    
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) {
        if (e.getPlayer().hasPermission("admin") && GameManager.getState() == GameManager.GameState.WAITINGFORPLAYERS) {
            return;
        } else {
            e.setCancelled(true);
        }
    }
    
    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        if (e.getPlayer().hasPermission("admin") && GameManager.getState() == GameManager.GameState.WAITINGFORPLAYERS) {
            return;
        } else {
            e.setCancelled(true);
        }
    }
    
    @EventHandler
    public void onPlayerPunch(EntityDamageByEntityEvent e) {
        e.setCancelled(true);
    }
    
    @EventHandler
    public void onHungerChange(FoodLevelChangeEvent e) {
        e.setCancelled(true);
    }
    
}
