package aurora;

import net.md_5.bungee.api.ChatColor;
import org.json.JSONObject;

import java.util.List;

public class BungeeMain {

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
