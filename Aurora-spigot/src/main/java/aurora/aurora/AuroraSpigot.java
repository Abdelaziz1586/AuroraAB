package aurora.aurora;

import aurora.SpigotMain;
import aurora.checks.SpigotProxyChecker;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

public final class AuroraSpigot extends JavaPlugin implements Listener {

    HashMap<String, JSONObject> IPInfo = new HashMap<>();

    List<?> lastBlockedCountries;

    String lastReconnectMessage;
    String lastBlockedMessage;
    String lastIllegalCharactersMessage;
    String lastCommandPermission;
    String lastProxyBlockedMessage;
    String lastBlockedCountryMessage;

    boolean lastConsoleFilter;
    boolean lastProxyDetection;

    @Override
    public void onEnable() {
        sendConsoleMessage("§eLoading §bAurora");

        Bukkit.getPluginManager().registerEvents(this, this);
        getConfig().options().copyDefaults(true);
        saveDefaultConfig();

        this.lastReconnectMessage = getConfig().getString("reconnect-message");
        this.lastBlockedMessage = getConfig().getString("blocked-message");
        this.lastIllegalCharactersMessage = getConfig().getString("illegal-characters-message");
        this.lastCommandPermission = getConfig().getString("command-permission");
        this.lastProxyBlockedMessage = getConfig().getString("proxy-blocked-message");
        this.lastBlockedCountryMessage = getConfig().getString("blocked-country-message");
        this.lastProxyDetection = getConfig().getBoolean("proxy-detections");
        this.lastConsoleFilter = getConfig().getBoolean("console-filter");
        this.lastBlockedCountries = getConfig().getList("blocked-countries");
        reloadPluginConfig();

        new AuroraLoggerSpigot().registerFilter();

        sendConsoleMessage("§aLoaded §bAurora");
    }

    @Override
    public void onDisable() {
        sendConsoleMessage("§bAurora §cHas Been Unloaded");
    }

    public void sendConsoleMessage(String message) {
        getServer().getConsoleSender().sendMessage(message);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender s, Command cmd, @NotNull String label, String[] args) {
        if(cmd.getName().equalsIgnoreCase("aurora")) {
            if (!(s instanceof Player)) {
                if(args.length == 1) {
                    if (args[0].equalsIgnoreCase("reload")) {
                        s.sendMessage("§eReloading...");
                        reloadPluginConfig();
                        s.sendMessage("§aReloaded The Config.");
                    } else {
                        s.sendMessage("§cInvalid arguments (Reload)");
                    }
                } else {
                    s.sendMessage("§cInvalid arguments (Reload)");
                }
            } else if (s.hasPermission(SpigotMain.CommandPermission)) {
                if(args.length == 1) {
                    if (args[0].equalsIgnoreCase("reload")) {
                        s.sendMessage("§eReloading...");
                        reloadPluginConfig();
                        s.sendMessage("§aReloaded The Config.");
                    } else {
                        s.sendMessage("§cInvalid arguments (Reload)");
                    }
                } else {
                    s.sendMessage("§cInvalid arguments (Reload)");
                }
            }
        }
        return true;
    }

    @EventHandler
    public void onPlayerPreLogin(AsyncPlayerPreLoginEvent e) throws IOException {
        String IP = e.getAddress().getHostAddress();
        String name = e.getName();
        String numRegex = ".*[0-9].*";
        String alphaRegex = ".*[A-Z-a-z].*";

        if (!(name.matches(numRegex) || name.matches(alphaRegex)) || name.contains(" ")) {
            e.disallow(AsyncPlayerPreLoginEvent.Result.KICK_BANNED, SpigotMain.translate(SpigotMain.IllegalCharactersBlockMessage));
            return;
        }

        if (!IPInfo.containsKey(IP)) createDefaultJSONConfig(IP);

        String savedName = getName(IP);

        if (isBlacklisted(IP)) {
            e.disallow(AsyncPlayerPreLoginEvent.Result.KICK_BANNED, SpigotMain.translate(SpigotMain.BlockedMessage));
        } else if (!IPInfo.containsKey(IP)) {
            e.disallow(AsyncPlayerPreLoginEvent.Result.KICK_BANNED, SpigotMain.translate(SpigotMain.ReconnectMessage));
            replaceName(IP, name);
        } else if (!savedName.equals(name)) {
            e.disallow(AsyncPlayerPreLoginEvent.Result.KICK_BANNED, SpigotMain.translate(SpigotMain.ReconnectMessage));
            replaceName(IP, name);
            addTry(IP);
        } else {
            removeTry(IP);
            SpigotProxyChecker.saveIPInfo(IP);
            if (SpigotMain.BlackListCountries.contains(SpigotProxyChecker.getCountry(IP)))
                e.disallow(AsyncPlayerPreLoginEvent.Result.KICK_BANNED, SpigotMain.translate(SpigotMain.BlackListCountryMessage));
            if (SpigotMain.ProxyDetections && SpigotProxyChecker.isProxy(IP))
                e.disallow(AsyncPlayerPreLoginEvent.Result.KICK_BANNED, SpigotMain.translate(SpigotMain.ProxyBlockMessage));
        }
    }

    public void addTry(String IP) {
        JSONObject jsonObject = IPInfo.get(IP);
        int i = jsonObject.getInt("Fails");
        i++;
        if (i > 3) {
            blackList(IP);
        } else {
            jsonObject.remove("Fails");
            jsonObject.put("Fails", i);
            saveJSONInfo(IP, jsonObject);
        }
    }

    public void removeTry(String IP) {
        JSONObject jsonObject = IPInfo.get(IP);
        int i = jsonObject.getInt("Fails");
        if (i == 0) return;
        i--;
        jsonObject.remove("Fails");
        jsonObject.put("Fails", i);
        saveJSONInfo(IP, jsonObject);
    }

