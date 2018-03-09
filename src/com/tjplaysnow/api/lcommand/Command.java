package com.tjplaysnow.api.lcommand;

import java.util.List;

import org.bukkit.command.CommandSender;

public interface Command {
	public String getLabel();
	public String getPermissions();
	
	public String getArgTypes(CommandSender sender);
	public String getDescription(CommandSender sender);
	public String getNoPermsMessage(CommandSender sender);
	
	public boolean run(CommandSender sender, List<String> args);	
}