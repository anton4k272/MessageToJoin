package com.github;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

public class MessageToJoin extends JavaPlugin implements Listener {

    private FileConfiguration messagesConfig;

    @Override
    public void onEnable() {
        // Save the default config if it doesn't exist
        this.saveDefaultConfig();
        // Save the default messages file if it doesn't exist
        this.saveDefaultMessages();
        // Load messages from messages.yml
        this.loadMessages();
        // Register the event listener
        Bukkit.getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {
        // Any cleanup logic can go here
    }

    private void saveDefaultMessages() {
        File messagesFile = new File(getDataFolder(), "messages.yml");
        if (!messagesFile.exists()) {
            this.saveResource("messages.yml", false);
        }
    }

    private void loadMessages() {
        File messagesFile = new File(getDataFolder(), "messages.yml");
        messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);
    }

    public String getMessage(String key) {
        return messagesConfig.getString("messages." + key, "Message not found: " + key);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("messagetojoin")) {
            if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
                if (sender.hasPermission("messagetojoin.reload")) {
                    this.reloadConfig();
                    this.loadMessages();
                    sender.sendMessage(getMessage("configReloaded"));
                } else {
                    sender.sendMessage(getMessage("noPermission"));
                }
                return true;
            }
        }
        return false;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        FileConfiguration config = this.getConfig();
        Player player = event.getPlayer();
        if (config.getBoolean("plugin-enable") && player.hasPermission("messagetojoin.message")) {
            long delay = config.getLong("message-delay", 0L); // Get delay from config, default is 0L
            new BukkitRunnable() {
                @Override
                public void run() {
                    for (String key : config.getConfigurationSection("").getKeys(false)) {
                        if (!key.equals("plugin-enable") && !key.equals("message-delay")) {
                            String message = config.getString(key);
                            player.sendMessage(message);
                        }
                    }
                }
            }.runTaskLater(this, delay * 20L); // Delay is in seconds, so multiply by 20 to convert to ticks
        }
    }
}