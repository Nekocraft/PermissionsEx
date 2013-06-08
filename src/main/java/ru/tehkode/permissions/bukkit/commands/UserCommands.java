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

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.Plugin;
import ru.tehkode.permissions.PermissionGroup;
import ru.tehkode.permissions.PermissionManager;
import ru.tehkode.permissions.PermissionUser;
import ru.tehkode.permissions.bukkit.PermissionsEx;
import ru.tehkode.permissions.commands.Command;
import ru.tehkode.utils.DateUtils;
import ru.tehkode.utils.StringUtils;

public class UserCommands extends PermissionsCommand {

	@Command(name = "pex",
			syntax = "users list",
			permission = "permissions.manage.users",
			description = "列出所有注册的用户")
	public void usersList(Plugin plugin, CommandSender sender, Map<String, String> args) {
		PermissionUser[] users = PermissionsEx.getPermissionManager().getUsers();

		sender.sendMessage(ChatColor.WHITE + "当前注册的用户: ");
		for (PermissionUser user : users) {
			sender.sendMessage(" " + user.getName() + " " + ChatColor.DARK_GREEN + "[" + StringUtils.implode(user.getGroupsNames(), ", ") + "]");
		}
	}

	@Command(name = "pex",
			syntax = "users",
			permission = "permissions.manage.users",
			description = "列出所有注册的用户 (同义词)",
			isPrimary = true)
	public void userListAlias(Plugin plugin, CommandSender sender, Map<String, String> args) {
		this.usersList(plugin, sender, args);
	}

	@Command(name = "pex",
			syntax = "user",
			permission = "permissions.manage.users",
			description = "列出所有注册的用户 (同义词)")
	public void userListAnotherAlias(Plugin plugin, CommandSender sender, Map<String, String> args) {
		this.usersList(plugin, sender, args);
	}

	/**
	 * User permission management
	 */
	@Command(name = "pex",
			syntax = "user <user>",
			permission = "permissions.manage.users.permissions.<user>",
			description = "列出用户的权限 (list 的同义词)")
	public void userListAliasPermissions(Plugin plugin, CommandSender sender, Map<String, String> args) {
		String userName = this.autoCompletePlayerName(args.get("user"));
		String worldName = this.autoCompleteWorldName(args.get("world"));

		PermissionUser user = PermissionsEx.getPermissionManager().getUser(userName);

		if (user == null) {
			sender.sendMessage(ChatColor.RED + "用户不存在");
			return;
		}

		sender.sendMessage("'" + userName + "' 是一个成员在:");
		printEntityInheritance(sender, user.getGroups());

		for (String world : user.getAllGroups().keySet()) {
			if (world == null) {
				continue;
			}

			sender.sendMessage("  @" + world + ":");
			printEntityInheritance(sender, user.getAllGroups().get(world));
		}

		sender.sendMessage(userName + "的权限:");

		this.sendMessage(sender, this.mapPermissions(worldName, user, 0));

		sender.sendMessage(userName + "的选项:");
		for (Map.Entry<String, String> option : user.getOptions(worldName).entrySet()) {
			sender.sendMessage("  " + option.getKey() + " = \"" + option.getValue() + "\"");
		}
	}

	@Command(name = "pex",
			syntax = "user <user> list [world]",
			permission = "permissions.manage.users.permissions.<user>",
			description = "List user permissions")
	public void userListPermissions(Plugin plugin, CommandSender sender, Map<String, String> args) {
		String userName = this.autoCompletePlayerName(args.get("user"));
		String worldName = this.autoCompleteWorldName(args.get("world"));

		PermissionUser user = PermissionsEx.getPermissionManager().getUser(userName);

		if (user == null) {
			sender.sendMessage(ChatColor.RED + "用户不存在");
			return;
		}

		sender.sendMessage(userName + "的权限:");

		for (String permission : user.getPermissions(worldName)) {
			sender.sendMessage("  " + permission);
		}

	}

	@Command(name = "pex",
			syntax = "user <user> superperms",
			permission = "permissions.manage.users.permissions.<user>",
			description = "列出玩家的超级权限")
	public void userListSuperPermissions(Plugin plugin, CommandSender sender, Map<String, String> args) {
		String userName = this.autoCompletePlayerName(args.get("user"));

		Player player = Bukkit.getPlayer(userName);
		if (player == null) {
			sender.sendMessage(ChatColor.RED + "玩家不存在或不在线");
			return;
		}

		sender.sendMessage(userName + "的超级权限:");

		for (PermissionAttachmentInfo info : player.getEffectivePermissions()) {
			String pluginName = "built-in";

			if (info.getAttachment() != null && info.getAttachment().getPlugin() != null) {
				pluginName = info.getAttachment().getPlugin().getDescription().getName();
			}

			sender.sendMessage(" '" + ChatColor.GREEN + info.getPermission() + ChatColor.WHITE + "' = " + ChatColor.BLUE + info.getValue() + ChatColor.WHITE + " by " + ChatColor.DARK_GREEN + pluginName);
		}
	}

