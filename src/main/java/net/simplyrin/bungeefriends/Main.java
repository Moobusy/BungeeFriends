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
import net.simplyrin.bungeefriends.commands.alias.FLCommand;
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

	@Getter
	private Map<UUID, UUID> replyTargetMap;

	private boolean isEnabledMySQL;

	@Override
	public void onEnable() {
		this.configManager = new ConfigManager(this);
		this.prefixManager = new PrefixManager(this);
		this.playerManager = new PlayerManager(this);
		this.friendManager = new FriendManager(this);

		this.mySQLManager = new MySQLManager(this);

		this.languageManager = new LanguageManager(this);

		this.getProxy().getPluginManager().registerCommand(this, new FriendCommand(this, "friend"));
		this.getProxy().getPluginManager().registerCommand(this, new ReplyCommand(this, "reply"));
		this.getProxy().getPluginManager().registerCommand(this, new TellCommand(this));

		if (!this.configManager.getConfig().getBoolean("this.Disable-Aliases./f")) {
			this.getProxy().getPluginManager().registerCommand(this, new FriendCommand(this, "f"));
		}
		if (!this.configManager.getConfig().getBoolean("this.Disable-Aliases./r")) {
			this.getProxy().getPluginManager().registerCommand(this, new ReplyCommand(this, "r"));
		}
		if (!this.configManager.getConfig().getBoolean("this.Disable-Aliases./fl")) {
			this.getProxy().getPluginManager().registerCommand(this, new FLCommand(this));
		}

		this.getProxy().getPluginManager().registerListener(this, new EventListener(this));

		this.replyTargetMap = new HashMap<>();
		this.isEnabledMySQL = this.mySQLManager.getConfig().getBoolean("Enable");

		if (this.getProxy().getPluginManager().getPlugin("BungeeParties") != null) {
			NameManager.setBungeeFriendsInstance(this);
		}
	}

	@Override
	public void onDisable() {
		this.configManager.saveAndReload();
		this.playerManager.saveAndReload();

		if (this.isEnabledMySQL) {
			this.mySQLManager.getEditor().getMySQL().disconnect();
		}
	}

	public String getPrefix() {
		return this.configManager.getConfig().getString("this.Prefix");
	}

	public String getString(String key) {
		if (this.isEnabledMySQL) {
			return this.getMySQLManager().getEditor().get(key);
		}
		return this.getConfigManager().getConfig().getString(key);
	}

	public List<String> getStringList(String key) {
		if (this.isEnabledMySQL) {
			return this.getMySQLManager().getEditor().getList(key);
		}
		return this.getConfigManager().getConfig().getStringList(key);
	}

	public void set(String key, List<String> list) {
		if (this.isEnabledMySQL) {
			this.getMySQLManager().getEditor().set(key, list);
		} else {
			this.getConfigManager().getConfig().set(key, list);
		}
	}

	public void set(String key, String value) {
		if (this.isEnabledMySQL) {
			this.getMySQLManager().getEditor().set(key, value);
		} else {
			this.getConfigManager().getConfig().set(key, value);
		}
	}

	public UUID getPlayerUniqueId(String name) {
		UUID uuid = null;
		try {
			uuid = UUID.fromString(this.getString("Name." + name.toLowerCase()));
		} catch (Exception e) {
		}
		return uuid;
	}

	public String getPlayerName(UUID uuid) {
		return this.getString("UUID." + uuid.toString());
	}

	@SuppressWarnings("deprecation")
	public void info(String args) {
		this.getProxy().getConsole().sendMessage(ChatColor.translateAlternateColorCodes('&', this.getPrefix() + args));
	}

	@SuppressWarnings("deprecation")
	public void info(ProxiedPlayer player, String args) {
		player.sendMessage(ChatColor.translateAlternateColorCodes('&', this.getPrefix() + args));
	}

	@SuppressWarnings("deprecation")
	public void info(UUID uuid, String args) {
		ProxiedPlayer player = this.getProxy().getPlayer(uuid);
		if (player != null) {
			player.sendMessage(ChatColor.translateAlternateColorCodes('&', this.getPrefix() + args));
		}
	}

	public void info(ProxiedPlayer player, TextComponent args) {
		player.sendMessage(MessageBuilder.get(this.getPrefix()), args);
	}

}
