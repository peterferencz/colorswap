package me.peterferencz.utils;


import org.bukkit.Bukkit;

import net.md_5.bungee.api.ChatColor;

public class ChatUtils {
	public static String Color(String str) {
		return ChatColor.translateAlternateColorCodes('&', str);		
	}
	
	public static <T> void log(T msg) {
	    Bukkit.getConsoleSender().sendMessage(ChatUtils.Color("&e[DEBUG] "+msg));
    }
	
	public static <T> void error(T msg) {
	    Bukkit.getConsoleSender().sendMessage(ChatUtils.Color("&c[ERROR] "+msg));
	}
}
