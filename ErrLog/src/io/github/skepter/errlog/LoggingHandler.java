package io.github.skepter.errlog;

import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

import org.bukkit.ChatColor;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

public class LoggingHandler extends Handler {

	@Override
	public void close() throws SecurityException {
	}

	@Override
	public void flush() {
	}

	@Override
	public void publish(LogRecord record) {
		// double log issue somewhere in this code XXX
		if (!Main.INSTANCE.listeners.isEmpty()) {
			if (record.getThrown() != null) {
				// Message displayed in chat

				TextComponent message = new TextComponent("[" + ChatColor.YELLOW
						+ record.getThrown().getClass().getSimpleName() + ChatColor.WHITE + "] "
						+ record.getThrown().getMessage() + " (hover to see details)");

//				// Hover message
//				String componentString = ChatColor.YELLOW
//						+ record.getThrown().getCause().getClass().getSimpleName() + ": "
//						+ record.getThrown().getCause().getMessage() + "\n";
//				componentString = componentString + ChatColor.GRAY + "Caused by:" + "\n";
//
//				for (StackTraceElement st : record.getThrown().getCause().getStackTrace()) {
//
//					// Contains the class and method name
//					String methodName = st.getClassName() + "." + st.getMethodName();
//
//					// Just the class name (e.g. Main)
//					String rawClassName = st.getClassName()
//							.substring(st.getClassName().lastIndexOf(".") + 1);
//
//					// Smart name highlighting:
//					if (methodName.startsWith("java") || methodName.startsWith("org.bukkit")
//							|| methodName.startsWith("net.minecraft.server")) {
//						componentString = componentString + ChatColor.GRAY + "  " + methodName + "("
//								+ rawClassName + ".java:" + st.getLineNumber() + ")" + "\n"
//								+ ChatColor.WHITE;
//					} else {
//						componentString = componentString + ChatColor.YELLOW + "  " + methodName + "("
//								+ rawClassName + ".java:" + st.getLineNumber() + ")" + "\n"
//								+ ChatColor.WHITE;
//						
//						try {
//							System.out.println("Faulty plugin: " + Utils.pluginSearcher(Class.forName(st.getClassName())));							
//						} catch (ClassNotFoundException e) {
//							e.printStackTrace();
//						}
//					}
//
//				}
//				// Remove the \n from the very end:
//				componentString = componentString.substring(0, componentString.length() - 3);

				String componentString = "Click to view error log interface";
				
				
				int errID = ThreadLocalRandom.current().nextInt();
				Main.INSTANCE.errors.put(errID, record.getThrown());

				message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
						new ComponentBuilder(componentString).create()));
				message.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/errupload " + errID));
				Main.INSTANCE.sendToListeners(message);
				return;
			}
			// else {
			// try {
			// Plugin pl =
			// JavaPlugin.getProvidingPlugin(Class.forName(record.getLoggerName()));
			// sendToListeners("[" + ChatColor.RED + pl.getName() +
			// ChatColor.WHITE + "] "
			// + ChatColor.GRAY + record.getMessage());
			// return;
			// } catch (Exception e) {
			// // sendToListeners("[" + ChatColor.RED +
			// // record.getLoggerName() + ChatColor.WHITE + "]
			// // " + ChatColor.GRAY + record.getMessage());
			// return;
			// }
			// }

		}
	}

}
