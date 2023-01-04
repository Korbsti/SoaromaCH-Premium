package me.korbsti.soaromach;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import com.gmail.nossr50.mcMMO;
import com.gmail.nossr50.api.PartyAPI;

import me.clip.placeholderapi.PlaceholderAPI;

public class ChatChannel implements Listener {
	PremiumChatChannels plugin;
	
	public ChatChannel(PremiumChatChannels instance) {
		plugin = instance;
	}
	
	@EventHandler(ignoreCancelled = true)
	public void chatEvent(AsyncPlayerChatEvent e) {
		String playerName = e.getPlayer().getName();
		if (plugin.currentChannel.get(playerName) == null) {
			plugin.currentChannel.put(playerName, plugin.getConfig().getString("channels.name.defaultGlobal"));
		}
		if (!plugin.currentChannel.get(playerName).equals(plugin.getConfig().getString("channels.name.defaultGlobal"))) {
			Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
				
				@Override
				public void run() {
					if (plugin.toggledParty.get(playerName)) {
						Player p = e.getPlayer();
						if (PartyAPI.inParty(p)) {
							String str = plugin.chatChannel.formatParty(p, e.getMessage());
							for (Player mem : PartyAPI.getOnlineMembers(p)) {
								mem.sendMessage(str);
							}
						} else {
							p.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("notInParty")));
						}
					} else {
						String perm = "permission";
						perm = plugin.getConfig().getString("channels.name." + plugin.currentChannel.get(playerName) + ".permission");
						if (perm == null) {
							return;
						}
						plugin.chatChannel.messageChannelSender(e.getPlayer(), e.getMessage(), perm, false, false);
					}
				}
			});
			e.setCancelled(true);
			return;
		}
		
		if (plugin.toggledParty.get(playerName)) {
			Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
				
				@Override
				public void run() {
					Player p = e.getPlayer();
					if (PartyAPI.inParty(p)) {
						String str = plugin.chatChannel.formatParty(p, e.getMessage());
						for (Player mem : PartyAPI.getOnlineMembers(p)) {
							mem.sendMessage(str);
						}
					} else {
						p.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("notInParty")));
					}
				}
			});
			e.setCancelled(true);
			return;
		}
		if (plugin.currentChannel.get(playerName).equalsIgnoreCase(plugin.globalChatChannel) && plugin.enableGlobalChat) {
			Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
				@Override
				public void run() {
					String displayMessage = plugin.getConfig().getString("channels.name.defaultGlobalMessageFormat").replace("{player}", e.getPlayer().getDisplayName()).replace("{message}", e.getMessage());
					for (Player p : Bukkit.getOnlinePlayers()) {
						if (plugin.hasPlaceholder) {
							p.sendMessage(ChatColor.translateAlternateColorCodes('&', PlaceholderAPI.setPlaceholders(e.getPlayer(), displayMessage)));
						} else {
							p.sendMessage(ChatColor.translateAlternateColorCodes('&', displayMessage));
						}
					}
					Bukkit.getLogger().info(ChatColor.translateAlternateColorCodes('&', displayMessage));
				}
				
			});
			e.setCancelled(true);
		}
	}
}
