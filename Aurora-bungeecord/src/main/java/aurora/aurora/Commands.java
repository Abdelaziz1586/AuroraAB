package aurora.aurora;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Command;

import static aurora.aurora.AuroraBungeeCord.CommandPermission;

public class Commands extends Command {
    private final AuroraBungeeCord mainClass;

    public Commands(AuroraBungeeCord main) {
        super("Aurora");
        this.mainClass = main;
    }

    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof net.md_5.bungee.api.connection.ProxiedPlayer)) {
            if(args.length == 1) {
                if (args[0].equalsIgnoreCase("reload")) {
                    sender.sendMessage(new TextComponent("§eReloading..."));
                    this.mainClass.reloadConfig();
                    sender.sendMessage(new TextComponent("§aReloaded The Config."));
                } else {
                    sender.sendMessage(new TextComponent("§cInvalid arguments (Reload)"));
                }
            } else {
                sender.sendMessage(new TextComponent("§cInvalid arguments (Reload)"));
            }
        } else if (sender.hasPermission(CommandPermission)) {
            if(args.length == 1) {
                if (args[0].equalsIgnoreCase("reload")) {
                    sender.sendMessage(new TextComponent("§eReloading..."));
                    this.mainClass.reloadConfig();
                    sender.sendMessage(new TextComponent("§aReloaded The Config."));
                } else {
                    sender.sendMessage(new TextComponent("§cInvalid arguments (Reload)"));
                }
            } else {
                sender.sendMessage(new TextComponent("§cInvalid arguments (Reload)"));
            }
        }
    }
}