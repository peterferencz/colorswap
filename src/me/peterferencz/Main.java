package me.peterferencz;

import org.bukkit.plugin.java.JavaPlugin;

import me.peterferencz.commands.ForceStartCommand;
import me.peterferencz.items.ArenaBoundSetterWand;
import me.peterferencz.listeners.GameBreakListener;
import me.peterferencz.listeners.PlayerJoinLeaveListener;
import me.peterferencz.listeners.PlayerMoveListener;
import me.peterferencz.managers.GameManager;
import me.peterferencz.utils.ChatUtils;
import me.peterferencz.utils.PositionsConfiguration;

public class Main extends JavaPlugin {

    private static Main instance;
    public static Main getInstance() {
        return instance;
    }
    
	@Override
	public void onEnable() {
	    instance = this;
	    ChatUtils.log("&a[ColorSwap] Plugin started");
	    
	    getConfig().options().copyDefaults(true);
	    saveConfig();
		
	    PositionsConfiguration.init();
		GameManager.init();
		
		new ForceStartCommand();
		new ArenaBoundSetterWand();
		new PlayerJoinLeaveListener();
		new GameBreakListener();
		new PlayerMoveListener();
		
		super.onEnable();
	}
	
	@Override
	public void onDisable() {
		ChatUtils.log("&a[ColorSwap] Plugin disabled");
		super.onDisable();
	}
	
}
