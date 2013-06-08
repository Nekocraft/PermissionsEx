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

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import ru.tehkode.permissions.PermissionGroup;
import ru.tehkode.permissions.PermissionUser;
import ru.tehkode.permissions.bukkit.PermissionsEx;
import ru.tehkode.permissions.commands.Command;
import ru.tehkode.utils.DateUtils;
import ru.tehkode.utils.StringUtils;

public class GroupCommands extends PermissionsCommand {

	@Command(name = "pex",
			syntax = "groups list [world]",
			permission = "permissions.manage.groups.list",
			description = "列出所有注册的组")
	public void groupsList(Plugin plugin, CommandSender sender, Map<String, String> args) {
		PermissionGroup[] groups = PermissionsEx.getPermissionManager().getGroups();
		String worldName = this.autoCompleteWorldName(args.get("world"));

		sender.sendMessage(ChatColor.WHITE + "已注册的组: ");
		for (PermissionGroup group : groups) {
			String rank = "";
			if (group.isRanked()) {
				rank = " (等级: " + group.getRank() + "@" + group.getRankLadder() + ") ";
			}

			sender.sendMessage(String.format("  %s %s %s %s[%s]", group.getName(), " #" + group.getWeight(), rank, ChatColor.DARK_GREEN, StringUtils.implode(group.getParentGroupsNames(worldName), ", ")));
		}
	}

	@Command(name = "pex",
			syntax = "groups",
			permission = "permissions.manage.groups.list",
			description = "列出所有注册的组 (同义词)")
	public void groupsListAlias(Plugin plugin, CommandSender sender, Map<String, String> args) {
		this.groupsList(plugin, sender, args);
	}

	@Command(name = "pex",
			syntax = "group",
			permission = "permissions.manage.groups.list",
			description = "列出所有注册的组 (同义词)")
	public void groupsListAnotherAlias(Plugin plugin, CommandSender sender, Map<String, String> args) {
		this.groupsList(plugin, sender, args);
	}

	@Command(name = "pex",
			syntax = "group <group> weight [weight]",
			permission = "permissions.manage.groups.weight.<group>",
			description = "获取或设置组的权重")
	public void groupPrintSetWeight(Plugin plugin, CommandSender sender, Map<String, String> args) {
		String groupName = this.autoCompleteGroupName(args.get("group"));

		PermissionGroup group = PermissionsEx.getPermissionManager().getGroup(args.get("group"));

		if (group == null) {
			sender.sendMessage(ChatColor.RED + "组不存在");
			return;
		}

		if (args.containsKey("weight")) {
			try {
				group.setWeight(Integer.parseInt(args.get("weight")));
			} catch (NumberFormatException e) {
				sender.sendMessage("错误! 权重必须是一个整数.");
				return;
			}
		}

		sender.sendMessage("Group " + group.getName() + " have " + group.getWeight() + " calories.");
	}

	@Command(name = "pex",
			syntax = "group <group> toggle debug",
			permission = "permissions.manage.groups.debug.<group>",
			description = "切换单独调试模式对 <组>")
	public void groupToggleDebug(Plugin plugin, CommandSender sender, Map<String, String> args) {
		String groupName = this.autoCompleteGroupName(args.get("group"));

		PermissionGroup group = PermissionsEx.getPermissionManager().getGroup(args.get("group"));

		if (group == null) {
			sender.sendMessage(ChatColor.RED + "组不存在");
			return;
		}

		group.setDebug(!group.isDebug());

		sender.sendMessage("Debug mode for group " + group.getName() + " have been " + (group.isDebug() ? "enabled" : "disabled") + "!");
	}

	@Command(name = "pex",
			syntax = "group <group> prefix [newprefix] [world]",
			permission = "permissions.manage.groups.prefix.<group>",
			description = "获取或设置 <组> 的前缀.")
	public void groupPrefix(Plugin plugin, CommandSender sender, Map<String, String> args) {
		String groupName = this.autoCompleteGroupName(args.get("group"));
		String worldName = this.autoCompleteWorldName(args.get("world"));

		PermissionGroup group = PermissionsEx.getPermissionManager().getGroup(args.get("group"));

		if (group == null) {
			sender.sendMessage(ChatColor.RED + "组不存在");
			return;
		}

		if (args.containsKey("newprefix")) {
			group.setPrefix(args.get("newprefix"), worldName);
		}

		sender.sendMessage(group.getName() + "的前缀: " + group.getPrefix(worldName) + "\"");
	}

