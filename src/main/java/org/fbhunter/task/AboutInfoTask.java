package org.fbhunter.task;

import org.fbhunter.AppProperties;
import org.fbhunter.parser.UserSession;
import org.fbhunter.resultview.CollectResult;

public class AboutInfoTask extends FacebookTask {

    private final String[] TARGET_IDS;

    /**
     * Создание новой задачи, готовой к выполнению
     */
    public AboutInfoTask(int id, UserSession session, String... targetIds) {
        super(id, session, TaskType.AboutInfoTask);
        this.TARGET_IDS = targetIds;
    }

    @Override
    public void run() {
        if (AppProperties.proxyEnabled && !getSession().checkProxy()) {
            setStatus(TaskStatus.PROXY_FAILED);
            return;
        }

        if (getSession().tryAuth()) {
            CollectResult result = getSession().collectAboutInfo(TARGET_IDS);
            setCollectResult(result);
            setStatus(result == null ? TaskStatus.FAILED_IN_PROGRESS : TaskStatus.DONE);
        } else {
            setStatus(TaskStatus.AUTH_FAILED);
        }
    }
}
