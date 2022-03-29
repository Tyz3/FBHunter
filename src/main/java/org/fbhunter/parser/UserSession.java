package org.fbhunter.parser;

import org.fbhunter.AppProperties;
import org.fbhunter.object.PageUrl;
import org.fbhunter.object.SeleniumDriver;
import org.fbhunter.resultview.AboutInfoResult;
import org.fbhunter.resultview.CollectResult;
import org.fbhunter.resultview.FriendsIdsResult;
import org.fbhunter.util.Log;
import org.fbhunter.util.Randomize;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.IOException;
import java.net.*;
import java.time.Duration;
import java.util.List;

public class UserSession {

    private final WebDriver DRIVER;
    private final SeleniumDriver SELENIUM_DRIVER;

    private final String PROXY;
    private final String LOGIN;
    private final String PASSWORD;

    public UserSession(String login, String password, String proxy) {
        PROXY = proxy;
        SELENIUM_DRIVER = SeleniumDriver.create(null, PROXY);
        DRIVER = SELENIUM_DRIVER.getWebDriver();
        LOGIN = login;
        PASSWORD = password;
    }

    public void close() {
        SELENIUM_DRIVER.quit();
    }

    public boolean checkProxy() {
        try {
            Proxy proxy = getProxyFromString(PROXY);
            Log.INFO.print("Проверка прокси (%s)...".formatted(proxy.toString()));

            URL url = new URL(AppProperties.proxySiteForCheck);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection(proxy);
            connection.setConnectTimeout(10000);
            connection.getResponseCode();
            return true;
        } catch (IOException e) {
            Log.ERROR.print("Ошибка в работе прокси:", e.getMessage());
            return false;
        }
    }

    public boolean tryAuth() {
        Log.INFO.print("Попытка авторизоваться...");
        By xpath = By.xpath("/html/body/div[1]/div/div[1]/div/div[2]/div[4]/div[1]/div[4]/a");
        try {
            DRIVER.get(PageUrl.WWW.get());

            WebElement emailInput = DRIVER.findElement(By.id("email"));
            emailInput.clear();
            Randomize.enterKeysWithSleeps(emailInput, LOGIN);
            Randomize.shortSleep();
            emailInput.sendKeys(Keys.TAB);

            WebElement passwordInput = DRIVER.findElement(By.id("pass"));
            passwordInput.clear();
            Randomize.enterKeysWithSleeps(passwordInput, PASSWORD);
            Randomize.shortSleep();
            passwordInput.sendKeys(Keys.ENTER);

            WebDriverWait wait = new WebDriverWait(DRIVER, Duration.ofSeconds(10));
            WebElement userImage = wait.until(ExpectedConditions.presenceOfElementLocated(xpath));
            return userImage != null;
        } catch (Exception e) {
            Log.ERROR.print("Ошибка авторизации:", e.getMessage());
            if (AppProperties.debug) {
                e.printStackTrace();
            }
            return false;
        }
    }

    public String getProxy() {
        return PROXY;
    }

    public String getLogin() {
        return LOGIN;
    }

    public String getPassword() {
        return PASSWORD;
    }

    public CollectResult collectFriendsIds(String targetId, int amount) {
        FacebookUser user = new FacebookUser(targetId, DRIVER);
        List<String> friendsIds = user.collectFriendsIdsUrl(amount);
        return new FriendsIdsResult(targetId, friendsIds);
    }

    public CollectResult collectAboutInfo(String... targetIds) {
        AboutInfoResult result = new AboutInfoResult();
        int progress = 0;
        for (String targetId : targetIds) {
            FacebookUser user = new FacebookUser(targetId, DRIVER);
            String userName = user.collectUserName();
            String mainPhotoUrl = user.collectMainPhoto();
            result.putTarget(targetId, user.collectAboutInfo(), userName, mainPhotoUrl);
            progress++;
            Log.INFO.print("Прогресс (%d/%d)...".formatted(progress, targetIds.length));
            Randomize.mediumSleep();
//            Randomize.longSleep();
        }
        return result;
    }

    private static Proxy getProxyFromString(String rawProxy) {
        // socks5://1.1.1.1:2324
        String[] args = rawProxy.split("//");
        Proxy.Type proxyType = args[0].startsWith("socks5") ? Proxy.Type.SOCKS : Proxy.Type.HTTP;
        String[] args2 = args[1].split(":");
        return new Proxy(proxyType, new InetSocketAddress(args2[0], Integer.parseInt(args2[1])));
    }

}
