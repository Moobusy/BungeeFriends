package net.simplyrin.bungeefriends.utils;

import java.io.File;
import java.util.concurrent.TimeUnit;

import lombok.Getter;
import net.simplyrin.bungeefriends.Main;

/**
 * Created by SimplyRin on 2018/07/04.
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
public class FileBackupManager {

	private Main plugin;
	@Getter
	private Runnable runnable;

	public FileBackupManager(Main plugin) {
		this.plugin = plugin;

		File folder = new File(this.plugin.getDataFolder(), "backups");
		if(!folder.exists()) {
			folder.mkdir();
		}

		this.runnable = new Runnable() {
			@Override
			public void run() {
				while(true) {
					try {
						TimeUnit.HOURS.sleep(2);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}

					File file = new File(folder, "");
				}
			}
		};

		this.plugin.getProxy().getScheduler().runAsync(this.plugin, this.runnable);
	}

}