	@Command(name = "pex",
			syntax = "group <group> suffix [newsuffix] [world]",
			permission = "permissions.manage.groups.suffix.<group>",
			description = "获取或设置 <group> 的后缀")
	public void groupSuffix(Plugin plugin, CommandSender sender, Map<String, String> args) {
		String groupName = this.autoCompleteGroupName(args.get("group"));
		String worldName = this.autoCompleteWorldName(args.get("world"));

		PermissionGroup group = PermissionsEx.getPermissionManager().getGroup(args.get("group"));

		if (group == null) {
			sender.sendMessage(ChatColor.RED + "组不存在");
			return;
		}

		if (args.containsKey("newsuffix")) {
			group.setSuffix(args.get("newsuffix"), worldName);
		}

		sender.sendMessage(group.getName() + "的后缀: " + group.getSuffix(worldName) + "\"");
	}

	@Command(name = "pex",
			syntax = "group <group> create [parents]",
			permission = "permissions.manage.groups.create.<group>",
			description = "创建 <组> 和/或 设置 [父级]")
	public void groupCreate(Plugin plugin, CommandSender sender, Map<String, String> args) {
		PermissionGroup group = PermissionsEx.getPermissionManager().getGroup(args.get("group"));

		if (group == null) {
			sender.sendMessage(ChatColor.RED + "组不存在");
			return;
		}

		if (!group.isVirtual()) {
			sender.sendMessage(ChatColor.RED + "组 " + args.get("group") + " 已经存在");
			return;
		}

		if (args.get("parents") != null) {
			String[] parents = args.get("parents").split(",");
			List<PermissionGroup> groups = new LinkedList<PermissionGroup>();

			for (String parent : parents) {
				groups.add(PermissionsEx.getPermissionManager().getGroup(parent));
			}

			group.setParentGroups(groups.toArray(new PermissionGroup[0]), null);
		}

		sender.sendMessage(ChatColor.WHITE + "组 " + group.getName() + " 已创建!");

		group.save();
	}

	@Command(name = "pex",
			syntax = "group <group> delete",
			permission = "permissions.manage.groups.remove.<group>",
			description = "删除 <组>")
	public void groupDelete(Plugin plugin, CommandSender sender, Map<String, String> args) {
		String groupName = this.autoCompleteGroupName(args.get("group"));

		PermissionGroup group = PermissionsEx.getPermissionManager().getGroup(groupName);

		if (group == null) {
			sender.sendMessage(ChatColor.RED + "组不存在");
			return;
		}

		sender.sendMessage(ChatColor.WHITE + "组 " + group.getName() + " 已删除!");

		group.remove();
		PermissionsEx.getPermissionManager().resetGroup(group.getName());
		group = null;
	}

	/**
	 * Group inheritance
	 */
	@Command(name = "pex",
			syntax = "group <group> parents [world]",
			permission = "permissions.manage.groups.inheritance.<group>",
			description = "列出 <的> 父级 (同义词)")
	public void groupListParentsAlias(Plugin plugin, CommandSender sender, Map<String, String> args) {
		this.groupListParents(plugin, sender, args);
	}

	@Command(name = "pex",
			syntax = "group <group> parents list [world]",
			permission = "permissions.manage.groups.inheritance.<group>",
			description = "列出 <的> 父级")
	public void groupListParents(Plugin plugin, CommandSender sender, Map<String, String> args) {
		String groupName = this.autoCompleteGroupName(args.get("group"));
		String worldName = this.autoCompleteWorldName(args.get("world"));

		PermissionGroup group = PermissionsEx.getPermissionManager().getGroup(groupName);

		if (group == null) {
			sender.sendMessage(ChatColor.RED + "组不存在");
			return;
		}

		if (group.getParentGroups(worldName).length == 0) {
			sender.sendMessage(ChatColor.RED + "组 " + group.getName() + " 没有父级");
			return;
		}

		sender.sendMessage("组的 " + group.getName() + " 父级:");

		for (PermissionGroup parent : group.getParentGroups(worldName)) {
			sender.sendMessage("  " + parent.getName());
		}

	}

