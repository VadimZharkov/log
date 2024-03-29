package com.vzharkov.log;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.function.Consumer;

import static com.vzharkov.log.Log.Level.*;

/**
 * A simple logger where you can easily customize the output and format of the message.
 *
 * Usage example:
 *
 *  Log.d("Test log %d", 1);
 *
 *  This will produce the next output by default:
 *  26-07-19:10:08:203 (main:1) [DEBUG] Main:main:15 - Test log 1
 *
 *  Or you can use custom log:
 *
 *  Log log = new Log();
 *  log.setOutput(System.out::println);
 *  log.setFormat((level, params, message) -> level + " - " + message);
 *  log.debug("Test log 2");
 *
 *  This will produce the output:
 *  DEBUG - Test log 2
 *
 * @author Vadim Zharkov
 */

@SuppressWarnings({"UnusedDeclaration", "WeakerAccess"})
public class Log {
    public enum Level {
        NONE,
        TRACE,
        DEBUG,
        INFO,
        WARNING,
        ERROR
    }

    public static class Params {
        private final Date date;
        private final String threadName;
        private final long threadId;
        private final String className;
        private final String methodName;
        private final int lineNumber;

        public Params(final Date date, final String threadName, final long threadId,
                      final String className, final String methodName, final int lineNumber) {
            this.date = date;
            this.threadName = threadName;
            this.threadId = threadId;
            this.className = className;
            this.methodName = methodName;
            this.lineNumber = lineNumber;
        }

        public Date getDate() { return date; }
        public String getThreadName() { return threadName; }
        public long getThreadId() { return threadId; }
        public String getClassName() { return className; }
        public String getMethodName() { return methodName; }
        public int getLineNumber() { return lineNumber; }
    }

    @FunctionalInterface
    public interface Formatter {
        String format(Level level, Params params, String message);
    }

    private static Log shared;

    public static Log getShared() {
        if (shared == null) {
            synchronized (Log.class) {
                if (shared == null) {
                    shared = new Log();
                }
            }
        }
        return shared;
    }

    protected static final Consumer<String> defaultOutput = System.out::println;

    protected static final Formatter defaultFormatter = (level, params, message) -> {
        StringBuilder builder = new StringBuilder(1000);
        builder.append(new SimpleDateFormat("dd-MM-yy:HH:mm:SS").format(params.getDate())).append(" ")
                .append("(").append(params.getThreadName()).append(":").append(params.getThreadId()).append(") ")
                .append("[").append(level.toString()).append("] ")
                .append(params.getClassName()).append(":").append(params.getMethodName()).append(":").append(params.getLineNumber())
                .append(" - ").append(message)
                .append("\n");

        return builder.toString();
    };

    private volatile Consumer<String> output;
    private volatile Formatter format;
    private volatile Level level;

    public Log() {
        this(defaultOutput, defaultFormatter, DEBUG);
    }

    public Log(final Consumer<String> output, final Formatter format, final Level level) {
        this.output = output;
        this.format = format;
        this.level = level;
    }

    public void setOutput(final Consumer<String> output) {
        this.output = output;
    }

    public void setFormat(final Formatter format) {
        this.format = format;
    }

    public void setLevel(final Level level) {
        this.level = level;
    }

    public Level getLevel() { return level; }

    public boolean isLoggable(final Level level) {
        return this.level.ordinal() >= level.ordinal();
    }

    protected void log(final Level level, final Params params, final String message) {
        output.accept(format.format(level, params, message));
    }

    public void trace(final String format, final Object... args) {
        if (level.ordinal() >= TRACE.ordinal()) {
            log(TRACE, createParams(), String.format(format, args));
        }
    }

    public static void t(final String format, final Object... args) {
        getShared().trace(format, args);
    }

    public void debug(final String format, final Object... args) {
        if (level.ordinal() >= DEBUG.ordinal()) {
            log(DEBUG, createParams(), String.format(format, args));
        }
    }

    public static void d(final String format, final Object... args) {
        getShared().debug(format, args);
    }

    public void info(final String format, final Object... args) {
        if (level.ordinal() >= INFO.ordinal()) {
            log(INFO, createParams(), String.format(format, args));
        }
    }

    public static void i(final String format, final Object... args) {
        getShared().info(format, args);
    }

    public void warn(final String format, final Object... args) {
        if (level.ordinal() >= WARNING.ordinal()) {
            log(WARNING, createParams(), String.format(format, args));
        }
    }

    public static void w(final String format, final Object... args) {
        getShared().warn(format, args);
    }

    public void error(final String format, final Object... args) {
        if (level.ordinal() >= ERROR.ordinal()) {
            log(ERROR, createParams(), String.format(format, args));
        }
    }

    public static void e(final String format, final Object... args) {
        getShared().error(format, args);
    }

    protected Params createParams() {
        final Date date = new Date();

        final Thread currentThread = Thread.currentThread();
        final String threadName = currentThread.getName();
        final long threadId = currentThread.getId();

        final StackTraceElement ste = Arrays.stream(currentThread.getStackTrace())
                .filter(this::isBottomMethod)
                .findAny()
                .orElse(null);

        Params params;
        if (ste != null) {
            final String fullClassName = ste.getClassName();
            final String className = fullClassName.substring(fullClassName.lastIndexOf('.') + 1);
            params = new Params(date, threadName, threadId, className, ste.getMethodName(), ste.getLineNumber());
        } else {
            params = new Params(date, threadName, threadId, "", "", -1);
        }

        return params;
    }

    private boolean isBottomMethod(StackTraceElement ste) {
        if (ste.isNativeMethod()) {
            return false;
        }
        if (ste.getClassName().equals(Thread.class.getName())) {
            return false;
        }

        return !ste.getClassName().equals(this.getClass().getName());
    }
}
