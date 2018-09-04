package net.simplyrin.bungeefriends.utils;

import java.io.File;
import java.io.IOException;

import lombok.Getter;
import net.md_5.bungee.config.Configuration;
import net.simplyrin.bungeefriends.Main;
import net.simplyrin.config.Config;

/**
 * Created by SimplyRin on 2018/09/04.
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
public class PrefixManager {

	private Main plugin;
	@Getter
	private Runnable runnable;
	@Getter
	private Configuration config;

	public PrefixManager(Main plugin) {
		this.plugin = plugin;

		this.createConfig();
		this.saveAndReload();
	}

	public void saveAndReload() {
		File config = new File(this.plugin.getDataFolder(), "prefix.yml");

		Config.saveConfig(this.config, config);
		this.config = Config.getConfig(config);
	}

	public void createConfig() {
		File folder = this.plugin.getDataFolder();
		if(!folder.exists()) {
			folder.mkdir();
		}

		File prefix = new File(folder, "prefix.yml");
		if(!prefix.exists()) {
			try {
				prefix.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}

			this.config = Config.getConfig(prefix);

			this.config.set("List.VIP.Prefix", "&a[VIP] ");
			this.config.set("List.VIP.Permission", "friends.prefix.vip");

			this.config.set("List.VIP+.Prefix", "&a[VIP&6+&a] ");
			this.config.set("List.VIP+.Permission", "friends.prefix.vip_plus");

			Config.saveConfig(this.config, prefix);
		}

		this.config = Config.getConfig(prefix);
		this.saveAndReload();
	}

}
