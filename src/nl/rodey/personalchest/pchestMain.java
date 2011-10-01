package nl.rodey.personalchest;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Type;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.config.Configuration;
import com.nijiko.permissions.PermissionHandler;
import com.nijikokun.bukkit.Permissions.Permissions;
import org.bukkit.plugin.Plugin;

public class pchestMain extends JavaPlugin {
	private Logger log = Logger.getLogger("Minecraft");
	
	private pchestManager chestManager = new pchestManager(this);
	private PluginManager pm;
	
    public static PermissionHandler Permissions = null;
    public boolean usingpermissions = false;
	public boolean debug = false;
	public String pchestWorlds = null;
	public String pchestResRegions = null;
	public String pchestWGRegions = null;

	@Override
	public void onEnable() {
		
		log.info("["+getDescription().getName()+"] version "+getDescription().getVersion()+" loading...");
		
		log = getServer().getLogger();
	    final PluginManager pm = getServer().getPluginManager();
	    if (pm.getPlugin("Spout") == null)
        try {
            pm.enablePlugin(pm.getPlugin("Spout"));
        } catch (final Exception ex) {
            log.warning("["+getDescription().getName()+"] Failed to load Spout, you may have to restart your server or install it.");
            return;
        }
	
        // Load configuration
        loadConfig();
       
        // Register Player Listeners
        registerEvents();
		
        // Register player commands
        getCommand("pchest").setExecutor(new pchestCommand(this, chestManager));
        
        // Check and load permissions
        Plugin permissions = getServer().getPluginManager().getPlugin("Permissions");
		if (Permissions == null)
		{
		    if (permissions != null)
		    {
			    Permissions = ((Permissions)permissions).getHandler();
			    log.info("["+getDescription().getName()+"] version "+getDescription().getVersion()+" is enabled with permissions!");
			    usingpermissions = true;
		    }
		    else
		    {
		    	log.info("["+getDescription().getName()+"] version "+getDescription().getVersion()+" is enabled without permissions!");
		    	usingpermissions = false;
		    }
		}
	}

	@Override
	public void onDisable() {		
		log.info("["+getDescription().getName()+"] version "+getDescription().getVersion()+" is disabled!");
	}	

	public void registerEvents()
    {	
		// Must be loaded after library check
		final pchestPlayerListener playerListener = new pchestPlayerListener(this, chestManager);
		final pchestInventoryListener inventoryListener = new pchestInventoryListener(this, chestManager);
		final pchestEntityListener entityListener = new pchestEntityListener(this, chestManager);
		final pchestBlockListener blockListener = new pchestBlockListener(this, chestManager);
		
        pm = getServer().getPluginManager();

        /* Entity events */
        pm.registerEvent(Type.ENTITY_EXPLODE, entityListener, Event.Priority.Normal, this);

        /* Player events */
        pm.registerEvent(Type.PLAYER_INTERACT, playerListener, Event.Priority.Normal, this);
        
        /* Block events */
		pm.registerEvent(Type.BLOCK_PLACE, blockListener, Event.Priority.Normal, this);
		pm.registerEvent(Type.BLOCK_BREAK, blockListener, Event.Priority.Normal, this);
        
        /* Inventory events */
		pm.registerEvent(Type.CUSTOM_EVENT, inventoryListener, Event.Priority.Normal, this);
    }
	
	public void ShowHelp(Player player)
	{
        player.sendMessage(ChatColor.GREEN + "["+getDescription().getName()+"]" + ChatColor.WHITE + " Usable commands:");
        player.sendMessage("/pchest [create|remove|info]");
        
		return;
    }
	
    public void loadConfig()
    {
        // Ensure config directory exists
        File configDir = this.getDataFolder();
        if (!configDir.exists())
            configDir.mkdir();

        // Check for existance of config file
        File configFile = new File(this.getDataFolder().toString()
                + "/config.yml");
        Configuration config = new Configuration(configFile);

        config.load();
        debug = config.getBoolean("Debug", false);
        pchestWorlds = config.getString("Worlds", null);
        if(pchestWorlds != null)
        {
        	log.info("["+getDescription().getName()+"] All Chests Worlds: " + pchestWorlds);
        }
        pchestResRegions = config.getString("ResidenceRegions", null);
        pchestWGRegions = config.getString("WorldGuardRegions", null);
        if(pchestResRegions != null)
        {
        	log.info("["+getDescription().getName()+"] All Residence Regions: " + pchestResRegions);
        }
        
        if(pchestWGRegions != null)
        {
        	log.info("["+getDescription().getName()+"] All World Guard Regions: " + pchestWGRegions);
        }

        // Create default configuration if required
        if (!configFile.exists())
        {
            try
            {
                configFile.createNewFile();
            } 
            catch (IOException e)
            {
                reportError(e, "IOError while creating config file");
            }

            config.save();
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
	
	public boolean checkpermissions(Player player, String string, Boolean standard)
	{
		return ( (player.isOp() == true) || (usingpermissions ? Permissions.has(player,string) : standard));
	}
}