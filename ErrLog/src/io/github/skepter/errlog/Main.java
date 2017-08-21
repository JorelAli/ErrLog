package io.github.skepter.errlog;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Handler;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

public class Main extends JavaPlugin implements Listener {

	HashSet<Player> listeners;
	HashMap<Integer, Throwable> errors;

	public static Main INSTANCE;
	
	@Override
	public void onEnable() {
		INSTANCE = this;
		listeners = new HashSet<Player>();
		errors = new HashMap<Integer, Throwable>();
		Utils.readFile();

		getServer().getPluginManager().registerEvents(this, this);

		Enumeration<String> loggerNames = LogManager.getLogManager().getLoggerNames();

		// Add logger handler
		while (loggerNames.hasMoreElements()) {
			Logger.getLogger(loggerNames.nextElement()).addHandler(new Handler() {

				@Override
				public void close() throws SecurityException {
				}

				@Override
				public void flush() {
				}

				@Override
				public void publish(LogRecord record) {
					// double log issue somewhere in this code XXX
					if (!listeners.isEmpty()) {
						if (record.getThrown() != null) {
							// Message displayed in chat

							TextComponent message = new TextComponent("[" + ChatColor.YELLOW
									+ record.getThrown().getClass().getSimpleName() + ChatColor.WHITE + "] "
									+ record.getThrown().getMessage() + " (hover to see details)");

							// Hover message
							String componentString = ChatColor.YELLOW
									+ record.getThrown().getCause().getClass().getSimpleName() + ": "
									+ record.getThrown().getCause().getMessage() + "\n";
							componentString = componentString + ChatColor.GRAY + "Caused by:" + "\n";

							for (StackTraceElement st : record.getThrown().getCause().getStackTrace()) {

								// Contains the class and method name
								String methodName = st.getClassName() + "." + st.getMethodName();

								// Just the class name (e.g. Main)
								String rawClassName = st.getClassName()
										.substring(st.getClassName().lastIndexOf(".") + 1);

								// Smart name highlighting:
								if (methodName.startsWith("java") || methodName.startsWith("org.bukkit")
										|| methodName.startsWith("net.minecraft.server")) {
									componentString = componentString + ChatColor.GRAY + "  " + methodName + "("
											+ rawClassName + ".java:" + st.getLineNumber() + ")" + "\n"
											+ ChatColor.WHITE;
								} else {
									componentString = componentString + ChatColor.YELLOW + "  " + methodName + "("
											+ rawClassName + ".java:" + st.getLineNumber() + ")" + "\n"
											+ ChatColor.WHITE;
								}

							}
							// Remove the \n from the very end:
							componentString = componentString.substring(0, componentString.length() - 3);

							int errID = ThreadLocalRandom.current().nextInt();
							errors.put(errID, record.getThrown());
							
							message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
									new ComponentBuilder(componentString).create()));
							message.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/errupload " + errID));
							sendToListeners(message);
							return;
						}
//						else {
//							try {
//								Plugin pl = JavaPlugin.getProvidingPlugin(Class.forName(record.getLoggerName()));
//								sendToListeners("[" + ChatColor.RED + pl.getName() + ChatColor.WHITE + "] "
//										+ ChatColor.GRAY + record.getMessage());
//								return;
//							} catch (Exception e) {
//								// sendToListeners("[" + ChatColor.RED +
//								// record.getLoggerName() + ChatColor.WHITE + "]
//								// " + ChatColor.GRAY + record.getMessage());
//								return;
//							}
//						}

					}
				}

			});
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
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (label.equalsIgnoreCase("errlog")) {
			if (!(sender instanceof Player)) {
				sender.sendMessage("Console cannot use this command.");
				return true;
			}

			Player player = (Player) sender;
			if (!player.hasPermission("errlog.use") || !player.isOp()) {
				player.sendMessage("You do not have permission to use this command.");
				return true;
			}

			if (args.length > 1)
				return false;

			// Toggle
			if (args.length == 0) {
				if (listeners.contains(player)) {
					player.sendMessage("You are no longer viewing console");
					listeners.remove(player);
				} else {
					player.sendMessage("You are now viewing console");
					listeners.add(player);
				}
				return true;
			}

			// On or off explicitly stated
			if (args.length == 1) {
				if (args[0].equalsIgnoreCase("on")) {
					if (!listeners.contains(player)) {
						player.sendMessage("You are now viewing console");
						listeners.add(player);
					}
					return true;
				} else if (args[0].equalsIgnoreCase("off")) {
					if (listeners.contains(player)) {
						player.sendMessage("You are no longer viewing console");
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
		} else if(label.equalsIgnoreCase("errupload")) {
			Throwable throwable = errors.get(Integer.parseInt(args[0]));
			//remove int from errors or something?
			StringWriter strWriter = new StringWriter();
			PrintWriter writer = new PrintWriter(strWriter);
			
			throwable.printStackTrace(writer);
			Plugin instance = this;
			Bukkit.getScheduler().runTaskAsynchronously(this, new Runnable() {

				@Override
				public void run() {
					try {
						String link = Utils.post(strWriter.toString());
						Bukkit.getScheduler().runTask(instance, new Runnable() {

							@Override
							public void run() {
								sendToListeners("[" + ChatColor.YELLOW + "Hastebin" + ChatColor.WHITE + "] Error uploaded: " + link);
							}
							
						});
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				
			});
		}
		return true;
	}

}