	@Command(name = "pex",
			syntax = "group <group> parents set <parents> [world]",
			permission = "permissions.manage.groups.inheritance.<group>",
			description = "设置 <组> 的父级 (单个或逗号间隔的列表)")
	public void groupSetParents(Plugin plugin, CommandSender sender, Map<String, String> args) {
		String groupName = this.autoCompleteGroupName(args.get("group"));
		String worldName = this.autoCompleteWorldName(args.get("world"));

		PermissionGroup group = PermissionsEx.getPermissionManager().getGroup(groupName);

		if (group == null) {
			sender.sendMessage(ChatColor.RED + "组不存在");
			return;
		}

		if (args.get("parents") != null) {
			String[] parents = args.get("parents").split(",");
			List<PermissionGroup> groups = new LinkedList<PermissionGroup>();

			for (String parent : parents) {
				PermissionGroup parentGroup = PermissionsEx.getPermissionManager().getGroup(this.autoCompleteGroupName(parent));

				if (parentGroup != null && !groups.contains(parentGroup)) {
					groups.add(parentGroup);
				}
			}

			group.setParentGroups(groups.toArray(new PermissionGroup[0]), worldName);

			sender.sendMessage(ChatColor.WHITE + "组 " + group.getName() + " 的继承更新!");

			group.save();
		}
	}

	@Command(name = "pex",
			syntax = "group <group> parents add <parents> [world]",
			permission = "permissions.manage.groups.inheritance.<group>",
			description = "添加 <组> 的父级 (单个或逗号间隔的列表)")
	public void groupAddParents(Plugin plugin, CommandSender sender, Map<String, String> args) {
		String groupName = this.autoCompleteGroupName(args.get("group"));
		String worldName = this.autoCompleteWorldName(args.get("world"));

		PermissionGroup group = PermissionsEx.getPermissionManager().getGroup(groupName);

		if (group == null) {
			sender.sendMessage(ChatColor.RED + "组不存在");
			return;
		}

		if (args.get("parents") != null) {
			String[] parents = args.get("parents").split(",");
			List<PermissionGroup> groups = new LinkedList<PermissionGroup>(Arrays.asList(group.getParentGroups(worldName)));

			for (String parent : parents) {
				PermissionGroup parentGroup = PermissionsEx.getPermissionManager().getGroup(this.autoCompleteGroupName(parent));

				if (parentGroup != null && !groups.contains(parentGroup)) {
					groups.add(parentGroup);
				}
			}

			group.setParentGroups(groups.toArray(new PermissionGroup[0]), worldName);

			sender.sendMessage(ChatColor.WHITE + "组 " + group.getName() + " 的继承已更新!");

			group.save();
		}
	}

	@Command(name = "pex",
			syntax = "group <group> parents remove <parents> [world]",
			permission = "permissions.manage.groups.inheritance.<group>",
			description = "删除 <组> 的父级 (单个或逗号间隔的列表)")
	public void groupRemoveParents(Plugin plugin, CommandSender sender, Map<String, String> args) {
		String groupName = this.autoCompleteGroupName(args.get("group"));
		String worldName = this.autoCompleteWorldName(args.get("world"));

		PermissionGroup group = PermissionsEx.getPermissionManager().getGroup(groupName);

		if (group == null) {
			sender.sendMessage(ChatColor.RED + "组不存在");
			return;
		}

		if (args.get("parents") != null) {
			String[] parents = args.get("parents").split(",");
			List<PermissionGroup> groups = new LinkedList<PermissionGroup>(Arrays.asList(group.getParentGroups(worldName)));

			for (String parent : parents) {
				PermissionGroup parentGroup = PermissionsEx.getPermissionManager().getGroup(this.autoCompleteGroupName(parent));

				groups.remove(parentGroup);
			}

			group.setParentGroups(groups.toArray(new PermissionGroup[groups.size()]), worldName);

			sender.sendMessage(ChatColor.WHITE + "组 " + group.getName() + " 的继承已更新!");

			group.save();
		}
	}

