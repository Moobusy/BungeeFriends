package net.simplyrin.bungeefriends;

import java.util.UUID;

import lombok.Getter;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;
import net.simplyrin.bungeefriends.commands.FriendCommand;
import net.simplyrin.bungeefriends.listeners.EventListener;
import net.simplyrin.bungeefriends.utils.ConfigManager;
import net.simplyrin.bungeefriends.utils.FriendManager;
import net.simplyrin.bungeefriends.utils.MessageBuilder;
import net.simplyrin.bungeefriends.utils.PlayerManager;

/**
 * Created by SimplyRin on 2018/07/03.
 *
 * Copyright (C) 2018 SimplyRin
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
public class Main extends Plugin {

	// @Getter
	private static Main plugin;

	@Getter
	private ConfigManager configManager;
	@Getter
	private PlayerManager playerManager;
	@Getter
	private FriendManager friendManager;

	@Override
	public void onEnable() {
		plugin = this;

		plugin.configManager = new ConfigManager(plugin);
		plugin.playerManager = new PlayerManager(plugin);
		plugin.friendManager = new FriendManager(plugin);

		plugin.getProxy().getPluginManager().registerCommand(plugin, new FriendCommand(plugin));
		plugin.getProxy().getPluginManager().registerListener(plugin, new EventListener(plugin));
	}

	@Override
	public void onDisable() {
		plugin.configManager.saveAndReload();
		plugin.playerManager.saveAndReload();
	}

	public String getPrefix() {
		return plugin.getConfigManager().getConfig().getString("Plugin.Prefix");
	}

	@SuppressWarnings("deprecation")
	public void info(String args) {
		plugin.getProxy().getConsole().sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getPrefix() + args));
	}

	@SuppressWarnings("deprecation")
	public void info(ProxiedPlayer player, String args) {
		player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getPrefix() + args));
	}

	@SuppressWarnings("deprecation")
	public void info(UUID uuid, String args) {
		ProxiedPlayer player = this.getProxy().getPlayer(uuid);
		if(player != null) {
			player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getPrefix() + args));
		}
	}

	public void info(ProxiedPlayer player, TextComponent args) {
		player.sendMessage(MessageBuilder.get(plugin.getPrefix()), args);
	}

}
