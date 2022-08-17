package me.peterferencz.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import me.peterferencz.Main;
import me.peterferencz.managers.GameManager;
import me.peterferencz.utils.PositionsConfiguration;

public class PlayerMoveListener implements Listener{

    Double deathPlaneY;
    
    public PlayerMoveListener() {
        Main.i.getServer().getPluginManager().registerEvents(this, Main.i);
        trySetDeathPlaneY();
    }
    
    @EventHandler
    public void OnPlayerMove(PlayerMoveEvent e) {
        if (!trySetDeathPlaneY()) {
            return;
        }
        
        if (e.getPlayer().getLocation().getY() < deathPlaneY) {
            GameManager.PlayerDie(e.getPlayer());
        }
    }
    
    private boolean trySetDeathPlaneY() {
        if (deathPlaneY != null) {
            return true;
        }
        try {
            PositionsConfiguration.isAllSet();
            deathPlaneY = PositionsConfiguration.getPlayAreaMinimum().getY();
            return true;
        } catch (Exception exception) {
            return false;
        }
    }
    
}
