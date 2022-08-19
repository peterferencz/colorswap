package me.peterferencz.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import me.peterferencz.Main;
import me.peterferencz.managers.GameManager;

public class PlayerJoinLeaveListener implements Listener{
	
    public PlayerJoinLeaveListener() {
        Main.getInstance().getServer().getPluginManager().registerEvents(this, Main.getInstance());
    }
    
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent e) {
		GameManager.playerJoin(e.getPlayer());
	}
	
	@EventHandler
	public void onPlayerLeave(PlayerQuitEvent e) {
		GameManager.playerLeave(e.getPlayer());
	}
}
