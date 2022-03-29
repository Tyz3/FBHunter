package org.fbhunter.util;

import org.fbhunter.AppProperties;
import org.openqa.selenium.WebElement;

public class Randomize {

    public static void randSleep(long min, long max) {
        try {
            long timeout = (long) (min + Math.random() * (max - min + 1));
            Thread.sleep(timeout);
        } catch (InterruptedException e) {
            Log.ERROR.print(e.getMessage());
            if (AppProperties.debug) {
                e.printStackTrace();
            }
        }
    }

    public static void randSleep(double minSec, double maxSec) {
        randSleep((long)(minSec*1000), (long)(maxSec*1000));
    }

    public static void shortSleep() {
        randSleep(0.78, 3.10);
    }

    public static void mediumSleep() {
        randSleep(4.94, 8.61);
    }

    public static void longSleep() {
        randSleep(9.37, 16.78);
    }

    public static void enterKeysWithSleeps(WebElement element, String keys) {
        for (char c : keys.toCharArray()) {
            element.sendKeys(String.valueOf(c));
            randSleep(0.04, 0.18);
        }
    }
}
