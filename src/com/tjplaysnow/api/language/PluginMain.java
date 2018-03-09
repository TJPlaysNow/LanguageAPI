package com.tjplaysnow.api.language;

import org.bukkit.plugin.java.JavaPlugin;

import com.tjplaysnow.api.levents.JoinEvents;

public class PluginMain extends JavaPlugin {
	
	private Language language;
	private JoinEvents joinEvents;
	
	@Override
	public void onEnable() {
		joinEvents = new JoinEvents(this);
		language = new Language(this);
		joinEvents.onJoin(language.registerPlayerJoinEvent());
		getCommand("language").setExecutor(language.registerCommandManager());
		Interface.setLanguage(language);
	}
	
	@Override
	public void onDisable() {
		language.save();
	}
}