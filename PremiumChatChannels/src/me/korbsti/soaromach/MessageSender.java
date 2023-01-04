package me.korbsti.soaromach;

import me.clip.placeholderapi.PlaceholderAPI;

import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import com.gmail.nossr50.api.PartyAPI;

import github.scarsz.discordsrv.api.events.GameChatMessagePostProcessEvent;
import github.scarsz.discordsrv.api.events.GameChatMessagePreProcessEvent;
import github.scarsz.discordsrv.dependencies.jda.api.entities.GuildChannel;
import github.scarsz.discordsrv.dependencies.jda.api.entities.TextChannel;
import github.scarsz.discordsrv.util.WebhookUtil;
import net.md_5.bungee.api.ChatColor;

public class MessageSender {
	
	PremiumChatChannels plugin;
	
	public MessageSender(PremiumChatChannels instance) {
		this.plugin = instance;
	}
	
	private final String creditForHexSnippet = "Elementeral";
	
	private static final char COLOR_CHAR = '\u00A7';
	
	public String translateHexColorCodes(String message) {
		final Pattern hexPattern = Pattern.compile("&" + "#" + "([A-Fa-f0-9]{6})");
		Matcher matcher = hexPattern.matcher(message);
		StringBuffer buffer = new StringBuffer(message.length() + 4 * 8);
		while (matcher.find()) {
			String group = matcher.group(1);
			matcher.appendReplacement(buffer, COLOR_CHAR + "x"
			        + COLOR_CHAR + group.charAt(0) + COLOR_CHAR + group.charAt(1)
			        + COLOR_CHAR + group.charAt(2) + COLOR_CHAR + group.charAt(3)
			        + COLOR_CHAR + group.charAt(4) + COLOR_CHAR + group.charAt(5));
		}
		
		return ChatColor.translateAlternateColorCodes('&', matcher.appendTail(buffer).toString());
	}
	
	public String formatToDiscord(String channelPrefix, Player p, String message, boolean isGlobal, boolean fromCommand) {
		
		if (channelPrefix == null) {
			channelPrefix = plugin.getConfig()
			        .getString("channels.name." + plugin.currentChannel.get(p.getName()) + ".prefix");
		}
		String format = plugin.getConfig()
		        .getString("channels.name." + plugin.currentChannel.get(p.getName()) + ".toDiscordFormat");
		if (isGlobal) {
			format = plugin.getConfig().getString("channels.name.toGlobalChannelDiscord");
			channelPrefix = "";
		}
		
		
		String format1 = format.replace("{channel-prefix}", channelPrefix);
		String format2 = format1.replace("{player}", p.getDisplayName());
		String format3 = format2.replace("{message}", message);
		format3 = format3.replace("[i]", "[item]");
		for (String str : plugin.emojis) {
			if (plugin.chatEmojiData.getString("emojis." + str + ".check") != null) {
				if (format3.contains(plugin.chatEmojiData.getString("emojis." + str + ".check"))) {
					format3 = format3.replace(plugin.chatEmojiData.getString("emojis." + str + ".check"),
					        plugin.chatEmojiData.getString("emojis." + str + ".replacement"));
				}
			}
		}
		
		if (plugin.hasPlaceholder) {
			format3 = (PlaceholderAPI.setPlaceholders(p.getPlayer(), format3));
		}
		
		return translateHexColorCodes(format3);
	}
	
