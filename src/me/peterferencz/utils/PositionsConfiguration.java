package me.peterferencz.utils;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;
import me.peterferencz.Main;

public class PositionsConfiguration {
    private static String fileName = "positions.yml";
    
    static File file;
    static YamlConfiguration config;
    
    public static void init() {
        file = new File(Main.getInstance().getDataFolder(), fileName);
        if (!file.exists()) {
            file.getParentFile().mkdirs();
            Main.getInstance().saveResource(fileName, false);
        }
        
        config = YamlConfiguration.loadConfiguration(file);
        
        
        //config.setDefaults(YamlConfiguration.loadConfiguration(new InputStreamReader(Main.i.getResource("positions.yml"))));
    }
    
    public static Location getTemplatePos1() {
        return config.getLocation("template.pos1");
    }
    
    public static Location getTemplatePos2() {
        return  config.getLocation("template.pos2");
     }
    
    public static Location getPlayAreaPos1() {
        return config.getLocation("playarea.pos1");
    }
    
    public static Location getPlayAreaPos2() {
        return config.getLocation("playarea.pos2");
    }
    
    public static void setTemplatePos1(Location l) {
        config.set("template.pos1", l);
        saveConfiguration();
    }
    
    public static void setTemplatePos2(Location l) {
        config.set("template.pos2", l);
        saveConfiguration();
     }
    
    public static void setPlayAreaPos1(Location l) {
        config.set("playarea.pos1", l);
        saveConfiguration();
    }
    
    public static void setPlayAreaPos2(Location l) {
        config.set("playarea.pos2", l);
        saveConfiguration();
    }
    
    public static Location getTemplateMinimum() {
        return new Location(getTemplatePos1().getWorld(),
                Math.min(getTemplatePos1().getBlockX(), getTemplatePos2().getBlockX()),
                Math.min(getTemplatePos1().getBlockY(), getTemplatePos2().getBlockY()),
                Math.min(getTemplatePos1().getBlockZ(), getTemplatePos2().getBlockZ()));
    }
    
    public static Location getTemplateMaximum() {
        return new Location(getTemplatePos1().getWorld(),
                Math.max(getTemplatePos1().getBlockX(), getTemplatePos2().getBlockX()),
                Math.max(getTemplatePos1().getBlockY(), getTemplatePos2().getBlockY()),
                Math.max(getTemplatePos1().getBlockZ(), getTemplatePos2().getBlockZ()));
    }
    
    public static Location getPlayAreaMinimum() {
        return new Location(getPlayAreaPos1().getWorld(),
                Math.min(getPlayAreaPos1().getBlockX(), getPlayAreaPos2().getBlockX()),
                Math.min(getPlayAreaPos1().getBlockY(), getPlayAreaPos2().getBlockY()),
                Math.min(getPlayAreaPos1().getBlockZ(), getPlayAreaPos2().getBlockZ()));
    }
    
    public static Location getPlayAreaMaximum() {
        return new Location(getPlayAreaPos1().getWorld(),
                Math.max(getPlayAreaPos1().getBlockX(), getPlayAreaPos2().getBlockX()),
                Math.max(getPlayAreaPos1().getBlockY(), getPlayAreaPos2().getBlockY()),
                Math.max(getPlayAreaPos1().getBlockZ(), getPlayAreaPos2().getBlockZ()));
    }
    
    public static void saveConfiguration() {
        try {
            config.save(file);
        } catch (IOException e) {
            ChatUtils.error("Couldn't save " + fileName + " to " + file.getPath());
        }
    }
    
    public static Iterator<Location> iterateThroughBox(Location min, Location max){
        return new Iterator<Location>() {
            int x = min.getBlockX();
            int y = min.getBlockY();
            int z = min.getBlockZ();
            boolean hasNext = true;
            
            @Override
            public Location next() {
                if(++x > max.getBlockX()) {
                    x = min.getBlockX();
                    if(++y > max.getBlockY()) {
                        y = min.getBlockY();
                        if(++z > max.getBlockZ()) {
                            z = min.getBlockZ();
                            hasNext = false;
                        }
                    }
                }
                return new Location(min.getWorld(), x, y, z);
            }

            @Override
            public boolean hasNext() {
                return hasNext;
            }
        };
    }

    public static void isAllSet() throws Exception{
        Location t1 = getTemplatePos1();
        Location t2 = getTemplatePos2();
        Location a1 = getPlayAreaPos1();
        Location a2 = getPlayAreaPos2();
        if (t1 == null || t2 == null || a1 == null || a2 == null) {
            throw new Exception("Not all areas are set in positions.yml (use /arenasetter)!");
        }
        
        if ((!t1.getWorld().equals(t2.getWorld()))||(!t2.getWorld().equals(a1.getWorld()))||(!a1.getWorld().equals(a2.getWorld()))) {
            throw new Exception("All of the area locations should be in the same world!");
        }
        
        
        Location tMin = getTemplateMinimum();
        Location tMax = getTemplateMaximum();
        Location aMin = getPlayAreaMinimum();
        Location aMax = getPlayAreaMaximum();
        
        //TODO check overlap
        
        if(!getDimension(aMin, aMax).equals(getDimension(tMin, tMax))) {            
            throw new Exception("Template and playarea sizes don't match");
        }
    }
    
    public static Location getDimension(Location min, Location max) {
        return new Location(min.getWorld(),
                max.getBlockX() - min.getBlockX(),
                max.getBlockY() - min.getBlockY(),
                max.getBlockZ() - min.getBlockZ());
    }
}