	@Command(name = "pex",
			syntax = "user <user> prefix [newprefix] [world]",
			permission = "permissions.manage.users.prefix.<user>",
			description = "获取或设置 <用户> 的前缀")
	public void userPrefix(Plugin plugin, CommandSender sender, Map<String, String> args) {
		String userName = this.autoCompletePlayerName(args.get("user"));
		String worldName = this.autoCompleteWorldName(args.get("world"));

		PermissionUser user = PermissionsEx.getPermissionManager().getUser(userName);

		if (user == null) {
			sender.sendMessage(ChatColor.RED + "用户不存在");
			return;
		}

		if (args.containsKey("newprefix")) {
			user.setPrefix(args.get("newprefix"), worldName);
		}

		sender.sendMessage(user.getName() + "的前缀是: \"" + user.getPrefix() + "\"");
	}

	@Command(name = "pex",
			syntax = "user <user> suffix [newsuffix] [world]",
			permission = "permissions.manage.users.suffix.<user>",
			description = "获取或设置 <用户> 的后缀")
	public void userSuffix(Plugin plugin, CommandSender sender, Map<String, String> args) {
		String userName = this.autoCompletePlayerName(args.get("user"));
		String worldName = this.autoCompleteWorldName(args.get("world"));

		PermissionUser user = PermissionsEx.getPermissionManager().getUser(userName);

		if (user == null) {
			sender.sendMessage(ChatColor.RED + "用户不存在");
			return;
		}

		if (args.containsKey("newsuffix")) {
			user.setSuffix(args.get("newsuffix"), worldName);
		}

		sender.sendMessage(user.getName() + "的后缀是: \"" + user.getSuffix() + "\"");
	}

	@Command(name = "pex",
			syntax = "user <user> toggle debug",
			permission = "permissions.manage.<user>",
			description = "切换单独调试模式对 <玩家>")
	public void userToggleDebug(Plugin plugin, CommandSender sender, Map<String, String> args) {
		String userName = this.autoCompletePlayerName(args.get("user"));

		PermissionUser user = PermissionsEx.getPermissionManager().getUser(userName);

		if (user == null) {
			sender.sendMessage(ChatColor.RED + "用户不存在");
			return;
		}

		user.setDebug(!user.isDebug());

		sender.sendMessage("调试模式已对 " + userName + " " + (user.isDebug() ? "开启" : "关闭") + "!");
	}

	@Command(name = "pex",
			syntax = "user <user> check <permission> [world]",
			permission = "permissions.manage.<user>",
			description = "检查 <用户> 是否拥有 <权限>")
	public void userCheckPermission(Plugin plugin, CommandSender sender, Map<String, String> args) {
		String userName = this.autoCompletePlayerName(args.get("user"));
		String worldName = this.autoCompleteWorldName(args.get("world"));

		PermissionUser user = PermissionsEx.getPermissionManager().getUser(userName);

		if (user == null) {
			sender.sendMessage(ChatColor.RED + "用户不存在");
			return;
		}

		worldName = this.getSafeWorldName(worldName, userName);

		String permission = user.getMatchingExpression(args.get("permission"), worldName);

		if (permission == null) {
			sender.sendMessage("玩家 \"" + userName + "\" 没有此权限");
		} else {
			sender.sendMessage("玩家 \"" + userName + "\" 有权限 \"" + permission + "\" = " + user.explainExpression(permission));
		}
	}

	@Command(name = "pex",
			syntax = "user <user> get <option> [world]",
			permission = "permissions.manage.<user>",
			description = "获取 <用户> 的 <选项> 值于 [世界]")
	public void userGetOption(Plugin plugin, CommandSender sender, Map<String, String> args) {
		String userName = this.autoCompletePlayerName(args.get("user"));
		String worldName = this.autoCompleteWorldName(args.get("world"));

		PermissionUser user = PermissionsEx.getPermissionManager().getUser(userName);

		if (user == null) {
			sender.sendMessage(ChatColor.RED + "用户不存在");
			return;
		}

		worldName = this.getSafeWorldName(worldName, userName);

		String value = user.getOption(args.get("option"), worldName, null);

		sender.sendMessage("用户 " + userName + " @ " + worldName + " 世界的选项 \"" + args.get("option") + "\" = \"" + value + "\"");
	}

