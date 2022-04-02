package aurora.aurora;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.json.JSONObject;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public final class AuroraSpigot extends JavaPlugin implements Listener {

    HashMap<String, UUID> PlayerDetails = new HashMap<>();
    HashMap<String, Integer> Tries = new HashMap<>();
    ArrayList<String> Blocked = new ArrayList<>();
    HashMap<String, JSONObject> IPInfo = new HashMap<>();
    List<?> BlockedCountries;
    List<?> lastBlockedCountries;

    String ReconnectMessage;
    String lastReconnectMessage;
    String BlockedMessage;
    String CommandPermission;
    String ProxyBlockedMessage;
    String BlockedCountryMessage;
    String lastBlockedMessage;
    String IllegalCharactersMessage;
    String lastIllegalCharactersMessage;
    String lastCommandPermission;
    String lastProxyBlockedMessage;
    String lastBlockedCountryMessage;

    static boolean ConsoleFilter;
    boolean ProxyDetection;
    boolean lastConsoleFilter;
    boolean lastProxyDetection;

    @Override
    public void onEnable() {
        sendConsoleMessage("§eLoading §bAurora");

        getServer().getPluginManager().registerEvents(this, this);
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
        sendConsoleMessage("§bAurora §cHas Been Unloadd");
    }

    @EventHandler
    public void onConnect(AsyncPlayerPreLoginEvent e) throws IOException {
        String numRegex   = ".*[0-9].*";
        String alphaRegex = ".*[A-Z-a-z].*";
        String IP = e.getAddress().getHostAddress();
        UUID uuid = e.getUniqueId();
        String name= e.getName();

        JSONObject object = getIPInfo(IP);

        if (!(name.matches(numRegex) || name.matches(alphaRegex)) || name.contains(" ")) {
            e.disallow(AsyncPlayerPreLoginEvent.Result.KICK_BANNED, translate(IllegalCharactersMessage));
        } else {
            if (!Blocked.contains(IP)) {
                if (!PlayerDetails.containsKey(IP)) {
                    e.disallow(AsyncPlayerPreLoginEvent.Result.KICK_BANNED, translate(ReconnectMessage));
                    replaceUUID(IP, uuid);
                } else if (!PlayerDetails.get(IP).equals(uuid)) {
                    e.disallow(AsyncPlayerPreLoginEvent.Result.KICK_BANNED, translate(ReconnectMessage));
                    addTry(IP);
                    replaceUUID(IP, uuid);
                } else {
                    removeTry(IP);
                    if(BlockedCountries.contains(object.get("country").toString())) {
                        e.disallow(AsyncPlayerPreLoginEvent.Result.KICK_BANNED, translate(BlockedCountryMessage));
                        return;
                    }
                    if(ProxyDetection) if(object.get("proxy").equals("true")) e.disallow(AsyncPlayerPreLoginEvent.Result.KICK_BANNED, translate(ProxyBlockedMessage));
                }
            } else {
                e.disallow(AsyncPlayerPreLoginEvent.Result.KICK_BANNED, translate(BlockedMessage));
            }
        }
    }

    public void replaceUUID(String IP, UUID uuid) {
        PlayerDetails.remove(IP);
        PlayerDetails.put(IP, uuid);
    }

    public void addTry(String IP) {
        int tries = 0;
        if(Tries.containsKey(IP)) tries = Tries.get(IP);
        tries++;
        if(tries > 5) {
            Blocked.add(IP);
        }
        Tries.remove(IP);
        Tries.put(IP, tries);
    }

    public void removeTry(String IP) {
        int tries = 0;
        if(Tries.containsKey(IP)) tries = Tries.get(IP);
        if (tries > 0) {
            tries--;
            Tries.remove(IP);
            Tries.put(IP, tries);
        }
    }

    public JSONObject getIPInfo(String IP) throws IOException {
        if (IPInfo.containsKey(IP)) return IPInfo.get(IP);
        InputStream inputStream;
        URL url = new URL("http://ip-api.com/json/" + IP + "?fields=country,proxy");
        HttpURLConnection http = (HttpURLConnection) url.openConnection();
        http.setRequestProperty("Accept", "application/json");
        int responseCode = http.getResponseCode();
        if (200 <= responseCode && responseCode <= 299) {
            inputStream = http.getInputStream();
        } else {
            inputStream = http.getErrorStream();
        }
        BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder response = new StringBuilder();
        String currentLine;
        while ((currentLine = in.readLine()) != null)
            response.append(currentLine);
        in.close();
        JSONObject object = new JSONObject(response.toString());
        IPInfo.put(IP, object);
        return object;
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
            } else if (s.hasPermission(CommandPermission)) {
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

    public String translate(String text) {
        return ChatColor.translateAlternateColorCodes('&', text).replace("%nl%", "\n");
    }

    public void reloadPluginConfig() {
        reloadConfig();
        this.ReconnectMessage = getConfig().getString("reconnect-message");
        this.BlockedMessage = getConfig().getString("blocked-message");
        this.IllegalCharactersMessage = getConfig().getString("illegal-characters-message");
        this.CommandPermission = getConfig().getString("command-permission");
        this.ProxyBlockedMessage = getConfig().getString("proxy-blocked-message");
        this.BlockedCountryMessage = getConfig().getString("blocked-country-message");
        this.BlockedCountries = getConfig().getList("blocked-countries");
        this.ProxyDetection = getConfig().getBoolean("proxy-detections");
        ConsoleFilter = getConfig().getBoolean("console-filter");

        if(!this.lastReconnectMessage.equals(ReconnectMessage)) sendConsoleMessage(translate("\n&eChanged reconnect message from\n" + lastReconnectMessage + "\n&eTo\n" + ReconnectMessage));
        if(!this.lastBlockedMessage.equals(BlockedMessage)) sendConsoleMessage(translate("\n&eChanged blocked message from\n" + lastBlockedMessage + "\n&eTo\n" + BlockedMessage));
        if(!this.lastIllegalCharactersMessage.equals(IllegalCharactersMessage)) sendConsoleMessage(translate("\n&eChanged illegal characters message from\n" + lastIllegalCharactersMessage + "\n&eTo\n" + IllegalCharactersMessage));
        if(!this.lastProxyBlockedMessage.equals(ProxyBlockedMessage)) sendConsoleMessage(translate("\n&eChanged proxy blocked message from\n" + lastProxyBlockedMessage + "\n&eTo\n" + ProxyBlockedMessage));
        if(!this.lastCommandPermission.equals(CommandPermission)) sendConsoleMessage(translate("&eChanged command permission from &c" + lastCommandPermission + " &eTo &a" + CommandPermission));
        if(!this.lastBlockedCountryMessage.equals(BlockedCountryMessage)) sendConsoleMessage(translate("\n&eChanged country blocked message from\n" + lastBlockedCountryMessage + "\n&eTo\n" + BlockedCountryMessage));
        if(!this.lastBlockedCountries.equals(BlockedCountries)) sendConsoleMessage(translate("&aUpdated Blocked Country List."));

        if(!this.lastConsoleFilter == ConsoleFilter) {
            String To;
            String From;
            if(ConsoleFilter) {
                To = "&aTrue";
                From = "&cFalse";
            } else {
                To = "&cFalse";
                From = "&aTrue";
            }
            sendConsoleMessage(translate("&eSwitched Console Filter modes. " + From + " &b-> " + To));
        }
        if(!this.lastProxyDetection == ProxyDetection) {
            String To;
            String From;
            if(ProxyDetection) {
                To = "&aTrue";
                From = "&cFalse";
            } else {
                To = "&cFalse";
                From = "&aTrue";
            }
            sendConsoleMessage(translate("&eSwitched Proxy Detection modes. " + From + " &b-> " + To));
        }
        this.lastReconnectMessage = ReconnectMessage;
        this.lastBlockedMessage = BlockedMessage;
        this.lastIllegalCharactersMessage = IllegalCharactersMessage;
        this.lastCommandPermission = CommandPermission;
        this.lastConsoleFilter = ConsoleFilter;
        this.lastProxyBlockedMessage = ProxyBlockedMessage;
        this.lastBlockedCountryMessage = BlockedCountryMessage;
        this.lastProxyDetection = ProxyDetection;
        this.lastBlockedCountries = BlockedCountries;
    }

}
