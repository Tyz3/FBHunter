package org.fbhunter.resultview;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FriendsIdsResult implements CollectResult {

    private final static Pattern USER_ID_IN_URL = Pattern.compile("(id=\\d+|[.\\w]+$)");

    private final String targetId;
    private final List<String> friendsIds = new ArrayList<>();

    public FriendsIdsResult(String targetId, List<String> friendsIdsUrl) {
        this.targetId = targetId;
        friendsIdsUrl.stream().map(url -> {
            Matcher m = USER_ID_IN_URL.matcher(url);
            return m.find() ? m.group() : url;
        }).forEach(friendsIds::add);
    }

    @Override
    public Object getResult() {
        Map<String, Object> map = new HashMap<>();
        map.put("targetId", targetId);
        map.put("friendsIds", friendsIds);
        return map;
    }
}
