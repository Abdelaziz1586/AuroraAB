package aurora.aurora;

import aurora.BungeeMain;
import aurora.checks.BotChecker;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.*;
import java.util.List;

public final class AuroraBungeeCord extends Plugin {

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
        getLogger().info("&eLoading &bAurora".replace("&", "§"));
        getProxy().getPluginManager().registerListener(this, new BotChecker(this));
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
        getLogger().info(BungeeMain.translate("&aLoaded &bAurora"));
    }

    @Override
    public void onDisable() {
        getLogger().info(BungeeMain.translate("&bAurora &cHas Been Unloaded"));
    }


    public void reloadConfig() {
        File file = new File(getDataFolder().getPath(), "config.yml");
        try {
            Configuration config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(file);
            ConfigurationProvider.getProvider(YamlConfiguration.class).save(config, file);
            BungeeMain.ReconnectMessage = config.getString("reconnect-message");
            BungeeMain.BlockedMessage = config.getString("blocked-message");
            BungeeMain.IllegalCharactersBlockMessage = config.getString("illegal-characters-message");
            BungeeMain.ProxyBlockMessage = config.getString("proxy-blocked-message");
            BungeeMain.ProxyDetections = config.getBoolean("proxy-detections");
            BungeeMain.BlackListCountries = config.getList("blocked-countries");
            BungeeMain.CommandPermission = config.getString("command-permission");
            BungeeMain.ConsoleFilter = config.getBoolean("console-filter");
            BungeeMain.BlackListCountryMessage = config.getString("blocked-country-message");

            if(!this.lastReconnectMessage.equals(BungeeMain.ReconnectMessage)) getLogger().info(BungeeMain.translate("\n&eChanged reconnect message from\n" + lastReconnectMessage + "\n&eTo\n" + BungeeMain.ReconnectMessage));
            if(!this.lastBlockedMessage.equals(BungeeMain.BlockedMessage)) getLogger().info(BungeeMain.translate("\n&eChanged blocked message from\n" + lastBlockedMessage + "\n&eTo\n" + BungeeMain.BlockedMessage));
            if(!this.lastIllegalCharactersMessage.equals(BungeeMain.IllegalCharactersBlockMessage)) getLogger().info(BungeeMain.translate("\n&eChanged illegal characters message from\n" + lastIllegalCharactersMessage + "\n&eTo\n" + BungeeMain.IllegalCharactersBlockMessage));
            if(!this.lastProxyBlockedMessage.equals(BungeeMain.ProxyBlockMessage)) getLogger().info(BungeeMain.translate("\n&eChanged proxy blocked message from\n" + lastProxyBlockedMessage + "\n&eTo\n" + BungeeMain.ProxyBlockMessage));
            if(!this.lastCommandPermission.equals(BungeeMain.CommandPermission)) getLogger().info(BungeeMain.translate("&eChanged command permission from &c" + lastCommandPermission + " &eTo &a" + BungeeMain.CommandPermission));
            if(!this.lastBlockedCountryMessage.equals(BungeeMain.BlackListCountryMessage)) getLogger().info(BungeeMain.translate("\n&eChanged country blocked message from\n" + lastBlockedCountryMessage + "\n&eTo\n" + BungeeMain.BlackListCountryMessage));
            if(!this.lastBlockedCountries.equals(BungeeMain.BlackListCountries)) getLogger().info(BungeeMain.translate("&aUpdated Blocked Country List."));

            if(!this.lastConsoleFilter == BungeeMain.ConsoleFilter) {
                String To;
                String From;
                if(BungeeMain.ConsoleFilter) {
                    To = "&aTrue";
                    From = "&cFalse";
                } else {
                    To = "&cFalse";
                    From = "&aTrue";
                }
                getLogger().info(BungeeMain.translate("&eSwitched Console Filter modes. " + From + " &b-> " + To));
            }
            if(!this.lastProxyDetection == BungeeMain.ProxyDetections) {
                String To;
                String From;
                if(BungeeMain.ProxyDetections) {
                    To = "&aTrue";
                    From = "&cFalse";
                } else {
                    To = "&cFalse";
                    From = "&aTrue";
                }
                getLogger().info(BungeeMain.translate("&eSwitched Proxy Detection modes. " + From + " &b-> " + To));
            }
            this.lastReconnectMessage = BungeeMain.ReconnectMessage;
            this.lastBlockedMessage = BungeeMain.BlockedMessage;
            this.lastIllegalCharactersMessage = BungeeMain.IllegalCharactersBlockMessage;
            this.lastCommandPermission = BungeeMain.CommandPermission;
            this.lastConsoleFilter = BungeeMain.ConsoleFilter;
            this.lastProxyBlockedMessage = BungeeMain.ProxyBlockMessage;
            this.lastBlockedCountryMessage = BungeeMain.BlackListCountryMessage;
            this.lastProxyDetection = BungeeMain.ProxyDetections;
            this.lastBlockedCountries = BungeeMain.BlackListCountries;
        } catch (IOException e) {
            getLogger().warning("§cAn error has occurred while reloading the config!\n§bInfo:\n");
            e.printStackTrace();
        }
    }
}
