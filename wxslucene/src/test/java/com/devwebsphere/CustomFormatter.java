package com.devwebsphere;


import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;
 
/**
 * A {@link Formatter} that may be customised in a {@code logging.properties}
 * file. The syntax of the property
 * {@code com.thinktankmaths.logging.TerseFormatter.format}
 * specifies the output. A newline will be appended to the string and the
 * following special characters will be expanded (case sensitive):-
 * <ul>
 * <li>{@code %m} - message</li>
 * <li>{@code %L} - log level</li>
 * <li>{@code %n} - name of the logger</li>
 * <li>{@code %t} - a timestamp (in ISO-8601 "yyyy-MM-dd HH:mm:ss Z" format)</li>
 * <li>{@code %M} - source method name (if available, otherwise "?")</li>
 * <li>{@code %c} - source class name (if available, otherwise "?")</li>
 * <li>{@code %C} - source simple class name (if available, otherwise "?")</li>
 * <li>{@code %T} - thread ID</li>
 * </ul>
 * The default format is {@value #DEFAULT_FORMAT}. Curly brace characters are not
 * allowed.
 * 
 * @author Samuel Halliday
 */
public class CustomFormatter extends Formatter {
    private static final String DEFAULT_FORMAT = "%L: %m [%c.%M %t]";
 
    private final MessageFormat messageFormat;
 
    private final DateFormat dateFormat =
        new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z");
 
    /** */
    public CustomFormatter() {
        super();
 
        // load the format from logging.properties
        String propName = getClass().getName() + ".format";
        String format = LogManager.getLogManager().getProperty(propName);
        if (format == null || format.trim().length() == 0)
            format = DEFAULT_FORMAT;
        if (format.contains("{") || format.contains("}"))
            throw new IllegalArgumentException("curly braces not allowed");
 
        // convert it into the MessageFormat form
        format = format.replace("%L", "{0}").replace("%m", "{1}").replace("%M",
            "{2}").replace("%t", "{3}").replace("%c", "{4}").replace("%T", "{5}").
            replace("%n", "{6}").replace("%C", "{7}") + "\n";
 
        messageFormat = new MessageFormat(format);
    }
 
    @Override
    public String format(LogRecord record) {
        String[] arguments = new String[8];
        // %L
        arguments[0] = record.getLevel().toString();
        arguments[1] = record.getMessage();
        // sometimes the message is empty, but there is a throwable
        if (arguments[1] == null || arguments[1].length() == 0) {
            Throwable thrown = record.getThrown();
            if (thrown != null) {
                arguments[1] = thrown.getMessage();
            }
        }
        // %m
        arguments[1] = record.getMessage();
        // %M
        if (record.getSourceMethodName() != null) {
            arguments[2] = record.getSourceMethodName();
        } else {
            arguments[2] = "?";
        }
        // %t
        Date date = new Date(record.getMillis());
        synchronized (dateFormat) {
            arguments[3] = dateFormat.format(date);
        }
        // %c
        if (record.getSourceClassName() != null) {
            arguments[4] = record.getSourceClassName();
        } else {
            arguments[4] = "?";
        }
        // %T
        arguments[5] = Integer.valueOf(record.getThreadID()).toString();
        // %n
        arguments[6] = record.getLoggerName();
        // %C
        int start = arguments[4].lastIndexOf(".") + 1;
        if (start > 0 && start < arguments[4].length()) {
            arguments[7] = arguments[4].substring(start);
        } else {
            arguments[7] = arguments[4];
        }
 
        synchronized (messageFormat) {
            return messageFormat.format(arguments);
        }
    }
}