package io.github.gifcha.hashpass;

import io.github.gifcha.hashpass.events.pluginEvents;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Objects;

public final class HashPass extends JavaPlugin {


    public static HashMap<String, Boolean> loggedIn = new HashMap<String, Boolean>(); //hash map of logged in players

    public static FileConfiguration accounts;
    public static File accountFile;
    public static FileConfiguration config = null;
    public static HashPass that; //in your case "plugin" would be "HashPass.that"


    private void createCustomConfig() {
        accountFile = new File(getDataFolder(), "accounts.yml");
        accounts = YamlConfiguration.loadConfiguration(accountFile);
    }

    public static void saveAccounts() {
        try {
            accounts.save(accountFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onEnable() {
        that = this;
        config = getConfig();
        createCustomConfig();
        CommandLogin loginExecutor = new CommandLogin();
        CommandRegister registerExecutor = new CommandRegister();
        Objects.requireNonNull(this.getCommand("register")).setExecutor(registerExecutor);
        Objects.requireNonNull(this.getCommand("login")).setExecutor(loginExecutor);
        Objects.requireNonNull(this.getCommand("unregister")).setExecutor(new CommandUnregister());
        getServer().getPluginManager().registerEvents(new pluginEvents(), this);
        this.saveDefaultConfig();
        saveAccounts();
        getServer().getConsoleSender().sendMessage(ChatColor.GREEN+"[HashPass]: enabled");

    }



    @Override
    public void onDisable() {
        that = null;
        getServer().getConsoleSender().sendMessage(ChatColor.RED+"[HashPass]: disabled");
        saveAccounts();
    }


}
