package org.fbhunter;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.apache.commons.io.IOUtils;
import org.fbhunter.parser.UserSession;
import org.fbhunter.util.Log;
import org.fbhunter.util.Utils;
import org.javatuples.Pair;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class WebServer {

    private static WebServer inst;

    private WebServer() {}

    public static WebServer getInstance() {
        return inst == null ? inst = new WebServer() : inst;
    }

    public void load() {
        Log.INFO.setEnd("").print("Запуск веб-сервера... ");
        HttpServer server = null;
        try {
            server = HttpServer.create(
                    new InetSocketAddress(AppProperties.address, AppProperties.port), 0
            );
        } catch (IOException e) {
            Log.printFailed();
            Log.ERROR.print("Ошибка:", e.getMessage());
            if (AppProperties.debug) {
                e.printStackTrace();
            }
            System.exit(1);
        }

        server.createContext("/", new BasicHandler());
        ThreadPoolExecutor threadPoolExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(
                AppProperties.webServerThreads
        );
        server.setExecutor(threadPoolExecutor);
        server.start();

        Log.printOk();
    }

    private static class BasicHandler implements HttpHandler {

        private enum ContentType {
            JSON("application/json"),
            HTML("text/html; charset=utf-8"),
            DATA("multipart/form-data; boundary=something");

            String value;
            ContentType(String value) {
                this.value = value;
            }
        }

        public void send(HttpExchange req, int rCode) throws IOException {
            req.sendResponseHeaders(rCode, 0);
            req.close();
        }

        public void send(HttpExchange req, int rCode, String content, ContentType contentType) throws IOException {
            if (content != null) {
                byte[] body = content.getBytes(StandardCharsets.UTF_8);
                req.getResponseHeaders().set("Content-type", contentType.value);
                req.sendResponseHeaders(rCode, body.length);
                req.getResponseBody().write(body, 0, body.length);
                req.close();
            } else send(req, rCode);
        }

        public void send(HttpExchange req, int rCode, String content) throws IOException {
            send(req, rCode, content, ContentType.HTML);
        }

        public void send(HttpExchange req, Pair<Integer, String> response, ContentType contentType) throws IOException {
            send(req, response.getValue0(), response.getValue1(), contentType);
        }

        public void send(HttpExchange req, Pair<Integer, String> response) throws IOException {
            send(req, response, ContentType.HTML);
        }

        @Override
        public void handle(HttpExchange req) throws IOException {
            try {
                switch (req.getRequestMethod()) {
                    case "GET" -> get(req);
                    case "POST" -> post(req);
                    default -> send(req, 500);
                }
            } catch (Exception e) {
                Log.ERROR.print("Неизвестная ошибка:", e.getMessage());
                if (AppProperties.debug) {
                    e.printStackTrace();
                }
                send(req, 500);
            }
        }

        public void get(HttpExchange req) throws IOException {
            String path = req.getRequestURI().getPath();
            String query = req.getRequestURI().getQuery();

            Map<String, String> params = Utils.parseQuery(query);

            if (path.startsWith("/getTaskInfo")) {
                Log.DEBUG.print("Принят запрос на /getTaskInfo, query = %s".formatted(query));

                if (absentQueryParams(query, "taskId")) {
                    send(req, 400, "Проверьте наличие параметров: taskId");
                    return;
                }

                int taskId = Integer.parseInt(params.get("taskId"));
                String info = TaskEngine.getTaskInfo(taskId);

                if (info == null) {
                    send(req, 404, "Задачи не существует");
                } else {
                    send(req, 200, info);
                }
            } else {
                send(req, 404);
            }
        }

        public void post(HttpExchange req) throws IOException {
            String path = req.getRequestURI().getPath();
            String body = IOUtils.toString(req.getRequestBody());
            JsonObject json = getJsonObject(body);

            if (json == null) {
                send(req, 404);
                return;
            }

            // контекст: /collectAboutInfo
            if (path.startsWith("/collectAboutInfo")) {
                Log.DEBUG.print("Принят запрос на /collectAboutInfo, body = %s".formatted(body));

                if (absentJsonParams(json, "login", "password", "targetIds")) {
                    send(req, 400, "Проверьте наличие полей: login, password, targetIds");
                    return;
                }

                String login = json.get("login").getAsString();
                String password = json.get("password").getAsString();
                String proxy = json.get("proxy").getAsString();
                JsonElement array = json.get("targetIds").getAsJsonArray();
                String[] targetIds = new Gson().fromJson(array, String[].class);

                UserSession session = TaskEngine.newUserSession(login, password, proxy);
                send(req, 201, TaskEngine.collectAboutInfo(session, targetIds));
            // контекст: /collectFriendsIds
            } else if (path.startsWith("/collectFriendsIds")) {
                Log.DEBUG.print("Принят запрос на /collectFriendsIds, body = %s".formatted(body));

                if (absentJsonParams(json, "login", "password", "targetId", "maxAmount")) {
                    send(req, 400, "Проверьте наличие полей: login, password, targetId, maxAmount");
                    return;
                }

                String login = json.get("login").getAsString();
                String password = json.get("password").getAsString();
                String proxy = json.get("proxy").getAsString();
                String targetId = json.get("targetId").getAsString();
                int maxAmount = json.get("maxAmount").getAsInt();

                UserSession session = TaskEngine.newUserSession(login, password, proxy);
                send(req, 201, TaskEngine.collectFriendsIds(session, targetId, maxAmount));
            } else {
                send(req, 404);
            }
        }

        private static boolean absentQueryParams(String query, String... params) {
            Map<String, String> args = Utils.parseQuery(query);
            for (String param : params) {
                if (args.containsKey(param)) continue;
                return true;
            }
            return false;
        }

        private static boolean absentJsonParams(JsonObject json, String... params) {
            for (String param : params) {
                if (json.has(param)) continue;
                return true;
            }
            return false;
        }

        private static JsonObject getJsonObject(String rawJson) {
            return new Gson().fromJson(rawJson, JsonObject.class);
        }
    }

}
