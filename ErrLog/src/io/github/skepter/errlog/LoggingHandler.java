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
						+ record.getThrown().getMessage() + " (click to view error)");

				String componentString = "Click to view error log interface";
				
				
				int errID = ThreadLocalRandom.current().nextInt();
				Main.INSTANCE.errors.put(errID, record.getThrown());
				Main.INSTANCE.errorTimes.put(errID, System.currentTimeMillis());

				message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
						new ComponentBuilder(componentString).create()));
				message.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/errview " + errID));
				Main.INSTANCE.sendToListeners(message);
				return;
			}
		}
	}

}
