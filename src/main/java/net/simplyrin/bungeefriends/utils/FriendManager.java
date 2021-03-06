package net.simplyrin.bungeefriends.utils;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.simplyrin.bungeefriends.Main;
import net.simplyrin.bungeefriends.exceptions.AlreadyAddedException;
import net.simplyrin.bungeefriends.exceptions.FailedAddingException;
import net.simplyrin.bungeefriends.exceptions.FriendSlotLimitException;
import net.simplyrin.bungeefriends.exceptions.IgnoredException;
import net.simplyrin.bungeefriends.exceptions.NotAddedException;
import net.simplyrin.bungeefriends.exceptions.SelfException;
import net.simplyrin.bungeefriends.messages.Permissions;

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

			Object object = FriendManager.this.plugin.getString("Player." + this.uuid.toString() + ".Name");
			if (object == null || object.equals("")) {
				ProxiedPlayer player = FriendManager.this.plugin.getProxy().getPlayer(this.uuid);

				FriendManager.this.plugin.info("Creating data for player " + player.getName() + "...");

				FriendManager.this.plugin.set("Name." + player.getName().toLowerCase(), player.getUniqueId().toString());
				FriendManager.this.plugin.set("UUID." + player.getUniqueId().toString(), player.getName().toLowerCase());

				FriendManager.this.plugin.set("Player." + this.uuid.toString() + ".Name", player.getName());
				FriendManager.this.plugin.set("Player." + this.uuid.toString() + ".Language", FriendManager.this.plugin.getConfigManager().getConfig().getString("Plugin.Default-Language"));
				FriendManager.this.plugin.set("Player." + this.uuid.toString() + ".Prefix", "&7");
				FriendManager.this.plugin.set("Player." + this.uuid.toString() + ".Friends", "[]");
			}
		}

		public ProxiedPlayer getPlayer() {
			return FriendManager.this.plugin.getProxy().getPlayer(this.uuid);
		}

		public String getDisplayName() {
			return this.getPrefix() + this.getName();
		}

		public String getName() {
			return FriendManager.this.plugin.getString("Player." + this.uuid.toString() + ".Name");
		}

		public FriendUtils setPrefix(String prefix) {
			FriendManager.this.plugin.set("Player." + this.uuid.toString() + ".Prefix", prefix);
			return this;
		}

		public String getPrefix() {
			ProxiedPlayer player = this.getPlayer();
			if (player != null) {
				Collection<String> collection = FriendManager.this.plugin.getPrefixManager().getConfig().getSection("List").getKeys();
				for (String list : collection) {
					String prefix = FriendManager.this.plugin.getPrefixManager().getConfig().getString("List." + list + ".Prefix");
					String permission = FriendManager.this.plugin.getPrefixManager().getConfig().getString("List." + list + ".Permission");

					if (player.hasPermission(permission)) {
						FriendManager.this.plugin.set("Player." + this.uuid.toString() + ".Prefix", prefix);
						return prefix;
					}
				}
			}

			return FriendManager.this.plugin.getString("Player." + this.uuid.toString() + ".Prefix");
		}

		public UUID getUniqueId() {
			return this.uuid;
		}

		public List<String> getRequests() {
			return FriendManager.this.plugin.getStringList("Player." + this.uuid.toString() + ".Requests");
		}

		public boolean isFriend(UUID targetUniqueId) {
			List<String> list = this.getFriends();
			return list.contains(targetUniqueId.toString());
		}

		public FriendUtils addRequest(ProxiedPlayer player) throws AlreadyAddedException, FailedAddingException, SelfException, IgnoredException, FriendSlotLimitException {
			return this.addRequest(player.getUniqueId());
		}

		public FriendUtils addRequest(UUID uuid) throws AlreadyAddedException, FailedAddingException, SelfException, IgnoredException, FriendSlotLimitException {
			if (this.uuid.toString().equals(uuid.toString())) {
				throw new SelfException();
			}

			List<String> list = this.getFriends();
			if (list.contains(uuid.toString())) {
				throw new AlreadyAddedException();
			}

			ProxiedPlayer player = this.getPlayer();
			if (player != null) {
				if (this.getPlayer().hasPermission("friends.limit." + this.getFriends().size()) && !this.getPlayer().hasPermission(Permissions.ADMIN)) {
					throw new FriendSlotLimitException();
				}
			}

			FriendUtils targetFriendUtils = FriendManager.this.getPlayer(uuid);
			List<String> ignoreList = targetFriendUtils.getIgnoreList();
			if (ignoreList.contains(this.uuid.toString())) {
				throw new IgnoredException();
			}

			List<String> requests = FriendManager.this.plugin.getStringList("Player." + this.uuid.toString() + ".Requests");
			if (requests.contains(uuid.toString())) {
				throw new FailedAddingException();
			}
			requests.add(uuid.toString());
			FriendManager.this.plugin.set("Player." + this.uuid.toString() + ".Requests", requests);
			return this;
		}

		public FriendUtils removeRequest(ProxiedPlayer player) throws NotAddedException {
			return this.removeRequest(player.getUniqueId());
		}

		public FriendUtils removeRequest(UUID uuid) throws NotAddedException {
			List<String> requests = FriendManager.this.plugin.getStringList("Player." + this.uuid.toString() + ".Requests");
			if (!requests.contains(uuid.toString())) {
				throw new NotAddedException();
			}
			requests.remove(uuid.toString());
			FriendManager.this.plugin.set("Player." + this.uuid.toString() + ".Requests", requests);
			return this;
		}

		public List<String> getFriends() {
			return FriendManager.this.plugin.getStringList("Player." + this.uuid.toString() + ".Friends");
		}

		public FriendUtils add(ProxiedPlayer player) throws AlreadyAddedException, FailedAddingException {
			return this.add(player.getUniqueId());
		}

		public FriendUtils add(UUID uuid) throws AlreadyAddedException, FailedAddingException {
			if (this.uuid.toString().equals(uuid.toString())) {
				throw new FailedAddingException();
			}

			List<String> list = this.getFriends();
			if (list.contains(uuid.toString())) {
				throw new AlreadyAddedException();
			}
			list.add(uuid.toString());
			FriendManager.this.plugin.set("Player." + this.uuid.toString() + ".Friends", list);


			FriendUtils targetFriends = FriendManager.this.plugin.getFriendManager().getPlayer(uuid);
			List<String> targetList = targetFriends.getFriends();
			if (targetList.contains(this.uuid.toString())) {
				throw new AlreadyAddedException();
			}
			targetList.add(this.uuid.toString());
			FriendManager.this.plugin.set("Player." + uuid.toString() + ".Friends", targetList);
			return this;
		}

		public FriendUtils remove(ProxiedPlayer player) throws NotAddedException, SelfException {
			return this.remove(player.getUniqueId());
		}

		public FriendUtils remove(UUID uuid) throws NotAddedException, SelfException {
			if (this.uuid.toString().equals(uuid.toString())) {
				throw new SelfException();
			}

			List<String> list = this.getFriends();
			if (!list.contains(uuid.toString())) {
				throw new NotAddedException();
			}
			list.remove(uuid.toString());
			FriendManager.this.plugin.set("Player." + this.uuid.toString() + ".Friends", list);


			FriendUtils targetFriends = FriendManager.this.plugin.getFriendManager().getPlayer(uuid);
			List<String> targetList = targetFriends.getFriends();
			if (!targetList.contains(this.uuid.toString())) {
				throw new NotAddedException();
			}
			targetList.remove(this.uuid.toString());
			FriendManager.this.plugin.set("Player." + uuid.toString() + ".Friends", targetList);
			return this;
		}

		public List<String> getIgnoreList() {
			return FriendManager.this.plugin.getStringList("Player." + this.uuid.toString() + ".IgnoreList");
		}

		public FriendUtils addIgnore(ProxiedPlayer player) throws AlreadyAddedException {
			return this.addIgnore(player.getUniqueId());
		}

		public FriendUtils addIgnore(UUID uuid) throws AlreadyAddedException {
			List<String> ignoreList = FriendManager.this.plugin.getStringList("Player." + this.uuid.toString() + ".IgnoreList");
			if (ignoreList.contains(uuid.toString())) {
				throw new AlreadyAddedException();
			}
			ignoreList.add(uuid.toString());
			FriendManager.this.plugin.set("Player." + this.uuid.toString() + ".IgnoreList", ignoreList);
			return this;
		}

		public FriendUtils removeIgnore(ProxiedPlayer player) throws NotAddedException {
			return this.removeIgnore(player.getUniqueId());
		}

		public FriendUtils removeIgnore(UUID uuid) throws NotAddedException {
			List<String> ignoreList = FriendManager.this.plugin.getStringList("Player." + this.uuid.toString() + ".IgnoreList");
			if (!ignoreList.contains(uuid.toString())) {
				throw new NotAddedException();
			}
			ignoreList.remove(uuid.toString());
			FriendManager.this.plugin.set("Player." + this.uuid.toString() + ".IgnoreList", ignoreList);
			return this;
		}

	}

}