	@Command(name = "pex",
			syntax = "user <user> delete",
			permission = "permissions.manage.users.<user>",
			description = "删除 <用户>")
	public void userDelete(Plugin plugin, CommandSender sender, Map<String, String> args) {
		String userName = this.autoCompletePlayerName(args.get("user"));
		PermissionUser user = PermissionsEx.getPermissionManager().getUser(userName);

		if (user == null) {
			sender.sendMessage(ChatColor.RED + "用户不存在");
			return;
		}

		if (user.isVirtual()) {
			sender.sendMessage(ChatColor.RED + "用户是虚拟的");
		}

		user.remove();

		PermissionsEx.getPermissionManager().resetUser(userName);

		sender.sendMessage(ChatColor.WHITE + "用户 \"" + user.getName() + "\" 已移除!");
	}

	@Command(name = "pex",
			syntax = "user <user> add <permission> [world]",
			permission = "permissions.manage.users.permissions.<user>",
			description = "添加 <权限> 给 <用户> 于 [世界]")
	public void userAddPermission(Plugin plugin, CommandSender sender, Map<String, String> args) {
		String userName = this.autoCompletePlayerName(args.get("user"));
		String worldName = this.autoCompleteWorldName(args.get("world"));

		PermissionUser user = PermissionsEx.getPermissionManager().getUser(userName);

		if (user == null) {
			sender.sendMessage(ChatColor.RED + "用户不存在");
			return;
		}

		user.addPermission(args.get("permission"), worldName);

		sender.sendMessage(ChatColor.WHITE + "权限 \"" + args.get("permission") + "\" 已添加!");

		this.informPlayer(plugin, userName, "你的权限已被更改");
	}

	@Command(name = "pex",
			syntax = "user <user> remove <permission> [world]",
			permission = "permissions.manage.users.permissions.<user>",
			description = "删除 <用户> 的权限于 [世界]")
	public void userRemovePermission(Plugin plugin, CommandSender sender, Map<String, String> args) {
		String userName = this.autoCompletePlayerName(args.get("user"));
		String worldName = this.autoCompleteWorldName(args.get("world"));

		PermissionUser user = PermissionsEx.getPermissionManager().getUser(userName);

		if (user == null) {
			sender.sendMessage(ChatColor.RED + "用户不存在");
			return;
		}

		String permission = this.autoCompletePermission(user, args.get("permission"), worldName);

		user.removePermission(permission, worldName);
		user.removeTimedPermission(permission, worldName);

		sender.sendMessage(ChatColor.WHITE + "权限 \"" + permission + "\" 已移除!");
		this.informPlayer(plugin, userName, "你的权限已被更改");
	}

	@Command(name = "pex",
			syntax = "user <user> swap <permission> <targetPermission> [world]",
			permission = "permissions.manage.users.permissions.<user>",
			description = "在权限列表中交换 <权限> 与 <目标权限> . 可以是数字或者权限本身")
	public void userSwapPermission(Plugin plugin, CommandSender sender, Map<String, String> args) {
		String userName = this.autoCompletePlayerName(args.get("user"));
		String worldName = this.autoCompleteWorldName(args.get("world"));

		PermissionUser user = PermissionsEx.getPermissionManager().getUser(userName);

		if (user == null) {
			sender.sendMessage(ChatColor.RED + "用户不存在");
			return;
		}

		String[] permissions = user.getOwnPermissions(worldName);

		try {
			int sourceIndex = this.getPosition(this.autoCompletePermission(user, args.get("permission"), worldName, "permission"), permissions);
			int targetIndex = this.getPosition(this.autoCompletePermission(user, args.get("targetPermission"), worldName, "targetPermission"), permissions);

			String targetPermission = permissions[targetIndex];

			permissions[targetIndex] = permissions[sourceIndex];
			permissions[sourceIndex] = targetPermission;

			user.setPermissions(permissions, worldName);

			sender.sendMessage("权限已交换!");
		} catch (Throwable e) {
			sender.sendMessage(ChatColor.RED + "Error: " + e.getMessage());
		}
	}

