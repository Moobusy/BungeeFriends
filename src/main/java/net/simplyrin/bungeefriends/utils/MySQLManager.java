package net.simplyrin.bungeefriends.utils;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

import lombok.Getter;
import net.md_5.bungee.config.Configuration;
import net.simplyrin.bungeefriends.Main;
import net.simplyrin.bungeefriends.messages.Messages;
import net.simplyrin.bungeefriends.tools.MySQL;
import net.simplyrin.bungeefriends.tools.MySQL.Editor;
import net.simplyrin.config.Config;
import net.simplyrin.threadpool.ThreadPool;

/**
 * Created by SimplyRin on 2018/09/04.
 *
 * Copyright (c) 2018 SimplyRin
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
public class MySQLManager {

	private Main plugin;
	@Getter
	private Runnable runnable;
	@Getter
	private Configuration config;
	@Getter
	private Editor editor;

	@Getter
	private boolean debugMode;

	public MySQLManager(Main plugin) {
		this.plugin = plugin;

		this.createConfig();
		if(this.config.getBoolean("Enable")) {

			this.plugin.info("&c" + Messages.CONSOLE_HYPHEN);
			this.plugin.info("");
			this.plugin.info("&4&lMySQL is currently under development");
			this.plugin.info("&4&lData guarantee is not supported.");
			this.plugin.info("&4&lPlease use it at your own risk.");
			this.plugin.info("");
			this.plugin.info("&c" + Messages.CONSOLE_HYPHEN);

			this.loginToMySQL();
			this.migrate();
		}
	}

	public void createConfig() {
		File folder = this.plugin.getDataFolder();
		if(!folder.exists()) {
			folder.mkdir();
		}

		File file = new File(folder, "mysql.yml");
		if(!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}

			this.config = Config.getConfig(file);
			this.config.set("Enable", false);
			this.config.set("DebugMode", false);
			this.config.set("Username", "ROOT");
			this.config.set("Password", "PASSWORD");
			this.config.set("Address", "localhost:3306");
			this.config.set("Database", "bungeefriends");
			this.config.set("Timezone", "JST");
			this.config.set("UseSSL", false);
			Config.saveConfig(this.config, file);
		}

		this.config = Config.getConfig(file);

		this.debugMode = this.config.getBoolean("DebugMode");
	}

	public void loginToMySQL() {
		MySQL mySQL = new MySQL(this.config.getString("Username"), this.config.getString("Password"));
		mySQL.setAddress(this.config.getString("Address"));
		mySQL.setDatabase(this.config.getString("Database"));
		mySQL.setTable("main");
		mySQL.setTimezone(this.config.getString("Timezone"));
		mySQL.setUseSSL(this.config.getBoolean("UseSSL"));

		try {
			this.editor = mySQL.connect();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		ThreadPool.run(() -> {
			this.autoReconnect();
		});
	}

	public void autoReconnect() {
		if(this.debugMode) {
			this.plugin.info("Reconnect in 30 minutes...");
		}

		try {
			TimeUnit.MINUTES.sleep(30);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		if(this.debugMode) {
			this.plugin.info("Reconnecting...");
		}
		try {
			this.editor = this.editor.getMySQL().reconnect();
		} catch (SQLException e) {
			if(this.debugMode) {
				this.plugin.info("Reconnection failed");
			}
			this.autoReconnect();
			return;
		}
		if(this.debugMode) {
			this.plugin.info("Reconnection was successfully completed!");
		}
		this.autoReconnect();
	}

	public void migrate() {
		File folder = this.plugin.getDataFolder();
		if(!folder.exists()) {
			folder.mkdir();
		}

		File file = new File(folder, "config.yml");
		if(file.exists()) {
			Configuration config = Config.getConfig(file);
			if(config.getBoolean("Plugin.AlreadyMigrated")) {
				return;
			}

			if(this.debugMode) {
				this.plugin.info("Migration from config file to MySQL is starting... (config.yml)");
			}

			this.editor.set("Plugin.Prefix", config.getString("Plugin.Prefix"));

			Collection<String> player = config.getSection("Player").getKeys();
			for(String value : player) {
				if(this.debugMode) {
					this.plugin.info("Migrating: " + value);
				}
				this.editor.set("Player." + value + ".Name", config.getString("Player." + value + ".Name"));
				this.editor.set("Player." + value + ".Language", config.getString("Player." + value + ".Language"));
				this.editor.set("Player." + value + ".Prefix", config.getString("Player." + value + ".Prefix"));
				this.editor.set("Player." + value + ".Friends", config.getStringList("Player." + value + ".Friends"));
				List<String> list = config.getStringList("Player." + value + ".Requests");
				if(list.size() == 0) {
					this.editor.set("Player." + value + ".Requests", "[]");
				} else {
					this.editor.set("Player." + value + ".Requests", config.getStringList("Player." + value + ".Requests"));
				}
			}

			config.set("Plugin.AlreadyMigrated", true);
			Config.saveConfig(config, file);

			if(this.debugMode) {
				this.plugin.info("Migration successful! (config.yml)");
			}
		}

		file = new File(folder, "player.yml");
		if(file.exists()) {
			Configuration config = Config.getConfig(file);
			if(config.getBoolean("Plugin.AlreadyMigrated")) {
				return;
			}

			if(this.debugMode) {
				this.plugin.info("Migration from config file to MySQL is starting... (player.yml)");
			}

			Collection<String> collection = config.getSection("UUID").getKeys();
			for(String value : collection) {
				if(this.debugMode) {
					this.plugin.info("Migrating: " + value);
				}
				this.editor.set("UUID." + value, config.getString("UUID." + value));
			}
			collection = config.getSection("Name").getKeys();
			for(String value : collection) {
				if(this.debugMode) {
					this.plugin.info("Migrating: " + value);
				}
				this.editor.set("Name." + value, config.getString("Name." + value));
			}

			config.set("Plugin.AlreadyMigrated", true);
			Config.saveConfig(config, file);

			if(this.debugMode) {
				this.plugin.info("Migration successful! (player.yml)");
			}
		}
	}

}
