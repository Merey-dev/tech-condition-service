package kz.kus.sa.tech.condition.util;

import io.micrometer.core.instrument.util.StringUtils;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static kz.kus.sa.tech.condition.util.Constants.*;

public class CommonUtils {

    public static String getFullName(String surname, String givenName, String fathersName) {
        List<String> parts = Arrays.asList(surname, givenName, fathersName);
        return parts.stream()
                .filter(part -> !StringUtils.isBlank(part))
                .collect(Collectors.joining(" "))
                .trim();
    }

    public static String formatPhoneNumber(String phoneNumber) {
        if (StringUtils.isNotBlank(phoneNumber)) {
            if (phoneNumber.length() != 12)
                return phoneNumber;
            return phoneNumber.replaceAll(PHONE_NUMBERS, "+$1 ($2)-$3-$4-$5");
        }
        return phoneNumber;
    }

    public static String formattedDate(LocalDate date) {
        if (Objects.isNull(date))
            return "";
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern(DATE_FORMAT);
        return date.format(fmt);
    }

    public static String formattedDate(OffsetDateTime dateTime) {
        if (Objects.isNull(dateTime))
            return "";
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern(DATE_FORMAT);
        return dateTime.format(fmt);
    }

    public static String formattedDateTime(OffsetDateTime dateTime) {
        if (Objects.isNull(dateTime))
            return "";
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern(DATE_TIME_FORMAT);
        return dateTime.withOffsetSameInstant(ZoneOffset.ofHours(5)) // по времени Астаны
                .format(fmt);
    }

    public static String mergeEmails(Set<String> emails) {
        emails.removeIf(String::isEmpty);
        return String.join(",", emails);
    }

    public static String stringOrEmpty(Object o) {
        if (Objects.isNull(o))
            return "";
        return o.toString();
    }

    public static boolean isNullOrEmpty(String s) {
        return s == null || s.isEmpty();
    }

    public static boolean isNullOrEmpty(Collection<?> collection) {
        return collection == null || collection.isEmpty();
    }
}