	/**
	 * Group permissions
	 */
	@Command(name = "pex",
			syntax = "group <group>",
			permission = "permissions.manage.groups.permissions.<group>",
			description = "列出 <组> 的所有权限 (同义词)")
	public void groupListAliasPermissions(Plugin plugin, CommandSender sender, Map<String, String> args) {
		this.groupListPermissions(plugin, sender, args);
	}

	@Command(name = "pex",
			syntax = "group <group> list [world]",
			permission = "permissions.manage.groups.permissions.<group>",
			description = "列出 <组> 的所有权限于 [世界]")
	public void groupListPermissions(Plugin plugin, CommandSender sender, Map<String, String> args) {
		String groupName = this.autoCompleteGroupName(args.get("group"));
		String worldName = this.autoCompleteWorldName(args.get("world"));

		PermissionGroup group = PermissionsEx.getPermissionManager().getGroup(groupName);

		if (group == null) {
			sender.sendMessage(ChatColor.RED + "组不存在");
			return;
		}

		sender.sendMessage("'" + groupName + "' 继承于:");
		printEntityInheritance(sender, group.getParentGroups());

		for (String world : group.getAllParentGroups().keySet()) {
			if (world == null) {
				continue;
			}

			sender.sendMessage("  @" + world + ":");
			printEntityInheritance(sender, group.getAllParentGroups().get(world));
		}

		sender.sendMessage("组 " + group.getName() + "的权限:");
		this.sendMessage(sender, this.mapPermissions(worldName, group, 0));

		sender.sendMessage("组 " + group.getName() + "的选项: ");
		for (Map.Entry<String, String> option : group.getOptions(worldName).entrySet()) {
			sender.sendMessage("  " + option.getKey() + " = \"" + option.getValue() + "\"");
		}
	}

	@Command(name = "pex",
			syntax = "group <group> add <permission> [world]",
			permission = "permissions.manage.groups.permissions.<group>",
			description = "添加 <权限> 给 <组> 与 [世界]")
	public void groupAddPermission(Plugin plugin, CommandSender sender, Map<String, String> args) {
		String groupName = this.autoCompleteGroupName(args.get("group"));
		String worldName = this.autoCompleteWorldName(args.get("world"));

		PermissionGroup group = PermissionsEx.getPermissionManager().getGroup(groupName);

		if (group == null) {
			sender.sendMessage(ChatColor.RED + "组不存在");
			return;
		}

		group.addPermission(args.get("permission"), worldName);

		sender.sendMessage(ChatColor.WHITE + "已添加权限 \"" + args.get("permission") + "\" 给 " + group.getName() + " !");

		this.informGroup(plugin, group, "你的权限已被更改");
	}

	@Command(name = "pex",
			syntax = "group <group> set <option> <value> [world]",
			permission = "permissions.manage.groups.permissions.<group>",
			description = "设置 <选项> 为 <值> 对 <group> 于 [世界]")
	public void groupSetOption(Plugin plugin, CommandSender sender, Map<String, String> args) {
		String groupName = this.autoCompleteGroupName(args.get("group"));
		String worldName = this.autoCompleteWorldName(args.get("world"));

		PermissionGroup group = PermissionsEx.getPermissionManager().getGroup(groupName);

		if (group == null) {
			sender.sendMessage(ChatColor.RED + "组不存在");
			return;
		}

		group.setOption(args.get("option"), args.get("value"), worldName);

		if (args.containsKey("value") && args.get("value").isEmpty()) {
			sender.sendMessage(ChatColor.WHITE + "选项 \"" + args.get("option") + "\" 已清除!");
		} else {
			sender.sendMessage(ChatColor.WHITE + "选项 \"" + args.get("option") + "\" 已设置!");
		}

		this.informGroup(plugin, group, "Your permissions has been changed");
	}

	@Command(name = "pex",
			syntax = "group <group> remove <permission> [world]",
			permission = "permissions.manage.groups.permissions.<group>",
			description = "删除 <权限> 从 <组> 于 [世界]")
	public void groupRemovePermission(Plugin plugin, CommandSender sender, Map<String, String> args) {
		String groupName = this.autoCompleteGroupName(args.get("group"));
		String worldName = this.autoCompleteWorldName(args.get("world"));

		PermissionGroup group = PermissionsEx.getPermissionManager().getGroup(groupName);

		if (group == null) {
			sender.sendMessage(ChatColor.RED + "组不存在");
			return;
		}

		String permission = this.autoCompletePermission(group, args.get("permission"), worldName);

		group.removePermission(permission, worldName);
		group.removeTimedPermission(permission, worldName);

		sender.sendMessage(ChatColor.WHITE + "已移除权限 \"" + permission + "\" 从 " + group.getName() + " !");

		this.informGroup(plugin, group, "你的权限已更改");
	}

