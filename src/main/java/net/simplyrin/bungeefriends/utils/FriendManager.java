package net.simplyrin.bungeefriends.utils;

import java.util.List;
import java.util.UUID;

import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.simplyrin.bungeefriends.Main;
import net.simplyrin.bungeefriends.exceptions.AlreadyAddedException;
import net.simplyrin.bungeefriends.exceptions.FailedAddingException;
import net.simplyrin.bungeefriends.exceptions.NotAddedException;

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
public class FriendManager {

	private Main plugin;

	public FriendManager(Main plugin) {
		this.plugin = plugin;
	}

	public FriendUtils getPlayer(ProxiedPlayer player) {
		return new FriendUtils(player.getUniqueId());
	}

	public FriendUtils getPlayer(String uuid) {
		return new FriendUtils(UUID.fromString(uuid));
	}

	public FriendUtils getPlayer(UUID uniqueId) {
		return new FriendUtils(uniqueId);
	}

	public class FriendUtils {

		private UUID uuid;

		public FriendUtils(UUID uuid) {
			this.uuid = uuid;

			// TODO: なかったら作れカス
			if(FriendManager.this.plugin.getConfigManager().getConfig().get("Player." + this.uuid.toString()) == null) {
				ProxiedPlayer player = FriendManager.this.plugin.getProxy().getPlayer(this.uuid);

				FriendManager.this.plugin.info("Creating data for player " + player.getName() + "...");

				FriendManager.this.plugin.getConfigManager().getConfig().set("Player." + this.uuid.toString() + ".Name", player.getName());
				FriendManager.this.plugin.getConfigManager().getConfig().set("Player." + this.uuid.toString() + ".Toggle-Reception-Request", true);
				FriendManager.this.plugin.getConfigManager().getConfig().set("Player." + this.uuid.toString() + ".Prefix", "&7");
				FriendManager.this.plugin.getConfigManager().getConfig().set("Player." + this.uuid.toString() + ".Friends", "&7");
			}
		}

		public ProxiedPlayer getPlayer() {
			return FriendManager.this.plugin.getProxy().getPlayer(this.getName());
		}

		public String getDisplayName() {
			return this.getPrefix() + this.getName();
		}

		public String getName() {
			return FriendManager.this.plugin.getConfigManager().getConfig().getString("Player." + this.uuid.toString() + ".Name");
		}

		public boolean isEnabledReceiveRequest() {
			return FriendManager.this.plugin.getConfigManager().getConfig().getBoolean("Player." + this.uuid.toString() + ".Toggle-Reception-Request");
		}

		public FriendUtils setPrefix(String prefix) {
			FriendManager.this.plugin.getConfigManager().getConfig().set("Player." + this.uuid.toString() + ".Prefix", prefix);
			return this;
		}

		public String getPrefix() {
			return FriendManager.this.plugin.getConfigManager().getConfig().getString("Player." + this.uuid.toString() + ".Prefix");
		}

		public List<String> getFriends() {
			return FriendManager.this.plugin.getConfigManager().getConfig().getStringList("Player." + this.uuid.toString() + ".Friends");
		}

		public FriendUtils add(ProxiedPlayer player) throws AlreadyAddedException, FailedAddingException {
			return this.add(player.getUniqueId());
		}

		public FriendUtils add(UUID uuid) throws AlreadyAddedException, FailedAddingException {
			if(this.uuid.toString().equals(uuid.toString())) {
				throw new FailedAddingException("You can't add yourself as a friend!");
			}

			List<String> list = this.getFriends();
			if(list.contains(uuid.toString())) {
				throw new AlreadyAddedException();
			}
			list.add(uuid.toString());
			FriendManager.this.plugin.getConfigManager().getConfig().set("Player." + this.uuid.toString() + ".Friends", list);


			FriendUtils targetFriends = FriendManager.this.plugin.getFriendManager().getPlayer(uuid);
			List<String> targetList = targetFriends.getFriends();
			if(targetList.contains(this.uuid.toString())) {
				throw new AlreadyAddedException();
			}
			targetList.add(this.uuid.toString());
			FriendManager.this.plugin.getConfigManager().getConfig().set("Player." + uuid.toString() + ".Friends", targetList);
			return this;
		}

		public FriendUtils remove(ProxiedPlayer player) throws NotAddedException, FailedAddingException {
			return this.remove(player.getUniqueId());
		}

		public FriendUtils remove(UUID uuid) throws NotAddedException, FailedAddingException {
			if(this.uuid.toString().equals(uuid.toString())) {
				throw new FailedAddingException(this.getDisplayName() + " &cisn't on your friends list!");
			}

			List<String> list = this.getFriends();
			if(!list.contains(uuid.toString())) {
				throw new NotAddedException();
			}
			list.remove(uuid.toString());
			FriendManager.this.plugin.getConfigManager().getConfig().set("Player." + this.uuid.toString() + ".Friends", list);


			FriendUtils targetFriends = FriendManager.this.plugin.getFriendManager().getPlayer(uuid);
			List<String> targetList = targetFriends.getFriends();
			if(!targetList.contains(this.uuid.toString())) {
				throw new NotAddedException();
			}
			targetList.remove(this.uuid.toString());
			FriendManager.this.plugin.getConfigManager().getConfig().set("Player." + uuid.toString() + ".Friends", targetList);
			return this;
		}

	}

}
