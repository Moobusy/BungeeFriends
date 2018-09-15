package net.simplyrin.bungeefriends.commands;

import java.util.UUID;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.simplyrin.bungeefriends.Main;
import net.simplyrin.bungeefriends.messages.Messages;
import net.simplyrin.bungeefriends.messages.Permissions;
import net.simplyrin.bungeefriends.utils.FriendManager.FriendUtils;
import net.simplyrin.bungeefriends.utils.LanguageManager.LanguageUtils;

/**
 * Created by SimplyRin on 2018/09/14.
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
public class TellCommand extends Command {

	private Main plugin;

	public TellCommand(Main plugin) {
		super("tell", null, "msg", "message");
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

		if(args.length > 0) {
			UUID target = this.plugin.getPlayerUniqueId(args[0]);
			if(target == null) {
				this.plugin.info(player, Messages.HYPHEN);
				this.plugin.info(player, langUtils.getString("Cant-Find").replace("%name", args[0]));
				this.plugin.info(player, Messages.HYPHEN);
				return;
			}
			FriendUtils targetFriends = this.plugin.getFriendManager().getPlayer(target);
			LanguageUtils targetLangUtils = this.plugin.getLanguageManager().getPlayer(target);

			if(!(myFriends.isFriend(targetFriends.getUniqueId()) || player.hasPermission(Permissions.ADMIN))) {
				this.plugin.info(player, Messages.HYPHEN);
				this.plugin.info(player, langUtils.getString("Tell-Command.MustBeFriends").replace("%targetDisplayName", targetFriends.getDisplayName()));
				this.plugin.info(player, Messages.HYPHEN);
				return;
			}

			if(targetFriends.getPlayer() == null) {
				this.plugin.info(player, Messages.HYPHEN);
				this.plugin.info(player, langUtils.getString("Tell-Command.Offline").replace("%targetDisplayName", targetFriends.getDisplayName()));
				this.plugin.info(player, Messages.HYPHEN);
				return;
			}

			if(args.length > 1) {
				String message = "";
				for(int i = 1; i < args.length; i++) {
					message = message + args[i] + " ";
				}

				this.plugin.info(player, langUtils.getString("Tell-Command.YourSelf").replace("%targetDisplayName", targetFriends.getDisplayName()).replace("%message", message));
				this.plugin.info(targetFriends.getPlayer(), targetLangUtils.getString("Tell-Command.Target").replace("%displayName", myFriends.getDisplayName()).replace("%message", message));

				this.plugin.getReplyTargetMap().put(targetFriends.getUniqueId(), player.getUniqueId());
				return;
			}
		}

		this.plugin.info(player, langUtils.getString("Tell-Command.Usage"));
	}

}
