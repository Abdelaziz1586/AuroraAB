package aurora.aurora;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.log.ConciseFormatter;
import java.util.logging.Logger;

import static aurora.aurora.AuroraBungeeCord.ConsoleFilter;

public class AuroraLoggerBungee {

    public void registerFilter() {
        if (ProxyServer.getInstance().getVersion().contains("Waterfall")) {
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
        if(!ConsoleFilter) return true;
        return !message.contains(" <-> InitialHandler");
    }

}
