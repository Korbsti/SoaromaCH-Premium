package me.korbsti.soaromach.configmanager;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;

import me.korbsti.soaromach.PremiumChatChannels;

public class Configuration {
	PremiumChatChannels plugin;
	
	
	String directoryPathFile = System.getProperty("user.dir") + File.separator + "plugins" + File.separator + "SoaromaCH-Premium";
	public Configuration(PremiumChatChannels plugin) {
		this.plugin = plugin;
	}
	
	
	
	public void configCreator(String fileName, String message) {
		if (new File(directoryPathFile).mkdirs()) Bukkit.getLogger().info("Generated SoaromaCH-Premium configuration and data folder...");
		plugin.configFile = new File(plugin.getDataFolder(), fileName);
		plugin.dataFile = new File(plugin.getDataFolder(), message);

		
		if (!plugin.configFile.exists())
			plugin.saveDefaultConfig();
		if (!plugin.dataFile.exists())try {plugin.dataFile.createNewFile();} catch (IOException e) {e.printStackTrace();}
		plugin.configYaml = YamlConfiguration.loadConfiguration(plugin.configFile);
		plugin.dataYaml = YamlConfiguration.loadConfiguration(plugin.dataFile);
		
		
		
		if (plugin.emojiExists) {
			plugin.chatEmojisFile = new File(directoryPathFile);
			plugin.chatEmojiData = YamlConfiguration.loadConfiguration(plugin.chatEmojisFile);
			Set<String> keys = plugin.chatEmojiData.getKeys(true);
			
			for (String str : keys) {
				if (!str.endsWith(".gui-name")
				        && !str.endsWith(".check")
				        && !str.endsWith(".replacement")
				        && !str.endsWith(".creator")) {
					plugin.emojis.add(str.replace("emojis.", ""));
				}
			}
		}
	}
	
	
	
	
	
}
