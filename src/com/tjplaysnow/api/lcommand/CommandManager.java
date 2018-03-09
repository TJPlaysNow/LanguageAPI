package com.tjplaysnow.api.lcommand;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class CommandManager implements CommandExecutor{
	
	private String label;
	List<Command> commands;
	
	private Consumer<CommandSender> error;
	
	public CommandManager(String label) {
		this.label = label;
		commands = new ArrayList<Command>();
	}
	
	public void setError(Consumer<CommandSender> error) {
		this.error = error;
	}

	public void addCommand(Command command) {
		commands.add(command);
	}

	public void addCommands(List<Command> commands) {
		for (Command command : commands) {
			this.commands.add(command);
		}
	}
	
	public Command getCommand(int index) {
		return commands.get(index);
	}
	
	public List<Command> getCommands() {
		return commands;
	}
	
	public void cloneCommands(CommandManager commands) {
		this.commands.addAll(commands.commands);
	}
	
	@Override
	public boolean onCommand(CommandSender sender, org.bukkit.command.Command paramCommand, String label, String[] args) {
		if (label.equalsIgnoreCase(this.label)) {
			boolean returnB = false;
			for (Command command : commands) {
				if (args.length <= 0) {
					break;
				}
				if (args.length >= 1) {
					if (args[0].equalsIgnoreCase(command.getLabel())) {
						List<String> argss = new ArrayList<String>();
						for (String arg : args) {
							argss.add(arg);
						}
						argss.remove(0);
						returnB = true;
						if (command.getPermissions() == "") {
							command.run(sender, argss);
							break;
						} else if (sender.hasPermission(command.getPermissions())) {
							command.run(sender, argss);
							break;
						} else {
							sender.sendMessage(command.getPermissions());
						}
					}
				}
			}
			if (!returnB) {
				error.accept(sender);
			}
			return returnB;
		}
		return false;
	}
}