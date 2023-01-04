package me.korbsti.soaromach.discordsrv;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.entity.Player;

import github.scarsz.discordsrv.api.Subscribe;
import github.scarsz.discordsrv.api.events.DiscordGuildMessageReceivedEvent;
import me.korbsti.soaromach.PremiumChatChannels;
import net.md_5.bungee.api.ChatColor;

public class ChannelListener {
	
	PremiumChatChannels plugin;
	
	public ChannelListener(PremiumChatChannels instance) {
		this.plugin = instance;
	}
	 
	@Subscribe
	public void discordMessageReceived(DiscordGuildMessageReceivedEvent event) {
		// Example of logging a message sent in Discord
		
		event.getChannel();
		
		if (event.getChannel().getId().equals(plugin.globalChannelID) || event.getAuthor().isBot() ||  event.getMessage().toString().split(":")[2].equalsIgnoreCase("pl")) {
			return;
		}
		outloop:
		for (String str : plugin.channels) {
			if (!str.equals(plugin.globalChatChannel)) {
				if (event.getChannel().getId().toString().equals(plugin.getConfig().getString("channels.name." + str + ".channelID"))) {
					String message;
					java.awt.Color color = event.getMember().getRoles().get(0).getColor();
					int r = color.getRed();
					int g = color.getGreen();
					int b = color.getBlue();
					String hex = String.format("#%02x%02x%02x", r, g, b);  
					
					message = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("channels.name."+ str + ".fromDiscordFormat")).replace("{role}",  "&" + hex + event.getMember().getRoles().get(0).getName()).replace("{user}", event.getMember().getEffectiveName()).replace("{message}", "" + event.getMessage().getContentDisplay());
					String permission = plugin.getConfig().getString("channels.name." + str + ".permission");
					for(Player p : Bukkit.getOnlinePlayers()) {
						if(p.hasPermission(permission))
							p.sendMessage(plugin.chatChannel.translateHexColorCodes(message)  );
					}
					break outloop;
				}
			}
		}
		
	}
	
}
