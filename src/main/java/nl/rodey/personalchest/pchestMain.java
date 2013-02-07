package nl.rodey.personalchest;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class pchestMain extends JavaPlugin {
	private Logger log = Logger.getLogger("Minecraft");
	
	private pchestManager chestManager = new pchestManager(this);
	
	public boolean debug = false;
	public String pchestWorlds = null;
	public String pchestResRegions = null;
	public String pchestWGRegions = null;
	//public boolean SpoutLoaded = true;
	
	private FileConfiguration config;

	@Override
	public void onEnable() {
		
		log.info("["+getDescription().getName()+"] version "+getDescription().getVersion()+" loading...");
		
		log = getServer().getLogger();
	    
		/*
	    if (pm.getPlugin("Spout") == null)
        try {
            pm.enablePlugin(pm.getPlugin("Spout"));
        } catch (final Exception ex) {
            log.warning("["+getDescription().getName()+"] Failed to load Spout, you may have to restart your server or install it.");
            SpoutLoaded = false;
        }
        */
	
        // Load configuration
        loadConfig();
       
        // Register Player Listeners
        registerEvents();
		
        // Register player commands
        getCommand("pchest").setExecutor(new pchestCommand(this, chestManager));
        
        log.info("["+getDescription().getName()+"] version "+getDescription().getVersion()+" is enabled");
	}

	@Override
	public void onDisable() {		
		log.info("["+getDescription().getName()+"] version "+getDescription().getVersion()+" is disabled!");
	}	

	public void registerEvents()
    {	

		final PluginManager pm = getServer().getPluginManager();
		
		// Must be loaded after library check
		final pchestPlayerListener playerListener = new pchestPlayerListener(this, chestManager);
		final pchestEntityListener entityListener = new pchestEntityListener(this, chestManager);
		final pchestBlockListener blockListener = new pchestBlockListener(this, chestManager);
		final pchestInventoryListener inventoryListener = new pchestInventoryListener(this, chestManager);

        /* Entity events */
		pm.registerEvents(entityListener, this);

        /* Player events */
		pm.registerEvents(playerListener, this);
        
        /* Block events */
		pm.registerEvents(blockListener, this);

        /* Inventory events */
		pm.registerEvents(inventoryListener, this);
		
        /* Spout Required events
		if(SpoutLoaded)
		{

		} */
    }
	
	public void ShowHelp(Player player)
	{
        player.sendMessage(ChatColor.GREEN + "["+getDescription().getName()+"]" + ChatColor.WHITE + " Usable commands:");
        player.sendMessage("/pchest [create|remove|info]");
        
		return;
    }
	
	public void loadConfig(){
		// Ensure config directory exists
		File configDir = this.getDataFolder();
		if (!configDir.exists())
			configDir.mkdir();

		// Check for existance of config file
		File configFile = new File(this.getDataFolder().toString() + "/config.yml");
		config = YamlConfiguration.loadConfiguration(configFile);
		
		// Adding Variables
		if(!config.contains("Debug"))
		{
			config.addDefault("Debug", false);
	       
	        config.addDefault("Worlds", "ExampleWorld1, ExampleWorld2");
	
	        config.addDefault("Regions.Residence", "ExampleWorld.ExampleResRegion1, ExampleWorld.ExampleResRegion2");
	        
	        config.addDefault("Regions.WorldGuard", "ExampleWorld.ExampleWGRegion1, ExampleWorld.ExampleWGRegion2");     
		}
        
        // Loading the variables from config
    	debug = (Boolean) config.get("Debug");
    	pchestWorlds = (String) config.get("Worlds");
    	pchestResRegions = (String) config.get("Regions.Residence");
    	pchestWGRegions = (String) config.get("Regions.WorldGuard");
        
        if(pchestWorlds != null)
        {
        	log.info("["+getDescription().getName()+"] All Chests Worlds: " + pchestWorlds);
        }
        
        if(pchestResRegions != null)
        {
        	log.info("["+getDescription().getName()+"] All Residence Regions: " + pchestResRegions);
        }
        
        if(pchestWGRegions != null)
        {
        	log.info("["+getDescription().getName()+"] All World Guard Regions: " + pchestWGRegions);
        }
        
        config.options().copyDefaults(true);
        try {
            config.save(configFile);
        } catch (IOException ex) {
            Logger.getLogger(JavaPlugin.class.getName()).log(Level.SEVERE, "Could not save config to " + configFile, ex);
        }
    }

    public void reportError(Exception e, String message)
    {
        reportError(e, message, true);
    }

    public void reportError(Exception e, String message, boolean dumpStackTrace)
    {
        PluginDescriptionFile pdfFile = this.getDescription();
        log.severe("["+getDescription().getName()+"] " + pdfFile.getVersion() + " - " + message);
        if (dumpStackTrace)
            e.printStackTrace();
    }

	public Player getPlayerByString(String playerName)
	{
		Player player = getServer().getPlayer(playerName);
		
		return player;
	}
	
}