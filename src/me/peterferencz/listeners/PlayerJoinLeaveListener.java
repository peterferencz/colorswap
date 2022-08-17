package me.peterferencz.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import me.peterferencz.Main;
import me.peterferencz.managers.GameManager;

public class PlayerJoinLeaveListener implements Listener{
	
    public PlayerJoinLeaveListener() {
        Main.i.getServer().getPluginManager().registerEvents(this, Main.i);
    }
    
	@EventHandler
	public void OnPlayerJoin(PlayerJoinEvent e) {
		GameManager.PlayerJoin(e.getPlayer());
	}
	
	@EventHandler
	public void OnPlayerLeave(PlayerQuitEvent e) {
		GameManager.PlayerLeave(e.getPlayer());
	}
}
