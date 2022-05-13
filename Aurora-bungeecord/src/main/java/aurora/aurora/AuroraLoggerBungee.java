package aurora.aurora;

import aurora.BungeeMain;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.log.ConciseFormatter;
import java.util.logging.Logger;

public class AuroraLoggerBungee {

    public void registerFilter() {
        if (!ProxyServer.getInstance().getVersion().contains("BungeeCord")) {
            new AuroraLoggerBungeeLog4J().registerFilter();
        } else {
            Logger logger = ProxyServer.getInstance().getLogger();
            logger.setFilter(record -> {
                String msg = (new ConciseFormatter(false)).formatMessage(record).trim();
                return logMessage(msg);
            });
        }
    }

    public static boolean logMessage(String message) {
        if(!BungeeMain.ConsoleFilter) return true;
        if(message.contains("Event ConnectionInitEvent(remoteAddress=")) return false;
        return !message.contains(" <-> InitialHandler");
    }

}
