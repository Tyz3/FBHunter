package org.fbhunter.object;

import org.fbhunter.AppProperties;

public enum PageUrl {
    WWW                             ("", ""),
    PROFILE                         ("{id}", "profile.php?id={id}"),
    ABOUT                           ("{id}/{page}", "profile.php?id={id}&sk={page}"),
    ABOUT_OVERVIEW                  ("{id}/{page}", "profile.php?id={id}&sk={page}"),
    ABOUT_WORK_AND_EDUCATION        ("{id}/{page}", "profile.php?id={id}&sk={page}"),
    ABOUT_PLACES                    ("{id}/{page}", "profile.php?id={id}&sk={page}"),
    ABOUT_CONTACT_AND_BASIC_INFO    ("{id}/{page}", "profile.php?id={id}&sk={page}"),
    ABOUT_FAMILY_AND_RELATIONSHIPS  ("{id}/{page}", "profile.php?id={id}&sk={page}"),
    ABOUT_DETAILS                   ("{id}/{page}", "profile.php?id={id}&sk={page}"),
    ABOUT_LIFE_EVENTS               ("{id}/{page}", "profile.php?id={id}&sk={page}"),
    FRIENDS                         ("{id}/{page}", "profile.php?id={id}&sk={page}"),
    PHOTOS                          ("{id}/{page}", "profile.php?id={id}&sk={page}");

    private final String URL1;
    private final String URL2;

    PageUrl(String url1, String url2) {
        URL1 = url1;
        URL2 = url2;
    }

    public String of(String userId) {
        boolean hasAliasName = !userId.startsWith("id=");
        userId = hasAliasName ? userId : userId.replace("id=", "");

        return AppProperties.domain +
                (hasAliasName ? URL1 : URL2)
                        .replace("{id}", userId)
                        .replace("{page}", this.name().toLowerCase());
    }

    public String get() {
        return AppProperties.domain;
    }
}
