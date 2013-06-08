/*
 * PermissionsEx - Permissions plugin for Bukkit
 * Copyright (C) 2011 t3hk0d3 http://www.tehkode.ru
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package ru.tehkode.permissions.bukkit.commands;

import java.util.Map;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import ru.tehkode.permissions.PermissionGroup;
import ru.tehkode.permissions.PermissionUser;
import ru.tehkode.permissions.bukkit.PermissionsEx;
import ru.tehkode.permissions.commands.Command;
import ru.tehkode.permissions.exceptions.RankingException;

public class PromotionCommands extends PermissionsCommand {

	@Command(name = "pex",
			syntax = "group <group> rank [rank] [ladder]",
			description = "获取或设置 <组> [等级] [阶级]",
			isPrimary = true,
			permission = "permissions.groups.rank.<group>")
	public void rankGroup(Plugin plugin, CommandSender sender, Map<String, String> args) {
		String groupName = this.autoCompleteGroupName(args.get("group"));

		PermissionGroup group = PermissionsEx.getPermissionManager().getGroup(groupName);

		if (group == null) {
			sender.sendMessage(ChatColor.RED + "组 \"" + groupName + "\" 不存在");
			return;
		}

		if (args.get("rank") != null) {
			String newRank = args.get("rank").trim();

			try {
				group.setRank(Integer.parseInt(newRank));
			} catch (NumberFormatException e) {
				sender.sendMessage("无效的等级, 必须是一个数组.");
			}

			if (args.containsKey("ladder")) {
				group.setRankLadder(args.get("ladder"));
			}
		}

		int rank = group.getRank();

		if (rank > 0) {
			sender.sendMessage("组 " + group.getName() + " 的等级是 " + rank + " (阶级 = " + group.getRankLadder() + ")");
		} else {
			sender.sendMessage("组 " + group.getName() + " 没有等级");
		}
	}

	@Command(name = "pex",
			syntax = "promote <user> [ladder]",
			description = "提升 <用户> 到下一个组 于 [阶级] 上",
			isPrimary = true)
	public void promoteUser(Plugin plugin, CommandSender sender, Map<String, String> args) {
		String userName = this.autoCompletePlayerName(args.get("user"));
		PermissionUser user = PermissionsEx.getPermissionManager().getUser(userName);

		if (user == null) {
			sender.sendMessage("指定的玩家 \"" + args.get("user") + "\" 不存在!");
			return;
		}

		String promoterName = "console";
		String ladder = "default";

		if (args.containsKey("ladder")) {
			ladder = args.get("ladder");
		}

		PermissionUser promoter = null;
		if (sender instanceof Player) {
			promoter = PermissionsEx.getPermissionManager().getUser(((Player) sender).getName());
			if (promoter == null || !promoter.has("permissions.user.promote." + ladder, ((Player) sender).getWorld().getName())) {
				sender.sendMessage(ChatColor.RED + "你没有足够的权限去提升阶级组");
				return;
			}

			promoterName = promoter.getName();
		}

		try {
			PermissionGroup targetGroup = user.promote(promoter, ladder);

			this.informPlayer(plugin, user.getName(), "你已经被提升为 " + targetGroup.getRankLadder() + " 级阶级 " + targetGroup.getName() + " 组");
			sender.sendMessage("用户 " + user.getName() + " 已被提升到 " + targetGroup.getName() + " 组");
			Logger.getLogger("Minecraft").info("用户 " + user.getName() + " 已被提升到 " + targetGroup.getName() + " 组在 " + targetGroup.getRankLadder() + " 阶级 " + promoterName);
		} catch (RankingException e) {
			sender.sendMessage(ChatColor.RED + "提升错误: " + e.getMessage());
			Logger.getLogger("Minecraft").severe("Ranking Error (" + promoterName + " > " + e.getTarget().getName() + "): " + e.getMessage());
		}
	}

	@Command(name = "pex",
			syntax = "demote <user> [ladder]",
			description = "降低 <用户> 到上一个组于 [阶级]",
			isPrimary = true)
	public void demoteUser(Plugin plugin, CommandSender sender, Map<String, String> args) {
		String userName = this.autoCompletePlayerName(args.get("user"));
		PermissionUser user = PermissionsEx.getPermissionManager().getUser(userName);

		if (user == null) {
			sender.sendMessage(ChatColor.RED + "指定的用户 \"" + args.get("user") + "\" 不存在!");
			return;
		}

		String demoterName = "console";
		String ladder = "default";

		if (args.containsKey("ladder")) {
			ladder = args.get("ladder");
		}

		PermissionUser demoter = null;
		if (sender instanceof Player) {
			demoter = PermissionsEx.getPermissionManager().getUser(((Player) sender).getName());

			if (demoter == null || !demoter.has("permissions.user.demote." + ladder, ((Player) sender).getWorld().getName())) {
				sender.sendMessage(ChatColor.RED + "你没有足够的权限来降低阶级组");
				return;
			}

			demoterName = demoter.getName();
		}

		try {
			PermissionGroup targetGroup = user.demote(demoter, args.get("ladder"));

			this.informPlayer(plugin, user.getName(), "你已被降低到 " + targetGroup.getRankLadder() + " 阶级 " + targetGroup.getName() + " 组");
			sender.sendMessage("用户 " + user.getName() + " 已被降低到 " + targetGroup.getName() + " 组");
			Logger.getLogger("Minecraft").info("用户 " + user.getName() + " 已被 " + demoterName + " 降低到 " + targetGroup.getName() + " 组于 " + targetGroup.getRankLadder() + " 阶级 ");
		} catch (RankingException e) {
			sender.sendMessage(ChatColor.RED + "降级出错: " + e.getMessage());
			Logger.getLogger("Minecraft").severe("Ranking Error (" + demoterName + " demotes " + e.getTarget().getName() + "): " + e.getMessage());
		}
	}

	@Command(name = "promote",
			syntax = "<user>",
			description = "提升 <用户> 到下一个等级",
			isPrimary = true,
			permission = "permissions.user.rank.promote")
	public void promoteUserAlias(Plugin plugin, CommandSender sender, Map<String, String> args) {
		this.promoteUser(plugin, sender, args);
	}

	@Command(name = "demote",
			syntax = "<user>",
			description = "降低 <用户> 到上一个等级",
			isPrimary = true,
			permission = "permissions.user.rank.demote")
	public void demoteUserAlias(Plugin plugin, CommandSender sender, Map<String, String> args) {
		this.demoteUser(plugin, sender, args);
	}
}
