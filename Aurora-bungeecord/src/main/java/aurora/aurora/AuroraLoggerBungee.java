package aurora.aurora;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.log.ConciseFormatter;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.filter.AbstractFilter;

import static aurora.aurora.AuroraBungeeCord.ConsoleFilter;

public class AuroraLoggerBungee extends AbstractFilter implements Filter {

    public void registerFilter() {
        if (ProxyServer.getInstance().getVersion().contains("Waterfall")) {
            new AuroraLoggerBungeeLog4J().registerFilter();
            ProxyServer.getInstance().getLogger().info("§aEnabled Log4J Filters");
        } else {
            ProxyServer.getInstance().getLogger().info("§eEnabling Default Filters...");
            java.util.logging.Logger logger = ProxyServer.getInstance().getLogger();
            logger.setFilter(record -> {
                if(ConsoleFilter) {
                    String msg = (new ConciseFormatter(false)).formatMessage(record).trim();
                    return logMessage(msg);
                }
                return false;
            });
            ProxyServer.getInstance().getLogger().info("§aEnabled Default Filters");
        }
    }

    public static boolean logMessage(String message) {
        if (message.contains("lost connection:") && message.contains("Aurora")) {
            return false;
        }
        return !message.contains(" <-> InitialHandler");
    }

}
