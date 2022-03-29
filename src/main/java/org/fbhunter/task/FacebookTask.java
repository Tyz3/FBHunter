package org.fbhunter.task;

import com.google.gson.GsonBuilder;
import org.fbhunter.AppProperties;
import org.fbhunter.object.JSONConfig;
import org.fbhunter.parser.UserSession;
import org.fbhunter.resultview.CollectResult;
import org.fbhunter.util.Log;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.*;

public abstract class FacebookTask implements Runnable {

    public enum TaskStatus {
        NEW, IN_PROGRESS, PROXY_FAILED, AUTH_FAILED, FAILED_IN_PROGRESS, DONE
    }

    public enum TaskType {
        FriendsIdsTask, AboutInfoTask
    }

    public static final SimpleDateFormat SDF = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");

    private final int ID;
    private final UserSession SESSION;
    private final TaskType TASK_TYPE;
    private final long START_TIME;

    private long endTime;
    private TaskStatus status;

    private CollectResult result;

    private FacebookTask(int id, UserSession session, TaskStatus status, TaskType taskType) {
        ID = id;
        SESSION = session;
        TASK_TYPE = taskType;
        START_TIME = System.currentTimeMillis();

        this.status = status;
    }

    public FacebookTask(int id, UserSession session, TaskType taskType) {
        this(id, session, TaskStatus.NEW, taskType);
    }

    public String toJson() {
        Map<String, Object> map = new HashMap<>();
        map.put("id", getId());
        map.put("type", getTaskType().name());
        map.put("status", getStatus().name());

        if (getCollectResult() != null) {
            map.put("result", getCollectResult().getResult());
        }

        if (getSession() != null) {
            map.put("login", getSession().getLogin());
            map.put("password", getSession().getPassword());
            map.put("proxy", getSession().getProxy());
        }

        if (getStartTime() != 0) {
            map.put("startTime", SDF.format(new Date(getStartTime())));
        }

        if (getEndTime() != 0) {
            map.put("endTime", SDF.format(new Date(getEndTime())));
        }

        return new GsonBuilder().setPrettyPrinting().create().toJson(map);
    }

    public void saveToJsonFile() {
        JSONConfig file = new JSONConfig(
                Path.of(AppProperties.taskDir + File.separator + "%d.json".formatted(getId()))
        );
        file.set("id", getId());
        file.set("type", getTaskType().name());
        file.set("status", getStatus().name());

        if (getCollectResult() != null) {
            file.set("result", getCollectResult().getResult());
        }

        if (getSession() != null) {
            file.set("login", getSession().getLogin());
            file.set("password", getSession().getPassword());
            file.set("proxy", getSession().getProxy());
        }

        if (getStartTime() != 0) {
            file.set("startTime", SDF.format(new Date(getStartTime())));
        }

        if (getEndTime() != 0) {
            file.set("endTime", SDF.format(new Date(getEndTime())));
        }

        file.save();
    }

    public static String getTaskInfoFromJsonFile(Path path) throws Exception {
        // {id: 1, status: DONE, type: FriendsIdsTask, result: ...}
        return Files.readString(path, Charset.defaultCharset());
    }

    public void start() {
        Log.DEBUG.print("Произведён запуск задачи #%d".formatted(ID));
        new Thread(this).start();
    }

    public void end() {
        setEndTime(System.currentTimeMillis());
        SESSION.close();
    }

    public boolean isEnded() {
        return status == TaskStatus.DONE || status == TaskStatus.FAILED_IN_PROGRESS || status == TaskStatus.AUTH_FAILED
                || status == TaskStatus.PROXY_FAILED;
    }

    public int getId() {
        return ID;
    }

    public UserSession getSession() {
        return SESSION;
    }

    public TaskType getTaskType() {
        return TASK_TYPE;
    }

    public long getStartTime() {
        return START_TIME;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public CollectResult getCollectResult() {
        return result;
    }

    public void setCollectResult(CollectResult result) {
        this.result = result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FacebookTask that = (FacebookTask) o;
        return ID == that.ID;
    }

    @Override
    public int hashCode() {
        return Objects.hash(ID);
    }

    @Override
    public String toString() {
        return "FacebookTask{" +
                "TASK_TYPE=" + TASK_TYPE +
                ", status=" + status +
                '}';
    }
}
