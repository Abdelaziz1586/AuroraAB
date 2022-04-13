package aurora;


import org.bukkit.ChatColor;

import java.util.List;

public class SpigotMain {

    public static List<?> BlackListCountries;

    public static String BlockedMessage;
    public static String ReconnectMessage;
    public static String ProxyBlockMessage;
    public static String IllegalCharactersBlockMessage;
    public static String BlackListCountryMessage;
    public static String CommandPermission;

    public static boolean ConsoleFilter;
    public static boolean ProxyDetections;

    public static String translate(String text) {
        return ChatColor.translateAlternateColorCodes('&', text).replace("%nl%", "\n");
    }

}