	@Command(name = "pex",
			syntax = "group <group> swap <permission> <targetPermission> [world]",
			permission = "permissions.manage.groups.permissions.<group>",
			description = "交换权限列表中的 <权限> 和 <目标权限> . 可以是数字或者权限本身")
	public void userSwapPermission(Plugin plugin, CommandSender sender, Map<String, String> args) {
		String groupName = this.autoCompleteGroupName(args.get("group"));
		String worldName = this.autoCompleteWorldName(args.get("world"));

		PermissionGroup group = PermissionsEx.getPermissionManager().getGroup(groupName);

		if (group == null) {
			sender.sendMessage(ChatColor.RED + "组不存在");
			return;
		}


		String[] permissions = group.getOwnPermissions(worldName);

		try {
			int sourceIndex = this.getPosition(this.autoCompletePermission(group, args.get("permission"), worldName, "permission"), permissions);
			int targetIndex = this.getPosition(this.autoCompletePermission(group, args.get("targetPermission"), worldName, "targetPermission"), permissions);

			String targetPermission = permissions[targetIndex];

			permissions[targetIndex] = permissions[sourceIndex];
			permissions[sourceIndex] = targetPermission;

			group.setPermissions(permissions, worldName);

			sender.sendMessage("权限已交换!");
		} catch (Throwable e) {
			sender.sendMessage(ChatColor.RED + "Error: " + e.getMessage());
		}
	}

	@Command(name = "pex",
			syntax = "group <group> timed add <permission> [lifetime] [world]",
			permission = "permissions.manage.groups.permissions.timed.<group>",
			description = "添加限时 <权限> 给 <组> 存活 [存活时间] 与 [世界]")
	public void groupAddTimedPermission(Plugin plugin, CommandSender sender, Map<String, String> args) {
		String groupName = this.autoCompleteGroupName(args.get("group"));
		String worldName = this.autoCompleteWorldName(args.get("world"));

		int lifetime = 0;

		if (args.containsKey("lifetime")) {
			lifetime = DateUtils.parseInterval(args.get("lifetime"));
		}

		PermissionGroup group = PermissionsEx.getPermissionManager().getGroup(groupName);

		if (group == null) {
			sender.sendMessage(ChatColor.RED + "组不存在");
			return;
		}

		group.addTimedPermission(args.get("permission"), worldName, lifetime);

		sender.sendMessage(ChatColor.WHITE + "Timed permission added!");
		this.informGroup(plugin, group, "你的权限已更改!");

		logger.info("组 " + groupName + " 获得了限时权限 \"" + args.get("permission") + "\" "
				+ (lifetime > 0 ? "持续 " + lifetime + " 秒 " : " ") + "from " + getSenderName(sender));
	}

	@Command(name = "pex",
			syntax = "group <group> timed remove <permission> [world]",
			permission = "permissions.manage.groups.permissions.timed.<group>",
			description = "删除限时权限 <权限> 对 <组> 与 [世界]")
	public void groupRemoveTimedPermission(Plugin plugin, CommandSender sender, Map<String, String> args) {
		String groupName = this.autoCompleteGroupName(args.get("group"));
		String worldName = this.autoCompleteWorldName(args.get("world"));

		PermissionGroup group = PermissionsEx.getPermissionManager().getGroup(groupName);

		if (group == null) {
			sender.sendMessage(ChatColor.RED + "组不存在");
			return;
		}

		group.removeTimedPermission(args.get("permission"), worldName);

		sender.sendMessage(ChatColor.WHITE + "限时权限 \"" + args.get("permission") + "\" 已删除!");
		this.informGroup(plugin, group, "你的权限已更改!");
	}