	@SuppressWarnings("deprecation")
	public String formatParty(Player p, String message) {
		String format = plugin.getConfig().getString("partyFormat");
		String format1 = format.replace("{party-name}", PartyAPI.getPartyName(p));
		String format2 = format1.replace("{player}", p.getDisplayName());
		String format3 = format2.replace("{message}", message);
		
		for (String str : plugin.emojis) {
			if (plugin.chatEmojiData.getString("emojis." + str + ".check") != null) {
				if (format3.contains(plugin.chatEmojiData.getString("emojis." + str + ".check"))) {
					format3 = format3.replace(plugin.chatEmojiData.getString("emojis." + str + ".check"), plugin.chatEmojiData.getString("emojis." + str + ".replacement"));
				}
			}
		}
	
		format3 = format3.replace("[i]", "[item]");

		if (plugin.hasPlaceholder) {
			format3 = (PlaceholderAPI.setPlaceholders(p.getPlayer(), format3));
		}
		try {
			
			if (!plugin.getConfig().getString("partyID").equals("")) {
				format3 = ChatColor.translateAlternateColorCodes('&', format3);
				// GuildChannel channel =
				// plugin.api.getMainGuild().getGuildChannelById(plugin.getConfig().getString("partyID"));
				
				//String channel = plugin.getConfig().getString("partyID");
				
				String str = plugin.getConfig().getString("partyDiscordFormat");
				String format11 = str.replace("{party-name}", PartyAPI.getPartyName(p));
				String format22 = format11.replace("{player}", p.getDisplayName());
				String format33 = format22.replace("{message}", message);
				
				String channelID = plugin.getConfig().getString("partyID");
				GuildChannel channel = plugin.api.getMainGuild().getGuildChannelById(channelID);
				TextChannel txtChannel = (TextChannel) channel;
				
				String sendDiscordMessage = (translateHexColorCodes(format33));
				
				sendDiscordMessage = translateHexColorCodes(sendDiscordMessage.replace("§4", "").replace("§c", "").replace("§6", "")
				        .replace("§e","").replace("§2", "").replace("§a", "").replace("§b", "").replace("§3", "")
				        .replace("§1","").replace("§9", "").replace("§d", "").replace("§5", "").replace("§f", "")
				        .replace("§7", "").replace("§8", "").replace("§0", "").replace("§x", "")
				        .replace("§m", "")
				        .replace("§l", "")
				        .replace("§q", "")
				        .replace("§r", "")
				        .replace("§n", "")
				        .replace("§x", "")
				        .replace("§0", ""));
				
				@SuppressWarnings("deprecation")
				String channelTest = plugin.api.getPlugin().getDestinationGameChannelNameForTextChannel(txtChannel);

				@SuppressWarnings("deprecation")
				GameChatMessagePreProcessEvent preEvent = plugin.api.api.callEvent(new GameChatMessagePreProcessEvent(channelID, sendDiscordMessage, p));
				
				GameChatMessagePostProcessEvent postEvent = plugin.api.api.callEvent(new GameChatMessagePostProcessEvent(channelID, sendDiscordMessage, p, preEvent.isCancelled()));
				plugin.api.getPlugin().getOptionalTextChannel(channelTest).sendMessage(preEvent.getMessage()).queue();
				
				
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		Bukkit.getLogger().info(format3);
		return translateHexColorCodes(format3);
	}
	
	public String format(String channelPrefix, Player p, String message, boolean isGlobal, boolean fromCommand) {
		if (channelPrefix == null) {
			channelPrefix = plugin.getConfig().getString("channels.name." + plugin.currentChannel.get(p.getName()) + ".prefix");
		}
		
		String format = plugin.getConfig().getString("channels.name." + plugin.currentChannel.get(p.getName()) + ".messageFormat");
		if (isGlobal) {
			format = plugin.getConfig().getString("channels.name.defaultGlobalMessageFormat");
			channelPrefix = "";
		}
		
		String format1 = format.replace("{channel-prefix}", channelPrefix);
		String format2 = format1.replace("{player}", p.getDisplayName());
		String format3 = format2.replace("{message}", message);
		for (String str : plugin.emojis) {
			if (plugin.chatEmojiData.getString("emojis." + str + ".check") != null) {
				if (format3.contains(plugin.chatEmojiData.getString("emojis." + str + ".check"))) {
					format3 = format3.replace(plugin.chatEmojiData.getString("emojis." + str + ".check"),
					        plugin.chatEmojiData.getString("emojis." + str + ".replacement"));
				}
			}
		}
		format3 = format3.replace("[i]", "[item]");

		if (plugin.hasPlaceholder) {
			format3 = (PlaceholderAPI.setPlaceholders(p.getPlayer(), format3));
		}
		try {

			if (!isGlobal) {
				if (plugin.previousMessage.get(p.getName()) == 0) {

					if (!plugin.getConfig().getString("channels.name." + plugin.currentChannel.get(p.getName()) + ".channelID").equals("")) {
												
						
						format3 = ChatColor.translateAlternateColorCodes('&', format3);
						
						String channelID = plugin.getConfig().getString("channels.name."+ plugin.currentChannel.get(p.getName()) + ".channelID");
						GuildChannel channel = plugin.api.getMainGuild().getGuildChannelById(channelID);
						TextChannel txtChannel = (TextChannel) channel;
						String sendDiscordMessage = (formatToDiscord(channelPrefix, p, message, isGlobal, fromCommand));
						
						sendDiscordMessage = translateHexColorCodes(sendDiscordMessage.replace("§4", "").replace("§c", "").replace("§6", "")
						        .replace("§e","").replace("§2", "").replace("§a", "").replace("§b", "").replace("§3", "")
						        .replace("§1","").replace("§9", "").replace("§d", "").replace("§5", "").replace("§f", "")
						        .replace("§7", "").replace("§8", "").replace("§0", "").replace("§x", "")
						        .replace("§m", "")
						        .replace("§l", "")
						        .replace("§q", "")
						        .replace("§r", "")
						        .replace("§n", "")
						        .replace("§x", "")
						        .replace("§0", ""));
						String channelTest = plugin.api.getPlugin().getDestinationGameChannelNameForTextChannel(txtChannel);

						@SuppressWarnings("deprecation")
						GameChatMessagePreProcessEvent preEvent = plugin.api.api.callEvent(new GameChatMessagePreProcessEvent(channelID, sendDiscordMessage, p));
						
						GameChatMessagePostProcessEvent postEvent = plugin.api.api.callEvent(new GameChatMessagePostProcessEvent(channelID, sendDiscordMessage, p, preEvent.isCancelled()));
						plugin.api.getPlugin().getOptionalTextChannel(channelTest).sendMessage(preEvent.getMessage()).queue();
						
						
					}
				}
			}
			
			if (fromCommand && isGlobal) {
				if (plugin.previousMessage.get(p.getName()) == 0) {
					if (!plugin.getConfig().getString("channels.name.globalChannelID").equals("")) {
						String newLine = format;
						if (isGlobal && fromCommand) {
							newLine = plugin.getConfig().getString("channels.name.toGlobalChannelDiscord");
							channelPrefix = "";
						}
						
						newLine = newLine.replace("{channel-prefix}", channelPrefix).replace("{player}", p.getDisplayName()).replace("{message}", message).replace("[i]", "[item]");
																	
						if (plugin.hasPlaceholder) {
							newLine = (PlaceholderAPI.setPlaceholders(p.getPlayer(), newLine));
						}
						
						String channelID = plugin.getConfig().getString("channels.name.globalChannelID");
						GuildChannel channel = plugin.api.getMainGuild().getGuildChannelById(channelID);
						TextChannel txtChannel = (TextChannel) channel;
						
						String sendDiscordMessage = (formatToDiscord(channelPrefix, p, message, isGlobal, fromCommand));
						
						sendDiscordMessage = translateHexColorCodes(sendDiscordMessage.replace("§4", "").replace("§c", "").replace("§6", "")
						        .replace("§e","").replace("§2", "").replace("§a", "").replace("§b", "").replace("§3", "")
						        .replace("§1","").replace("§9", "").replace("§d", "").replace("§5", "").replace("§f", "")
						        .replace("§7", "").replace("§8", "").replace("§0", "").replace("§x", "")
						        .replace("§m", "")
						        .replace("§l", "")
						        .replace("§q", "")
						        .replace("§r", "")
						        .replace("§n", "")
						        .replace("§x", "")
						        .replace("§0", ""));
						
						String channelTest = plugin.api.getPlugin().getDestinationGameChannelNameForTextChannel(txtChannel);

						@SuppressWarnings("deprecation")
						GameChatMessagePreProcessEvent preEvent = plugin.api.api.callEvent(new GameChatMessagePreProcessEvent(channelID, sendDiscordMessage, p));
						
						GameChatMessagePostProcessEvent postEvent = plugin.api.api.callEvent(new GameChatMessagePostProcessEvent(channelID, sendDiscordMessage, p, preEvent.isCancelled()));
						plugin.api.getPlugin().getOptionalTextChannel(channelTest).sendMessage(preEvent.getMessage()).queue();
						
						/*
						 * GuildChannel channel =
						 * plugin.api.getMainGuild().getGuildChannelById(plugin.getConfig()
						 * .getString("channels.name.globalChannelID")); TextChannel txtChannel =
						 * (TextChannel) channel; txtChannel.sendMessage(format3.replace("§4",
						 * "").replace("§c", "").replace("§6", "").replace( "§e", "").replace("§2",
						 * "").replace("§a", "").replace("§b", "").replace("§3", "").replace( "§1",
						 * "").replace("§9", "").replace("§d", "").replace("§5", "").replace("§f", "")
						 * .replace( "§7", "").replace("§8", "").replace("§0", "").replace("§x", "")
						 * .replace("§m", "") .replace("§l", "") .replace("§q", "") .replace("§r", "")
						 * .replace("§n", "") .replace("§x", "") .replace("§0", "")).queue();
						 */
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		plugin.previousMessage.put(p.getName(), 1);
		return translateHexColorCodes(format3);
	}
	
	public void messageChannelSender(Player player, String message, String permission, boolean isGlobal, boolean fromCommand) {
		
		String prefixChannel = null;
		for (Player p : Bukkit.getOnlinePlayers()) {
			String name = p.getName();
			if (plugin.currentChannel.get(name) == null) {
				plugin.currentChannel.put(name, plugin.getConfig().getString("channels.name.defaultGlobal"));
			}
			if (plugin.spyChannels.get(p.getName()).contains(plugin.currentChannel.get(player.getName()))) {
				
				if (permission == plugin.getConfig().getString("channels.name." + plugin.currentChannel.get(player
				        .getName()) + ".permission")) {
					prefixChannel = plugin.getConfig().getString("channels.name." + plugin.currentChannel.get(player
					        .getName()) + ".prefix");
				}
				p.sendMessage(format(prefixChannel, player, message, isGlobal, fromCommand));
				continue;
			}
			
			if (p.hasPermission(permission)) {
				if (!plugin.getConfig().getBoolean("channels.name." + plugin.currentChannel.get(player.getName())
				        + ".enableDistanceMessage")) {
					
					if (permission == plugin.getConfig().getString("channels.name." + plugin.currentChannel.get(player
					        .getName()) + ".permission")) {
						prefixChannel = plugin.getConfig().getString("channels.name." + plugin.currentChannel.get(player
						        .getName()) + ".prefix");
					}
					if (plugin.getConfig().getBoolean("channels.name." + plugin.currentChannel.get(player.getName())
					        + ".sendRegardlessOfCurrentChannel") == true) {
						p.sendMessage(format(prefixChannel, player, message,
						        isGlobal, fromCommand));
					} else {
						if (plugin.currentChannel.get(p.getName()) == plugin.currentChannel.get(player.getName())) {
							p.sendMessage(format(prefixChannel, player, message, isGlobal, fromCommand));
						}
					}
					
				} else {
					Double dis = plugin.getConfig().getDouble("channels.name." + plugin.currentChannel.get(player.getName()) + ".distanceMessage");
					Bukkit.getScheduler().runTask(plugin, new Runnable() {
						
						@Override
						public void run() {
							String prefix = null;
							for (Entity entity : player.getNearbyEntities(dis, dis, dis)) {
								if (entity instanceof Player) {
									Player playerd = (Player) entity;
									if (plugin.spyChannels.get(playerd.getName()) != null) {
										if (plugin.spyChannels.get(playerd.getName()).contains(plugin.currentChannel
										        .get(player.getName()))) {
											continue;
										}
									}
									
									if (permission == plugin.getConfig().getString("channels.name."
									        + plugin.currentChannel.get(player.getName()) + ".permission")) {
										prefix = plugin.getConfig().getString("channels.name." + plugin.currentChannel
										        .get(player.getName()) + ".prefix");
									}
									if (plugin.getConfig().getBoolean("channels.name." + plugin.currentChannel.get(
									        player.getName()) + ".sendRegardlessOfCurrentChannel") == true) {
										playerd.sendMessage(format(prefix,
										        player, message, isGlobal, fromCommand));
									} else {
										
										if (plugin.currentChannel.get(playerd.getName()) == plugin.currentChannel
										        .get(player.getName())) {
											playerd.sendMessage(format(
											        plugin.getConfig().getString("channels.name."
											                + plugin.currentChannel.get(player.getName()) + ".prefix"),
											        player, message, isGlobal, fromCommand));
										}
									}
									
								}
							}
							
							if (permission == plugin.getConfig().getString("channels.name." + plugin.currentChannel.get(
							        player.getName()) + ".permission")) {
								prefix = plugin.getConfig().getString("channels.name." + plugin.currentChannel.get(
								        player.getName()) + ".prefix");
							}
							if (plugin.getConfig().getBoolean("channels.name." + plugin.currentChannel.get(player
							        .getName()) + ".sendRegardlessOfCurrentChannel") == true) {
								player.sendMessage(format(prefix, player,
								        message, isGlobal, fromCommand));
							} else {
								if (plugin.currentChannel.get(player.getName()) == plugin.currentChannel.get(player
								        .getName())) {
									player.sendMessage(format(plugin
									        .getConfig().getString("channels.name." + plugin.currentChannel.get(player
									                .getName()) + ".prefix"), player, message, isGlobal, fromCommand));
								}
							}
							Bukkit.getLogger().info(format(plugin
							        .getConfig().getString("channels.name." + plugin.currentChannel.get(player
							                .getName()) + ".prefix"), player, message, isGlobal, fromCommand));
						}
						
					});
					plugin.previousMessage.put(player.getName(), 0);
					return;
				}
			}
			
		}
		plugin.previousMessage.put(player.getName(), 0);
		Bukkit.getLogger().info(format(prefixChannel, player, message,
		        isGlobal, false));
	}
}
