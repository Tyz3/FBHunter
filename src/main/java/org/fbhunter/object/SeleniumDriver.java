package org.fbhunter.object;

import org.fbhunter.AppProperties;
import org.fbhunter.util.Log;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class SeleniumDriver {

    private final String PROFILE_ID;
    private final File DRIVER_FILE;
    private final ChromeOptions OPTIONS = new ChromeOptions();

    private ChromeDriverService service;
    private WebDriver driver;

    public SeleniumDriver(String profileId) {
        this.PROFILE_ID = profileId;
        this.DRIVER_FILE = AppProperties.driverPath.toFile();
        System.setProperty("webdriver.chrome.driver", DRIVER_FILE.getAbsolutePath());
    }

    public void setUserAgent(String userAgent) {
        OPTIONS.addArguments("--user-agent=user-agent=%s".formatted(userAgent));
    }

    public void disableAutomationControlled() {
        OPTIONS.addArguments("--disable-blink-features=AutomationControlled");
        OPTIONS.setExperimentalOption("excludeSwitches", List.of("enable-automation"));
    }

    public void disableGeoAndNotifications() {
        Map<String, Object> prefs = Map.of(
                "profile.default_content_setting_values.notifications", 2,
                "profile.default_content_setting_values.geolocation", 2
        );
        OPTIONS.setExperimentalOption("prefs", prefs);
    }

    public void enableProfile() {
        if (PROFILE_ID != null) {
            Path profilePath = Path.of(PROFILE_ID);
            File profileFile = profilePath.toFile();
            if (profileFile.exists() || profileFile.mkdirs()) {
                OPTIONS.addArguments(
                        "--allow-profiles-outside-user-dir",
                        "--enable-profile-shortcut-manager",
                        "--user-data-dir=%s".formatted(profileFile.getAbsolutePath()),
                        "--profile-directory=%s".formatted(PROFILE_ID)
                );
            }
        }
    }

    public void setProxy(String proxy) {
        if (proxy != null) {
            OPTIONS.addArguments("--proxy-server=%s".formatted(proxy));
        }
    }

    public void setHeadless(boolean enable) {
        if (enable) {
            OPTIONS.addArguments("--headless");
        }
    }

    public void build() {
        try {
            service = ChromeDriverService.createDefaultService();
            service.start();
        } catch (IOException e) {
            Log.ERROR.print("Ошибка при запуске ChromeDriver:", e.getMessage());
            if (AppProperties.debug) {
                e.printStackTrace();
            }
            System.exit(1);
        }

        driver = new ChromeDriver(service, OPTIONS);
    }

    public WebDriver getWebDriver() {
        return driver;
    }

    public void quit() {
        driver.quit();
        service.stop();
        service.close();
    }

    public static SeleniumDriver create(String profileId, String proxy) {
        SeleniumDriver driver = new SeleniumDriver(profileId);
        driver.setUserAgent(AppProperties.userAgents.get(new Random().nextInt(AppProperties.userAgents.size())));
        driver.setHeadless(AppProperties.headless);
        if (AppProperties.proxyEnabled) {
            driver.setProxy(proxy);
        }
        driver.enableProfile();
        driver.disableAutomationControlled();
        driver.disableGeoAndNotifications();
        driver.build();
        return driver;
    }

}
