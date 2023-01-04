package me.korbsti.soaromach.hooks;

import java.util.ArrayList;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.korbsti.soaromach.PremiumChatChannels;

public class PAPI extends PlaceholderExpansion {
	public PremiumChatChannels plugin;
	
	@Override
	public String onPlaceholderRequest(Player p, String identifier) {
		// placeholder: %customplaceholder_staff_count%
		// always check if the player is null for placeholders related to the player!
		if (p == null) {
			return "";
		}
		if (identifier.equals("channel")) {
			String name = p.getName();
			return plugin.currentChannel.get(name);
		}
		
		return null;
	}
	
	@Override
	public boolean canRegister() {
		return (plugin = (PremiumChatChannels) Bukkit.getPluginManager().getPlugin(getRequiredPlugin())) != null;
	}
	
	@Override
	public String getAuthor() {
		return plugin.getDescription().getAuthors().toString();
	}
	
	@Override
	public String getIdentifier() {
		// TODO Auto-generated method stub
		return "soaromachpremium";
	}
	
	@Override
	public String getVersion() {
		// TODO Auto-generated method stub
		return plugin.getDescription().getVersion();
	}
	
	@Override
	public String getRequiredPlugin() {
		return "SoaromaCH-Premium";
	}
	
}

/*
 * if (identifier.equals("married_to_uuid")) { return
 * String.valueOf(plugin.playerDataManager.getMarriedToUUID(uuid)); } if
 * (identifier.equals("married_to")) { return
 * String.valueOf(plugin.playerDataManager.getMarriedTo(uuid)); }
 */