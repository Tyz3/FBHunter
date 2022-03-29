package org.fbhunter;

import org.fbhunter.parser.UserSession;
import org.fbhunter.task.AboutInfoTask;
import org.fbhunter.task.FacebookTask;
import org.fbhunter.task.FriendsIdsTask;
import org.fbhunter.util.Log;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class TaskEngine extends TimerTask {

    private static final Map<Integer, FacebookTask> TASKS = new ConcurrentHashMap<>();
    private static int lastTaskId;

    private static TaskEngine inst;

    private TaskEngine() {}

    public static TaskEngine getInstance() {
        return inst == null ? inst = new TaskEngine() : inst;
    }

    public void load() {
        Log.INFO.setEnd("").print("Запуск TaskEngine... ");
        File taskDir = Path.of(AppProperties.taskDir).toFile();
        if (taskDir.exists() || taskDir.mkdirs()) {
            File[] files = taskDir.listFiles();
            lastTaskId = files == null ? 0 : files.length;
        }

        new Timer().schedule(this, 2000L, 1000L);
        Log.printOk();
    }

    public static UserSession newUserSession(String login, String password, String proxy) {
        return new UserSession(login, password, proxy);
    }

    /**
     * GET /collectFriendsIds?
     * @return {id, type, login, password, proxy}
     */
    public synchronized static String collectFriendsIds(UserSession session, String targetId, int amount) {
        lastTaskId++; // синхронизированный инкремент

        FacebookTask task = new FriendsIdsTask(lastTaskId, session, targetId, amount);
        TASKS.put(lastTaskId, task);
        return task.toJson();
    }

    /**
     * POST /collectAboutInfo
     * {targetIds: [...]}
     * @return {id, type, login, password, proxy}
     */
    public synchronized static String collectAboutInfo(UserSession session, String... targetIds) {
        lastTaskId++; // синхронизированный инкремент

        FacebookTask task = new AboutInfoTask(lastTaskId, session, targetIds);
        TASKS.put(lastTaskId, task);
        return task.toJson();
    }

    /**
     * GET /getTaskInfo?id=
     * @return {id, type, login, password, proxy, result}
     */
    public static String getTaskInfo(int taskId) {
        Log.INFO.print("Получен запрос на получение информации по задаче #%d".formatted(taskId));

        if (!TASKS.containsKey(taskId)) {
            Log.DEBUG.print("Поиск задачи #%d в сохранённых файлах...".formatted(taskId));
            Path taskPath = Path.of(AppProperties.taskDir + File.separator + "%d.json".formatted(taskId));
            File taskFile = taskPath.toFile();

            if (taskFile.exists()) {
                Log.DEBUG.print("Задача #%d найдена".formatted(taskId));

                try {
                    return FacebookTask.getTaskInfoFromJsonFile(taskPath);
                } catch (Exception e) {
                    Log.ERROR.print("Ошибка загрузки задачи #%d из файла: %s".formatted(taskId, e.getMessage()));
                    if (AppProperties.debug) {
                        e.printStackTrace();
                    }
                    return null;
                }
            } else {
                Log.DEBUG.print("Задача #%d не найдена".formatted(taskId));
                return null;
            }
        } else return TASKS.get(taskId).toJson();
    }

    @Override
    public void run() {
        Log.DEBUG.print("Заявки в работе: " + TASKS);

        if (TASKS.size() == 0) return;

        // Запуск в работу новых задач
        List<Map.Entry<Integer, FacebookTask>> newTasks = TASKS.entrySet().stream()
                .filter(e -> e.getValue().getStatus() == FacebookTask.TaskStatus.NEW)
                .collect(Collectors.toList());

        if (newTasks.size() != 0) {
            Log.INFO.print("Новых задач: %d".formatted(newTasks.size()));
            Log.INFO.print("Запуск в работу новых задач...");
            newTasks.forEach(e -> {
                e.getValue().setStatus(FacebookTask.TaskStatus.IN_PROGRESS);
                e.getValue().start();
            });
        }

        // Определение завершённых задач
        List<Map.Entry<Integer, FacebookTask>> endedTasks = TASKS.entrySet().stream()
                .filter(entry -> entry.getValue().isEnded())
                .collect(Collectors.toList());

        // Сохранение завершённых задач
        if (endedTasks.size() != 0) {
            Log.INFO.print("Найдено завершённых задач: %d".formatted(endedTasks.size()));
            Log.INFO.print("Сохранение завершённых задач в файл...");
            endedTasks.forEach(e -> {
                FacebookTask task = TASKS.remove(e.getKey());
                task.end();
                task.saveToJsonFile();
            });
            Log.INFO.print("Все завершённые задачи сохранены");
        }
    }
}
