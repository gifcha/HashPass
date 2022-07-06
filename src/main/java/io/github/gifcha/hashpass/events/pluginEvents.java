package io.github.gifcha.hashpass.events;

import io.github.gifcha.hashpass.HashPass;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;


public class pluginEvents implements Listener {

    static FileConfiguration config = HashPass.config;
    HashMap<String, Integer> LoginTaskList = new HashMap<String, Integer>();
    HashMap<String, Integer> RegisterTaskList = new HashMap<String, Integer>();
    HashMap<String, String> joinedIps = new HashMap<String, String>();

    public boolean IsLoggedIn(Player player) { // checks if given player is logged in
        return HashPass.loggedIn.get(player.getName()) != null;
    }

    public void registerCheck(Player player, ItemStack[] inv) { // Function for showing the player the register title
        RegisterTaskList.put(player.getName(), Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(HashPass.that, new Runnable() {
            @Override
            public void run() {
                if (Bukkit.getPlayerExact(player.getName()) == null) { StopTask(); } // if the player isn't online stop
                if (IsLoggedIn(player)) {
                    if (((String) config.get("options.hide_inventory")).equals("true")) { player.getInventory().setContents(inv); }
                    player.resetTitle(); // remove register title
                    StopTask(); // stop the task connected to the player
                } else {
                    player.sendTitle((String) config.get("messages.register_title"), (String) config.get("messages.register_subtitle"), 0, 20, 0);
                }
            } // delay in ticks

            public void StopTask() {
                Bukkit.getServer().getScheduler().cancelTask(RegisterTaskList.get(player.getName()));
            }
        }, 0/* delay before first run */, 0 /* delay between runs */));
    }

    public void loginCheck(Player player, ItemStack[] inv, Location savedLocation) { // Function for showing the player the login title
        LoginTaskList.put(player.getName(), Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(HashPass.that, new Runnable() {
            @Override
            public void run() {
                if (Bukkit.getPlayerExact(player.getName()) == null) { StopTask(); } // if the player isn't online stop
                if (IsLoggedIn(player)) {
                    if ( config.getBoolean("options.hide_inventory")) { player.getInventory().setContents(inv); }
                    if ( config.getBoolean("options.hide_location_on_spawn")) { player.teleport(savedLocation); }
                    player.resetTitle(); // remove login title
                    StopTask(); // stop the task connected to the player
                } else {
                    player.sendTitle((String) config.get("messages.login_title"), (String) config.get("messages.login_subtitle"), 0, 20, 0);
                }
            } // delay in ticks

            public void StopTask() {
                Bukkit.getServer().getScheduler().cancelTask(LoginTaskList.get(player.getName()));
            }
        }, 0/* delay before first run */, 0 /* delay between runs */));
    }




    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (!IsLoggedIn(player)) { // if not logged in
            event.setCancelled(true); // cancel movement
            player.setAllowFlight(true);
        }
        else if(IsLoggedIn(player) && player.getGameMode() != GameMode.CREATIVE) { // if logged in and not in creative
            player.setAllowFlight(false);
        }
    }

    @EventHandler
    public void onBreakBlock(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (player != null && !IsLoggedIn(player)) { // if not logged in
            event.setCancelled(true); // cancel block break
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        String playerIp = player.getAddress().getHostString();
        if(IsLoggedIn(player)) { // if logged in
            HashPass.loggedIn.remove(player.getName()); // remove logged in status
        }
        if (joinedIps.get(playerIp).equals(player.getName())) { // check if the ip belongs to this player
            joinedIps.remove(playerIp); // if so remove it to the list
        }
    }



    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        String playerIp = player.getAddress().getHostString();
        if (joinedIps.containsKey(playerIp)) { player.kickPlayer("§cA player with the same address already exists!"); } // check if there is another player with the same IP
        else { joinedIps.put(playerIp, player.getName()); } // if there isn't add this ip to the list

        ItemStack[] savedInv = player.getInventory().getContents(); // store the player's inventory while hiding it
        boolean hideInv = config.getBoolean("options.hide_inventory");
        if (hideInv) { player.getInventory().clear(); }

        Location savedLocation = player.getLocation();
        boolean hideLoc = config.getBoolean("options.hide_location_on_spawn");
        if (hideLoc) { player.teleport(player.getServer().getWorlds().get(0).getSpawnLocation()); }

        // teleport player to spawn if option teleport_to_spawn is set to true
        boolean tpToSpawn = config.getBoolean("options.teleport_on_spawn");
        if (tpToSpawn) { player.teleport(player.getServer().getWorlds().get(0).getSpawnLocation()); }

        // if the player isn't registered
        if (!HashPass.accounts.contains(player.getName()) || HashPass.accounts.get(player.getName()+".password") == null) {
            // sets permanent register title until player logs in
            // Creates a new repeating task
            registerCheck(player, savedInv);
        }

        // if the player is registered
        else {
            // sets permanent login title until player logs in
            // Creates a new repeating task
            loginCheck(player, savedInv, savedLocation);
        }
    }



    @EventHandler
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        String message = event.getMessage();
        if (!IsLoggedIn(player)) { // if not logged in
            if (message.startsWith("/register") || message.startsWith("/login") || message.startsWith("/reg") || message.startsWith("/l")) {
                event.setCancelled(false);
            }
            else {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPlayerMessage(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        if (!IsLoggedIn(player)){ // if not logged in
            event.setCancelled(true); // cancel send chat
        }
    }

    @EventHandler
    public void onPlayerDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            if (!IsLoggedIn(player)) { // if not logged in
                event.setCancelled(true); // disable damage
            }
        }
    }

    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent event) {
        Player player = (Player) event.getPlayer();
        if (!IsLoggedIn(player)){ // if not logged in
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        if (!IsLoggedIn(player)) { // if not logged in
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onItemPickup(EntityPickupItemEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            if (!IsLoggedIn(player)) { // if not logged in
                event.setCancelled(true); // disable item pickup
            }
        }
    }

    @EventHandler
    public void onPlayerEat(PlayerItemConsumeEvent event) {
        Player player = event.getPlayer();
        if (!IsLoggedIn(player)) { // if not logged in
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (!IsLoggedIn(player)) { // if not logged in
            event.setCancelled(true);
        }
    }
}
