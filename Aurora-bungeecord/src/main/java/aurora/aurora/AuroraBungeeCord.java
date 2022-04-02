package aurora.aurora;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.event.LoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;
import net.md_5.bungee.event.EventHandler;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public final class AuroraBungeeCord extends Plugin implements Listener {

    HashMap<String, UUID> PlayerDetails = new HashMap<>();
    HashMap<String, Integer> Tries = new HashMap<>();
    ArrayList<String> Blocked = new ArrayList<>();

    HashMap<String, JSONObject> IPInfo = new HashMap<>();
    List<?> BlockedCountries;
    List<?> lastBlockedCountries;

    String ReconnectMessage;
    String lastReconnectMessage;
    String BlockedMessage;
    String BlockedCountryMessage;
    String ProxyBlockedMessage;
    static String CommandPermission;
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
        getLogger().info("&eLoading &bAurora".replace("&", "§"));
        getProxy().getPluginManager().registerListener(this, this);
        ProxyServer.getInstance().getPluginManager().registerCommand(this, new Commands(this));
        try {
            if (!getDataFolder().exists())
                getDataFolder().mkdir();
            File file = new File(getDataFolder().getPath(), "config.yml");
            if (!file.exists()) {
                file.createNewFile();
                Configuration config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(file);
                config.set("reconnect-message", "&bAurora%nl%&6Please Reconnect.");
                config.set("blocked-message", "&bAurora%nl%&4Your IP has been blocked.");
                config.set("illegal-characters-message", "&bAurora%nl%&cName Contains Illegal Characters.");
                config.set("proxy-blocked-message", "&bAurora%nl%&cVPN/Proxy Detected.");
                config.set("blocked-country-message", "&bAurora%nl%&cYour Country Is Blocked.");
                config.set("proxy-detections", true);
                config.set("command-permission", "aurora.admin");
                config.set("console-filter", true);
                config.set("blocked-countries", "");
                ConfigurationProvider.getProvider(YamlConfiguration.class).save(config, file);
            }
            Configuration config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(file);
            this.lastReconnectMessage = config.getString("reconnect-message");
            this.lastBlockedMessage = config.getString("blocked-message");
            this.lastIllegalCharactersMessage = config.getString("illegal-characters-message");
            this.lastConsoleFilter = config.getBoolean("console-filter");
            this.lastCommandPermission = config.getString("command-permission");
            this.lastProxyDetection = config.getBoolean("proxy-detections");
            this.lastProxyBlockedMessage = config.getString("proxy-blocked-message");
            this.lastBlockedCountries = config.getList("blocked-countries");
            this.lastBlockedCountryMessage = config.getString("blocked-country-message");
            reloadConfig();
            ConfigurationProvider.getProvider(YamlConfiguration.class).save(config, file);

        } catch (IOException e) {
            getLogger().warning("§cAn error has occurred while reloading the config!\n§bInfo:\n");
            e.printStackTrace();
        }
        new AuroraLoggerBungee().registerFilter();
        getLogger().info(translate("&aLoaded &bAurora"));
    }

    @Override
    public void onDisable() {
        getLogger().info("&bAurora &cHas Been Unloaded");
    }

    @EventHandler
    public void onConnect(LoginEvent e) throws IOException {
        String name = e.getConnection().getName();
        UUID uuid = e.getConnection().getUniqueId();
        String IP = e.getConnection().getSocketAddress().toString().split(":")[0];
        String numRegex   = ".*[0-9].*";
        String alphaRegex = ".*[A-Z-a-z].*";

        JSONObject object = getIPInfo(IP.replace("/", ""));

        if (!(name.matches(numRegex) || name.matches(alphaRegex)) || name.contains(" ")) {
            e.getConnection().disconnect(new TextComponent(translate(IllegalCharactersMessage)));
        } else {
            if (!Blocked.contains(IP)) {
                if (!PlayerDetails.containsKey(IP)) {
                    e.getConnection().disconnect(new TextComponent(translate(ReconnectMessage)));
                    replaceUUID(IP, uuid);
                } else if (!PlayerDetails.get(IP).equals(uuid)) {
                    e.getConnection().disconnect(new TextComponent(translate(ReconnectMessage)));
                    addTry(IP);
                    replaceUUID(IP, uuid);
                } else {
                    removeTry(IP);
                    if(BlockedCountries.contains(object.get("country").toString())) {
                        e.getConnection().disconnect(new TextComponent(translate(BlockedCountryMessage)));
                        return;
                    }
                    if(ProxyDetection) if(object.get("proxy").equals("true")) e.getConnection().disconnect(new TextComponent(translate(ProxyBlockedMessage)));
                }
            } else {
                e.getConnection().disconnect(new TextComponent(translate(BlockedMessage)));
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

    public String translate(String text) {
        return ChatColor.translateAlternateColorCodes('&', text).replace("%nl%", "\n");
    }

    public void reloadConfig() {
        File file = new File(getDataFolder().getPath(), "config.yml");
        try {
            Configuration config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(file);
            ConfigurationProvider.getProvider(YamlConfiguration.class).save(config, file);
            this.ReconnectMessage = config.getString("reconnect-message");
            this.BlockedMessage = config.getString("blocked-message");
            this.IllegalCharactersMessage = config.getString("illegal-characters-message");
            this.ProxyBlockedMessage = config.getString("proxy-blocked-message");
            this.ProxyDetection = config.getBoolean("proxy-detections");
            this.BlockedCountries = config.getList("blocked-countries");
            CommandPermission = config.getString("command-permission");
            ConsoleFilter = config.getBoolean("console-filter");
            this.BlockedCountryMessage = config.getString("blocked-country-message");

            if(!this.lastReconnectMessage.equals(ReconnectMessage)) getLogger().info(translate("\n&eChanged reconnect message from\n" + lastReconnectMessage + "\n&eTo\n" + ReconnectMessage));
            if(!this.lastBlockedMessage.equals(BlockedMessage)) getLogger().info(translate("\n&eChanged blocked message from\n" + lastBlockedMessage + "\n&eTo\n" + BlockedMessage));
            if(!this.lastIllegalCharactersMessage.equals(IllegalCharactersMessage)) getLogger().info(translate("\n&eChanged illegal characters message from\n" + lastIllegalCharactersMessage + "\n&eTo\n" + IllegalCharactersMessage));
            if(!this.lastProxyBlockedMessage.equals(ProxyBlockedMessage)) getLogger().info(translate("\n&eChanged proxy blocked message from\n" + lastProxyBlockedMessage + "\n&eTo\n" + ProxyBlockedMessage));
            if(!this.lastCommandPermission.equals(CommandPermission)) getLogger().info(translate("&eChanged command permission from &c" + lastCommandPermission + " &eTo &a" + CommandPermission));
            if(!this.lastBlockedCountryMessage.equals(BlockedCountryMessage)) getLogger().info(translate("\n&eChanged country blocked message from\n" + lastBlockedCountryMessage + "\n&eTo\n" + BlockedCountryMessage));
            if(!this.lastBlockedCountries.equals(BlockedCountries)) getLogger().info(translate("&aUpdated Blocked Country List."));

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
                getLogger().info(translate("&eSwitched Console Filter modes. " + From + " &b-> " + To));
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
                getLogger().info(translate("&eSwitched Proxy Detection modes. " + From + " &b-> " + To));
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
        } catch (IOException e) {
            getLogger().warning("§cAn error has occurred while reloading the config!\n§bInfo:\n");
            e.printStackTrace();
        }
    }
}
