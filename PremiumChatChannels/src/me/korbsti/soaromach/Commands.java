package me.korbsti.soaromach;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.gmail.nossr50.api.PartyAPI;

import me.clip.placeholderapi.PlaceholderAPI;

public class Commands implements CommandExecutor {
	PremiumChatChannels plugin;
	
	public Commands(PremiumChatChannels instance) {
		this.plugin = instance;
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (label.equalsIgnoreCase("chlist")) {
			if (sender.hasPermission("ch.list")) {
				ArrayList<String> send = new ArrayList<>();
				for (String channel : plugin.channels) {
					send.add(channel);
				}
				int dd = send.size() - 1;
				int holder = 0;
				while (holder != dd) {
					for (int x = 0; x != send.size(); x++) {
						String channel = send.get(x);
						if (plugin.getConfig().getBoolean("channels.name." + channel + ".chlistDisplayAll") == false
						        && !channel.equals(plugin.getConfig().getString("channels.name.defaultGlobal"))
						        && !sender.hasPermission(
						                plugin.getConfig().getString("channels.name." + channel + ".permission"))) {
							if (send.get(x).equals(channel)) {
								send.remove(x);
								x = 0;
							}
						}
					}
					holder++;
				}
				sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
				        (plugin.getConfig().getString("channel-list").replace("{channels}", send.toString()))));
				return true;
			} else {
				sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("noPerm")));
			}
		}
		
		if (label.equalsIgnoreCase("chreload")) {
			if (sender.hasPermission("ch.reload")) {
				sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("reloadNotRec")));

				Set<String> allKeys = plugin.getConfig().getKeys(true);
				plugin.channels = new ArrayList<String>();
				for (String key : allKeys) {
					if (!key.endsWith("defaultGlobal") && !key.endsWith("defaultGlobalPermission")
					        && key.startsWith("channels.name.") && !key.endsWith(".permission") && !key.endsWith(
					                ".prefix")
					        && !key.endsWith(".sendRegardlessOfCurrentChannel") && !key.endsWith(".distanceMessage")
					        && !key.endsWith(".enableDistanceMessage") && !key.endsWith(".messageFormat")
					        && !key.endsWith(".chlistDisplayAll") && !key.endsWith(".channelExists")
					        && !key.endsWith(".defaultGlobalMessageFormat") && !key.endsWith(
					                ".enableGlobalMessageFormat")
					        && !key.endsWith(".channelUponJoining")
					        && !key.endsWith(".spyPermission")) {
						plugin.channels.add(key.replace("channels.name.", ""));
					}
				}
				plugin.enableGlobalChat = plugin.getConfig().getBoolean("channels.name.enableGlobalMessageFormat");
				plugin.channels.add(plugin.getConfig().getString("channels.name.defaultGlobal"));
				if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
					plugin.hasPlaceholder = true;
				}
				int dd = plugin.channels.size() - 1;
				int holder = 0;
				while (dd != holder) {
					for (int x = 0; x != plugin.channels.size(); x++) {
						String channel = plugin.channels.get(x);
						if (!plugin.getConfig().getBoolean("channels.name." + channel + ".channelExists")
						        && !channel.equals(plugin.getConfig().getString("channels.name.defaultGlobal"))) {
							for (int x1 = 0; x1 != plugin.channels.size(); x1++) {
								if (plugin.channels.get(x).equals(channel)) {
									plugin.channels.remove(x);
									x1 = 0;
									x = 0;
								}
							}
						}
					}
					holder++;
				}
				for (Player p : Bukkit.getOnlinePlayers()) {
					plugin.currentChannel.put(p.getName(), plugin.globalChatChannel);
				}
				sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString(
				        "reloaded")));
				return true;
			} else {
				sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("noPerm")));
			}
		}
		if (label.equalsIgnoreCase("chspy")) {
			if (!(sender.hasPermission("ch.use.spy"))) {
				sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("noPerm")));
				return true;
			}
			if (args.length == 0) {
				sender.sendMessage(
				        ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("invalidArgs")));
				return true;
			}
			for (String str : plugin.channels) {
				if (args[0].equalsIgnoreCase(str)) {
					if (str.equalsIgnoreCase(plugin.getConfig().getString("channels.name.defaultGlobal"))) {
						sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString(
						        "cannotSpyGlobal")));
						return true;
					}
					
					if (sender.hasPermission(plugin.getConfig().getString("channels.name." + str + ".spyPermission"))) {
						int x = 0;
						for (String ch : plugin.spyChannels.get(sender.getName())) {
							if (ch.equalsIgnoreCase(str)) {
								plugin.spyChannels.get(sender.getName()).remove(x);
								sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig()
								        .getString("turnSpyOff").replace("{channel-name}", str)));
								return true;
							}
							x++;
						}
						plugin.spyChannels.get(sender.getName()).add(str);
						sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString(
						        "turnSpyOn").replace("{channel-name}", str)));
						return true;
					}
					sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString(
					        "noPerm")));
					return true;
				}
			}
		}
		if (label.equalsIgnoreCase("ch")) {
			if (plugin.cacheSQL.get(((Player) sender).getUniqueId().toString())) {
				sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("mySQLLoading")));
				return false;
			}
			if (!(sender.hasPermission("ch.use.channels"))) {
				sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("noPerm")));
				return true;
			}
			if (args.length == 0) {
				sender.sendMessage(
				        ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("invalidArgs")));
				return true;
			}
			if (plugin.derp.get(sender.getName()) == null) {
				plugin.derp.put(sender.getName(), false);
			}
			if (plugin.currentChannel.get(sender.getName()) == null) {
				plugin.currentChannel.put(sender.getName(), plugin.getConfig().getString(
				        "channels.name.defaultGlobal"));
			}
			if (args[0].equalsIgnoreCase("party") && args.length >= 2) {
				if(Bukkit.getPluginManager().getPlugin("mcMMO") == null) {
					sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("notInParty")));
					return false;
				}
				
				Player p = (Player) sender;
				StringBuilder msg = new StringBuilder();
				for (int i = 1; i != args.length; i++) {
					msg.append(args[i] + " ");
				}
				if (PartyAPI.inParty(p)) {
					String str = plugin.chatChannel.formatParty(p, msg.toString());
					for (Player mem : PartyAPI.getOnlineMembers(p)) {
						mem.sendMessage(str);
					}
				} else {
					p.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("notInParty")));
				}
				
				return true;
			}
			if (args[0].equalsIgnoreCase("party")) {
				if(Bukkit.getPluginManager().getPlugin("mcMMO") == null) {
					sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("notInParty")));
					return false;
				}
				Player p = (Player) sender;
				
				if (!plugin.useMySQLDatabase) {
					plugin.dataYaml.set(p.getUniqueId().toString() + ".inParty", true);
					
					try {
						plugin.dataYaml.save(plugin.dataFile);
					} catch (IOException e) {
						;
					}
					
				} else {
					
					plugin.mySQLApi.api.updateTableInformation("playerinformation", plugin.playerData.get(p.getUniqueId().toString()).getID() + "", "inParty", "true", p);
				}
				
				plugin.toggledParty.put(p.getName(), true);
				sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString(
				        "partyTrue")));
				return true;
			}
			plugin.channels.forEach(channel -> {
				if (args[0].toString().equalsIgnoreCase(channel)) {
					if (args.length >= 2 && plugin.enableArgsAsMessage) {
						String previousChannel = plugin.currentChannel.get(sender.getName());
						
						StringBuilder msg = new StringBuilder();
						for (int i = 1; i != args.length; i++) {
							msg.append(args[i] + " ");
						}
						if (args[0].equalsIgnoreCase(plugin.globalChatChannel)) {
							if (sender.hasPermission(plugin.getConfig().getString("channels.name.defaultGlobalPermission"))) {
								
								Player pa = (Player) sender;
								if (plugin.enableGlobalChat) {
									String displayMessage = plugin.getConfig().getString("channels.name.defaultGlobalMessageFormat").replace("{player}", pa.getPlayer().getDisplayName()).replace("{message}", msg.toString());
									for (Player p : Bukkit.getOnlinePlayers()) {
										if (plugin.hasPlaceholder) {
											p.sendMessage(ChatColor.translateAlternateColorCodes('&', PlaceholderAPI
											        .setPlaceholders(pa.getPlayer(), displayMessage)));
										} else {
											p.sendMessage(ChatColor.translateAlternateColorCodes('&', displayMessage));
										}
									}
									Bukkit.getLogger().info(ChatColor.translateAlternateColorCodes('&',
									        displayMessage));
									return;
								}
								plugin.previousMessage.put(sender.getName(), 0);
								plugin.currentChannel.put(sender.getName(), channel);
								String str = plugin.chatChannel.format("", (Player) sender, msg.toString(), true, true);
								for (Player p : Bukkit.getOnlinePlayers()) {
									p.sendMessage(str);
								}
								plugin.derp.put(sender.getName(), true);
								plugin.currentChannel.put(sender.getName(), previousChannel);
								
							} else {
								sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
								        plugin.getConfig().getString("noPerm")));
								plugin.derp.put(sender.getName(), true);
							}
							return;
						}
						if (!sender.hasPermission(plugin.getConfig().getString("channels.name." + channel
						        + ".permission"))) {
							
							sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString(
							        "noPerm")));
							plugin.derp.put(sender.getName(), true);
							return;
						}
						plugin.currentChannel.put(sender.getName(), channel);
						plugin.chatChannel.messageChannelSender((Player) sender, msg.toString(), plugin.getConfig()
						        .getString("channels.name." + channel + ".permission"), false, true);
						plugin.derp.put(sender.getName(), true);
						
						Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, new Runnable() {
							
							@Override
							public void run() {
								plugin.currentChannel.put(sender.getName(), previousChannel);
							}
							
						}, 2);
						
						return;
					}
					
					if (channel.equalsIgnoreCase(plugin.globalChatChannel)) {
						if (sender.hasPermission(plugin.getConfig().getString("channels.name.defaultGlobalPermission"))) {
							plugin.currentChannel.put(sender.getName(), channel);
							Player p = (Player) sender;
							
							if (!plugin.useMySQLDatabase) {
								plugin.dataYaml.set(p.getUniqueId().toString() + ".channel", channel);
								plugin.dataYaml.set(p.getUniqueId().toString() + ".inParty", false);
								
								try {
									plugin.dataYaml.save(plugin.dataFile);
								} catch (Exception e) {
									e.printStackTrace();
								}
								
							} else {
								plugin.mySQLApi.api.updateTableInformation("playerinformation", plugin.playerData.get(p.getUniqueId().toString()).getID() + "", "channel", channel, p);
								
								plugin.mySQLApi.api.updateTableInformation("playerinformation", plugin.playerData.get(p.getUniqueId().toString()).getID() + "", "inParty", "false", p);
								
							}
							
							plugin.toggledParty.put(sender.getName(), false);
							sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig()
							        .getString("switchedChannel").replace("{channel-name}", channel)));
							plugin.derp.put(sender.getName(), true);
						} else {
							sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
							        plugin.getConfig().getString("noPerm")));
							plugin.derp.put(sender.getName(), true);
						}
						return;
					}
					
					if (!sender.hasPermission(plugin.getConfig().getString("channels.name." + channel + ".permission"))) {
						sender.sendMessage(
						        ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("noPerm")));
						plugin.derp.put(sender.getName(), true);
						return;
					}
					plugin.currentChannel.put(sender.getName(), channel);
					Player p = (Player) sender;
					if (!plugin.useMySQLDatabase) {
						plugin.dataYaml.set(p.getUniqueId().toString() + ".channel", channel);
						plugin.dataYaml.set(p.getUniqueId().toString() + ".inParty", false);
						
						
						try {
							plugin.dataYaml.save(plugin.dataFile);
						} catch (IOException e) {
							;
						}
						
						
					} else {
						plugin.mySQLApi.api.updateTableInformation("playerinformation", plugin.playerData.get(p
						        .getUniqueId().toString()).getID() + "", "channel", channel, p);
						
						plugin.mySQLApi.api.updateTableInformation("playerinformation", plugin.playerData.get(p
						        .getUniqueId().toString()).getID() + "", "inParty", "false", p);
						
					}
					
					plugin.toggledParty.put(sender.getName(), false);
					sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
					        plugin.getConfig().getString("switchedChannel").replace("{channel-name}", channel)));
					plugin.derp.put(sender.getName(), true);
					return;
				}
				
			});
			if (plugin.derp.get(sender.getName()) == false) {
				sender.sendMessage(
				        ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("invalidChannel")));
			}
			plugin.derp.put(sender.getName(), false);
			return true;
		}
		return true;
	}
}
