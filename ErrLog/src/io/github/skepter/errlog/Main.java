package io.github.skepter.errlog;

import java.text.SimpleDateFormat;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TimeZone;
import java.util.Map.Entry;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.plugin.java.JavaPlugin;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

public class Main extends JavaPlugin implements Listener {

	HashSet<Player> listeners;
	HashMap<Integer, Throwable> errors;
	HashMap<Integer, String> cachedErrors;
	HashMap<Integer, Long> errorTimes;

	public static Main INSTANCE;
	
	//errlogs to view all errors since restart

	@Override
	public void onEnable() {
		INSTANCE = this;

		listeners = new HashSet<Player>();
		errors = new HashMap<Integer, Throwable>();
		cachedErrors = new HashMap<Integer, String>();
		errorTimes = new HashMap<Integer, Long>();
		Utils.readFile();

		getServer().getPluginManager().registerEvents(new InventoryHandler(), this);
		getServer().getPluginManager().registerEvents(this, this);

		Enumeration<String> loggerNames = LogManager.getLogManager().getLoggerNames();

		// Add logger handler
		while (loggerNames.hasMoreElements()) {
			Logger.getLogger(loggerNames.nextElement()).addHandler(new LoggingHandler());
		}
	}

	protected void sendToListeners(String message) {
		for (Player p : listeners) {
			p.sendMessage(message);
		}
	}

	protected void sendToListeners(TextComponent message) {
		for (Player p : listeners) {
			p.spigot().sendMessage(message);
		}
	}

	@Override
	public void onDisable() {
		Utils.writeFile();
		listeners.clear();
	}
	
	@SuppressWarnings("null")
	@EventHandler
	public void onEvent(PlayerCommandPreprocessEvent event) {
		if(event.getMessage().contains("errlogevent")) {
			String str = null;
			str.toUpperCase();
		}
	}
	
	@SuppressWarnings("null")
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {	
		Player player = null;
		if(sender instanceof Player) {
			player = (Player) sender;
		}
		if (label.equalsIgnoreCase("errlog")) {
			if (!(sender instanceof Player)) {
				sender.sendMessage("Console cannot use this command.");
				return true;
			}

			if (!player.hasPermission("errlog.use") || !player.isOp()) {
				player.sendMessage("You do not have permission to use this command.");
				return true;
			}

			if (args.length > 1)
				return false;

			// Toggle
			if (args.length == 0) {
				if (listeners.contains(player)) {
					player.sendMessage("You are no longer viewing error logs");
					listeners.remove(player);
				} else {
					player.sendMessage("You are now viewing error logs");
					listeners.add(player);
				}
				return true;
			}

			// On or off explicitly stated
			if (args.length == 1) {
				if (args[0].equalsIgnoreCase("on")) {
					if (!listeners.contains(player)) {
						player.sendMessage("You are now viewing error logs");
						listeners.add(player);
					}
					return true;
				} else if (args[0].equalsIgnoreCase("off")) {
					if (listeners.contains(player)) {
						player.sendMessage("You are no longer viewing error logs");
						listeners.remove(player);
					}
					return true;
				} else if (args[0].equalsIgnoreCase("err1")) {
					System.out.println(args[1]);
					return true;
				} else if (args[0].equalsIgnoreCase("err2")) {
					String str = null;
					str.toUpperCase();
					return true;
				} else {
					return false;
				}
			}
		} else if (label.equalsIgnoreCase("errview")) {
			if (!(sender instanceof Player)) {
				sender.sendMessage("Console cannot use this command.");
				return true;
			}
			if (args.length != 1) {
				sender.sendMessage("Argument length incorrect");
				return true;
			}
			int errorID = Integer.parseInt(args[0]);
			player.openInventory(InventoryHandler.getInventory(errorID));
			return true;
		}  else if (label.equalsIgnoreCase("errlogs")) {
			if (!(sender instanceof Player)) {
				sender.sendMessage("Console cannot use this command.");
				return true;
			}
			player.sendMessage("[" + ChatColor.YELLOW + "Error logs" + ChatColor.WHITE + "] Found " + errors.size() + " errors");
			for(Entry<Integer, Throwable> entry : errors.entrySet()) {
				
				SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm (zz) E d MMM yyyy");
				dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
				
				TextComponent message = new TextComponent(" [" + ChatColor.YELLOW
						+ entry.getValue().getClass().getSimpleName() + ChatColor.WHITE + "] " + dateFormat.format(errorTimes.get(entry.getKey())));

				String componentString = "Click to view error log interface";
				message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
						new ComponentBuilder(componentString).create()));
				message.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/errview " + entry.getKey()));
				player.spigot().sendMessage(message);
			}
			return true;
		}
		return true;
	}

}
