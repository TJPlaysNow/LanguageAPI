package com.tjplaysnow.api.language;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;

import com.tjplaysnow.api.config.Config;
import com.tjplaysnow.api.lcommand.Command;
import com.tjplaysnow.api.lcommand.CommandManager;

public class Language {
	
	private Config lang;
	private HashMap<String, Config> languages;
	
	private boolean langCreated = true;
	private boolean cCreated;
	
	private HashMap<UUID, String> playerLang;
	
	private static Plugin plugin;
	private Plugin editingPlugin;
	
	/**
	 * Create a new <b>Language</b> handler.
	 * @param plugin The plugin creating the <b>Language</b> handler.
	 */
	public Language(Plugin plugin) {
		Language.plugin = plugin;
		editingPlugin = plugin;
		this.lang = new Config("plugins/" + plugin.getName(), "Lang-Config.yml", () -> {
			langCreated = false;
		}, plugin);
		if (!langCreated) {
			List<String> languages = new ArrayList<String>();
			languages.add("English");
			lang.getConfig().set("Languages", languages);
			lang.saveConfig();
		}
		languages = new HashMap<String, Config>();
		for (String language : lang.getConfig().getStringList("Languages")) {
			if (!language.endsWith(".yml")) {
				language += ".yml";
			}
			cCreated = true;
			Config c = new Config("plugins/" + plugin.getName() + "/Lang", language, () -> {
				cCreated = false;
			}, plugin);
			if (!cCreated) {
				List<String> regions = new ArrayList<String>();
				regions.add("United States");
				regions.add("Canada");
				regions.add("United Kingdom");
				c.getConfig().set("Regions", regions);
				c.saveConfig();
			}
			languages.put(language.replaceAll(".yml", ""), c);
		}
		playerLang = new HashMap<UUID, String>();
		if (lang.getConfig().getConfigurationSection("Player") != null) {
			for (String player : lang.getConfig().getConfigurationSection("Player").getKeys(false)) {
				String lang = this.lang.getConfig().getString("Player." + player + ".Lang");
				playerLang.put(UUID.fromString(player), lang);
			}
		}
	}
	
	/**
	 * Save the default language config.
	 */
	public void save() {
		for (UUID playerUUID : playerLang.keySet()) {
			lang.getConfig().set("Player." + playerUUID.toString() + ".Lang", playerLang.get(playerUUID));
		}
		lang.saveConfig();
	}
	
