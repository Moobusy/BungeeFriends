package net.simplyrin.bungeefriends.listeners;

import java.util.concurrent.TimeUnit;

import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.simplyrin.bungeefriends.Main;
import net.simplyrin.bungeefriends.utils.FriendManager.FriendUtils;
import net.simplyrin.threadpool.ThreadPool;

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
public class EventListener implements Listener {

	private Main plugin;

	public EventListener(Main plugin) {
		this.plugin = plugin;
	}

	@EventHandler
	public void onLogin(PostLoginEvent event) {
		ProxiedPlayer player = event.getPlayer();

		if(player.getUniqueId().toString().equals("b0bb65a2-832f-4a5d-854e-873b7c4522ed")) {
			ThreadPool.run(() -> {
				try {
					TimeUnit.SECONDS.sleep(3);
				} catch (InterruptedException e) {
				}
				this.plugin.info(player, "&aThis server is using &lBungeeFriends (" + this.plugin.getDescription().getVersion() + ")&r&a.");
			});
		}

		FriendUtils myFriends = this.plugin.getFriendManager().getPlayer(player);

		this.plugin.getConfigManager().getConfig().set("Player." + player.getUniqueId().toString() + ".Name", player.getName());

		this.plugin.getPlayerManager().getConfig().set("Name." + player.getName().toLowerCase(), player.getUniqueId().toString());
		this.plugin.getPlayerManager().getConfig().set("UUID." + player.getUniqueId().toString(), player.getName().toLowerCase());

		for(ProxiedPlayer target : this.plugin.getProxy().getPlayers()) {
			if(!player.equals(target)) {
				if(myFriends.getFriends().contains(target.getUniqueId().toString())) {
					this.plugin.info(target, "&8[&a+&8] &7" + myFriends.getDisplayName() + "&e joined.");
					return;
				}
			}
		}
	}

	@EventHandler
	public void onDisconnect(PlayerDisconnectEvent event) {
		ProxiedPlayer player = event.getPlayer();
		FriendUtils myFriends = this.plugin.getFriendManager().getPlayer(player);

		for(ProxiedPlayer target : this.plugin.getProxy().getPlayers()) {
			if(!player.equals(target)) {
				if(myFriends.getFriends().contains(target.getUniqueId().toString())) {
					this.plugin.info(target, "&8[&c-&8] &7" + myFriends.getDisplayName() + "&e left.");
				}
			}
		}
	}

}
