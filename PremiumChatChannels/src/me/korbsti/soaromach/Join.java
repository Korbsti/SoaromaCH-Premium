package me.korbsti.soaromach;

import java.io.IOException;
import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import me.korbsti.soaromach.player.PlayerData;

public class Join implements Listener {
	PremiumChatChannels plugin;
	
	public Join(PremiumChatChannels instance) {
		this.plugin = instance;
	}
	
	@EventHandler
	public void onJoin(PlayerJoinEvent event) {
		Player p = event.getPlayer();
		String name = p.getName();
		
		plugin.spyChannels.put(name, new ArrayList<String>());
		plugin.currentChannel.put(name, plugin.getConfig().getString("channels.name.channelUponJoining"));
		plugin.previousMessage.put(name, 1);
		plugin.toggledParty.put(name, false);
		String uuid = p.getUniqueId().toString();
		plugin.cacheSQL.put(uuid, false);
		if(plugin.useMySQLDatabase) {
		Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {

			@Override
			public void run() {
				

				
				plugin.cacheSQL.put(uuid, true);
				ArrayList<String> userVars = new ArrayList<String>();
				userVars.add("id");
				userVars.add("uuid");
				userVars.add("inParty");
				userVars.add("channel");
				int id = 0;
				for(String str : plugin.mySQLApi.api.getTableInformation("playerinformation", userVars)) {
					String[] data = str.split("=~=~");
					if(data[2].equals(uuid)) {
						plugin.playerData.put(uuid, new PlayerData(plugin, Integer.parseInt(data[4]), p));
						plugin.cacheSQL.put(uuid, false);
						plugin.currentChannel.put(name, data[4]);
						return;
					}
					id++;
				}
				
				ArrayList<String> userEquals = new ArrayList<String>();
				userEquals.add(id + "");
				userEquals.add(uuid);
				userEquals.add("false");
				userEquals.add(plugin.getConfig().getString("channels.name.channelUponJoining"));

				
				plugin.mySQLApi.api.addNewTableInformation("playerinformation",userVars,  userEquals);
				
				
				for(String str : plugin.mySQLApi.api.getTableInformation("playerinformation", userVars)) {
					String[] data = str.split("=~=~");
					if(data[2].equals(uuid)) {
						plugin.playerData.put(uuid, new PlayerData(plugin, Integer.parseInt(data[4]), p));
						plugin.cacheSQL.put(uuid, false);
						plugin.currentChannel.put(name, data[4]);
						return;
					}
				}
				
			}
			
			
		});
		}
		
		
		if (!plugin.useMySQLDatabase) {
			if (plugin.dataYaml.getString(p.getUniqueId().toString() + ".channel") != null) {
				plugin.currentChannel.put(event.getPlayer().getName(), plugin.dataYaml.getString(p.getUniqueId().toString() + ".channel"));
			} else {
				plugin.dataYaml.set(p.getUniqueId().toString() + ".channel", plugin.getConfig().getString("channels.name.channelUponJoining"));
				try {
					plugin.dataYaml.save(plugin.dataFile);
				} catch (IOException e) {
					;
				}
			}
			if (plugin.dataYaml.getString(p.getUniqueId().toString() + ".inParty") == null) {
				plugin.dataYaml.set(p.getUniqueId().toString() + ".inParty", false);
				try {
					plugin.dataYaml.save(plugin.dataFile);
				} catch (IOException e) {
					;
				}
			}
		}
	}
}
