package org.fbhunter.task;

import org.fbhunter.AppProperties;
import org.fbhunter.parser.UserSession;
import org.fbhunter.resultview.CollectResult;

public class FriendsIdsTask extends FacebookTask {

    private final int MAX_AMOUNT;
    private final String TARGET_ID;

    /**
     * Создание новой задачи, готовой к выполнению
     */
    public FriendsIdsTask(int id, UserSession session, String targetId, int maxAmount) {
        super(id, session, TaskType.FriendsIdsTask);
        this.TARGET_ID = targetId;
        this.MAX_AMOUNT = maxAmount;
    }

    @Override
    public void run() {
        if (AppProperties.proxyEnabled && !getSession().checkProxy()) {
            setStatus(TaskStatus.PROXY_FAILED);
            return;
        }

        if (getSession().tryAuth()) {
            CollectResult result = getSession().collectFriendsIds(TARGET_ID, MAX_AMOUNT);
            setCollectResult(result);
            setStatus(result == null ? TaskStatus.FAILED_IN_PROGRESS : TaskStatus.DONE);
        } else {
            setStatus(TaskStatus.AUTH_FAILED);
        }
    }
}
