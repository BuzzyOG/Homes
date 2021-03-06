package me.Jaaakee224.Homes;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import ru.tehkode.permissions.PermissionManager;
import ru.tehkode.permissions.bukkit.PermissionsEx;

public final class Homes extends JavaPlugin {
	public File homeDataFile;
	public YamlConfiguration homeConfig;
	public PermissionsEx pex;

	public void onEnable() {
		getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
		getServer().getPluginManager().registerEvents(new AnvilGUI(this), this);
		this.homeDataFile = new File(getDataFolder(), "homeData.yml");
		if (!this.homeDataFile.exists()) {
			this.homeDataFile.getParentFile().mkdirs();
			copy(getResource("homeData.yml"), this.homeDataFile);
		}
		this.homeConfig = new YamlConfiguration();
		try {
			this.homeConfig.load(this.homeDataFile);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InvalidConfigurationException e) {
			e.printStackTrace();
		}
	}

	private void copy(InputStream in , File file) {
		try {
			OutputStream out = new FileOutputStream(file);
			byte[] buf = new byte[1024];
			int len;
			while ((len = in .read(buf)) > 0) {
				out.write(buf, 0, len);
			}
			out.close(); in .close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (cmd.getName().equalsIgnoreCase("home")) {
			if (getPEX() != null) {
				PermissionManager pex = getPEX();
				if ((sender instanceof Player)) {
					if ((pex.has((Player) sender, "Home.use") | sender.isOp())) {
						Player player = (Player) sender;
						player.openInventory(HomeInventory.createHomeInventory(this, player.getName()));
						return true;
					}
					sender.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
					return true;
				}
				sender.sendMessage("You must be a player!");
			} else {
				if ((sender.hasPermission("Home.use") | sender.isOp())) {
					Player player = (Player) sender;
					player.openInventory(HomeInventory.createHomeInventory(this, player.getName()));
					return true;
				}
				sender.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
				return true;
			}
			return true;
		}
		return false;
	}

	public PermissionManager getPEX() {
		Plugin pex = getServer().getPluginManager().getPlugin("PermissionsEx");
		if ((this.pex != null) && (pex != null)) {
			return PermissionsEx.getPermissionManager();
		}
		return null;
	}

	public static Object getInstance() {
		// TODO Auto-generated method stub
		return null;
	}
}