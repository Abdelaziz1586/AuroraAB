package aurora.aurora;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.message.Message;
import org.bukkit.Bukkit;

public class AuroraLoggerSpigot implements Filter {

    public void registerFilter() {
        ((Logger) LogManager.getRootLogger()).addFilter(this);
        Bukkit.getConsoleSender().sendMessage("§aEnabled Log4J Filters");
    }

    @Override
    public Result getOnMismatch() {
        return Result.NEUTRAL;
    }

    @Override
    public Result getOnMatch() {
        return Result.NEUTRAL;
    }

    public Filter.Result filter(Logger arg0, Level arg1, Marker arg2, String message, Object... arg4) {
        return logMessage(message);
    }

    public Filter.Result filter(Logger arg0, Level arg1, Marker arg2, String message, Object arg4) {
        return logMessage(message);
    }

    public Filter.Result filter(Logger arg0, Level arg1, Marker arg2, Object message, Throwable arg4) {
        return logMessage(message.toString());
    }

    public Filter.Result filter(Logger arg0, Level arg1, Marker arg2, Message message, Throwable arg4) {
        return logMessage(message.getFormattedMessage());
    }

    public Filter.Result filter(Logger arg0, Level arg1, Marker arg2, String message, Object arg4, Object arg5) {
        return logMessage(message);
    }

    public Filter.Result filter(Logger arg0, Level arg1, Marker arg2, String message, Object arg4, Object arg5, Object arg6) {
        return logMessage(message);
    }

    public Filter.Result filter(Logger arg0, Level arg1, Marker arg2, String message, Object arg4, Object arg5, Object arg6, Object arg7) {
        return logMessage(message);
    }

    public Filter.Result filter(Logger arg0, Level arg1, Marker arg2, String message, Object arg4, Object arg5, Object arg6, Object arg7, Object arg8) {
        return logMessage(message);
    }

    public Filter.Result filter(Logger arg0, Level arg1, Marker arg2, String message, Object arg4, Object arg5, Object arg6, Object arg7, Object arg8, Object arg9) {
        return logMessage(message);
    }

    public Filter.Result filter(Logger arg0, Level arg1, Marker arg2, String message, Object arg4, Object arg5, Object arg6, Object arg7, Object arg8, Object arg9, Object arg10) {
        return logMessage(message);
    }

    public Filter.Result filter(Logger arg0, Level arg1, Marker arg2, String message, Object arg4, Object arg5, Object arg6, Object arg7, Object arg8, Object arg9, Object arg10, Object arg11) {
        return logMessage(message);
    }

    public Filter.Result filter(Logger arg0, Level arg1, Marker arg2, String message, Object arg4, Object arg5, Object arg6, Object arg7, Object arg8, Object arg9, Object arg10, Object arg11, Object arg12) {
        return logMessage(message);
    }

    public Filter.Result filter(Logger arg0, Level arg1, Marker arg2, String message, Object arg4, Object arg5, Object arg6, Object arg7, Object arg8, Object arg9, Object arg10, Object arg11, Object arg12, Object arg13) {
        return logMessage(message);
    }

    @Override
    public Result filter(LogEvent event) {
        return logMessage(event.getMessage().getFormattedMessage());
    }

    @Override
    public State getState() {
        try {
            return State.STARTED;
        } catch (Exception exception) {
            return null;
        }
    }

    @Override
    public void initialize() {

    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }

    @Override
    public boolean isStarted() {
        return true;
    }

    @Override
    public boolean isStopped() {
        return false;
    }

    public static Result logMessage(String message) {
        if (message.contains("lost connection:")) {
            return Result.DENY;
        }
        if (message.contains("Disconnecting com.mojang.authlib.GameProfile")) {
            return Result.DENY;
        }
        if (message.contains("UUID of player ") && message.contains(" is ")) {
            return Result.DENY;
        }
        if(message.contains("An exceptionCaught() event was fired, and it reached at the tail of the pipeline. It usually means the last handler in the pipeline did not handle the exception.")) {
            return Result.DENY;
        }
        if(message.contains("Selector.select() returned prematurely 512 times in a row; rebuilding selector.")) {
            return Result.DENY;
        }
        return Result.NEUTRAL;
    }

}
