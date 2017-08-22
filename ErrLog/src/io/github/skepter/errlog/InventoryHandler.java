package io.github.skepter.errlog;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

public class InventoryHandler implements Listener {

	@EventHandler
	public void invClick(InventoryClickEvent event) {
		if(event.getInventory().getTitle().contains(" Error")) {
			event.setCancelled(true);
			int errorID = Integer.parseInt(event.getInventory().getItem(7).getItemMeta().getDisplayName());
			Throwable throwable  = Main.INSTANCE.errors.getOrDefault(errorID, null);
			switch(event.getSlot()) {
				case 1: {
					if (throwable == null) {
						Main.INSTANCE.sendToListeners("[" + ChatColor.YELLOW + "Hastebin" + ChatColor.WHITE + "] "
								+ Main.INSTANCE.cachedErrors.get(errorID));
						return;
					}

					// remove int from errors or something?
					StringWriter strWriter = new StringWriter();
					PrintWriter writer = new PrintWriter(strWriter);

					throwable.printStackTrace(writer);
					Plugin instance = Main.INSTANCE;
					Bukkit.getScheduler().runTaskAsynchronously(Main.INSTANCE, new Runnable() {

						@Override
						public void run() {
							try {
								String link = Utils.post(strWriter.toString());
								Main.INSTANCE.cachedErrors.put(errorID, link);
								Bukkit.getScheduler().runTask(instance, new Runnable() {

									@Override
									public void run() {
										Main.INSTANCE.sendToListeners("[" + ChatColor.YELLOW + "Hastebin" + ChatColor.WHITE
												+ "] Error uploaded: " + link);
									}

								});
							} catch (IOException e) {
								e.printStackTrace();
							}
						}

					});
					break;
				}
				case 4: {
					File file = new File(Main.INSTANCE.getDataFolder(), "errorlog" + errorID + ".txt");
					try {
						boolean result = file.createNewFile();
						if(result == false) {
							Main.INSTANCE.sendToListeners("[" + ChatColor.YELLOW + "ErrLog" + ChatColor.WHITE + "] Error file already exists: /plugins/ErrLog/errorlog" + errorID + ".txt");
							return;
						}
						
						StringWriter strWriter = new StringWriter();
						PrintWriter writer = new PrintWriter(strWriter);

						throwable.printStackTrace(writer);
						
						Files.write(file.toPath(), strWriter.toString().getBytes());
						Main.INSTANCE.sendToListeners("[" + ChatColor.YELLOW + "ErrLog" + ChatColor.WHITE + "] Error saved to /plugins/ErrLog/errorlog" + errorID + ".txt");
					} catch (IOException e) {
						e.printStackTrace();
					}
					break;
				}
				case 8:
					event.getWhoClicked().closeInventory();
					break;
			}
		}
	}
	
	public static Inventory getInventory(int errorID) {
		Throwable error = Main.INSTANCE.errors.get(errorID);
		Inventory inv = Bukkit.createInventory(null, 9, error.getCause().getClass().getSimpleName().replace("Exception", " Error"));
		Set<String> pluginNames = new HashSet<String>();
		for(StackTraceElement st : error.getCause().getStackTrace()) {
			
			if(st.getClassName().startsWith("java") || st.getClassName().startsWith("org.bukkit") || st.getClassName().startsWith("net.minecraft.server") || st.getClassName().startsWith("sun")) {
				continue;
			}
			
			try {
				pluginNames.add(Utils.pluginSearcher(Class.forName(st.getClassName())));
				break;
			} catch (ClassNotFoundException e) {
				continue;
			}
			
		}
		
		List<String> pluginDetails = new ArrayList<String>();
		for(String pluginName : pluginNames) {
			Plugin plugin = Bukkit.getPluginManager().getPlugin(pluginName);
			pluginDetails.add(ChatColor.GREEN + plugin.getName());
			pluginDetails.add(ChatColor.WHITE + " Version: " + plugin.getDescription().getVersion());
			pluginDetails.add(ChatColor.WHITE + " Authors: " + Arrays.deepToString(plugin.getDescription().getAuthors().toArray()));
			pluginDetails.add("");
		}
		
		inv.setItem(0, itemGenerator(Material.PAPER, "Plugins responsible for error", pluginDetails));
		inv.setItem(1, itemGenerator(Material.WOOL, "Upload to Hastebin"));
		inv.setItem(2, itemGenerator(Material.KNOWLEDGE_BOOK, "Error details (for developers)", errorDetails(error, false)));
		inv.setItem(3, itemGenerator(Material.BOOK, "Simplified error details (for developers)", errorDetails(error, true))); 
		inv.setItem(4, itemGenerator(Material.BOOK_AND_QUILL, "Save error log to a file"));
		inv.setItem(5, SimpleErrorAnalyser.exceptionToString(error));
		
		SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm (zz) E d MMM yyyy");
		dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
		
		inv.setItem(6, itemGenerator(Material.WATCH, "Time in which error occured", Arrays.asList(dateFormat.format(new Date(Main.INSTANCE.errorTimes.get(errorID))))));
		inv.setItem(8, itemGenerator(Material.BARRIER, "Close"));
		
		ItemStack is = new ItemStack(Material.SUGAR);
		ItemMeta meta = is.getItemMeta();
		meta.setDisplayName(String.valueOf(errorID));
		meta.setLore(Arrays.asList("Ignore this, this is an ID", "for the ErrLog plugin to", "use to identify the error"));
		is.setItemMeta(meta);
		inv.setItem(7, is);
		return inv;
		
	}
	
	private static List<String> errorDetails(Throwable error, boolean simple) {
		
		List<String> errorStr = new ArrayList<String>();
		errorStr.add(ChatColor.YELLOW
				+ error.getCause().getClass().getSimpleName() + ": "
				+ error.getCause().getMessage());
		
		errorStr.add(ChatColor.GRAY + "Caused by:");

		for (StackTraceElement st : error.getCause().getStackTrace()) {

			// Contains the class and method name
			String methodName = st.getClassName() + "." + st.getMethodName();

			// Just the class name (e.g. Main)
			String rawClassName = st.getClassName()
					.substring(st.getClassName().lastIndexOf(".") + 1);

			// Smart name highlighting:
			if (methodName.startsWith("java") || methodName.startsWith("org.bukkit") || methodName.startsWith("net.minecraft.server") || st.getClassName().startsWith("sun")) {
				if(!simple) {
					errorStr.add(ChatColor.GRAY + "  " + methodName + "(" + rawClassName + ".java:" + st.getLineNumber() + ")");
				}
			} else {
				errorStr.add(ChatColor.YELLOW + "  " + methodName + "(" + rawClassName + ".java:" + st.getLineNumber() + ")");
			}

		}
		return errorStr;
	}
	
	private static ItemStack itemGenerator(Material material, String name) {
		ItemStack is = new ItemStack(material);
		ItemMeta meta = is.getItemMeta();
		meta.setDisplayName(ChatColor.YELLOW + name);
		is.setItemMeta(meta);
		return is;
	}
	
	private static ItemStack itemGenerator(Material material, String name, List<String> lore) {
		ItemStack is = new ItemStack(material);
		ItemMeta meta = is.getItemMeta();
		meta.setDisplayName(ChatColor.YELLOW + name);
		meta.setLore(lore);
		is.setItemMeta(meta);
		return is;
	}
	
}
