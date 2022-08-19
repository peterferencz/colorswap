package me.peterferencz.utils;

import me.peterferencz.Main;
import net.md_5.bungee.api.ChatColor;

public class ChatUtils {
	public static String color(String str) {
		return ChatColor.translateAlternateColorCodes('&', str);		
	}
	
	public static <T> void log(T msg) {
	    Main.getInstance().getLogger().info(ChatUtils.color("&e[DEBUG] "+msg));
//	    Bukkit.getConsoleSender().sendMessage(ChatUtils.color("&e[DEBUG] "+msg));
    }
	
	public static <T> void error(T msg) {
	    Main.getInstance().getLogger().severe(ChatUtils.color("&e[ERROR] "+msg));
//	    Bukkit.getConsoleSender().sendMessage(ChatUtils.color("&c[ERROR] "+msg));
	}
}
