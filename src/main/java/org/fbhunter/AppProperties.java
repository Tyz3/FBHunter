package org.fbhunter;

import org.fbhunter.object.JSONConfig;
import org.fbhunter.util.Log;

import java.nio.file.Path;
import java.util.List;

public class AppProperties {

    private static final JSONConfig CONFIG = new JSONConfig("config.json", System.getProperty("user.dir"));

    // Default
    public static boolean debug;
    public static String domain;
    public static String taskDir;

    // WebServer
    public static String address;
    public static int port;
    public static int webServerThreads;

    // Selenium
    public static List<String> userAgents;
    public static Path driverPath;
    public static boolean headless;

    // Proxy
    public static boolean proxyEnabled;
    public static String proxyProtocol;
    public static Path proxyPath;
    public static Path invalidProxyPath;
    public static String proxySiteForCheck;
    public static int proxyNotifyOver;

    // Accounts
    public static Path accountsPath;
    public static Path invalidAccountsPath;
    public static int accountsNotifyOver;

    public static void load() {
        // Default
        debug = CONFIG.getBoolean("debug", true);
        Log.debug = debug;
        domain = CONFIG.getString("domain", "https://www.facebook.com/");
        taskDir = CONFIG.getString("taskDir", "tasks");

        // WebServer
        address = CONFIG.getString("WebServer.address", "localhost");
        port = CONFIG.getInt("WebServer.port", 3333);
        webServerThreads = CONFIG.getInt("WebServer.nThreads", 10);

        // Selenium
        userAgents = CONFIG.getStringList("Selenium.userAgents");
        driverPath = Path.of(CONFIG.getString("Selenium.driverPath", "chromedriver.exe"));
        if (!driverPath.toFile().exists()) {
            Log.ERROR.print("Не найден драйвер Selenium:", driverPath.toAbsolutePath().toString());
            System.exit(1);
        }
        headless = CONFIG.getBoolean("Selenium.headless", false);

        // Proxy
        proxyEnabled = CONFIG.getBoolean("Proxy.enabled", false);
        if (proxyEnabled) {
//            proxyProtocol = CONFIG.getString("Proxy.protocol", "socks5");
//            proxyPath = Path.of(CONFIG.getString("Proxy.file", "proxy.txt"));
//            if (!proxyPath.toFile().exists()) {
//                Log.ERROR.print("Файл с прокси не найден:", proxyPath.toAbsolutePath().toString());
//                System.exit(1);
//            }

//            invalidProxyPath = Path.of(CONFIG.getString("Proxy.invalid-file", "invalid-proxy.txt"));

            proxySiteForCheck = CONFIG.getString("Proxy.siteForCheck", "https://www.bing.com/");
//            proxyNotifyOver = CONFIG.getInt("Proxy.notifyOver", 10);
        }

        // Accounts
        accountsPath = Path.of(CONFIG.getString("Accounts.file", "accounts.txt"));
        invalidAccountsPath = Path.of(CONFIG.getString("Accounts.invalid-file", "invalid-accounts.txt"));
        accountsNotifyOver = CONFIG.getInt("Accounts.notifyOver", 3);
    }

    public static void show() {
        Log.INFO.print("ОБЩИЕ НАСТРОЙКИ");
        Log.INFO.setSep("\t").print("", "debug: %s".formatted(debug ? "ON" : "OFF"));
        Log.INFO.setSep("\t").print("", "domain: %s".formatted(domain));
        Log.INFO.setSep("\t").print("", "папка с сохранёнными задачами: %s".formatted(taskDir));

        Log.INFO.print("WEB SERVER");
        Log.INFO.setSep("\t").print("", "адрес: %s".formatted(address));
        Log.INFO.setSep("\t").print("", "порт: %d".formatted(port));
        Log.INFO.setSep("\t").print("", "потоков: %d".formatted(webServerThreads));

        Log.INFO.print("SELENIUM");
        Log.INFO.setSep("\t").print("", "userAgents: %s".formatted(String.join(", ", userAgents)));
        Log.INFO.setSep("\t").print("", "драйвер: %s".formatted(driverPath.toAbsolutePath()));
        Log.INFO.setSep("\t").print("", "headless: %s".formatted(headless));

        Log.INFO.print("PROXY");
        Log.INFO.setSep("\t").print("", "прокси: %s".formatted(proxyEnabled ? "ON" : "OFF"));
        if (proxyEnabled) {
//            Log.INFO.setSep("\t").print("", "", "протокол: %s".formatted(proxyProtocol));
//            Log.INFO.setSep("\t").print("", "", "файл с прокси: %s".formatted(proxyPath.toAbsolutePath()));
//            Log.INFO.setSep("\t").print("", "", "файл с невалидными прокси: %s".formatted(invalidProxyPath.toAbsolutePath()));
            Log.INFO.setSep("\t").print("", "", "сайт для проверки прокси: %s".formatted(proxySiteForCheck));
//            Log.INFO.setSep("\t").print("", "", "оповещать при отсутствии прокси: <%d шт.".formatted(proxyNotifyOver));
        }

        Log.INFO.print("ACCOUNTS");
        Log.INFO.setSep("\t").print("", "", "файл с аккаунтами: %s".formatted(accountsPath.toAbsolutePath()));
        Log.INFO.setSep("\t").print("", "", "файл с невалидными аккаунтами: %s".formatted(invalidAccountsPath.toAbsolutePath()));
        Log.INFO.setSep("\t").print("", "", "оповещать при отсутствии аккаунтов: <%d шт.".formatted(accountsNotifyOver));
    }

}
