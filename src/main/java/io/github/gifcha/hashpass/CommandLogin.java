package io.github.gifcha.hashpass;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Base64;

public class CommandLogin implements CommandExecutor {

    FileConfiguration accounts = HashPass.accounts;
    Base64.Encoder enc = Base64.getEncoder();
    FileConfiguration config = HashPass.config;

    private byte[] generateHash(String password, byte[] salt) throws NoSuchAlgorithmException, InvalidKeySpecException {
        KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 65536, 128);
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        return factory.generateSecret(spec).getEncoded();
    }


    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender.hasPermission("hashpass.login") && sender instanceof Player) { // if has permission and is player
            if (!HashPass.loggedIn.containsKey(sender.getName())) { // if not logged in
                if (accounts.contains(sender.getName())) { // if account is registered
                    String hash = null;
                    byte[] salt = (byte[]) accounts.get(sender.getName() + ".salt"); // get the salt from file
                    try {
                        byte[] h = generateHash(args[0], salt); // generate hash for input password using the saved salt
                        hash = enc.encodeToString(h); // encode hash so its readable
                    } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
                        e.printStackTrace();
                    }

                    if (accounts.get(sender.getName()+".password").equals(hash)) { // compare hashed password and hashed input
                        HashPass.loggedIn.put(sender.getName(), true);  // if password is correct set logged in
                        sender.sendMessage(config.getString("messages.login_success")); // send login success message
                        return true;
                    } else {
                        HashPass.that.getServer().getConsoleSender().sendMessage(ChatColor.RED+"failed login!");
                        sender.sendMessage(config.getString("messages.login_fail")); // send login fail message
                        ((Player) sender).kickPlayer((String) config.get("messages.login_fail")); // kick and display login fail message
                    }
                }
                else{
                    sender.sendMessage(config.getString("messages.not_registered")); // send not registered message
                }
            }
            else{
                sender.sendMessage(config.getString("messages.already_loggedin")); // send already logged in message
            }
        }
        return true;
    }
}
