package io.github.skepter.errlog;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.util.HashSet;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class Utils {

	/*
	 * Uploads inputString to hastebin then returns the link to the haste,
	 * if an error occurs, returns null
	 */
	public static String post(String inputString) throws IOException {
		InputStream is = null;
		byte[] data = null;
		ByteArrayOutputStream baos = null;
		byte[] content = inputString.getBytes();
		try {
			final URL url = new URL("https://hastebin.com/documents");
			final HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.addRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.0)");
			connection.setDoOutput(true);
			connection.setUseCaches(false);

			connection.setRequestMethod("POST");
			connection.setRequestProperty("Content-Type", "text/xml");
			connection.setRequestProperty("Content-Length", String.valueOf(content.length));

			OutputStream os = null;
			os = connection.getOutputStream();
			os.write(content);
			os.close();
			
			try {
				is = connection.getInputStream();
			} catch(IOException e) {
				if(e.getMessage() != null) {
					
					Bukkit.getScheduler().scheduleSyncDelayedTask(Main.INSTANCE, new Runnable() {

						@Override
						public void run() {
							Main.INSTANCE.sendToListeners("[" + ChatColor.YELLOW + "Hastebin" + ChatColor.WHITE + "] Encountered HTTP response code " + HTTPStatusCode.get(e.getMessage().replaceAll("\\D", "")));
						}});
				}
				return null;
			}			
			
			final byte[] buffer = new byte[2 * 1024];
			baos = new ByteArrayOutputStream();
			int n;
			while ((n = is.read(buffer)) >= 0) {
				baos.write(buffer, 0, n);
			}
			data = baos.toByteArray();
		} catch (Exception e) {
			return null;
		} finally {
			if(is != null)
				is.close();
			if(baos != null)
				baos.close();
		}
		String returnStr = new String(data);
		return "https://hastebin.com/" + returnStr.substring(8, returnStr.length() - 2) + ".java";
	}

	public static void readFile() {
		try {
			Main.INSTANCE.getDataFolder().mkdirs();
			File file = new File(Main.INSTANCE.getDataFolder(), "consoleplayers.txt");
			file.createNewFile();
			for (String str : Files.readAllLines(file.toPath())) {
				Player player = Bukkit.getPlayer(str);
				if (player != null) {
					Main.INSTANCE.listeners.add(player);
				}
			}
		} catch (IOException e) {
		}
	}

	public static void writeFile() {
		try {
			Main.INSTANCE.getDataFolder().mkdirs();
			File file = new File(Main.INSTANCE.getDataFolder(), "consoleplayers.txt");
			file.createNewFile();
			HashSet<String> playerNames = new HashSet<String>();
			for (Player player : Main.INSTANCE.listeners) {
				playerNames.add(player.getName());
			}
			Files.write(file.toPath(), playerNames);
		} catch (IOException e) {
		}
	}

	public static String pluginSearcher(Class<?> clazz) {
		InputStream is = getPluginYAML(clazz);
		if (is == null) {
			return "Unknown plugin";
		} else {
			byte[] buf = new byte[256];
			int pointer = 0;
			int b;
			try {
				while ((b = is.read()) != 10) {
					buf[pointer] = (byte) b;
					pointer++;
				}
			} catch (IOException e) {
				return "Unknown plugin";
			}
			return new String(buf).trim().substring(6);
		}
	}
	
	private static InputStream getPluginYAML(Class<?> clazz) {
		try {
			URL url = clazz.getClassLoader().getResource("plugin.yml");

			if (url == null) {
				return null;
			}

			URLConnection connection = url.openConnection();
			connection.setUseCaches(false);
			return connection.getInputStream();
		} catch (IOException localIOException) {
		}
		return null;
	}

}