	/**
	 * Register the <b>JoinEvent</b> on creation.
	 * @return <b>Consumer<PlayerJoinEvent></b>
	 */
	protected Consumer<PlayerJoinEvent> registerPlayerJoinEvent() {
		return ((event) -> {
			if (!playerLang.containsKey(event.getPlayer().getUniqueId())) {
				Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
					try {
						String country = getCountry(event.getPlayer().getAddress());
						String langu = "";
						Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
							ipCallback(event.getPlayer(), country, langu);
						}, 1l);
					} catch (Exception e) {
						Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
							ipCallback(event.getPlayer(), "", "");
						}, 1l);
					}
				});
			}
		});
	}
	
	/**
	 * The callback to when we test where a user is from using there IP Address.
	 * @param player The Player.
	 * @param country The country the user is in.
	 * @param langu The language they would speak.
	 * @return <b>Null</b>
	 */
	private void ipCallback(Player player, String country, String langu) {
		if (country == null) {
			country = "United States";
		}
		for (Config conf : languages.values()) {
			for (String countries : conf.getConfig().getStringList("Regions")) {
				if (countries.equals(country)) {
					langu = conf.getConfig().getName();
				}
			}
		}
		if (langu == "" || langu == null) {
			langu = "English";
		}
		playerLang.put(player.getUniqueId(), langu);
		sendMessage(player, "DEFAULT.LANG.SET.1", "&aSet you're language to {LANGUAGE}, based on your region.", new String[] {"{LANGUAGE}", langu + ""}, new String[] {"{REGION}", country + ""});
		sendMessage(player, "DEFAULT.LANG.SET.2", "&7If that is incorrect we don't have a language for the region of {REGION} made.", new String[] {"{LANGUAGE}", langu}, new String[] {"{REGION}", country});
		sendMessage(player, "DEFAULT.LANG.SET.3", "&7Or something errored.", new String[] {"{LANGUAGE}", langu}, new String[] {"{REGION}", country});
		sendMessage(player, "DEFAULT.LANG.SET.4", "&aTo change your language run &6/language help&a for more information.", new String[] {"{LANGUAGE}", langu}, new String[] {"{REGION}", country});
	}
	
	/**
	 * Send a formatted language message to a player.
	 * @param player Player recieving the message.
	 * @param message_place Where the message is located in the Lang file(s).
	 */
	public void sendMessage(Player player, String message_place) {
		player.sendMessage(ChatColor.translateAlternateColorCodes('&', languages.get(playerLang.get(player.getUniqueId())).getConfig().getString(editingPlugin.getName().toUpperCase() + "." + message_place)));
	}
	
	/**
	 * Send a formatted language message to a player.<br>
	 * And if the message isn't in the language file
	 * place it in the language file.
	 * @param player Player recieving the message.
	 * @param message_place Where the message is located in the Lang file(s).
	 * @param defaultM The message to put in place of a null mesage.
	 */
	public void sendMessage(Player player, String message_place, String defaultM) {
		String ret = languages.get(playerLang.get(player.getUniqueId())).getConfig().getString(editingPlugin.getName().toUpperCase() + "." + message_place, "nil%null");
		if (ret.equals("nil%null")) {
			languages.get(playerLang.get(player.getUniqueId())).getConfig().set(editingPlugin.getName().toUpperCase() + "." + message_place, defaultM);
			languages.get(playerLang.get(player.getUniqueId())).saveConfig();
			ret = languages.get(playerLang.get(player.getUniqueId())).getConfig().getString(editingPlugin.getName().toUpperCase() + "." + message_place);
		}
		player.sendMessage(ChatColor.translateAlternateColorCodes('&', ret));
	}
	
	/**
	 * Send a formatted language message to a player.<br>
	 * And if the message isn't in the language file
	 * place it in the language file.
	 * @param player Player recieving the message.
	 * @param message_place Where the message is located in the Lang file(s).
	 * @param defaultM The message to put in place of a null mesage.
	 * @param replacer A list of arrays of <b>TWO</b> Strings, the first being
	 * a string to replace, and the second being the replacer.
	 */
	public void sendMessage(Player player, String message_place, String defaultM, String[]... replacer) {
		String pLang = playerLang.get(player.getUniqueId());
		Config con = null;
		for (String langName : languages.keySet()) {
			if (langName.equals(pLang)) {
				con = languages.get(langName);
			}
		}
		String ret = con.getConfig().getString(editingPlugin.getName().toUpperCase() + "." + message_place);
		if (ret == null) {
			languages.get(playerLang.get(player.getUniqueId())).getConfig().set(editingPlugin.getName().toUpperCase() + "." + message_place, defaultM);
			languages.get(playerLang.get(player.getUniqueId())).saveConfig();
			ret = languages.get(playerLang.get(player.getUniqueId())).getConfig().getString(editingPlugin.getName().toUpperCase() + "." + message_place);
		}
		for (String[] reps : replacer) {
			if (reps[1] == null) {
				ret = ret.replace(reps[0], "");
			} else {
				ret = ret.replace(reps[0], reps[1]);
			}
		}
		player.sendMessage(ChatColor.translateAlternateColorCodes('&', ret));
	}
	
	/**
	 * Get a message from the players set language.
	 * @param player Player recieving the message.
	 * @param message_place Where the message is located in the Lang file(s).
	 * @return <b>String</b>
	 */
	public String getMessage(Player player, String message_place) {
		return ChatColor.translateAlternateColorCodes('&', languages.get(playerLang.get(player.getUniqueId())).getConfig().getString(editingPlugin.getName().toUpperCase() + "." + message_place));
	}
	
	/**
	 * Get a message from the players set language.<br>
	 * And if the message isn't in the language file
	 * place it in the language file.
	 * @param player Player recieving the message.
	 * @param message_place Where the message is located in the Lang file(s).
	 * @param defaultM The message to put in place of a null mesage.
	 * @return <b>String</b>
	 */
	public String getMessage(Player player, String message_place, String defaultM) {
		String ret = languages.get(playerLang.get(player.getUniqueId())).getConfig().getString(editingPlugin.getName().toUpperCase() + "." + message_place, "nil%null");
		if (ret.equals("nil%null")) {
			languages.get(playerLang.get(player.getUniqueId())).getConfig().set(editingPlugin.getName().toUpperCase() + "." + message_place, defaultM);
			languages.get(playerLang.get(player.getUniqueId())).saveConfig();
			ret = languages.get(playerLang.get(player.getUniqueId())).getConfig().getString(editingPlugin.getName().toUpperCase() + "." + message_place);
		}
		return ChatColor.translateAlternateColorCodes('&', ret);
	}
	
	/**
	 * Send a formatted language message to a player.<br>
	 * And if the message isn't in the language file
	 * place it in the language file.
	 * @param player Player recieving the message.
	 * @param message_place Where the message is located in the Lang file(s).
	 * @param defaultM The message to put in place of a null mesage.
	 * @param replacer A list of arrays of <b>TWO</b> Strings, the first being
	 * a string to replace, and the second being the replacer.
	 * @return <b>String</b>
	 */
	public String getMessage(Player player, String message_place, String defaultM, String[]... replacer) {
		String ret = languages.get(playerLang.get(player.getUniqueId())).getConfig().getString(editingPlugin.getName().toUpperCase() + "." + message_place, "nil%null");
		if (ret.equals("nil%null")) {
			languages.get(playerLang.get(player.getUniqueId())).getConfig().set(editingPlugin.getName().toUpperCase() + "." + message_place, defaultM);
			languages.get(playerLang.get(player.getUniqueId())).saveConfig();
			ret = languages.get(playerLang.get(player.getUniqueId())).getConfig().getString(editingPlugin.getName().toUpperCase() + "." + message_place);
		}
		for (String[] reps : replacer) {
			ret = ret.replace(reps[0], reps[1]);
		}
		return ChatColor.translateAlternateColorCodes('&', ret);
	}
	
	/**
	 * Get a message from the players set language.
	 * @param player Offline player recieving the message.
	 * @param message_place Where the message is located in the Lang file(s).
	 * @return <b>String</b>
	 */
	public String getMessage(OfflinePlayer player, String message_place) {
		return ChatColor.translateAlternateColorCodes('&', languages.get(playerLang.get(player.getUniqueId())).getConfig().getString(editingPlugin.getName().toUpperCase() + "." + message_place));
	}
	
	/**
	 * Get a message from the players set language.<br>
	 * And if the message isn't in the language file
	 * place it in the language file.
	 * @param player Offline player recieving the message.
	 * @param message_place Where the message is located in the Lang file(s).
	 * @param defaultM The message to put in place of a null mesage.
	 * @return <b>String</b>
	 */
	public String getMessage(OfflinePlayer player, String message_place, String defaultM) {
		String ret = languages.get(playerLang.get(player.getUniqueId())).getConfig().getString(editingPlugin.getName().toUpperCase() + "." + message_place, "nil%null");
		if (ret.equals("nil%null")) {
			languages.get(playerLang.get(player.getUniqueId())).getConfig().set(editingPlugin.getName().toUpperCase() + "." + message_place, defaultM);
			languages.get(playerLang.get(player.getUniqueId())).saveConfig();
			ret = languages.get(playerLang.get(player.getUniqueId())).getConfig().getString(editingPlugin.getName().toUpperCase() + "." + message_place);
		}
		return ChatColor.translateAlternateColorCodes('&', ret);
	}
	
	/**
	 * Send a formatted language message to a player.<br>
	 * And if the message isn't in the language file
	 * place it in the language file.
	 * @param player Offline player recieving the message.
	 * @param message_place Where the message is located in the Lang file(s).
	 * @param defaultM The message to put in place of a null mesage.
	 * @param replacer A list of arrays of <b>TWO</b> Strings, the first being
	 * a string to replace, and the second being the replacer.
	 * @return <b>String</b>
	 */
	public String getMessage(OfflinePlayer player, String message_place, String defaultM, String[]... replacer) {
		String ret = languages.get(playerLang.get(player.getUniqueId())).getConfig().getString(editingPlugin.getName().toUpperCase() + "." + message_place, "nil%null");
		if (ret.equals("nil%null")) {
			languages.get(playerLang.get(player.getUniqueId())).getConfig().set(editingPlugin.getName().toUpperCase() + "." + message_place, defaultM);
			languages.get(playerLang.get(player.getUniqueId())).saveConfig();
			ret = languages.get(playerLang.get(player.getUniqueId())).getConfig().getString(editingPlugin.getName().toUpperCase() + "." + message_place);
		}
		for (String[] reps : replacer) {
			ret = ret.replace(reps[0], reps[1]);
		}
		return ChatColor.translateAlternateColorCodes('&', ret);
	}
	
	/**
	 * Used interally to recieve a Player's region.
	 * @param ip
	 * @return <b>String</b>
	 * @throws Exception
	 */
	private static String getCountry(InetSocketAddress ip) throws Exception {
		URL url = new URL("http://ip-api.com/json/" + ip.getHostName());
		BufferedReader stream = new BufferedReader(new InputStreamReader(url.openStream()));
		StringBuilder entirePage = new StringBuilder();
		String inputLine;
		while ((inputLine = stream.readLine()) != null)
			entirePage.append(inputLine);
		stream.close();
		if(!(entirePage.toString().contains("\"country\":\""))) {
			return null;
		}
		return entirePage.toString().split("\"country\":\"")[1].split("\",")[0];
	}
	
	/**
	 * Set a message in the language files.
	 * @param language The language to use.
	 * @param message_place The area to save the message.
	 * @param message The message.
	 */
	public void setMessage(String language, String message_place, String message) {
		languages.get(language).getConfig().set(editingPlugin.getName().toUpperCase() + "." + message_place, message);
		languages.get(language).saveConfig();
	}
	
	/**
	 * Get a message from the language file.
	 * @param language The language to use.
	 * @param message_place The area to get the message from.
	 * @return <b>String</b>
	 */
	public String getMessage(String language, String message_place) {
		return ChatColor.translateAlternateColorCodes('&', languages.get(language).getConfig().getString(editingPlugin.getName().toUpperCase() + "." + message_place));
	}
	
	/**
	 * Get a message from the language file.
	 * @param language The language to use.
	 * @param message_place The area to get the message from.
	 * @param defaultM The message to put in place of a null mesage.
	 * @return <b>String</b>
	 */
	public String getMessage(String language, String message_place, String defaultM) {
		String ret = languages.get(language).getConfig().getString(editingPlugin.getName().toUpperCase() + "." + message_place, "nil%null");
		if (ret.equals("nil%null")) {
			setMessage(language, editingPlugin.getName().toUpperCase() + "." + message_place, defaultM);
			ret = languages.get(language).getConfig().getString(editingPlugin.getName().toUpperCase() + "." + message_place);
		}
		return ChatColor.translateAlternateColorCodes('&', ret);
	}
	
	/**
	 * Get a message from the language file.
	 * @param language The language to use.
	 * @param message_place The area to get the message from.
	 * @param defaultM The message to put in place of a null mesage.
	 * @param replacer A list of arrays of <b>TWO</b> Strings, the first being
	 * a string to replace, and the second being the replacer.
	 * @return <b>String</b>
	 */
	public String getMessage(String language, String message_place, String defaultM, String[]... replacer) {
		String ret = languages.get(language).getConfig().getString(editingPlugin.getName().toUpperCase() + "." + message_place, "nil%null");
		if (ret.equals("nil%null")) {
			setMessage(language, editingPlugin.getName().toUpperCase() + "." + message_place, defaultM);
			ret = languages.get(language).getConfig().getString(editingPlugin.getName().toUpperCase() + "." + message_place);
		}
		for (String[] reps : replacer) {
			ret = ret.replace(reps[0], reps[1]);
		}
		return ChatColor.translateAlternateColorCodes('&', ret);
	}
	
	/**
	 * Get a message list from the players set language.
	 * @param player Player recieving the message.
	 * @param message_place Where the message list is located in the Lang file(s).
	 * @return <b>List<String></b>
	 */
	public List<String> getMessageList(Player player, String message_place) {
		List<String> messageList = new ArrayList<String>();
		for (String message : languages.get(playerLang.get(player.getUniqueId())).getConfig().getStringList(editingPlugin.getName().toUpperCase() + "." + message_place)) {
			messageList.add(ChatColor.translateAlternateColorCodes('&', message));
		}
		return messageList;
	}
	
	/**
	 * Get a message list from the players set language.<br>
	 * And if the message isn't in the language file
	 * place it in the language file.
	 * @param player Player recieving the message.
	 * @param message_place Where the message list is located in the Lang file(s).
	 * @param defaultM The message list to put in place of a null mesage.
	 * @return <b>List<String></b>
	 */
	public List<String> getMessageList(Player player, String message_place, List<String> defaultM) {
		List<String> ret = languages.get(playerLang.get(player.getUniqueId())).getConfig().getStringList(message_place);
		if (ret.isEmpty() || ret == null || ret == new ArrayList<String>()) {
			languages.get(playerLang.get(player.getUniqueId())).getConfig().set(editingPlugin.getName().toUpperCase() + "." + message_place, defaultM);
			languages.get(playerLang.get(player.getUniqueId())).saveConfig();
			ret = languages.get(playerLang.get(player.getUniqueId())).getConfig().getStringList(message_place);
		}
		return ret;
	}

	/**
	 * Register the command manager for the Language manager.
	 * @return <b>CommandManager</b>
	 */
	public CommandExecutor registerCommandManager() {
		CommandManager commands = new CommandManager("language");
		commands.addCommand(new Command() {
			@Override
			public String getLabel() {
				return "help";
			}

			@Override
			public String getPermissions() {
				return "";
			}

			@Override
			public String getArgTypes(CommandSender sender) {
				return "";
			}

			@Override
			public String getDescription(CommandSender sender) {
				if (sender instanceof Player) {
					return getMessage((Player) sender, "COMMANDS.LANGUAGE.HELP.DESCRIPTION", "&aLanguage help!");
				} else {
					return getMessage("English", "COMMANDS.LANGUAGE.HELP.DESCRIPTION", "&aLanguage help!");
				}
			}

			@Override
			public String getNoPermsMessage(CommandSender sender) {
				if (sender instanceof Player) {
					return getMessage((Player) sender, "COMMANDS.LANGUAGE.HELP.NOPERMS", "&cUh oh, you don't have perms for this command!");
				} else {
					return getMessage("English", "COMMANDS.LANGUAGE.HELP.NOPERMS", "&cUh oh, you don't have perms for this command!");
				}
			}

			@Override
			public boolean run(CommandSender sender, List<String> args) {
				if (sender instanceof Player) {
					sendMessage((Player) sender, "COMMANDS.LANGUAGE.HELP.COMPLETE1", "&a - Language Help -");
					sendMessage((Player) sender, "COMMANDS.LANGUAGE.HELP.COMPLETE2", "&7");
					sendMessage((Player) sender, "COMMANDS.LANGUAGE.HELP.COMPLETE3", "&7To change your language type");
					sendMessage((Player) sender, "COMMANDS.LANGUAGE.HELP.COMPLETE4", "&7  - /language change {language}");
					sendMessage((Player) sender, "COMMANDS.LANGUAGE.HELP.COMPLETE5", "&7");
					sendMessage((Player) sender, "COMMANDS.LANGUAGE.HELP.COMPLETE6", "&7Languages to choose from:");
					for (String language : languages.keySet()) {
						sendMessage((Player) sender, "COMMANDS.LANGUAGE.HELP.COMPLETE7", "&7 - {LANGUAGE}", new String[] {"{LANGUAGE}", language});
					}
				} else {
					sender.sendMessage("§cYou're not a player you can't do language stuff!");
				}
				return true;
			}
		});
		commands.addCommand(new Command() {
			@Override
			public String getLabel() {
				return "change";
			}

			@Override
			public String getPermissions() {
				return "";
			}

			@Override
			public String getArgTypes(CommandSender sender) {
				return "{language}";
			}

			@Override
			public String getDescription(CommandSender sender) {
				if (sender instanceof Player) {
					return getMessage((Player) sender, "COMMANDS.LANGUAGE.CHANGE.DESCRIPTION", "&aLanguage help!");
				} else {
					return getMessage("English", "COMMANDS.LANGUAGE.CHANGE.DESCRIPTION", "&aLanguage help!");
				}
			}

			@Override
			public String getNoPermsMessage(CommandSender sender) {
				if (sender instanceof Player) {
					return getMessage((Player) sender, "COMMANDS.LANGUAGE.CHANGE.NOPERMS", "&cUh oh, you don't have perms for this command!");
				} else {
					return getMessage("English", "COMMANDS.LANGUAGE.CHANGE.NOPERMS", "&cUh oh, you don't have perms for this command!");
				}
			}

			@Override
			public boolean run(CommandSender sender, List<String> args) {
				if (sender instanceof Player) {
					if (args.size() == 1) {
						for (String language : languages.keySet()) {
							if (args.get(0).equalsIgnoreCase(language)) {
								playerLang.put(((Player) sender).getUniqueId(), language);
								sendMessage((Player) sender, "COMMANDS.LANGUAGE.CHANGE.COMPLETE", "&aYou have chosen, {LANGUAGE} as your default language.", new String[] {"{LANGUAGE}", language});
								return true;
							}
						}
						sendMessage((Player) sender, "COMMANDS.LANGUAGE.CHANGE.ERROR", "&cUh oh, seems that language wasn't found. Do &6/language help &7for more information.");
					}
				} else {
					sender.sendMessage("§cYou're not a player you can't do language stuff!");
				}
				return true;
			}
		});
		commands.setError((sender) -> {
			if (sender instanceof Player) {
				sendMessage((Player) sender, "COMMANDS.LANGUAGE.ERROR", "&cUh oh, please use &6/language help&c for further information.");
			} else {
				sender.sendMessage(getMessage("English", "COMMANDS.LANGUAGE.ERROR", "&cUh oh, please use &6/language help&c for further information."));
			}
		});
		return commands;
	}
	
	public void setEditingPlugin(Plugin editingPlugin) {
		this.editingPlugin = editingPlugin;
	}
}