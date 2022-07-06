package io.github.gifcha.hashpass;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Base64;


public class CommandRegister implements CommandExecutor {



    private byte[] generateHash(String password, byte[] salt) throws NoSuchAlgorithmException, InvalidKeySpecException {
        KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 65536, 128);
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        return factory.generateSecret(spec).getEncoded();
    }

    FileConfiguration accounts = HashPass.accounts;
    SecureRandom random = new SecureRandom();
    Base64.Encoder enc = Base64.getEncoder();
    FileConfiguration config = HashPass.config;

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(sender.hasPermission("hashpass.register") && sender instanceof Player) {
            if(!accounts.contains(sender.getName()) || accounts.get(sender.getName()) == null) { // if no account or password null

                byte[] hashedPassword;
                String hash = null;
                byte[] salt = new byte[16];
                random.nextBytes(salt);
                try {
                    hashedPassword = generateHash(args[0], salt); // generate hashed password
                    hash = enc.encodeToString(hashedPassword);
                } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
                    e.printStackTrace();
                }

                if (args.length == 2 && args[0] != null && args[1] != null && args[0].equals(args[1])) { // check if retyped password is the same and password type
                    accounts.set(sender.getName()+".password", hash); // add password to config/database
                    accounts.set(sender.getName()+".salt", salt); // add salt to database
                    HashPass.loggedIn.put(sender.getName(), true); // set logged in to true to unfreeze player
                    sender.sendMessage(config.getString("messages.register_success")); // send message register successful
                    HashPass.saveAccounts();
                }
                else if (args.length == 2 && !args[0].equals(args[1])) {
                    sender.sendMessage(config.getString("messages.register_confirm_pass")); // send message not confirmed password
                }
                else {
                    sender.sendMessage(config.getString("messages.register_fail")); // send message register fail
                }
            }
            else {
                sender.sendMessage(config.getString("messages.already_registered")); // send message already registered
            }
        }
        return true;
    }
}
