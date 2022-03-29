package org.fbhunter.parser;

import org.fbhunter.AppProperties;
import org.fbhunter.object.PageUrl;
import org.fbhunter.util.Log;
import org.fbhunter.util.Randomize;
import org.fbhunter.util.Utils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.net.URL;
import java.time.Duration;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class FacebookUser {

    private final static Pattern USER_NAME_IN_TITLE_PATTERN = Pattern.compile("([А-Яа-я\\w]+ )+");

    private final String TARGET_ID;
    private final WebDriver DRIVER;

    private PageUrl currentPageUrl;

    public FacebookUser(String targetId, WebDriver driver) {
        TARGET_ID = targetId;
        DRIVER = driver;

        currentPageUrl = PageUrl.WWW;
    }

    public void openPage(PageUrl pageUrl) {
        if (currentPageUrl != pageUrl) {
            for (int i = 0; i < 10; i++) {
                try {
                    DRIVER.get(pageUrl.of(TARGET_ID));
                    currentPageUrl = pageUrl;
                    break;
                } catch (Exception e) {
                    Log.ERROR.print("Ошибка открытия страницы сайта:", e.getMessage());
                    if (AppProperties.debug) {
                        e.printStackTrace();
                    }
                }
                Randomize.longSleep();
            }
        }
    }

    public String getProfileUrl() {
        return PageUrl.PROFILE.of(TARGET_ID);
    }

    public String collectUserName() {
        Log.INFO.print("Сбор ФИО пользователя...");
        openPage(PageUrl.PROFILE);

        String title = DRIVER.getTitle();
        Matcher m = USER_NAME_IN_TITLE_PATTERN.matcher(title);
        return m.find() ? m.group().trim() : title;
    }

    public String collectMainPhoto() {
        Log.INFO.print("Получение ссылки на главное фото...");
        openPage(PageUrl.PROFILE);

        try {
            Document doc = Jsoup.parse(DRIVER.getPageSource(), Parser.htmlParser());
            Element image = doc.select("image").get(1);

            if (image == null) {
                throw new Exception("Неправильно определён селектор");
            }

            URL url = new URL(image.attr("xlink:href"));
            return Utils.toBase64(Utils.downloadImage(url));
        } catch (Exception e) {
            Log.ERROR.print("Ошибка парсинга:", e.getMessage());
            if (AppProperties.debug) {
                e.printStackTrace();
            }
            return null;
        }
    }

    public List<String> collectProfileTimeLine(int postsAmount) {
        Log.INFO.print("Сбор ленты пользователя...");

        openPage(PageUrl.PROFILE);

        try {
            while (true) {
                Document doc = Jsoup.parse(DRIVER.getPageSource(), Parser.htmlParser());
                Element timeline = doc.select("div[data-pagelet=ProfileTimeline]").first();

                if (timeline == null) {
                    Log.ERROR.print("Ошибка парсинга: ProfileTimeline не найден");
                    return null;
                }

                Elements posts = timeline.select("div");

                boolean canScroll = timeline.select("div.rek2kq2y").first() != null;

                // В контейнере div с постами, присутствуют дополнительные теги div в конце списка, которые не являются
                // информативными, поэтому вычитаем их из общего количества len(posts).
                int postsLoadedAmount = canScroll ? posts.size() - 3 : posts.size() - 1;

                // Если нельзя скроллить или кол-во загруженных постов достаточно, то сохраняем содержимое блока в html
                // для дальнейшего анализа.
                if (!canScroll || postsLoadedAmount >= postsAmount) {
                    Log.INFO.print("Собрано постов: %d".formatted(postsLoadedAmount));

                    return posts.stream()
                            .filter(Element::hasText)
                            .map(Element::text)
                            .collect(Collectors.toList());
                } else {
                    new Actions(DRIVER).sendKeys(Keys.PAGE_DOWN).perform();
                    Randomize.mediumSleep();
                }
            }
        } catch (Exception e) {
            Log.ERROR.print("Ошибка парсинга:", e.getMessage());
            if (AppProperties.debug) {
                e.printStackTrace();
            }
            return null;
        }
    }

    public Map<String, Set<String>> collectAboutInfo() {
        Log.INFO.print("Собираем публичную информацию пользователя...");

        By xpath = By.xpath("/html/body/div[1]/div/div[1]/div/div[3]/div/div/div[1]/div[1]/div/div/div[4]/div/div" +
                "/div/div[1]/div/div/div/div/div[2]/div");
        List<PageUrl> urls = List.of(
                PageUrl.ABOUT,
                PageUrl.ABOUT_WORK_AND_EDUCATION,
                PageUrl.ABOUT_PLACES,
                PageUrl.ABOUT_CONTACT_AND_BASIC_INFO,
                PageUrl.ABOUT_FAMILY_AND_RELATIONSHIPS,
                PageUrl.ABOUT_DETAILS,
                PageUrl.ABOUT_LIFE_EVENTS
        );

        Map<String, Set<String>> aboutInfo = new HashMap<>();
        for (PageUrl url : urls) {
            Log.DEBUG.print("Сбор информации из блока %s...".formatted(url.name()));
            openPage(url);

            try {
                WebDriverWait wait = new WebDriverWait(DRIVER, Duration.ofSeconds(10));
                WebElement infoBlock = wait.until(ExpectedConditions.presenceOfElementLocated(xpath));
                infoBlock.click();
                infoBlock = DRIVER.findElement(xpath);
                Document doc = Jsoup.parse(infoBlock.getAttribute("innerHTML"), Parser.htmlParser());
                Elements elems = doc.select("div");

                // Разбиваем секции инфо-блока на элементы и записываем их в список
                // Сохраняем полученную информацию в виде текста
                aboutInfo.put(url.name(), elems.stream()
                        .map(Element::text)
                        .collect(Collectors.toSet()));
                Randomize.shortSleep();
            } catch (Exception e) {
                Log.ERROR.print("Ошибка парсинга:", e.getMessage());
                if (AppProperties.debug) {
                    e.printStackTrace();
                }
            }
        }

        return aboutInfo;
    }

    public List<String> collectFriendsIdsUrl(int maxAmount) {
        Log.INFO.print("Сбор списка друзей...");
        String xpath = "/html/body/div[1]/div/div[1]/div/div[3]/div/div/div[1]/div[1]/div/div/div[4]/div/div/div" +
                "/div/div/div/div/div/div[3]";
        openPage(PageUrl.FRIENDS);

        int tries = 0;
        int lastInt = 0;
        List<String> friendsIdsUrl = new ArrayList<>();
        try {
            while (true) {
                WebElement friendBlock = DRIVER.findElement(By.xpath(xpath));
                Document doc = Jsoup.parse(friendBlock.getAttribute("innerHTML"), Parser.htmlParser());
                Elements friends = doc.select("a[tabindex=0]");

                boolean canScroll = doc.select("div.rek2kq2y").first() != null;

                // Оставляем друзей с ФИО
                friends = friends.stream().filter(Element::hasText).collect(Collectors.toCollection(Elements::new));

                // В контейнере div с друзьями, присутствуют дополнительные теги div в конце списка, которые не являются
                // информативными, поэтому вычитаем их из общего количества friends.size().
                int friendsLoadedAmount = canScroll ? friends.size() - 3 : friends.size() - 1;
                Log.DEBUG.print("Можно скроллить страницу? - %s, а нужно? - %s".formatted(
                        canScroll, friendsLoadedAmount < maxAmount
                ));
                Log.INFO.print("Прогресс (%d/%d)...".formatted(friendsLoadedAmount, maxAmount));

                friends.stream().skip(lastInt).limit(friendsLoadedAmount - lastInt)
                        .map(f -> f.attributes().get("href"))
                        .forEach(friendsIdsUrl::add);

                // Если за последнюю прокрутку друзья не загрузились
                if (canScroll && friendsLoadedAmount - lastInt == 0) {
                    if (tries == 2) {
                        Log.WARN.print("На аккаунт наложено ограничение по запросам");
                        break;
                    } else {
                        tries++;
                        Randomize.longSleep();
                    }
                }

                lastInt = friendsLoadedAmount;

                // Если нельзя скроллить или кол-во загруженных постов достаточно, то сохраняем содержимое блока в html
                // для дальнейшего анализа.
                if (!canScroll || friendsLoadedAmount >= maxAmount) {
                    Log.INFO.print("Собрано друзей/подписчиков: %d".formatted(friendsLoadedAmount));
                    break;
                } else {
                    new Actions(DRIVER).sendKeys(Keys.PAGE_DOWN).sendKeys(Keys.PAGE_DOWN).perform();
                    Randomize.mediumSleep();
                }
            }
        } catch (Exception e) {
            Log.ERROR.print("Ошибка парсинга:", e.getMessage());
            if (AppProperties.debug) {
                e.printStackTrace();
            }
        }
        return friendsIdsUrl;
    }
}
