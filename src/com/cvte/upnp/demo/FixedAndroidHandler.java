package com.cvte.upnp.demo;

import android.util.Log;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

public class FixedAndroidHandler extends Handler {
    /**
     * Holds the formatter for all Android log handlers.
     */
    private static final Formatter THE_FORMATTER = new Formatter() {
        @Override
        public String format(LogRecord r) {
            Throwable thrown = r.getThrown();
            if (thrown != null) {
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                sw.write(r.getMessage());
                sw.write("\n");
                thrown.printStackTrace(pw);
                pw.flush();
                return sw.toString();
            } else {
                return r.getMessage();
            }
        }
    };

    /**
     * Constructs a new instance of the Android log handler.
     */
    public FixedAndroidHandler() {
        setFormatter(THE_FORMATTER);
    }

    @Override
    public void close() {
        // No need to close, but must implement abstract method.
    }

    @Override
    public void flush() {
        // No need to flush, but must implement abstract method.
    }

    @Override
    public void publish(LogRecord record) {
        try {
            int level = getAndroidLevel(record.getLevel());
            String tag = record.getLoggerName();

            if (tag == null) {
                // Anonymous logger.
                tag = "null";
            } else {
                // Tags must be <= 23 characters.
                int length = tag.length();
                if (length > 23) {
                    // Most loggers use the full class name. Try dropping the
                    // package.
                    int lastPeriod = tag.lastIndexOf(".");
                    if (length - lastPeriod - 1 <= 23) {
                        tag = tag.substring(lastPeriod + 1);
                    } else {
                        // Use last 23 chars.
                        tag = tag.substring(tag.length() - 23);
                    }
                }
            }
            String message = getFormatter().format(record);
            Log.println(level, tag, message);
        } catch (RuntimeException e) {
            Log.e("AndroidHandler", "Error logging message.", e);
        }
    }

    static int getAndroidLevel(Level level) {
        int value = level.intValue();
        if (value >= 1000) { // SEVERE
            return Log.ERROR;
        } else if (value >= 900) { // WARNING
            return Log.WARN;
        } else if (value >= 800) { // INFO
            return Log.INFO;
        } else {
            return Log.DEBUG;
        }
    }

}