package net.simplyrin.bungeefriends.commands.alias;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.simplyrin.bungeefriends.Main;
import net.simplyrin.bungeefriends.messages.Messages;
import net.simplyrin.bungeefriends.utils.FriendManager.FriendUtils;
import net.simplyrin.bungeefriends.utils.LanguageManager.LanguageUtils;

/**
 * Created by SimplyRin on 2018/09/16.
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
public class FLCommand extends Command {

	private Main plugin;

	public FLCommand(Main plugin) {
		super("fl");
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

		List<String> list = myFriends.getFriends();

		if(list.size() == 0) {
			this.plugin.info(player, langUtils.getString(Messages.HYPHEN));
			this.plugin.info(player, langUtils.getString("List.DontHave.One"));
			this.plugin.info(player, langUtils.getString("List.DontHave.Two"));
			this.plugin.info(player, langUtils.getString(Messages.HYPHEN));
			return;
		}

		List<String> online = new ArrayList<>();
		List<String> offline = new ArrayList<>();

		for(String uuid : list) {
			ProxiedPlayer target = this.plugin.getProxy().getPlayer(UUID.fromString(uuid));
			FriendUtils targetFriends = this.plugin.getFriendManager().getPlayer(UUID.fromString(uuid));

			if(target != null) {
				online.add(langUtils.getString("List.Online").replace("%targetDisplayName", targetFriends.getDisplayName()).replace("%server", target.getServer().getInfo().getName()));
			} else {
				offline.add(langUtils.getString("List.Offline").replace("%targetDisplayName", targetFriends.getDisplayName()));
			}
		}

		this.plugin.info(player, langUtils.getString(Messages.HYPHEN));
		for(String message : online) {
			this.plugin.info(player, message);
		}
		for(String message : offline) {
			this.plugin.info(player, message);
		}
		this.plugin.info(player, langUtils.getString(Messages.HYPHEN));
	}

}