	@Command(name = "pex",
			syntax = "user <user> timed add <permission> [lifetime] [world]",
			permission = "permissions.manage.users.permissions.timed.<user>",
			description = "添加限时 <权限> 给 <用户> 生存 [生存时间] 秒在 [世界]")
	public void userAddTimedPermission(Plugin plugin, CommandSender sender, Map<String, String> args) {
		String userName = this.autoCompletePlayerName(args.get("user"));
		String worldName = this.autoCompleteWorldName(args.get("world"));

		int lifetime = 0;

		if (args.containsKey("lifetime")) {
			lifetime = DateUtils.parseInterval(args.get("lifetime"));
		}

		PermissionUser user = PermissionsEx.getPermissionManager().getUser(userName);

		if (user == null) {
			sender.sendMessage(ChatColor.RED + "用户不存在");
			return;
		}

		String permission = args.get("permission");

		user.addTimedPermission(permission, worldName, lifetime);

		sender.sendMessage(ChatColor.WHITE + "已添加限时权限 \"" + permission + "\" !");
		this.informPlayer(plugin, userName, "你的权限已被更改");

		logger.info("用户 " + userName + " 得到限时权限 \"" + args.get("permission") + "\" "
				+ (lifetime > 0 ? "持续 " + lifetime + " 秒 " : " ") + "授权: " + getSenderName(sender));
	}

	@Command(name = "pex",
			syntax = "user <user> timed remove <permission> [world]",
			permission = "permissions.manage.users.permissions.timed.<user>",
			description = "Remove timed <permission> from <user> in [world]")
	public void userRemoveTimedPermission(Plugin plugin, CommandSender sender, Map<String, String> args) {
		String userName = this.autoCompletePlayerName(args.get("user"));
		String worldName = this.autoCompleteWorldName(args.get("world"));
		String permission = args.get("permission");

		PermissionUser user = PermissionsEx.getPermissionManager().getUser(userName);

		if (user == null) {
			sender.sendMessage(ChatColor.RED + "用户不存在");
			return;
		}

		user.removeTimedPermission(args.get("permission"), worldName);

		sender.sendMessage(ChatColor.WHITE + "Timed permission \"" + permission + "\" removed!");
		this.informPlayer(plugin, userName, "你的权限已被更改");
	}

	@Command(name = "pex",
			syntax = "user <user> set <option> <value> [world]",
			permission = "permissions.manage.users.permissions.<user>",
			description = "设置 <选项> 为 <值> 在 [world]")
	public void userSetOption(Plugin plugin, CommandSender sender, Map<String, String> args) {
		String userName = this.autoCompletePlayerName(args.get("user"));
		String worldName = this.autoCompleteWorldName(args.get("world"));

		PermissionUser user = PermissionsEx.getPermissionManager().getUser(userName);

		if (user == null) {
			sender.sendMessage(ChatColor.RED + "用户不存在");
			return;
		}

		user.setOption(args.get("option"), args.get("value"), worldName);


		if (args.containsKey("value") && args.get("value").isEmpty()) {
			sender.sendMessage(ChatColor.WHITE + "选项 \"" + args.get("option") + "\" 已清除!");
		} else {
			sender.sendMessage(ChatColor.WHITE + "选项 \"" + args.get("option") + "\" 已设置!");
		}

		this.informPlayer(plugin, userName, "你的权限已被更改");
	}

	/**
	 * User's groups management
	 */
	@Command(name = "pex",
			syntax = "user <user> group list [world]",
			permission = "permissions.manage.membership.<user>",
			description = "列出 <用户> 的所有组")
	public void userListGroup(Plugin plugin, CommandSender sender, Map<String, String> args) {
		String userName = this.autoCompletePlayerName(args.get("user"));
		String worldName = this.autoCompleteWorldName(args.get("world"));

		PermissionUser user = PermissionsEx.getPermissionManager().getUser(userName);

		if (user == null) {
			sender.sendMessage(ChatColor.RED + "用户不存在");
			return;
		}

		sender.sendMessage("User " + args.get("user") + " @" + worldName + " currently in:");
		for (PermissionGroup group : user.getGroups(worldName)) {
			sender.sendMessage("  " + group.getName());
		}
	}

	@Command(name = "pex",
			syntax = "user <user> group add <group> [world] [lifetime]",
			permission = "permissions.manage.membership.<group>",
			description = "Add <user> to <group>")
	public void userAddGroup(Plugin plugin, CommandSender sender, Map<String, String> args) {
		String userName = this.autoCompletePlayerName(args.get("user"));
		String groupName = this.autoCompleteGroupName(args.get("group"));
		String worldName = this.autoCompleteWorldName(args.get("world"));

		PermissionUser user = PermissionsEx.getPermissionManager().getUser(userName);

		if (user == null) {
			sender.sendMessage(ChatColor.RED + "用户不存在");
			return;
		}

		if (args.containsKey("lifetime")) {
			try {
				int lifetime = DateUtils.parseInterval(args.get("lifetime"));

				user.addGroup(groupName, worldName, lifetime);
			} catch (NumberFormatException e) {
				sender.sendMessage(ChatColor.RED + "组存活时间必须是数字!");
				return;
			}

		} else {
			user.addGroup(groupName, worldName);
		}


		sender.sendMessage(ChatColor.WHITE + "用户已添加到组 \"" + groupName + "\"!");
		this.informPlayer(plugin, userName, "你已经加入 \"" + groupName + "\" 组");
	}

