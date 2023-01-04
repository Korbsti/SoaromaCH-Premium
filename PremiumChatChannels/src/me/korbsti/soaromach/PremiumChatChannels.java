package me.korbsti.soaromach;

import me.clip.placeholderapi.PlaceholderAPI;
import me.korbsti.soaromach.configmanager.Configuration;
import me.korbsti.soaromach.discordsrv.ChannelListener;
import me.korbsti.soaromach.hooks.PAPI;
import me.korbsti.soaromach.mysql.MySQLAPI;
import me.korbsti.soaromach.player.PlayerData;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import github.scarsz.discordsrv.DiscordSRV;

public class PremiumChatChannels extends JavaPlugin implements Listener {
	
	public HashMap<String, Boolean> derp = new HashMap<String, Boolean>();
	public HashMap<String, String> currentChannel = new HashMap<String, String>();
	public HashMap<String, Integer> discordChannelDelay = new HashMap<String, Integer>();
	public ArrayList<String> emojis = new ArrayList<String>();
	public HashMap<String, Integer> previousMessage = new HashMap<String, Integer>();
	public HashMap<String, Boolean> toggledParty = new HashMap<String, Boolean>();
	public HashMap<String, ArrayList<String>> spyChannels = new HashMap<>();
	public HashMap<String, PlayerData> playerData = new HashMap<String, PlayerData>();
	public HashMap<String, Boolean> cacheSQL = new HashMap<String, Boolean>();
	
	
	public MessageSender chatChannel = new MessageSender(this);
	
	
	public Commands commands = new Commands(this);
	public Configuration config = new Configuration(this);
	public Set<String> allKeys;
	public ArrayList<String> channels;
	
	String dir = System.getProperty("user.dir");
	String directoryPathFile = dir + File.separator + "plugins" + File.separator + "ChatEmojis" + File.separator + "emojis.yml";
	
	public File chatEmojisFile;
	public YamlConfiguration chatEmojiData;
	
	public File dataFile;
	public YamlConfiguration dataYaml;
	
	public File configFile;
	public YamlConfiguration configYaml;
	
	
	
	public DiscordSRV api = DiscordSRV.getPlugin();
	public PremiumChatChannels main = this;
	public MySQLAPI mySQLApi;
	
	
	
	
	public String globalChatChannel;
	public String globalChannelID;
	
	public boolean emojiExists = false;
	public boolean useMySQLDatabase = false;
	public boolean hasPlaceholder = false;
	public boolean enableGlobalChat = false;
	public boolean enableArgsAsMessage = false;
	
	@Override
	public void onEnable() {
		if (new File(directoryPathFile).exists())
			emojiExists = true;
		
		DiscordSRV.api.subscribe(new ChannelListener(this));
        int pluginId = 15662;
        Metrics metrics = new Metrics(this, pluginId);
		PluginManager pm = Bukkit.getPluginManager();
		pm.registerEvents(new ChatChannel(this), this);
		pm.registerEvents(new Join(this), this);
		config.configCreator("config.yml", "data.yml");
		getCommand("ch").setExecutor(new Commands(this));
		getCommand("chreload").setExecutor(new Commands(this));
		getCommand("chlist").setExecutor(new Commands(this));
		getCommand("chspy").setExecutor(new Commands(this));
		Set<String> allKeys = getConfig().getKeys(true);
		channels = new ArrayList<String>();
		for (String key : allKeys) {
			if (!key.endsWith("defaultGlobal") && !key.endsWith("defaultGlobalPermission")
			        && key.startsWith("channels.name.") && !key.endsWith(".permission") && !key.endsWith(".prefix")
			        && !key.endsWith(".sendRegardlessOfCurrentChannel") && !key.endsWith(".distanceMessage")
			        && !key.endsWith(".enableDistanceMessage") && !key.endsWith(".messageFormat")
			        && !key.endsWith(".chlistDisplayAll") && !key.endsWith(".channelExists")
			        && !key.endsWith(".defaultGlobalMessageFormat") && !key.endsWith(".enableGlobalMessageFormat")
			        && !key.endsWith(".channelUponJoining")
			        && !key.endsWith(".spyPermission")
			        && !key.endsWith(".globalChannelID")
			        && !key.endsWith(".channelID")
			        && !key.endsWith(".fromDiscordFormat")
			        && !key.endsWith(".toDiscordFormat")) {
				channels.add(key.replace("channels.name.", ""));
			}
		}
		
		/////
		enableGlobalChat = getConfig().getBoolean("channels.name.enableGlobalMessageFormat");
		channels.add(getConfig().getString("channels.name.defaultGlobal"));
		enableArgsAsMessage = getConfig().getBoolean("channels.name.enableArgsAsMessage");
		globalChatChannel = configYaml.getString("channels.name.defaultGlobal");
		globalChannelID = configYaml.getString("channels.name.globalChannelID");
		
		
		if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
			hasPlaceholder = true;
		}
		int dd = channels.size() - 1;
		int holder = 0;
		while (dd != holder) {
			for (int x = 0; x != channels.size(); x++) {
				String channel = channels.get(x);
				if (!getConfig().getBoolean("channels.name." + channel + ".channelExists") && !channel.equals(getConfig().getString("channels.name.defaultGlobal"))) {
					for (int x1 = 0; x1 != channels.size(); x1++) {
						if (channels.get(x).equals(channel)) {
							channels.remove(x);
							x1 = 0;
							x = 0;
						}
					}
				}
			}
			holder++;
		}
		
