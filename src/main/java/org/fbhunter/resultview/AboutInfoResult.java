package org.fbhunter.resultview;

import java.util.*;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class AboutInfoResult implements CollectResult {

    private final static Pattern EMAIL_PATTERN = Pattern.compile("([.\\w]+@[A-Za-z]+\\.[A-Za-z]{2,6})");
//    private final static Pattern PHONE_NUMBER_PATTERN = Pattern.compile("([+]?[\\d][ \\-]?[(]?[\\d]{3}[)]?([ \\-]?\\d)+)");
    private final static Pattern PHONE_NUMBER_PATTERN = Pattern.compile("(\\s*)?(\\+)?([- _():=+]?\\d[- _():=+]?){10,14}(\\s*)?");
    private final static Pattern SITE_PATTERN = Pattern.compile("(((https|http)://[\\w]+\\.)+[\\w]{2,}(/[\\w\\-?&=#.]+)*)");
    private final static Pattern GENDER_PATTERN = Pattern.compile("(Женский|Мужской)");
    private final static Pattern RESIDENCE_CITY_PATTERN = Pattern.compile("(Живет в г\\. [А-ЯA-Z][а-яa-z]+(-[А-ЯA-Z]?[а-яa-z]+)*)");
    private final static Pattern NATIVE_CITY_PATTERN = Pattern.compile("(Из г\\. [А-ЯA-Z][а-яa-z]+([-'`][А-ЯA-Z]?[а-яa-z]+)*)");

    private final List<Map<String, Object>> aboutInfo = new ArrayList<>();

    public void putTarget(String targetId, Map<String, Set<String>> collectedInfo, String userName,
                          String mainPhotoUrl) {
        Map<String, Object> entry = new HashMap<>();
        List<String> infoBlocks = collectedInfo.values().stream()
                .map(strings -> {
                    StringBuilder sb = new StringBuilder();
                    strings.forEach(sb::append);
                    return sb.toString();
                }).collect(Collectors.toList());
        String info = String.join("\n", infoBlocks);

        Set<String> phoneNumbers = findPhoneNumbers(info);
        Set<String> emails = findEmails(info);
        Set<String> sites = findCites(info);
        String gender = findGender(info);
        String residenceCity = findResidenceCity(info);
        String nativeCity = findNativeCity(info);

        entry.put("targetId", targetId);
        entry.put("phoneNumbers", phoneNumbers);
        entry.put("emails", emails);
        entry.put("sites", sites);
        entry.put("gender", gender);
        entry.put("residenceCity", residenceCity);
        entry.put("nativeCity", nativeCity);
        entry.put("userName", userName);
        entry.put("mainPhotoUrl", mainPhotoUrl);
        aboutInfo.add(entry);
    }

    private static Set<String> findEmails(String text) {
        Matcher m = EMAIL_PATTERN.matcher(text);
        return m.find() ? m.results().map(MatchResult::group).collect(Collectors.toSet()) : null;
    }

    private static Set<String> findPhoneNumbers(String text) {
        Matcher m = PHONE_NUMBER_PATTERN.matcher(text);
        return m.find() ? m.results().map(MatchResult::group).map(String::trim).collect(Collectors.toSet()) : null;
    }

    private static Set<String> findCites(String text) {
        Matcher m = SITE_PATTERN.matcher(text);
        return m.find() ? m.results().map(MatchResult::group).collect(Collectors.toSet()) : null;
    }

    private static String findResidenceCity(String text) {
        Matcher m = RESIDENCE_CITY_PATTERN.matcher(text);
        return m.find() ? m.group().replace("Живет в г. ", "") : null;
    }

    private static String findNativeCity(String text) {
        Matcher m = NATIVE_CITY_PATTERN.matcher(text);
        return m.find() ? m.group().replace("Из г. ", "") : null;
    }

    private static String findGender(String text) {
        Matcher m = GENDER_PATTERN.matcher(text);
        return m.find() ? m.group() : null;
    }

    /*
    result: [
        {
            targetId: id,
            AboutInfo: {
                page1: info1,
                page2: info2
            }
        },
    ]
     */
    @Override
    public Object getResult() {
        return aboutInfo;
    }
}
