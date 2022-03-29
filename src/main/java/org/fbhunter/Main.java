package org.fbhunter;

import org.fbhunter.util.Log;
import org.fbhunter.util.Utils;

public class Main {

    public static void main(String[] args) {
        if (Utils.extractResource("config.json")) {
            Log.INFO.print("Создан новый файл: config.json");
        }
        if (Utils.extractResource("chromedriver.exe")) {
            Log.INFO.print("Создан новый файл: chromedriver.exe");
        }

        AppProperties.load();
        AppProperties.show();

        TaskEngine.getInstance().load();

        WebServer.getInstance().load();
    }
}