	@Command(name = "pex",
			syntax = "user <user> group set <group> [world]",
			permission = "",
			description = "设置 <用户> 的 <组> ")
	public void userSetGroup(Plugin plugin, CommandSender sender, Map<String, String> args) {
		PermissionManager manager = PermissionsEx.getPermissionManager();

		PermissionUser user = manager.getUser(this.autoCompletePlayerName(args.get("user")));
		String worldName = this.autoCompleteWorldName(args.get("world"));

		if (user == null) {
			sender.sendMessage(ChatColor.RED + "用户不存在");
			return;
		}

		String groupName = args.get("group");

		PermissionGroup[] groups;

		if (groupName.contains(",")) {
			String[] groupsNames = groupName.split(",");
			groups = new PermissionGroup[groupsNames.length];

			for (int i = 0; i < groupsNames.length; i++) {
				if (sender instanceof Player && !manager.has((Player) sender, "permissions.manage.membership." + groupsNames[i].toLowerCase())) {
					sender.sendMessage(ChatColor.RED + "没有足够的权限访问组 " + groupsNames[i]);
					return;
				}

				groups[i] = manager.getGroup(this.autoCompleteGroupName(groupsNames[i]));
			}

		} else {
			groupName = this.autoCompleteGroupName(groupName);

			if (groupName != null) {
				groups = new PermissionGroup[]{manager.getGroup(groupName)};

				if (sender instanceof Player && !manager.has((Player) sender, "permissions.manage.membership." + groupName.toLowerCase())) {
					sender.sendMessage(ChatColor.RED + "没有足够的权限访问组" + groupName);
					return;
				}

			} else {
				sender.sendMessage(ChatColor.RED + "没有组被设置!");
				return;
			}
		}

		if (groups.length > 0) {
			user.setGroups(groups, worldName);
			sender.sendMessage(ChatColor.WHITE + "已设置用户的组!");
		} else {
			sender.sendMessage(ChatColor.RED + "没有组被设置!");
		}

		this.informPlayer(plugin, user.getName(), "You are now only in \"" + groupName + "\" group");
	}

	@Command(name = "pex",
			syntax = "user <user> group remove <group> [world]",
			permission = "permissions.manage.membership.<group>",
			description = "从 <组> 中移除 <用户> ")
	public void userRemoveGroup(Plugin plugin, CommandSender sender, Map<String, String> args) {
		String userName = this.autoCompletePlayerName(args.get("user"));
		String groupName = this.autoCompleteGroupName(args.get("group"));
		String worldName = this.autoCompleteWorldName(args.get("world"));

		PermissionUser user = PermissionsEx.getPermissionManager().getUser(userName);

		if (user == null) {
			sender.sendMessage(ChatColor.RED + "用户不存在");
			return;
		}

		user.removeGroup(groupName, worldName);

		sender.sendMessage(ChatColor.WHITE + "用户已从 " + groupName + "中移除!");

		this.informPlayer(plugin, userName, "你已经从 \"" + groupName + "\" 组中被移除");
	}

	@Command(name = "pex",
			syntax = "users cleanup <group> [threshold]",
			permission = "permissions.manage.users.cleanup",
			description = "清除特定组中的用户, 当最后登录的间隔大于指定的天数时. 默认的间隔是30天.")
	public void usersCleanup(Plugin plugin, CommandSender sender, Map<String, String> args) {
		long threshold = 2304000;

		PermissionGroup group = PermissionsEx.getPermissionManager().getGroup(args.get("group"));

		if (args.containsKey("threshold")) {
			try {
				threshold = Integer.parseInt(args.get("threshold")) * 86400; // 86400 - seconds in one day
			} catch (NumberFormatException e) {
				sender.sendMessage(ChatColor.RED + "间隔必须是一个数字 (天)");
				return;
			}
		}

		int removed = 0;

		Long deadline = (System.currentTimeMillis() / 1000L) - threshold;
		for (PermissionUser user : group.getUsers()) {
			int lastLogin = user.getOwnOptionInteger("last-login-time", null, 0);

			if (lastLogin > 0 && lastLogin < deadline) {
				user.remove();
				removed++;
			}
		}

		sender.sendMessage("清除了 " + removed + " 个用户");
	}
}