		for (Player p : Bukkit.getOnlinePlayers()) {
			currentChannel.put(p.getName(), getConfig().getString("channels.name.defaultGlobal"));
			spyChannels.put(p.getName(), new ArrayList<String>());
		}
		
		useMySQLDatabase = configYaml.getBoolean("useMySQLDatabase");
		
		if(useMySQLDatabase) {
			mySQLApi = new MySQLAPI(configYaml.getString("database"), configYaml.getString("username"), configYaml.getString("password"), this);
			ArrayList<String> userVars = new ArrayList<String>();
			userVars.add("uuid");
			userVars.add("inParty");
			userVars.add("channel");
			mySQLApi.api.createTable("playerinformation", userVars);
		}
		
		if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
			new PAPI().register();
		}
		
		
		
		
		for(Player p : Bukkit.getOnlinePlayers()) {
			String name = p.getName();
			
			spyChannels.put(name, new ArrayList<String>());
			currentChannel.put(name, getConfig().getString("channels.name.channelUponJoining"));
			previousMessage.put(name, 1);
			toggledParty.put(name, false);
			String uuid = p.getUniqueId().toString();

			cacheSQL.put(uuid, false);

			if(useMySQLDatabase) {
			Bukkit.getScheduler().runTaskAsynchronously(this, new Runnable() {

				@Override
				public void run() {
					

					
					String uuid = p.getUniqueId().toString();
					cacheSQL.put(uuid, true);
					ArrayList<String> userVars = new ArrayList<String>();
					userVars.add("id");
					userVars.add("uuid");
					userVars.add("inParty");
					userVars.add("channel");
					int id = 0;
					for(String str : mySQLApi.api.getTableInformation("playerinformation", userVars)) {
						String[] data = str.split("=~=~");
						if(data[2].equals(uuid)) {
							playerData.put(uuid, new PlayerData(main, Integer.parseInt(data[0]), p));
							cacheSQL.put(uuid, false);
							currentChannel.put(name, data[4]);

							return;
						}
						id++;
					}
					
					ArrayList<String> userEquals = new ArrayList<String>();
					userEquals.add(id + "");
					userEquals.add(uuid);
					userEquals.add("false");
					userEquals.add(getConfig().getString("channels.name.channelUponJoining"));

					
					mySQLApi.api.addNewTableInformation("playerinformation",userVars,  userEquals);
					
					
					for(String str : mySQLApi.api.getTableInformation("playerinformation", userVars)) {
						String[] data = str.split("=~=~");
						if(data[2].equals(uuid)) {
							playerData.put(uuid, new PlayerData(main, Integer.parseInt(data[0]), p));
							cacheSQL.put(uuid, false);
							currentChannel.put(name, data[4]);
							return;
						}
					}
					
				}
				
				
			});
			
			}
			if (!useMySQLDatabase) {
				if (dataYaml.getString(p.getUniqueId().toString() + ".channel") != null) {
					currentChannel.put(p.getPlayer().getName(), dataYaml.getString(p.getUniqueId().toString() + ".channel"));
				} else {
					dataYaml.set(p.getUniqueId().toString() + ".channel", getConfig().getString("channels.name.channelUponJoining"));
					try {
						dataYaml.save(dataFile);
					} catch (IOException e) {
						;
					}
				}
				if (dataYaml.getString(p.getUniqueId().toString() + ".inParty") == null) {
					dataYaml.set(p.getUniqueId().toString() + ".inParty", false);
					try {
						dataYaml.save(dataFile);
					} catch (IOException e) {
						;
					}
				}
			}
		}
		
		
		
	}
	
	@Override
	public void onDisable() {
	}
}
