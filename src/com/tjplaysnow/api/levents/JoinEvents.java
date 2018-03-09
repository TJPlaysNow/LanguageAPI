package com.tjplaysnow.api.levents;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;

public class JoinEvents implements Listener {
	
	private final List<Consumer<PlayerJoinEvent>> listeners = new ArrayList<>();
	
	public JoinEvents(Plugin plugin) {
		Bukkit.getPluginManager().registerEvents(this, plugin);
	}
	
	public Consumer<PlayerJoinEvent> onJoin(Consumer<PlayerJoinEvent> listener){
        listeners.add(listener);
        return listener;
    }
    
    public List<Consumer<PlayerJoinEvent>> getJoinListeners() {
        return listeners;
    }
    
    @EventHandler
    public void playerJoin(PlayerJoinEvent event) {
    	for (Consumer<PlayerJoinEvent> events : listeners) {
    		events.accept(event);
    	}
    }
}