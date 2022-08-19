package me.peterferencz.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import me.peterferencz.Main;
import me.peterferencz.managers.GameManager;
import me.peterferencz.utils.ChatUtils;

public class ForceStartCommand implements CommandExecutor{
	
	public ForceStartCommand() {
		Main.getInstance().getCommand("start").setExecutor(this);
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
	    if(!sender.hasPermission("javaplugin.forcestart")) {
            sender.sendMessage(ChatUtils.color("&cYou don't have permissions to execute this command!"));
            return true;
        }
	    
	    GameManager.forceStart();
		return true;
	}

}
