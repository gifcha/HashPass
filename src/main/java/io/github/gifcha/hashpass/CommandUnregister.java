package io.github.gifcha.hashpass;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

public class CommandUnregister implements CommandExecutor {

    FileConfiguration accounts = HashPass.accounts;
    FileConfiguration config = HashPass.config;

    public void Unregister(CommandSender sender, String target) {
        if(accounts.contains(target)) {
            accounts.set(target, null);
            HashPass.saveAccounts();
            HashPass.loggedIn.remove(target);
            String message = config.getString("messages.unregister_success"); // set message variable for processing
            if (message.contains("%player%")) { message = message.replace("%player%", target); } // if message contains %player% replace it with target variable
            sender.sendMessage(message); // send message unregister success

            if (Bukkit.getOnlinePlayers().contains(target)) {
                Player player = Bukkit.getPlayer(target);
                player.kickPlayer(config.getString("messages.unregister_kick_message"));
            }

        }
        else {
            sender.sendMessage(config.getString("messages.unregister_fail")); // send message player not registered
        }
    }



    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender.hasPermission("hashpass.unregister.self") && args.length == 0) { // unregister self
            Unregister(sender, sender.getName());
            return true;
        }
        else if(sender.hasPermission("hashpass.unregister.other") && args.length < 2 && args[0] != null) { // unregister other
            String target = args[0];
            Unregister(sender, target);
        }
        else if (!sender.hasPermission("hashpass.unregister")) {
            sender.sendMessage(config.getString("messages.unregister_no_permission")); // send message no permission
        }
        else{
            sender.sendMessage(config.getString("messages.unregister_usage")); // send message wrong usage
        }
        return true;
    }
}
