package io.github.skepter.errlog;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class SimpleErrorAnalyser {

	public static ItemStack exceptionToString(Throwable error) {
		ItemStack is = new ItemStack(Material.ENCHANTED_BOOK);
		ItemMeta meta = is.getItemMeta();
		meta.setDisplayName(ChatColor.YELLOW + "Error information (for non-developers)");
		
		List<String> errorDescription = new ArrayList<String>();
		errorDescription.add(ChatColor.WHITE + "");
		Throwable cause = error.getCause();
		if(cause instanceof NullPointerException) {
			errorDescription.add(ChatColor.GREEN + "NullPointerException:");
			errorDescription.add(ChatColor.WHITE + " An error which occurs when a variable");
			errorDescription.add(ChatColor.WHITE + " is not correctly initialised or defined");
			errorDescription.add(ChatColor.WHITE + "");
		} else if(cause instanceof ArrayIndexOutOfBoundsException) {
			errorDescription.add(ChatColor.GREEN + "ArrayIndexOutOfBoundsException:");
			errorDescription.add(ChatColor.WHITE + " An error which occurs when trying");
			errorDescription.add(ChatColor.WHITE + " to access a list badly");
			errorDescription.add(ChatColor.WHITE + "");
		} else if(cause instanceof ClassCastException) {
			errorDescription.add(ChatColor.GREEN + "ClassCastException:");
			errorDescription.add(ChatColor.WHITE + " An error which occurs when the original");
			errorDescription.add(ChatColor.WHITE + " developer made a mistake between two variables");
			errorDescription.add(ChatColor.WHITE + " (e.g. mistaking a skeleton for a player)");
			errorDescription.add(ChatColor.WHITE + "");			
		} else if(cause instanceof NoSuchMethodError) {
			errorDescription.add(ChatColor.GREEN + "NoSuchMethodError:");
			errorDescription.add(ChatColor.WHITE + " An error which occurs when the code can't find");
			errorDescription.add(ChatColor.WHITE + " a specific function. Likely due to an old version");
			errorDescription.add(ChatColor.WHITE + " of a plugin which is incompatible with the");
			errorDescription.add(ChatColor.WHITE + " latest version of Spigot");
			errorDescription.add(ChatColor.WHITE + "");
		} else {
			errorDescription.add(ChatColor.GREEN + cause.getClass().getSimpleName() + ":");
			errorDescription.add(ChatColor.WHITE + " Some error. Ask Skepter to write a description");
			errorDescription.add(ChatColor.WHITE + " about this error to know more.");
			errorDescription.add(ChatColor.WHITE + "");
		}
		
		if(errorDetails(error) != null) {
			errorDescription.add(ChatColor.WHITE + "Error was caused by an event: " + errorDetails(error));
		} else if(methodDetector(error) != null) {
			errorDescription.add(ChatColor.WHITE + "Error was caused by this function: " + methodDetector(error));
		} else {
			errorDescription.add(ChatColor.WHITE + "Not sure what caused this error... :(");
		}
		errorDescription.add(ChatColor.WHITE + "This was caused by the plugin: " + responsiblePlugin(error));
		
		meta.setLore(errorDescription);
		is.setItemMeta(meta);
		return is;
	}

	private static String errorDetails(Throwable error) {
		for (StackTraceElement st : error.getCause().getStackTrace()) {

			// Removing NMS/Bukkit/Java issues
			if (st.getClassName().startsWith("java") || st.getClassName().startsWith("org.bukkit")
					|| st.getClassName().startsWith("net.minecraft.server") || st.getClassName().startsWith("sun")) {
				continue;
			} else {
				//yay!
				Class<?> errorClazz = null;
				try {
					errorClazz = Class.forName(st.getClassName());
				} catch (ClassNotFoundException e) {
					continue;
				}
				
				//checking if the method is event-related
				for(Method method : errorClazz.getDeclaredMethods()) {
					if(method.getName().equals(st.getMethodName())) {
						if(method.isAnnotationPresent(EventHandler.class)) {
							//ding DING DING!!!
							return method.getParameterTypes()[0].getSimpleName();
						}
					}
				}
			}
		}
		return null;
	}
	
	private static String methodDetector(Throwable error) {
		for (StackTraceElement st : error.getCause().getStackTrace()) {

			// Removing NMS/Bukkit/Java issues
			if (st.getClassName().startsWith("java") || st.getClassName().startsWith("org.bukkit")
					|| st.getClassName().startsWith("net.minecraft.server") || st.getClassName().startsWith("sun")) {
				continue;
			} else {
				return st.getMethodName();
			}
		}
		return null;
	}

	private static String responsiblePlugin(Throwable error) {
		for (StackTraceElement st : error.getCause().getStackTrace()) {

			// Removing NMS/Bukkit/Java issues
			if (st.getClassName().startsWith("java") || st.getClassName().startsWith("org.bukkit")
					|| st.getClassName().startsWith("net.minecraft.server") || st.getClassName().startsWith("sun")) {
				continue;
			} else {
				try {
					return Utils.pluginSearcher(Class.forName(st.getClassName()));
				} catch (ClassNotFoundException e) {
					continue;
				}
			}
		}
		return null;
	}
	
}
