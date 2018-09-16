package net.simplyrin.bungeefriends;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import lombok.Getter;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;
import net.simplyrin.bungeefriends.commands.FriendCommand;
import net.simplyrin.bungeefriends.commands.ReplyCommand;
import net.simplyrin.bungeefriends.commands.TellCommand;
import net.simplyrin.bungeefriends.listeners.EventListener;
import net.simplyrin.bungeefriends.utils.ConfigManager;
import net.simplyrin.bungeefriends.utils.FriendManager;
import net.simplyrin.bungeefriends.utils.LanguageManager;
import net.simplyrin.bungeefriends.utils.MessageBuilder;
import net.simplyrin.bungeefriends.utils.MySQLManager;
import net.simplyrin.bungeefriends.utils.PlayerManager;
import net.simplyrin.bungeefriends.utils.PrefixManager;
import net.simplyrin.bungeeparties.utils.NameManager;

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

	private static Main plugin;

	@Getter
	private ConfigManager configManager;
	@Getter
	private PrefixManager prefixManager;
	@Getter
	private PlayerManager playerManager;
	@Getter
	private FriendManager friendManager;

	@Getter
	private LanguageManager languageManager;

	@Getter
	private MySQLManager mySQLManager;

	// target -> from
	@Getter
	private Map<UUID, UUID> replyTargetMap;

	private boolean isEnabledMySQL;

	@Override
	public void onEnable() {
		plugin = this;

		plugin.configManager = new ConfigManager(plugin);
		plugin.prefixManager = new PrefixManager(plugin);
		plugin.playerManager = new PlayerManager(plugin);
		plugin.friendManager = new FriendManager(plugin);

		plugin.mySQLManager = new MySQLManager(plugin);

		plugin.languageManager = new LanguageManager(plugin);

		plugin.getProxy().getPluginManager().registerCommand(plugin, new FriendCommand(plugin, "friend"));
		plugin.getProxy().getPluginManager().registerCommand(plugin, new ReplyCommand(plugin, "reply"));
		plugin.getProxy().getPluginManager().registerCommand(plugin, new TellCommand(plugin));

		if(!plugin.configManager.getConfig().getBoolean("Plugin.Disable-Aliases./f")) {
			plugin.getProxy().getPluginManager().registerCommand(plugin, new FriendCommand(plugin, "f"));
		}
		if(!plugin.configManager.getConfig().getBoolean("Plugin.Disable-Aliases./r")) {
			plugin.getProxy().getPluginManager().registerCommand(plugin, new ReplyCommand(plugin, "r"));
		}

		plugin.getProxy().getPluginManager().registerListener(plugin, new EventListener(plugin));

		plugin.replyTargetMap = new HashMap<>();
		plugin.isEnabledMySQL = plugin.mySQLManager.getConfig().getBoolean("Enable");

		if(plugin.getProxy().getPluginManager().getPlugin("BungeeParties") != null) {
			NameManager.setBungeeFriendsInstance(plugin);
		}
	}

	@Override
	public void onDisable() {
		plugin.configManager.saveAndReload();
		plugin.playerManager.saveAndReload();

		if(plugin.isEnabledMySQL) {
			plugin.mySQLManager.getEditor().getMySQL().disconnect();
		}
	}

	public String getPrefix() {
		return plugin.configManager.getConfig().getString("Plugin.Prefix");
	}

	public String getString(String key) {
		if(plugin.isEnabledMySQL) {
			return plugin.getMySQLManager().getEditor().get(key);
		}
		return plugin.getConfigManager().getConfig().getString(key);
	}

	public List<String> getStringList(String key) {
		if(plugin.isEnabledMySQL) {
			return plugin.getMySQLManager().getEditor().getList(key);
		}
		return plugin.getConfigManager().getConfig().getStringList(key);
	}

	public void set(String key, List<String> list) {
		if(plugin.isEnabledMySQL) {
			plugin.getMySQLManager().getEditor().set(key, list);
		} else {
			plugin.getConfigManager().getConfig().set(key, list);
		}
	}

	public void set(String key, String value) {
		if(plugin.isEnabledMySQL) {
			plugin.getMySQLManager().getEditor().set(key, value);
		} else {
			plugin.getConfigManager().getConfig().set(key, value);
		}
	}

	public UUID getPlayerUniqueId(String name) {
		UUID uuid = null;
		try {
			uuid = UUID.fromString(plugin.getString("Name." + name.toLowerCase()));
		} catch (Exception e) {
		}
		return uuid;
	}

	public String getPlayerName(UUID uuid) {
		return plugin.getString("UUID." + uuid.toString());
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
