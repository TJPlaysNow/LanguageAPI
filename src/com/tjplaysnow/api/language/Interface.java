package com.tjplaysnow.api.language;

import org.bukkit.plugin.Plugin;

public class Interface {
	
	private static Language lang;
	
	/**
	 * Setting up the language so you can access it.<br>
	 * @param lang
	 */
	protected static void setLanguage(Language lang) {
		Interface.lang = lang;
	}
	
	/**
	 * Get the language class for use.
	 * @param plugin Your plugin class.
	 * @return <b>Language</b>
	 */
	public static Language getLanguage(Plugin plugin) {
		if (plugin == null) {
			return null;
		} else {
			lang.setEditingPlugin(plugin);
			return lang;
		}
	}
}