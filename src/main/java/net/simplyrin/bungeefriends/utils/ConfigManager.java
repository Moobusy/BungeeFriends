package net.simplyrin.bungeefriends.utils;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import com.google.common.base.Charsets;

import lombok.Getter;
import net.md_5.bungee.config.Configuration;
import net.simplyrin.bungeefriends.Main;
import net.simplyrin.bungeefriends.config.Config;

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
public class ConfigManager {

	private Main plugin;
	@Getter
	private Runnable runnable;
	@Getter
	private Configuration config;

	public ConfigManager(Main plugin) {
		this.plugin = plugin;

		this.createConfig();
		this.saveAndReload();
	}

	public void saveAndReload() {
		File config = new File(this.plugin.getDataFolder(), "config.yml");

		Config.saveConfig(this.config, config);
		this.config = Config.getConfig(config, Charsets.UTF_8);
	}

	public void createConfig() {
		File folder = this.plugin.getDataFolder();
		if (!folder.exists()) {
			folder.mkdir();
		}

		File config = new File(folder, "config.yml");
		if (!config.exists()) {
			try {
				config.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}

			this.config = Config.getConfig(config, Charsets.UTF_8);

			this.config.set("Plugin.Prefix", "&7[&cFriends&7] &r");

			this.config.set("Plugin.Disable-Aliases./f", false);
			this.config.set("Plugin.Disable-Aliases./r", false);
			this.config.set("Plugin.Disable-Aliases./fl", false);
			this.config.set("Plugin.Disable-Language", "english");

			this.config.set("Player.b0bb65a2-832f-4a5d-854e-873b7c4522ed.Name", "SimplyRin");
			this.config.set("Player.b0bb65a2-832f-4a5d-854e-873b7c4522ed.Language", "english");
			this.config.set("Player.b0bb65a2-832f-4a5d-854e-873b7c4522ed.Prefix", "&c[CREATOR] ");
			this.config.set("Player.b0bb65a2-832f-4a5d-854e-873b7c4522ed.Friends", (List<String>) Arrays.asList("64636120-8633-4541-aa5f-412b42ddb04d"));

			this.config.set("Player.64636120-8633-4541-aa5f-412b42ddb04d.Name", "SimplyFoxy");
			this.config.set("Player.64636120-8633-4541-aa5f-412b42ddb04d.Language", "english");
			this.config.set("Player.64636120-8633-4541-aa5f-412b42ddb04d.Prefix", "&c[CREATOR] ");
			this.config.set("Player.64636120-8633-4541-aa5f-412b42ddb04d.Friends", (List<String>) Arrays.asList("b0bb65a2-832f-4a5d-854e-873b7c4522ed"));

			Config.saveConfig(this.config, config);
		}

		this.config = Config.getConfig(config, Charsets.UTF_8);

		this.config.set("Plugin.Disable-Alias", null);

		this.resetValue("Plugin.Disable-Aliases./f");
		this.resetValue("Plugin.Disable-Aliases./r");
		this.resetValue("Plugin.Disable-Aliases./fl");

		if (this.config.getString("Plugin.Default-Language").equals("")) {
			this.config.set("Plugin.Default-Language", "english");
		}

		this.saveAndReload();
	}

	public void resetValue(String key) {
		if (!this.config.getBoolean(key)) {
			this.config.set(key, false);
		}
	}

}
