package aurora.checks;

import aurora.BungeeMain;
import aurora.aurora.AuroraBungeeCord;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.event.PreLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.logging.Logger;

public class BotChecker implements Listener {

    Logger logger;

    public BotChecker(AuroraBungeeCord plugin) {
        logger = plugin.getLogger();
    }

    HashMap<String, JSONObject> IPInfo = new HashMap<>();

    @EventHandler
    public void onPreLogin(PreLoginEvent e) throws IOException {
        String IP = e.getConnection().getSocketAddress().toString().split(":")[0].replace("/", "");
        String name = e.getConnection().getName();
        String numRegex = ".*[0-9].*";
        String alphaRegex = ".*[A-Z-a-z].*";

        if (!(name.matches(numRegex) || name.matches(alphaRegex)) || name.contains(" ")) {
            e.getConnection().disconnect(new TextComponent(BungeeMain.translate(BungeeMain.IllegalCharactersBlockMessage)));
            return;
        }

        if (!IPInfo.containsKey(IP)) createDefaultJSONConfig(IP);

        String savedName = getName(IP);

        if (isBlacklisted(IP)) {
            e.getConnection().disconnect(new TextComponent(BungeeMain.translate(BungeeMain.BlockedMessage)));
        } else if (!IPInfo.containsKey(IP)) {
            e.getConnection().disconnect(new TextComponent(BungeeMain.translate(BungeeMain.ReconnectMessage)));
            replaceName(IP, name);
        } else if (!savedName.equals(name)) {
            e.getConnection().disconnect(new TextComponent(BungeeMain.translate(BungeeMain.ReconnectMessage)));
            replaceName(IP, name);
            addTry(IP);
        } else {
            removeTry(IP);
            BungeeProxyChecker.saveIPInfo(IP);
            if (BungeeMain.BlackListCountries.contains(BungeeProxyChecker.getCountry(IP)))
                e.getConnection().disconnect(new TextComponent(BungeeMain.translate(BungeeMain.BlackListCountryMessage)));
            if (BungeeMain.ProxyDetections && BungeeProxyChecker.isProxy(IP))
                e.getConnection().disconnect(new TextComponent(BungeeMain.translate(BungeeMain.ProxyBlockMessage)));
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
}
