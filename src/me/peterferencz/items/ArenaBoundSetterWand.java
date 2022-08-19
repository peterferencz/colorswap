package me.peterferencz.items;

import java.util.Arrays;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import me.peterferencz.Main;
import me.peterferencz.managers.GameManager;
import me.peterferencz.managers.GameManager.GameState;
import me.peterferencz.utils.ChatUtils;
import me.peterferencz.utils.PositionsConfiguration;

public class ArenaBoundSetterWand implements Listener, CommandExecutor {
    
    public static String nameTemplate = "Set: Template";
    public static String namePlayArea = "Set: PlayArea";
    
    public ArenaBoundSetterWand() {
        Main.getInstance().getCommand("arenasetter").setExecutor(this);
        Main.getInstance().getServer().getPluginManager().registerEvents(this, Main.getInstance());
    }
    
    @EventHandler
    public void onItemDrop(PlayerDropItemEvent e) {
        if (!e.getPlayer().hasPermission("jvaaplugin.wand")) { return; }
        
        ItemStack item = e.getItemDrop().getItemStack();
        //if(item == null) { return; }
        
        ItemMeta itemMeta = item.getItemMeta();
        
        if(itemMeta.getDisplayName().equalsIgnoreCase(namePlayArea)) {
            itemMeta.setDisplayName(nameTemplate);
            item.setItemMeta(itemMeta);
            e.setCancelled(true);
        }else if(itemMeta.getDisplayName().equalsIgnoreCase(nameTemplate)){
            itemMeta.setDisplayName(namePlayArea);
            item.setItemMeta(itemMeta);
            e.setCancelled(true);
        }
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if(!(sender instanceof Player)) {
            sender.sendMessage(ChatUtils.color("&cOnly Players can execute this command!"));
            return true;
        }
        Player p = (Player) sender;
        if(!sender.hasPermission("jvaaplugin.wand")) {
            p.sendMessage(ChatUtils.color("&cYou don't have permissions to do that!"));
            return true;
        }
        
        p.getInventory().addItem(getItem());
        p.sendMessage(ChatUtils.color("&eYou shall have the stick!"));
        
        return true;
    }
    
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) {
        if (!e.getPlayer().hasPermission("jvaaplugin.wand")) { return; }
        
        ItemStack handItem = e.getItem();
        if(handItem == null) { return; }
        ItemMeta meta = handItem.getItemMeta();        
        if(meta == null) { return; }
        String dipslayName = meta.getDisplayName();
        
        //TODO check item in hand
        if (!(dipslayName.equals(nameTemplate)|| dipslayName.equals(namePlayArea))) {
            return;
        }

        if(e.getClickedBlock() == null) { return; }
        
        if(GameManager.getState() != GameState.WAITINGFORPLAYERS) {
            e.getPlayer().sendMessage(ChatUtils.color("&cYou cannot use the wand while a game is in progress"));
            return;
        }
        
        switch (e.getAction()) {
        case RIGHT_CLICK_BLOCK:
            if(dipslayName.equals(nameTemplate)) {
                PositionsConfiguration.setTemplatePos1(e.getClickedBlock().getLocation());
                e.getPlayer().sendMessage(ChatUtils.color(String.format("&eLocation of %s is set to %s", "Template1", prettyPrintLocation(e.getClickedBlock().getLocation()))));
                e.setCancelled(true);
            } else {
                PositionsConfiguration.setPlayAreaPos1(e.getClickedBlock().getLocation());
                e.getPlayer().sendMessage(ChatUtils.color(String.format("&eLocation of %s is set to %s", "PlayArea1", prettyPrintLocation(e.getClickedBlock().getLocation()))));
                e.setCancelled(true);
            }
            break;
        case LEFT_CLICK_BLOCK:
            if(dipslayName.equals(nameTemplate)) {
                PositionsConfiguration.setTemplatePos2(e.getClickedBlock().getLocation());
                e.getPlayer().sendMessage(ChatUtils.color(String.format("&eLocation of %s is set to %s", "Template2", prettyPrintLocation(e.getClickedBlock().getLocation()))));
                e.setCancelled(true);
            } else {
                PositionsConfiguration.setPlayAreaPos2(e.getClickedBlock().getLocation());
                e.getPlayer().sendMessage(ChatUtils.color(String.format("&eLocation of %s is set to %s", "PlayArea2", prettyPrintLocation(e.getClickedBlock().getLocation()))));
                e.setCancelled(true);
            }
        default:
            break;
        }
    }
    
    private static String prettyPrintLocation(Location location) {
        return String.format("%s %s %s", location.getX(), location.getY(), location.getBlockZ());
    }
    
    public static ItemStack getItem() {
        ItemStack stack = new ItemStack(Material.STICK);
        stack.addUnsafeEnchantment(Enchantment.ARROW_INFINITE, 1);
        
        ItemMeta meta = stack.getItemMeta();
        meta.setDisplayName(nameTemplate);
        meta.setLore(Arrays.asList("Drop item to change ", "current mode"));
        
        stack.setItemMeta(meta);
        
        return stack;
    }
}