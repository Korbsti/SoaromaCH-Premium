package me.korbsti.soaromach.player;

import org.bukkit.entity.Player;

import me.korbsti.soaromach.PremiumChatChannels;

public class PlayerData {
	
	PremiumChatChannels plugin;
	
	
	private int id;
	private Player p;
	
	
	
	public PlayerData(PremiumChatChannels plugin, int id, Player p) {
		this.plugin = plugin;
		this.id = id;
		this.p = p;
	}
	
	
	public int getID() {
		return this.id;
	}
	
	public Player getPlayer() {
		return this.p;
	}
	
	public void setID(int id) {
		this.id = id;
	}
	
	public void setPlayer(Player p) {
		this.p = p;
	}
	
	
	
	
	
	
	
	
	
	
}
