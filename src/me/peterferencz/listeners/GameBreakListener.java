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
        Main.i.getServer().getPluginManager().registerEvents(this, Main.i);
    }
    
    @EventHandler
    public void OnInventoryInteract(InventoryClickEvent e) {
        if (e.getWhoClicked().hasPermission("admin") && GameManager.state == GameManager.GameState.WAITINGFORPLAYERS) {
            return;
        } else {
            e.setCancelled(true);
        }
    }
    
    @EventHandler
    public void OnPlayerDropItem(PlayerDropItemEvent e) {
        if (e.getPlayer().hasPermission("admin") && GameManager.state == GameManager.GameState.WAITINGFORPLAYERS) {
            return;
        } else {
            e.setCancelled(true);
        }
    }
    
    @EventHandler
    public void OnPlayerPlaceBlock(BlockPlaceEvent e) {
        if (e.getPlayer().hasPermission("admin") && GameManager.state == GameManager.GameState.WAITINGFORPLAYERS) {
            return;
        } else {
            e.setCancelled(true);
        }
    }
    
    @EventHandler
    public void OnPlayerInteract(PlayerInteractEvent e) {
        if (e.getPlayer().hasPermission("admin") && GameManager.state == GameManager.GameState.WAITINGFORPLAYERS) {
            return;
        } else {
            e.setCancelled(true);
        }
    }
    
    @EventHandler
    public void BlockBreak(BlockBreakEvent e) {
        if (e.getPlayer().hasPermission("admin") && GameManager.state == GameManager.GameState.WAITINGFORPLAYERS) {
            return;
        } else {
            e.setCancelled(true);
        }
    }
    
    @EventHandler
    public void PlayerPunch(EntityDamageByEntityEvent e) {
        e.setCancelled(true);
    }
    
    @EventHandler
    public void Hunger(FoodLevelChangeEvent e) {
        e.setCancelled(true);
    }
    
}
