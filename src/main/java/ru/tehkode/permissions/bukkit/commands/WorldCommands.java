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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import ru.tehkode.permissions.PermissionManager;
import ru.tehkode.permissions.bukkit.PermissionsEx;
import ru.tehkode.permissions.commands.Command;
import ru.tehkode.utils.StringUtils;

public class WorldCommands extends PermissionsCommand {

	@Command(name = "pex",
			syntax = "worlds",
			description = "显示已加载的世界",
			isPrimary = true,
			permission = "permissions.manage.worlds")
	public void worldsTree(Plugin plugin, CommandSender sender, Map<String, String> args) {
		List<World> worlds = Bukkit.getServer().getWorlds();

		PermissionManager manager = PermissionsEx.getPermissionManager();

		sender.sendMessage("服务器中的世界: ");
		for (World world : worlds) {
			String[] parentWorlds = manager.getWorldInheritance(world.getName());
			String output = "  " + world.getName();
			if (parentWorlds.length > 0) {
				output += ChatColor.GREEN + " [" + ChatColor.WHITE + StringUtils.implode(parentWorlds, ", ") + ChatColor.GREEN + "]";
			}

			sender.sendMessage(output);
		}
	}

	@Command(name = "pex",
			syntax = "world <world>",
			description = "显示 <世界>继承信息",
			permission = "permissions.manage.worlds")
	public void worldPrintInheritance(Plugin plugin, CommandSender sender, Map<String, String> args) {
		String worldName = this.autoCompleteWorldName(args.get("world"));
		PermissionManager manager = PermissionsEx.getPermissionManager();
		if (Bukkit.getServer().getWorld(worldName) == null) {
			sender.sendMessage("指定的世界 \"" + args.get("world") + "\" 不存在.");
			return;
		}

		String[] parentWorlds = manager.getWorldInheritance(worldName);

		sender.sendMessage("世界 " + worldName + " 继承:");
		if (parentWorlds.length == 0) {
			sender.sendMessage("啥都没有 :3");
			return;
		}

		for (String parentWorld : parentWorlds) {
			String[] parents = manager.getWorldInheritance(parentWorld);
			String output = "  " + parentWorld;
			if (parentWorlds.length > 0) {
				output += ChatColor.GREEN + " [" + ChatColor.WHITE + StringUtils.implode(parentWorlds, ", ") + ChatColor.GREEN + "]";
			}

			sender.sendMessage(output);
		}
	}

	@Command(name = "pex",
			syntax = "world <world> inherit <parentWorlds>",
			description = "设置 <父世界> 给 <世界>",
			permission = "permissions.manage.worlds.inheritance")
	public void worldSetInheritance(Plugin plugin, CommandSender sender, Map<String, String> args) {
		String worldName = this.autoCompleteWorldName(args.get("world"));
		PermissionManager manager = PermissionsEx.getPermissionManager();
		if (Bukkit.getServer().getWorld(worldName) == null) {
			sender.sendMessage("指定的世界 \"" + args.get("world") + "\" 不存在.");
			return;
		}

		List<String> parents = new ArrayList<String>();
		String parentWorlds = args.get("parentWorlds");
		if (parentWorlds.contains(",")) {
			for (String world : parentWorlds.split(",")) {
				world = this.autoCompleteWorldName(world, "parentWorlds");
				if (!parents.contains(world)) {
					parents.add(world.trim());
				}
			}
		} else {
			parents.add(parentWorlds.trim());
		}

		manager.setWorldInheritance(worldName, parents.toArray(new String[0]));

		sender.sendMessage("世界 " + worldName + " 已继承 " + StringUtils.implode(parents, ", "));
	}
}
