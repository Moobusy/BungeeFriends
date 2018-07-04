package net.simplyrin.bungeefriends.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.simplyrin.bungeefriends.Main;
import net.simplyrin.bungeefriends.exceptions.AlreadyAddedException;
import net.simplyrin.bungeefriends.exceptions.FailedAddingException;
import net.simplyrin.bungeefriends.exceptions.FaliedRemovingException;
import net.simplyrin.bungeefriends.exceptions.NotAddedException;
import net.simplyrin.bungeefriends.messages.Messages;
import net.simplyrin.bungeefriends.messages.Permissions;
import net.simplyrin.bungeefriends.utils.FriendManager.FriendUtils;
import net.simplyrin.bungeefriends.utils.MessageBuilder;
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
public class FriendCommand extends Command {

	private Main plugin;

	public FriendCommand(Main plugin) {
		super("friend", null, "f");
		this.plugin = plugin;
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		if(!(sender instanceof ProxiedPlayer)) {
			this.plugin.info(Messages.INGAME_ONLY);
			return;
		}

		ProxiedPlayer player = (ProxiedPlayer) sender;
		FriendUtils myFriends = this.plugin.getFriendManager().getPlayer(player);

		if(!player.hasPermission(Permissions.MAIN)) {
			this.plugin.info(player, Messages.NO_PERMISSION);
			return;
		}

		if(args.length > 0) {
			if(args[0].equalsIgnoreCase("add")) {
				if(args.length > 1) {
					ProxiedPlayer target = this.plugin.getProxy().getPlayer(args[1]);
					if(target == null) {
						this.plugin.info(player, Messages.HYPHEN);
						this.plugin.info(player, "&cThat player is currently offline");
						this.plugin.info(player, Messages.HYPHEN);
						return;
					}
					FriendUtils targetFriends = this.plugin.getFriendManager().getPlayer(target);

					try {
						myFriends.addRequest(target);
					} catch (AlreadyAddedException e) {
						this.plugin.info(player, Messages.HYPHEN);
						this.plugin.info(player, "&cYou've already sent a friend request to this person!");
						this.plugin.info(player, Messages.HYPHEN);
						return;
					} catch (FailedAddingException e) {
						this.plugin.info(player, Messages.HYPHEN);
						this.plugin.info(player, "&cYou're already friends with this person!");
						this.plugin.info(player, Messages.HYPHEN);
						return;
					}

					this.plugin.info(player, Messages.HYPHEN);
					this.plugin.info(player, "&eYou sent a friend request to " + targetFriends.getDisplayName() + "&e!");
					this.plugin.info(player, "&eThey have 5 minutes to accept it!");
					this.plugin.info(player, Messages.HYPHEN);

					TextComponent prefix = MessageBuilder.get(this.plugin.getPrefix());
					TextComponent grayHyphen = MessageBuilder.get("&r &8- &r", null, ChatColor.DARK_GRAY, null, false);

					TextComponent accept = MessageBuilder.get("&a&l[ACCEPT]&r", "/friend accept " + myFriends.getName(), ChatColor.GREEN, "&bClick to accept the friend request", true);
					TextComponent deny = MessageBuilder.get("&c&l[DENY]&r", "/friend deny " + myFriends.getName(), ChatColor.GREEN, "&bClick to deny the friend request", true);

					this.plugin.info(target, Messages.HYPHEN);
					this.plugin.info(target, "&eFriend request from " + myFriends.getDisplayName() + "&e!");
					target.sendMessage(prefix, accept, grayHyphen, deny);
					this.plugin.info(target, Messages.HYPHEN);

					ThreadPool.run(new Runnable() {
						@Override
						public void run() {
							try {
								Thread.sleep(300000);
							} catch (Exception e) {
							}

							try {
								myFriends.removeRequest(target);
							} catch (NotAddedException e) {
								return;
							}

							FriendCommand.this.plugin.info(player, Messages.HYPHEN);
							FriendCommand.this.plugin.info(player, "&eYour friend request to " + targetFriends.getDisplayName() + "&e has expired.");
							FriendCommand.this.plugin.info(player, Messages.HYPHEN);

							FriendCommand.this.plugin.info(target, Messages.HYPHEN);
							FriendCommand.this.plugin.info(target, "&eThe friend request from " + myFriends.getDisplayName() + "&e has expired.");
							FriendCommand.this.plugin.info(target, Messages.HYPHEN);
						}
					});
					return;
				}
				this.plugin.info(player, "&cUsage: /friend add <player>");
				return;
			}

			if(args[0].equalsIgnoreCase("remove")) {
				if(args.length > 1) {
					ProxiedPlayer target = this.plugin.getProxy().getPlayer(args[1]);
					if(target == null) {
						this.plugin.info(player, Messages.HYPHEN);
						this.plugin.info(player, "&cThat player is currently offline");
						this.plugin.info(player, Messages.HYPHEN);
						return;
					}
					FriendUtils targetFriends = this.plugin.getFriendManager().getPlayer(target);

					try {
						myFriends.remove(target);
					} catch (NotAddedException e) {
						this.plugin.info(player, Messages.HYPHEN);
						this.plugin.info(player, "&c" + targetFriends.getDisplayName() + "&c isn't on your friends list!");
						this.plugin.info(player, Messages.HYPHEN);
						return;
					} catch (FaliedRemovingException e) {
						this.plugin.info(player, Messages.HYPHEN);
						this.plugin.info(player, "&c" + e.getMessage());
						this.plugin.info(player, Messages.HYPHEN);
						return;
					}
					this.plugin.info(player, Messages.HYPHEN);
					this.plugin.info(player, "&eYou removed " + targetFriends.getDisplayName() + "&e from your friends list!");
					this.plugin.info(player, Messages.HYPHEN);

					this.plugin.info(target, Messages.HYPHEN);
					this.plugin.info(target, "&e" + targetFriends.getDisplayName() + "&e removed you from their friends list!");
					this.plugin.info(target, Messages.HYPHEN);
					return;
				}
				this.plugin.info(player, "&cUsage: /friend remove <player>");
				return;
			}

			if(args[0].equalsIgnoreCase("accept")) {
				if(args.length > 1) {
					ProxiedPlayer target = this.plugin.getProxy().getPlayer(args[1]);
					if(target == null) {
						this.plugin.info(player, Messages.HYPHEN);
						this.plugin.info(player, "&cThat player is currently offline");
						this.plugin.info(player, Messages.HYPHEN);
						return;
					}
					FriendUtils targetFriends = this.plugin.getFriendManager().getPlayer(target);

					try {
						targetFriends.removeRequest(player);
					} catch (NotAddedException e) {
						this.plugin.info(player, Messages.HYPHEN);
						this.plugin.info(player, "&cThat person hasn't invited you to be friends! Try &e/friend add " + targetFriends.getName());
						this.plugin.info(player, Messages.HYPHEN);
						return;
					}

					this.plugin.info(player, Messages.HYPHEN);
					this.plugin.info(player, "&aYou are now friends with " + targetFriends.getDisplayName());
					this.plugin.info(player, Messages.HYPHEN);

					this.plugin.info(target, Messages.HYPHEN);
					this.plugin.info(target, "&aYou are now friends with " + myFriends.getDisplayName());
					this.plugin.info(target, Messages.HYPHEN);

					try {
						myFriends.add(target);
					} catch (AlreadyAddedException e) {
						e.printStackTrace();
					} catch (FailedAddingException e) {
						e.printStackTrace();
					}
					return;
				}
				this.plugin.info(player, "&cUsage: /friend accept <player>");
				return;
			}

			if(args[0].equalsIgnoreCase("deny")) {
				if(args.length > 1) {
					ProxiedPlayer target = this.plugin.getProxy().getPlayer(args[1]);
					if(target == null) {
						this.plugin.info(player, Messages.HYPHEN);
						this.plugin.info(player, "&cThat player is currently offline");
						this.plugin.info(player, Messages.HYPHEN);
						return;
					}
					FriendUtils targetFriends = this.plugin.getFriendManager().getPlayer(target);

					try {
						targetFriends.removeRequest(player);
					} catch (NotAddedException e) {
						this.plugin.info(player, Messages.HYPHEN);
						this.plugin.info(player, "&cThat person hasn't invited you to be friends! Try &e/friend add " + targetFriends.getName());
						this.plugin.info(player, Messages.HYPHEN);
						return;
					}

					this.plugin.info(player, Messages.HYPHEN);
					this.plugin.info(player, "&cDeclined " + targetFriends.getDisplayName() + "&e's friend request!");
					this.plugin.info(player, Messages.HYPHEN);
					return;
				}
				this.plugin.info(player, "&cUsage: /friend deny <player>");
				return;
			}

			if(args[0].equalsIgnoreCase("list")) {
				List<String> list = myFriends.getFriends();

				if(list.size() == 0) {
					this.plugin.info(player, Messages.HYPHEN);
					this.plugin.info(player, "&eYou don't have any friends yet!");
					this.plugin.info(player, "&eAdd some with /friend add <player>");
					this.plugin.info(player, Messages.HYPHEN);
					return;
				}

				List<String> online = new ArrayList<>();
				List<String> offline = new ArrayList<>();

				for(String uuid : list) {
					ProxiedPlayer target = this.plugin.getProxy().getPlayer(UUID.fromString(uuid));
					FriendUtils targetFriends = this.plugin.getFriendManager().getPlayer(UUID.fromString(uuid));

					if(target != null) {
						online.add("&e" + targetFriends.getDisplayName() + "&e is in a " + target.getServer().getInfo().getName() + " Server.");
					} else {
						offline.add("&e" + targetFriends.getDisplayName() + "&c is currently offline");
					}
				}

				this.plugin.info(player, Messages.HYPHEN);
				for(String message : online) {
					this.plugin.info(player, message);
				}
				for(String message : offline) {
					this.plugin.info(player, message);
				}
				this.plugin.info(player, Messages.HYPHEN);
				return;
			}

			if(args[0].equalsIgnoreCase("force-add")) {
				if(!player.hasPermission(Permissions.FORCE)) {
					this.plugin.info(player, Messages.NO_PERMISSION);
					return;
				}

				if(args.length > 1) {
					ProxiedPlayer target = this.plugin.getProxy().getPlayer(args[1]);
					if(target == null) {
						this.plugin.info(player, Messages.HYPHEN);
						this.plugin.info(player, "&cThat player is currently offline");
						this.plugin.info(player, Messages.HYPHEN);
						return;
					}

					FriendUtils targetFriends = this.plugin.getFriendManager().getPlayer(target);

					try {
						myFriends.add(target);
					} catch (AlreadyAddedException e) {
						this.plugin.info(player, Messages.HYPHEN);
						this.plugin.info(player, "&cYou're already friends with this person!");
						this.plugin.info(player, Messages.HYPHEN);
						return;
					} catch (FailedAddingException e) {
						this.plugin.info(player, Messages.HYPHEN);
						this.plugin.info(player, "&c" + e.getMessage());
						this.plugin.info(player, Messages.HYPHEN);
						return;
					}

					this.plugin.info(player, Messages.HYPHEN);
					this.plugin.info(player, "&aYou are now friends with " + targetFriends.getDisplayName() + " &c&l[FORCE]");
					this.plugin.info(player, Messages.HYPHEN);

					this.plugin.info(target, Messages.HYPHEN);
					this.plugin.info(target, "&aYou are now friends with " + myFriends.getDisplayName() + " &c&l[FORCE]");
					this.plugin.info(target, Messages.HYPHEN);
					return;
				}

				this.plugin.info(player, "&cUsage: /friend force-add <player>");
				return;
			}
		}

		this.plugin.info(player, Messages.HYPHEN);
		this.plugin.info(player, "&eFriend Commands:");
		this.plugin.info(player, "&e/friend add &7- &bAdd a player as a friend");
		this.plugin.info(player, "&e/friend remove &7- &bRemove a player from your friends");
		this.plugin.info(player, "&e/friend accept &7- &bAccept a friend request");
		this.plugin.info(player, "&e/friend deny &7- &bDecline a friend request");
		this.plugin.info(player, "&e/friend list &7- &bList your friends");
		this.plugin.info(player, Messages.HYPHEN);
	}

}
