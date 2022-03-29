package org.fbhunter.util;

import org.fusesource.jansi.AnsiConsole;

import java.util.Arrays;
import java.util.stream.Collectors;

public enum Log {
    INFO("\033[32m"),
    WARN("\033[33m"),
    ERROR("\033[31m"),
    DEBUG("\033[36m");

    static {
        AnsiConsole.systemInstall();
    }

    public static boolean debug = false;

    private final String COLOR;
    private static final String FORMAT = "[%s%s%s] [T-%s] %s%s";

    private String sep = " ";
    private String end = "\n";

    Log(String color) {
        this.COLOR = color;
    }

    public Log setSep(String sep) {
        this.sep = sep;
        return this;
    }

    public Log setEnd(String end) {
        this.end = end;
        return this;
    }

    public void print(Object... msgs) {
        if (!(this == Log.DEBUG && !debug)) {
            AnsiConsole.sysOut().format(
                FORMAT,
                this.COLOR, this.name(), "\033[0m",
                Thread.currentThread().getName(),
                Arrays.stream(msgs).map(Object::toString).collect(Collectors.joining(sep)),
                end
            );
        }

        this.sep = " ";
        this.end = "\n";
    }

    public static void printOk() {
        System.out.println("\033[32mOK\033[0m");
    }

    public static void printFailed() {
        System.out.println("\033[31mFAILED\033[0m");
    }

    public static void enableDebugMode() {
        debug = true;
    }

    public static void disableDebugMode() {
        debug = false;
    }

}
