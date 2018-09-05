package net.simplyrin.bungeefriends.commands;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.config.Configuration;
import net.simplyrin.bungeefriends.Main;
import net.simplyrin.bungeefriends.exceptions.AlreadyAddedException;
import net.simplyrin.bungeefriends.exceptions.FailedAddingException;
import net.simplyrin.bungeefriends.exceptions.IgnoredException;
import net.simplyrin.bungeefriends.exceptions.NotAddedException;
import net.simplyrin.bungeefriends.exceptions.SelfException;
import net.simplyrin.bungeefriends.messages.Messages;
import net.simplyrin.bungeefriends.messages.Permissions;
import net.simplyrin.bungeefriends.utils.FriendManager.FriendUtils;
import net.simplyrin.bungeefriends.utils.LanguageManager.LanguageUtils;
import net.simplyrin.bungeefriends.utils.MessageBuilder;
import net.simplyrin.config.Config;
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
		LanguageUtils langUtils = this.plugin.getLanguageManager().getPlayer(player);

		if(!player.hasPermission(Permissions.MAIN)) {
			this.plugin.info(player, langUtils.getString(Messages.NO_PERMISSION));
			return;
		}

		if(args.length > 0) {
			if(args[0].equalsIgnoreCase("add")) {
				if(args.length > 1) {
					this.add(player, myFriends, langUtils, args[1]);
					return;
				}
				this.plugin.info(player, langUtils.getString("Add.Usage"));
				return;
			}

			if(args[0].equalsIgnoreCase("remove")) {
				if(args.length > 1) {
					UUID target = this.plugin.getPlayerUniqueId(args[1]);
					if(target == null) {
						this.plugin.info(player, Messages.HYPHEN);
						this.plugin.info(player, langUtils.getString("Cant-Find").replace("%name", args[1]));
						this.plugin.info(player, Messages.HYPHEN);
						return;
					}
					FriendUtils targetFriends = this.plugin.getFriendManager().getPlayer(target);
					LanguageUtils targetLangUtils = this.plugin.getLanguageManager().getPlayer(target);

					try {
						myFriends.remove(target);
					} catch (NotAddedException e) {
						this.plugin.info(player, Messages.HYPHEN);
						this.plugin.info(player, langUtils.getString("Exceptions.IsntOnYourFriends").replace("%targetDisplayName", targetFriends.getDisplayName()));
						this.plugin.info(player, Messages.HYPHEN);
						return;
					} catch (SelfException e) {
						this.plugin.info(player, Messages.HYPHEN);
						this.plugin.info(player, langUtils.getString("Exceptions.CantRemoveYourself"));
						this.plugin.info(player, Messages.HYPHEN);
						return;
					}
					this.plugin.info(player, Messages.HYPHEN);
					this.plugin.info(player, langUtils.getString("Remove.YourSelf").replace("%targetDisplayName", targetFriends.getDisplayName()));
					this.plugin.info(player, Messages.HYPHEN);

					this.plugin.info(target, Messages.HYPHEN);
					this.plugin.info(target, targetLangUtils.getString("Remove.Target").replace("%displayName", myFriends.getDisplayName()));
					this.plugin.info(target, Messages.HYPHEN);
					return;
				}
				this.plugin.info(player, langUtils.getString("Remove.Usage"));
				return;
			}

			if(args[0].equalsIgnoreCase("accept")) {
				if(args.length > 1) {
					UUID target = this.plugin.getPlayerUniqueId(args[1]);
					if(target == null) {
						this.plugin.info(player, Messages.HYPHEN);
						this.plugin.info(player, langUtils.getString("Cant-Find").replace("%name", args[1]));
						this.plugin.info(player, Messages.HYPHEN);
						return;
					}
					FriendUtils targetFriends = this.plugin.getFriendManager().getPlayer(target);
					LanguageUtils targetLangUtils = this.plugin.getLanguageManager().getPlayer(target);

					try {
						targetFriends.removeRequest(player);
					} catch (NotAddedException e) {
						this.plugin.info(player, Messages.HYPHEN);
						this.plugin.info(player, langUtils.getString("Exceptions.NoInvited").replace("%name", targetFriends.getName()));
						this.plugin.info(player, Messages.HYPHEN);
						return;
					}

					this.plugin.info(player, Messages.HYPHEN);
					this.plugin.info(player, langUtils.getString("Accept.YourSelf").replace("%targetDisplayName", targetFriends.getDisplayName()));
					this.plugin.info(player, Messages.HYPHEN);

					this.plugin.info(target, Messages.HYPHEN);
					this.plugin.info(target, targetLangUtils.getString("Accept.Target").replace("%displayName", myFriends.getDisplayName()));
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
				this.plugin.info(player, langUtils.getString("Accept.Usage"));
				return;
			}

			if(args[0].equalsIgnoreCase("deny")) {
				if(args.length > 1) {
					UUID target = this.plugin.getPlayerUniqueId(args[1]);
					if(target == null) {
						this.plugin.info(player, Messages.HYPHEN);
						this.plugin.info(player, langUtils.getString("Cant-Find").replace("%name", args[1]));
						this.plugin.info(player, Messages.HYPHEN);
						return;
					}
					FriendUtils targetFriends = this.plugin.getFriendManager().getPlayer(target);

					try {
						targetFriends.removeRequest(player);
					} catch (NotAddedException e) {
						this.plugin.info(player, Messages.HYPHEN);
						this.plugin.info(player, langUtils.getString("Exceptions.HasntFriend").replace("%name", targetFriends.getName()));
						this.plugin.info(player, Messages.HYPHEN);
						return;
					}

					this.plugin.info(player, Messages.HYPHEN);
					this.plugin.info(player, langUtils.getString("Deny.Declined").replace("%targetDisplayName", targetFriends.getDisplayName()));
					this.plugin.info(player, Messages.HYPHEN);
					return;
				}
				this.plugin.info(player, langUtils.getString("Deny.Usage"));
				return;
			}

			if(args[0].equalsIgnoreCase("list")) {
				List<String> list = myFriends.getFriends();

				if(list.size() == 0) {
					this.plugin.info(player, Messages.HYPHEN);
					this.plugin.info(player, langUtils.getString("List.DontHave.One"));
					this.plugin.info(player, langUtils.getString("List.DontHave.Two"));
					this.plugin.info(player, Messages.HYPHEN);
					return;
				}

				List<String> online = new ArrayList<>();
				List<String> offline = new ArrayList<>();

				System.out.println(list.toString());
				for(String uuid : list) {
					ProxiedPlayer target = this.plugin.getProxy().getPlayer(UUID.fromString(uuid));
					FriendUtils targetFriends = this.plugin.getFriendManager().getPlayer(UUID.fromString(uuid));

					if(target != null) {
						online.add(langUtils.getString("List.Online").replace("%targetDisplayName", targetFriends.getDisplayName()).replace("%server", target.getServer().getInfo().getName()));
					} else {
						offline.add(langUtils.getString("List.Offline").replace("%targetDisplayName", targetFriends.getDisplayName()));
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

			if(args[0].equalsIgnoreCase("lang") || args[0].equalsIgnoreCase("language")) {
				File folder = this.plugin.getDataFolder();
				if(!folder.exists()) {
					folder.mkdir();
				}

				File languageFolder = new File(folder, "Language");
				if(!languageFolder.exists()) {
					languageFolder.mkdir();
				}

				List<String> availableList = new ArrayList<>();
				String available = "";
				File[] languages = languageFolder.listFiles();
				for(File languageFile : languages) {
					Configuration langConfig = Config.getConfig(languageFile);
					if(langConfig.getString("Language").length() > 1) {
						availableList.add(languageFile.getName().toLowerCase().replace(".yml", ""));
						available += langConfig.getString("Language") + ",";
					}
				}

				if(args.length > 1) {
					String lang = args[1];
					if(availableList.contains(lang.toLowerCase())) {
						langUtils.setLanguage(lang.toLowerCase());
						this.plugin.info(player, Messages.HYPHEN);
						this.plugin.info(player, langUtils.getString("Lang.Update").replace("%lang", langUtils.getLanguage()));
						this.plugin.info(player, Messages.HYPHEN);
						return;
					}
				}

				this.plugin.info(player, Messages.HYPHEN);
				this.plugin.info(player, langUtils.getString("Lang.Usage"));
				this.plugin.info(player, langUtils.getString("Lang.Available") + " <" + available.substring(0, available.length() - 1) + ">");
				this.plugin.info(player, Messages.HYPHEN);
				return;
			}

			if(args[0].equalsIgnoreCase("ignore")) {
				if(args.length > 1) {
					if(args[1].equalsIgnoreCase("add")) {
						if(args.length > 2) {
							UUID target = this.plugin.getPlayerUniqueId(args[2]);
							if(target == null) {
								this.plugin.info(player, Messages.HYPHEN);
								this.plugin.info(player, langUtils.getString("Cant-Find").replace("%name", args[2]));
								this.plugin.info(player, Messages.HYPHEN);
								return;
							}
							FriendUtils targetFriends = this.plugin.getFriendManager().getPlayer(target);

							try {
								myFriends.addIgnore(target);
							} catch (AlreadyAddedException e) {
								this.plugin.info(player, Messages.HYPHEN);
								this.plugin.info(player, langUtils.getString("Ignore.AlreadyAdded").replace("%targetDisplayName", targetFriends.getDisplayName()));
								this.plugin.info(player, Messages.HYPHEN);
								return;
							}

							this.plugin.info(player, Messages.HYPHEN);
							this.plugin.info(player, langUtils.getString("Ignore.Added").replace("%targetDisplayName", targetFriends.getDisplayName()));
							this.plugin.info(player, Messages.HYPHEN);
							return;
						}

						this.plugin.info(player, langUtils.getString("Ignore.Usage.Add"));
						return;
					}

					if(args[1].equalsIgnoreCase("remove")) {
						if(args.length > 2) {
							UUID target = this.plugin.getPlayerUniqueId(args[2]);
							if(target == null) {
								this.plugin.info(player, Messages.HYPHEN);
								this.plugin.info(player, langUtils.getString("Cant-Find").replace("%name", args[2]));
								this.plugin.info(player, Messages.HYPHEN);
								return;
							}
							FriendUtils targetFriends = this.plugin.getFriendManager().getPlayer(target);

							try {
								myFriends.removeIgnore(target);
							} catch (NotAddedException e) {
								this.plugin.info(player, Messages.HYPHEN);
								this.plugin.info(player, langUtils.getString("Ignore.NotAdded").replace("%targetDisplayName", targetFriends.getDisplayName()));
								this.plugin.info(player, Messages.HYPHEN);
								return;
							}

							this.plugin.info(player, Messages.HYPHEN);
							this.plugin.info(player, langUtils.getString("Ignore.Removed").replace("%targetDisplayName", targetFriends.getDisplayName()));
							this.plugin.info(player, Messages.HYPHEN);
							return;
						}

						this.plugin.info(player, Messages.HYPHEN);
						this.plugin.info(player, langUtils.getString("Ignore.Usage.Remove"));
						this.plugin.info(player, Messages.HYPHEN);
						return;
					}

					if(args[1].equalsIgnoreCase("list")) {
						this.plugin.info(player, Messages.HYPHEN);
						List<String> ignoreList = myFriends.getIgnoreList();
						if(ignoreList.size() == 0) {
							this.plugin.info(player, langUtils.getString("Ignore.Havent.One"));
							this.plugin.info(player, langUtils.getString("Ignore.Havent.Two"));
							this.plugin.info(player, Messages.HYPHEN);
							return;
						}
						for(String targetUniqueId : myFriends.getIgnoreList()) {
							FriendUtils targetFriends = this.plugin.getFriendManager().getPlayer(UUID.fromString(targetUniqueId));
							this.plugin.info(player, "&e- " + targetFriends.getDisplayName());
						}
						this.plugin.info(player, Messages.HYPHEN);
						return;
					}
				}
				this.plugin.info(player, langUtils.getString("Ignore.Usage.Main"));
				return;
			}

			if(args[0].equalsIgnoreCase("force-add")) {
				if(!player.hasPermission(Permissions.ADMIN)) {
					this.plugin.info(player, Messages.NO_PERMISSION);
					return;
				}

				if(args.length > 1) {
					UUID target = this.plugin.getPlayerUniqueId(args[1]);
					if(target == null) {
						this.plugin.info(player, Messages.HYPHEN);
						this.plugin.info(player, langUtils.getString("Cant-Find").replace("%name", args[1]));
						this.plugin.info(player, Messages.HYPHEN);
						return;
					}

					FriendUtils targetFriends = this.plugin.getFriendManager().getPlayer(target);
					LanguageUtils targetLangUtils = this.plugin.getLanguageManager().getPlayer(target);

					try {
						myFriends.add(target);
					} catch (AlreadyAddedException e) {
						this.plugin.info(player, Messages.HYPHEN);
						this.plugin.info(player, langUtils.getString("Exceptions.AlreadyFriend"));
						this.plugin.info(player, Messages.HYPHEN);
						return;
					} catch (FailedAddingException e) {
						this.plugin.info(player, Messages.HYPHEN);
						this.plugin.info(player, langUtils.getString("Exceptions.CantAddYourSelf"));
						this.plugin.info(player, Messages.HYPHEN);
						return;
					}

					this.plugin.info(player, Messages.HYPHEN);
					this.plugin.info(player, langUtils.getString("Force-Add.YourSelf").replace("%targetDisplayName", targetFriends.getDisplayName()));
					this.plugin.info(player, Messages.HYPHEN);

					this.plugin.info(target, Messages.HYPHEN);
					this.plugin.info(target, targetLangUtils.getString("Force-Add.Target").replace("%displayName", myFriends.getDisplayName()));
					this.plugin.info(target, Messages.HYPHEN);
					return;
				}

				this.plugin.info(player, langUtils.getString("Force-Add.Usage"));
				return;
			}

			if(args[0].equalsIgnoreCase("prefix")) {
				if(!player.hasPermission(Permissions.ADMIN)) {
					this.plugin.info(player, Messages.NO_PERMISSION);
					return;
				}

				if(args.length > 1) {
					UUID target = this.plugin.getPlayerUniqueId(args[1]);
					if(target == null) {
						this.plugin.info(player, Messages.HYPHEN);
						this.plugin.info(player, langUtils.getString("Cant-Find").replace("%name", args[1]));
						this.plugin.info(player, Messages.HYPHEN);
						return;
					}

					FriendUtils targetFriends = this.plugin.getFriendManager().getPlayer(target);

					if(args.length > 2) {
						String prefix = "";
						for(int i = 2; i < args.length; i++) {
							prefix = prefix + args[i] + " ";
						}

						if(!prefix.endsWith(" ")) {
							prefix += " ";
						}

						targetFriends.setPrefix(prefix);
						this.plugin.info(player, Messages.HYPHEN);
						this.plugin.info(player, langUtils.getString("Prefix.To").replace("%targetDisplayName", targetFriends.getDisplayName()).replace("%prefix", ChatColor.translateAlternateColorCodes('&', prefix).substring(0, prefix.length() - 1)));
						this.plugin.info(player, Messages.HYPHEN);
						return;
					}

					this.plugin.info(player, Messages.HYPHEN);
					this.plugin.info(player, langUtils.getString("Prefix.Current").replace("%targetDisplayName", targetFriends.getDisplayName()).replace("%prefix", targetFriends.getPrefix().substring(0, targetFriends.getPrefix().length() - 1)));
					this.plugin.info(player, Messages.HYPHEN);
					return;
				}

				this.plugin.info(player, langUtils.getString("Prefix.Usage"));
				return;
			}

			if(args[0].equalsIgnoreCase("help")) {
				this.printHelp(player, langUtils);
				return;
			}

			this.add(player, myFriends, langUtils, args[0]);
			return;
		}
		this.printHelp(player, langUtils);
	}

	public void printHelp(ProxiedPlayer player, LanguageUtils langUtils) {
		this.plugin.info(player, Messages.HYPHEN);
		this.plugin.info(player, langUtils.getString("Help.Command"));
		this.plugin.info(player, langUtils.getString("Help.Help"));
		this.plugin.info(player, langUtils.getString("Help.Lang"));
		this.plugin.info(player, langUtils.getString("Help.Add"));
		this.plugin.info(player, langUtils.getString("Help.Remove"));
		this.plugin.info(player, langUtils.getString("Help.Accept"));
		this.plugin.info(player, langUtils.getString("Help.Deny"));
		this.plugin.info(player, langUtils.getString("Help.List"));
		this.plugin.info(player, langUtils.getString("Help.Ignore"));
		if(player.hasPermission(Permissions.ADMIN)) {
			this.plugin.info(player, Messages.HYPHEN);
			this.plugin.info(player, langUtils.getString("Help.Force-Add"));
			this.plugin.info(player, langUtils.getString("Help.Prefix"));
		}
		this.plugin.info(player, Messages.HYPHEN);
	}

	public void add(ProxiedPlayer player, FriendUtils myFriends, LanguageUtils langUtils, String name) {
		UUID target = this.plugin.getPlayerUniqueId(name);
		if(target == null) {
			this.plugin.info(player, Messages.HYPHEN);
			this.plugin.info(player, langUtils.getString("Cant-Find").replace("%name", name));
			this.plugin.info(player, Messages.HYPHEN);
			return;
		}
		FriendUtils targetFriends = this.plugin.getFriendManager().getPlayer(target);
		LanguageUtils targetLangUtils = this.plugin.getLanguageManager().getPlayer(target);

		try {
			myFriends.addRequest(target);
		} catch (FailedAddingException e) {
			this.plugin.info(player, Messages.HYPHEN);
			this.plugin.info(player, langUtils.getString("Exceptions.AlreadySent"));
			this.plugin.info(player, Messages.HYPHEN);
			return;
		} catch (AlreadyAddedException e) {
			this.plugin.info(player, Messages.HYPHEN);
			this.plugin.info(player, langUtils.getString("Exceptions.AlreadyFriend"));
			this.plugin.info(player, Messages.HYPHEN);
			return;
		} catch (SelfException e) {
			this.plugin.info(player, Messages.HYPHEN);
			this.plugin.info(player, langUtils.getString("Exceptions.CantAddYourSelf"));
			this.plugin.info(player, Messages.HYPHEN);
			return;
		} catch (IgnoredException e) {
			this.plugin.info(player, Messages.HYPHEN);
			this.plugin.info(player, langUtils.getString("Exceptions.Ignored"));
			this.plugin.info(player, Messages.HYPHEN);
			return;
		}

		this.plugin.info(player, Messages.HYPHEN);
		this.plugin.info(player, langUtils.getString("Add.Sent").replace("%targetDisplayName", targetFriends.getDisplayName()));
		this.plugin.info(player, langUtils.getString("Add.5-Minutes"));
		this.plugin.info(player, Messages.HYPHEN);

		TextComponent prefix = MessageBuilder.get(this.plugin.getPrefix());
		TextComponent grayHyphen = MessageBuilder.get("&r &8- &r", null, ChatColor.DARK_GRAY, null, false);

		TextComponent accept = MessageBuilder.get(targetLangUtils.getString("Add.Accept.Prefix"), "/friend accept " + myFriends.getName(), ChatColor.GREEN, targetLangUtils.getString("Add.Accept.Message"), true);
		TextComponent deny = MessageBuilder.get(targetLangUtils.getString("Add.Deny.Prefix"), "/friend deny " + myFriends.getName(), ChatColor.GREEN, targetLangUtils.getString("Add.Deny.Message"), true);
		TextComponent ignore = MessageBuilder.get(targetLangUtils.getString("Add.Ignore.Prefix"), "/friend ignore add " + myFriends.getName(), ChatColor.GREEN, targetLangUtils.getString("Add.Ignore.Message"), true);

		this.plugin.info(target, Messages.HYPHEN);
		this.plugin.info(target, targetLangUtils.getString("Add.Request.Received").replace("%displayName", myFriends.getDisplayName()));
		if(targetFriends.getPlayer() != null) {
			targetFriends.getPlayer().sendMessage(prefix, accept, grayHyphen, deny, grayHyphen, ignore);
		}
		this.plugin.info(target, Messages.HYPHEN);

		ThreadPool.run(new Runnable() {
			@Override
			public void run() {
				try {
					TimeUnit.MINUTES.sleep(5);
				} catch (Exception e) {
				}

				try {
					myFriends.removeRequest(target);
				} catch (NotAddedException e) {
					return;
				}

				FriendCommand.this.plugin.info(player, Messages.HYPHEN);
				FriendCommand.this.plugin.info(player, langUtils.getString("Add.Expired.YourSelf").replace("%targetDisplayName", targetFriends.getDisplayName()));
				FriendCommand.this.plugin.info(player, Messages.HYPHEN);

				FriendCommand.this.plugin.info(target, Messages.HYPHEN);
				FriendCommand.this.plugin.info(target, langUtils.getString("Add.Expired.Target").replace("%displayName", myFriends.getDisplayName()));
				FriendCommand.this.plugin.info(target, Messages.HYPHEN);
			}
		});
	}

}