	/**
	 * Group users management
	 */
	@Command(name = "pex",
			syntax = "group <group> users",
			permission = "permissions.manage.membership.<group>",
			description = "列出 <组> 中的所有用户")
	public void groupUsersList(Plugin plugin, CommandSender sender, Map<String, String> args) {
		String groupName = this.autoCompleteGroupName(args.get("group"));

		PermissionUser[] users = PermissionsEx.getPermissionManager().getUsers(groupName);

		if (users == null || users.length == 0) {
			sender.sendMessage(ChatColor.RED + "组不存在或为空");
		}

		sender.sendMessage("组 " + groupName + " 的用户:");

		for (PermissionUser user : users) {
			sender.sendMessage("   " + user.getName());
		}
	}

	@Command(name = "pex",
			syntax = "group <group> user add <user> [world]",
			permission = "permissions.manage.membership.<group>",
			description = "添加 <用户> 到组(单个或逗号分隔的)")
	public void groupUsersAdd(Plugin plugin, CommandSender sender, Map<String, String> args) {
		String groupName = this.autoCompleteGroupName(args.get("group"));
		String worldName = this.autoCompleteWorldName(args.get("world"));

		String users[];

		if (!args.get("user").contains(",")) {
			users = new String[]{args.get("user")};
		} else {
			users = args.get("user").split(",");
		}

		for (String userName : users) {
			userName = this.autoCompletePlayerName(userName);
			PermissionUser user = PermissionsEx.getPermissionManager().getUser(userName);

			if (user == null) {
				sender.sendMessage(ChatColor.RED + "用户不存在");
				return;
			}

			user.addGroup(groupName, worldName);

			sender.sendMessage(ChatColor.WHITE + "用户 " + user.getName() + " 已被添加到组 " + groupName + " !");
			this.informPlayer(plugin, userName, "你加入了 \"" + groupName + "\" 组");
		}
	}

	@Command(name = "pex",
			syntax = "group <group> user remove <user> [world]",
			permission = "permissions.manage.membership.<group>",
			description = "从组中删除 <用户> (单个或逗号分隔的)")
	public void groupUsersRemove(Plugin plugin, CommandSender sender, Map<String, String> args) {
		String groupName = this.autoCompleteGroupName(args.get("group"));
		String worldName = this.autoCompleteWorldName(args.get("world"));

		String users[];

		if (!args.get("user").contains(",")) {
			users = new String[]{args.get("user")};
		} else {
			users = args.get("user").split(",");
		}

		for (String userName : users) {
			userName = this.autoCompletePlayerName(userName);
			PermissionUser user = PermissionsEx.getPermissionManager().getUser(userName);

			if (user == null) {
				sender.sendMessage(ChatColor.RED + "用户不存在");
				return;
			}

			user.removeGroup(groupName, worldName);

			sender.sendMessage(ChatColor.WHITE + "用户 " + user.getName() + " 已从组 " + args.get("group") + "中删除 !");
			this.informPlayer(plugin, userName, "你已从 \"" + groupName + "\" 组中被删除");

		}
	}

	@Command(name = "pex",
			syntax = "default group [world]",
			permission = "permissions.manage.groups.inheritance",
			description = "获取指定 [世界] 中的默认组")
	public void groupDefaultCheck(Plugin plugin, CommandSender sender, Map<String, String> args) {
		String worldName = this.autoCompleteWorldName(args.get("world"));


		PermissionGroup defaultGroup = PermissionsEx.getPermissionManager().getDefaultGroup(worldName);
		sender.sendMessage("默认组在 " + worldName + " 世界中是 " + defaultGroup.getName() + " 组");
	}

	@Command(name = "pex",
			syntax = "set default group <group> [world]",
			permission = "permissions.manage.groups.inheritance",
			description = "设置指定世界中的默认组")
	public void groupDefaultSet(Plugin plugin, CommandSender sender, Map<String, String> args) {
		String groupName = this.autoCompleteGroupName(args.get("group"));
		String worldName = this.autoCompleteWorldName(args.get("world"));

		PermissionGroup group = PermissionsEx.getPermissionManager().getGroup(groupName);

		if (group == null || group.isVirtual()) {
			sender.sendMessage(ChatColor.RED + "指定的组不存在");
			return;
		}

		PermissionsEx.getPermissionManager().setDefaultGroup(group, worldName);
		sender.sendMessage("默认组在 " + worldName + " 世界中已被设置为 " + group.getName() + " 组");
	}
}
