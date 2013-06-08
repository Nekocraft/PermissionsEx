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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import ru.tehkode.permissions.PermissionBackend;
import ru.tehkode.permissions.PermissionManager;
import ru.tehkode.permissions.bukkit.PermissionsEx;
import ru.tehkode.permissions.commands.Command;
import ru.tehkode.permissions.commands.CommandsManager.CommandBinding;

public class UtilityCommands extends PermissionsCommand {

	@Command(name = "pex",
			syntax = "reload",
			permission = "permissions.manage.reload",
			description = "重新加载环境")
	public void reload(Plugin plugin, CommandSender sender, Map<String, String> args) {
		PermissionsEx.getPermissionManager().reset();

		sender.sendMessage(ChatColor.WHITE + "权限已重新加载");
	}

	@Command(name = "pex",
			syntax = "config <node> [value]",
			permission = "permissions.manage.config",
			description = "获取或置 <节点> [值]")
	public void config(Plugin plugin, CommandSender sender, Map<String, String> args) {
		if (!(plugin instanceof PermissionsEx)) {
			return;
		}

		String nodeName = args.get("node");
		if (nodeName == null || nodeName.isEmpty()) {
			return;
		}

		FileConfiguration config = plugin.getConfig();

		if (args.get("value") != null) {
			config.set(nodeName, this.parseValue(args.get("value")));
			try {
				config.save(new File(plugin.getDataFolder(), "config.yml"));
			} catch (Throwable e) {
				sender.sendMessage(ChatColor.RED + "[权限] 保存配置文件失败: " + e.getMessage());
			}
		}

		Object node = config.get(nodeName);
		if (node instanceof Map) {
			sender.sendMessage("Node \"" + nodeName + "\": ");
			for (Map.Entry<String, Object> entry : ((Map<String, Object>) node).entrySet()) {
				sender.sendMessage("  " + entry.getKey() + " = " + entry.getValue());
			}
		} else if (node instanceof List) {
			sender.sendMessage("Node \"" + nodeName + "\": ");
			for (String item : ((List<String>) node)) {
				sender.sendMessage(" - " + item);
			}
		} else {
			sender.sendMessage("Node \"" + nodeName + "\" = \"" + node + "\"");
		}
	}

	@Command(name = "pex",
			syntax = "backend",
			permission = "permissions.manage.backend",
			description = "Print currently used backend")
	public void getBackend(Plugin plugin, CommandSender sender, Map<String, String> args) {
		sender.sendMessage("Current backend: " + PermissionsEx.getPermissionManager().getBackend());
	}

	@Command(name = "pex",
			syntax = "backend <backend>",
			permission = "permissions.manage.backend",
			description = "在使用中改变后端 (使用时请注意!)")
	public void setBackend(Plugin plugin, CommandSender sender, Map<String, String> args) {
		if (args.get("backend") == null) {
			return;
		}

		try {
			PermissionsEx.getPermissionManager().setBackend(args.get("backend"));
			sender.sendMessage(ChatColor.WHITE + "权限后端已更改!");
		} catch (RuntimeException e) {
			if (e.getCause() instanceof ClassNotFoundException) {
				sender.sendMessage(ChatColor.RED + "找不到指定的后端.");
			} else {
				sender.sendMessage(ChatColor.RED + "后端加载出错.");
				e.printStackTrace();
			}
		}
	}

	@Command(name = "pex",
			syntax = "hierarchy [world]",
			permission = "permissions.manage.users",
			description = "打印完整的 用户/组 阶层")
	public void printHierarhy(Plugin plugin, CommandSender sender, Map<String, String> args) {
		sender.sendMessage("用户/组 继承的阶层:");
		this.sendMessage(sender, this.printHierarchy(null, this.autoCompleteWorldName(args.get("world")), 0));
	}

	@Command(name = "pex",
			syntax = "dump <backend> <filename>",
			permission = "permissions.dump",
			description = "输出 用户/组 到指定的 <后端> 格式")
	public void dumpData(Plugin plugin, CommandSender sender, Map<String, String> args) {
		if (!(plugin instanceof PermissionsEx)) {
			return; // User informing is disabled
		}

		try {
			PermissionBackend backend = PermissionBackend.getBackend(args.get("backend"), PermissionsEx.getPermissionManager(), plugin.getConfig(), null);

			File dstFile = new File("plugins/PermissionsEx/", args.get("filename"));

			FileOutputStream outStream = new FileOutputStream(dstFile);

			backend.dumpData(new OutputStreamWriter(outStream, "UTF-8"));

			outStream.close();

			sender.sendMessage(ChatColor.WHITE + "[权限] 数据已导出为 \"" + dstFile.getName() + "\" ");
		} catch (RuntimeException e) {
			if (e.getCause() instanceof ClassNotFoundException) {
				sender.sendMessage(ChatColor.RED + "找不到指定的后端!");
			} else {
				sender.sendMessage(ChatColor.RED + "Error: " + e.getMessage());
				logger.severe("Error: " + e.getMessage());
				e.printStackTrace();
			}
		} catch (IOException e) {
			sender.sendMessage(ChatColor.RED + "IO Error: " + e.getMessage());
		}
	}

	@Command(name = "pex",
			syntax = "toggle debug",
			permission = "permissions.debug",
			description = "启用/禁用 调试模式")
	public void toggleFeature(Plugin plugin, CommandSender sender, Map<String, String> args) {
		PermissionManager manager = PermissionsEx.getPermissionManager();

		manager.setDebug(!manager.isDebug());

		String debugStatusMessage = "[权限] 调试模式 " + (manager.isDebug() ? "已启用" : "已禁用");

		if (sender instanceof Player) {
			sender.sendMessage(debugStatusMessage);
		}

		logger.warning(debugStatusMessage);
	}

	@Command(name = "pex",
			syntax = "help [page] [count]",
			permission = "permissions.manage",
			description = "权限使用说明")
	public void showHelp(Plugin plugin, CommandSender sender, Map<String, String> args) {
		List<CommandBinding> commands = this.manager.getCommands();

		int count = args.containsKey("count") ? Integer.parseInt(args.get("count")) : 4;
		int page = args.containsKey("page") ? Integer.parseInt(args.get("page")) : 1;

		if (page < 1) {
			sender.sendMessage("页码必须比 1 大");
			return;
		}

		int totalPages = (int) Math.ceil(commands.size() / count);

		sender.sendMessage(ChatColor.BLUE + "权限" + ChatColor.WHITE + " 指令 (第" + ChatColor.GOLD + page + "/" + totalPages + ChatColor.WHITE + "页): ");

		int base = count * (page - 1);

		for (int i = base; i < base + count; i++) {
			if (i >= commands.size()) {
				break;
			}

			Command command = commands.get(i).getMethodAnnotation();
			String commandName = String.format("/%s %s", command.name(), command.syntax()).replace("<", ChatColor.BOLD.toString() + ChatColor.RED + "<").replace(">", ">" + ChatColor.RESET + ChatColor.GOLD.toString()).replace("[", ChatColor.BOLD.toString() + ChatColor.BLUE + "[").replace("]", "]" + ChatColor.RESET + ChatColor.GOLD.toString());


			sender.sendMessage(ChatColor.GOLD + commandName);
			sender.sendMessage(ChatColor.AQUA + "    " + command.description());
		}
	}
}