    public void replaceName(String IP, String newName) {
        if (IPInfo.get(IP).getString("Name").equals(newName)) return;
        JSONObject jsonObject = IPInfo.get(IP);
        jsonObject.remove("Name");
        jsonObject.put("Name", newName);
        saveJSONInfo(IP, jsonObject);
    }

    public void blackList(String IP) {
        if (isBlacklisted(IP)) return;
        JSONObject jsonObject = IPInfo.get(IP);
        jsonObject.remove("isBlacklisted");
        jsonObject.put("isBlacklisted", true);
        saveJSONInfo(IP, jsonObject);
    }

    public void createDefaultJSONConfig(String IP) {
        if (IPInfo.containsKey(IP)) return;
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("isBlacklisted", false);
        jsonObject.put("Name", "");
        jsonObject.put("Fails", 0);
        saveJSONInfo(IP, jsonObject);
    }

    public void saveJSONInfo(String IP, JSONObject jsonObject) {
        IPInfo.remove(IP);
        IPInfo.put(IP, jsonObject);
    }

    public String getName(String IP) {
        return IPInfo.get(IP).getString("Name");
    }

    public boolean isBlacklisted(String IP) {
        return IPInfo.get(IP).getBoolean("isBlacklisted");
    }

    public void reloadPluginConfig() {
        reloadConfig();
        SpigotMain.ReconnectMessage = getConfig().getString("reconnect-message");
        SpigotMain.BlockedMessage = getConfig().getString("blocked-message");
        SpigotMain.IllegalCharactersBlockMessage = getConfig().getString("illegal-characters-message");
        SpigotMain.CommandPermission = getConfig().getString("command-permission");
        SpigotMain.ProxyBlockMessage = getConfig().getString("proxy-blocked-message");
        SpigotMain.BlackListCountryMessage = getConfig().getString("blocked-country-message");
        SpigotMain.BlackListCountries = getConfig().getList("blocked-countries");
        SpigotMain.ProxyDetections = getConfig().getBoolean("proxy-detections");
        SpigotMain.ConsoleFilter = getConfig().getBoolean("console-filter");

        if(!this.lastReconnectMessage.equals(SpigotMain.ReconnectMessage)) sendConsoleMessage(SpigotMain.translate("\n&eChanged reconnect message from\n" + lastReconnectMessage + "\n&eTo\n" + SpigotMain.ReconnectMessage));
        if(!this.lastBlockedMessage.equals(SpigotMain.BlockedMessage)) sendConsoleMessage(SpigotMain.translate("\n&eChanged blocked message from\n" + lastBlockedMessage + "\n&eTo\n" + SpigotMain.BlockedMessage));
        if(!this.lastIllegalCharactersMessage.equals(SpigotMain.IllegalCharactersBlockMessage)) sendConsoleMessage(SpigotMain.translate("\n&eChanged illegal characters message from\n" + lastIllegalCharactersMessage + "\n&eTo\n" + SpigotMain.IllegalCharactersBlockMessage));
        if(!this.lastProxyBlockedMessage.equals(SpigotMain.ProxyBlockMessage)) sendConsoleMessage(SpigotMain.translate("\n&eChanged proxy blocked message from\n" + lastProxyBlockedMessage + "\n&eTo\n" + SpigotMain.ProxyBlockMessage));
        if(!this.lastCommandPermission.equals(SpigotMain.CommandPermission)) sendConsoleMessage(SpigotMain.translate("&eChanged command permission from &c" + lastCommandPermission + " &eTo &a" + SpigotMain.CommandPermission));
        if(!this.lastBlockedCountryMessage.equals(SpigotMain.BlackListCountryMessage)) sendConsoleMessage(SpigotMain.translate("\n&eChanged country blocked message from\n" + lastBlockedCountryMessage + "\n&eTo\n" + SpigotMain.BlackListCountryMessage));
        if(!this.lastBlockedCountries.equals(SpigotMain.BlackListCountries)) sendConsoleMessage(SpigotMain.translate("&aUpdated Blocked Country List."));

        if(!this.lastConsoleFilter == SpigotMain.ConsoleFilter) {
            String To;
            String From;
            if(SpigotMain.ConsoleFilter) {
                To = "&aTrue";
                From = "&cFalse";
            } else {
                To = "&cFalse";
                From = "&aTrue";
            }
            sendConsoleMessage(SpigotMain.translate("&eSwitched Console Filter modes. " + From + " &b-> " + To));
        }
        if(!this.lastProxyDetection == SpigotMain.ProxyDetections) {
            String To;
            String From;
            if(SpigotMain.ProxyDetections) {
                To = "&aTrue";
                From = "&cFalse";
            } else {
                To = "&cFalse";
                From = "&aTrue";
            }
            sendConsoleMessage(SpigotMain.translate("&eSwitched Proxy Detection modes. " + From + " &b-> " + To));
        }
        this.lastReconnectMessage = SpigotMain.ReconnectMessage;
        this.lastBlockedMessage = SpigotMain.BlockedMessage;
        this.lastIllegalCharactersMessage = SpigotMain.IllegalCharactersBlockMessage;
        this.lastCommandPermission = SpigotMain.CommandPermission;
        this.lastConsoleFilter = SpigotMain.ConsoleFilter;
        this.lastProxyBlockedMessage = SpigotMain.ProxyBlockMessage;
        this.lastBlockedCountryMessage = SpigotMain.BlackListCountryMessage;
        this.lastProxyDetection = SpigotMain.ProxyDetections;
        this.lastBlockedCountries = SpigotMain.BlackListCountries;
    }

}
