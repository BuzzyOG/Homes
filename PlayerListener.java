package me.Jaaakee224.Homes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;

public class PlayerListener
implements Listener {
    private Homes Homes;
    private ArrayList < String > namePendingList = new ArrayList < String > ();

    public PlayerListener(Homes Homes) {
        this.Homes = Homes;
    }

    @
    EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (((event.getEntity() instanceof Player)) && (event.getCause().equals(EntityDamageEvent.DamageCause.SUFFOCATION))) {
            event.setCancelled(true);
        }
    }

    @
    EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        final Player player = (Player) event.getWhoClicked();
        Inventory inventory = event.getInventory();
        int slotNum = event.getRawSlot();

        String newPlayerName = player.getName();
        if (newPlayerName.length() >= 11) {
            newPlayerName = newPlayerName.substring(0, 11);
        }
        int homeCount = 0;
        if (this.Homes.homeConfig.contains(player.getName())) {
            homeCount = this.Homes.homeConfig.getConfigurationSection(player.getName()).getKeys(false).size();
        }
        if (inventory.getName().equals(newPlayerName + "'s Homes " + ChatColor.GREEN + "[Teleport]")) {
            if (slotNum < inventory.getSize()) {
                if (slotNum == inventory.getSize() - 2) {
                    this.namePendingList.add(player.getName());
                    player.sendMessage(ChatColor.GREEN + "Enter a name for your home.");
                    player.closeInventory();
                } else if (slotNum == inventory.getSize() - 1) {
                    player.openInventory(HomeInventory.createDeleteHomeInventory(this.Homes, player.getName()));
                } else if ((slotNum >= 0) && (slotNum <= homeCount) && (inventory.getItem(slotNum) != null)) {
                    String homeName = inventory.getItem(slotNum).getItemMeta().getDisplayName();
                    double x = this.Homes.homeConfig.getInt(player.getName() + "." + homeName + "." + "x") + 0.5D;
                    double y = this.Homes.homeConfig.getInt(player.getName() + "." + homeName + "." + "y");
                    double z = this.Homes.homeConfig.getInt(player.getName() + "." + homeName + "." + "z") + 0.5D;
                    float pitch = this.Homes.homeConfig.getInt(player.getName() + "." + homeName + "." + "pitch");
                    float yaw = this.Homes.homeConfig.getInt(player.getName() + "." + homeName + "." + "yaw");
                    String worldName = this.Homes.homeConfig.getString(player.getName() + "." + homeName + "." + "world");
                    World world = Bukkit.getWorld(worldName);
                    final Location location = new Location(world, x, y, z, yaw, pitch);

                    Entity entity = player.getVehicle();
                    if (entity != null) {
                        entity.eject();
                    }
                    boolean loaded = world.getChunkAt(location).load();
                    if (loaded) {
                        if (entity != null) {
                            if ((entity.getType().name().equals("BOAT") | entity.getType().name().equals("MINECART"))) {
                                player.getServer().getScheduler().scheduleSyncDelayedTask(this.Homes, new Runnable() {
                                    public void run() {
                                        player.teleport(location);
                                    }
                                }, 2L);
                            } else {
                                entity.teleport(location);
                                player.teleport(location);
                                entity.setPassenger(player);
                            }
                        } else {
                            player.teleport(location);
                        }
                        player.sendMessage(ChatColor.GREEN + "You have arrived at " + homeName + "!");
                    } else {
                        if (entity != null) {
                            if ((entity.getType().name().equals("BOAT") | entity.getType().name().equals("MINECART"))) {
                                player.getServer().getScheduler().scheduleSyncDelayedTask(this.Homes, new Runnable() {
                                    public void run() {
                                        player.teleport(location);
                                    }
                                }, 2L);
                            } else {
                                entity.teleport(location);
                                player.teleport(location);
                            }
                        }
                        player.sendMessage(ChatColor.GREEN + "You have arrived at " + homeName + "!");
                        while (!player.getWorld().isChunkLoaded(world.getChunkAt(location))) {
                            player.teleport(location);
                        }
                        if (entity != null) {
                            if (((entity.getType().name().equals("BOAT") ? 0 : 1) | (entity.getType().name().equals("MINECART") ? 0 : 1)) != 0) {
                                entity.setPassenger(player);
                            }
                        }
                    }
                }
            }
            event.setCancelled(true);
        } else if (inventory.getName().equals(newPlayerName + "'s Homes " + ChatColor.RED + "[Delete]")) {
            if (slotNum == inventory.getSize() - 1) {
                player.openInventory(HomeInventory.createHomeInventory(this.Homes, player.getName()));
            } else if ((slotNum >= 0) && (slotNum <= homeCount)) {
                if (inventory.getItem(slotNum) != null) {
                    String homeName = inventory.getItem(slotNum).getItemMeta().getDisplayName();
                    if (this.Homes.homeConfig.contains(player.getName() + "." + homeName)) {
                        this.Homes.homeConfig.set(player.getName() + "." + homeName, null);
                        player.sendMessage(ChatColor.GREEN + "Home deleted!");
                    }
                    if (this.Homes.homeConfig.contains(player.getName())) {
                        Set < String > homelist = this.Homes.homeConfig.getConfigurationSection(player.getName()).getKeys(false);
                        if (homelist.size() == 0) {
                            this.Homes.homeConfig.set(player.getName(), null);
                        }
                    }
                }
                player.openInventory(HomeInventory.createDeleteHomeInventory(this.Homes, player.getName()));
            }
            event.setCancelled(true);
        } else if ((inventory.getName().equals("Select Icon")) &&
            (slotNum >= 0) && (slotNum < 27)) {
            String homeName = inventory.getItem(slotNum).getItemMeta().getDisplayName();
            if (this.Homes.homeConfig.contains(player.getName())) {
                Set < String > homelist = this.Homes.homeConfig.getConfigurationSection(player.getName()).getKeys(false);

                int homeLimit = 0;
                for (int i = 1; i <= 52; i++) {
                    if (player.hasPermission("Homes.limit." + i)) {
                        homeLimit = i;
                    }
                }
                if (homeLimit == 0) {
                    homeLimit = 1;
                }
                if (homelist.size() < homeLimit) {
                    this.Homes.homeConfig.set(player.getName() + "." + homeName + "." + "icon", inventory.getItem(slotNum).getType().name());
                    this.Homes.homeConfig.set(player.getName() + "." + homeName + "." + "world", player.getWorld().getName());
                    this.Homes.homeConfig.set(player.getName() + "." + homeName + "." + "x", Integer.valueOf(player.getLocation().getBlockX()));
                    this.Homes.homeConfig.set(player.getName() + "." + homeName + "." + "y", Integer.valueOf(player.getLocation().getBlockY()));
                    this.Homes.homeConfig.set(player.getName() + "." + homeName + "." + "z", Integer.valueOf(player.getLocation().getBlockZ()));
                    this.Homes.homeConfig.set(player.getName() + "." + homeName + "." + "pitch", Float.valueOf(player.getLocation().getPitch()));
                    this.Homes.homeConfig.set(player.getName() + "." + homeName + "." + "yaw", Float.valueOf(player.getLocation().getYaw()));
                    player.sendMessage(ChatColor.GREEN + "Home '" + homeName + "' created!");
                    player.closeInventory();
                } else {
                    player.sendMessage(ChatColor.RED + "You have reached your allowed maximum of houses!");
                }
            } else {
                this.Homes.homeConfig.set(player.getName() + "." + homeName + "." + "icon", inventory.getItem(slotNum).getType().name());
                this.Homes.homeConfig.set(player.getName() + "." + homeName + "." + "world", player.getWorld().getName());
                this.Homes.homeConfig.set(player.getName() + "." + homeName + "." + "x", Integer.valueOf(player.getLocation().getBlockX()));
                this.Homes.homeConfig.set(player.getName() + "." + homeName + "." + "y", Integer.valueOf(player.getLocation().getBlockY()));
                this.Homes.homeConfig.set(player.getName() + "." + homeName + "." + "z", Integer.valueOf(player.getLocation().getBlockZ()));
                this.Homes.homeConfig.set(player.getName() + "." + homeName + "." + "pitch", Float.valueOf(player.getLocation().getPitch()));
                this.Homes.homeConfig.set(player.getName() + "." + homeName + "." + "yaw", Float.valueOf(player.getLocation().getYaw()));
                player.sendMessage(ChatColor.GREEN + "Home '" + homeName + "' created!");
            }
            player.closeInventory();
        }
        try {
            this.Homes.homeConfig.save(this.Homes.homeDataFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @
    EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        if (this.namePendingList.contains(player.getName())) {
            String homeName = event.getMessage();
            event.setCancelled(true);
            for (int i = 0; i < this.namePendingList.size(); i++) {
                if (((String) this.namePendingList.get(i)).equals(player.getName())) {
                    player.openInventory(HomeInventory.createSetIconInventory(this.Homes, homeName));
                    player.sendMessage(ChatColor.GREEN + "Select an Icon.");
                    this.namePendingList.remove(i);
                    break;
                }
            }
        }
    }

    @
    EventHandler
    public void onPlayerExit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (this.namePendingList.contains(player.getName())) {
            for (int i = 0; i < this.namePendingList.size(); i++) {
                if (((String) this.namePendingList.get(i)).equals(player.getName())) {
                    this.namePendingList.remove(i);
                    break;
                }
            }
        }
    }
}
