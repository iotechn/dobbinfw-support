package com.dobbinsoft.fw.support.utils;

import lombok.extern.slf4j.Slf4j;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Date;

@Slf4j
public class TimeUtils {

    public static final String TIME_FORMAT = "HH:mm:ss";

    public static final String DATE_FORMAT = "yyyy-MM-dd";

    public static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

    // 将时间戳转换为LocalDate
    public static LocalDate timestampToLocalDate(long timestamp) {
        return Instant.ofEpochMilli(timestamp).atZone(ZoneId.systemDefault()).toLocalDate();
    }

    // 将时间戳转换为LocalDateTime
    public static LocalDateTime timestampToLocalDateTime(long timestamp) {
        return Instant.ofEpochMilli(timestamp).atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    // 将LocalDate转换为时间戳
    public static long localDateToTimestamp(LocalDate localDate) {
        return localDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

    // 将LocalDateTime转换为时间戳
    public static long localDateTimeToTimestamp(LocalDateTime localDateTime) {
        return localDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

    /**
     * 将Date对象转换为指定格式的字符串
     *
     * @param date 要转换的Date对象
     * @param format 格式
     * @return 转换后的字符串，转换失败返回null
     */
    public static String dateToString(Date date, String format) {
        if (date == null) {
            return null;
        }
        DateFormat dateFormat = new SimpleDateFormat(format);
        return dateFormat.format(date);
    }

    /**
     * 将Date对象转换为指定格式的字符串
     *
     * @param date 要转换的Date对象
     * @return 转换后的字符串，转换失败返回null
     */
    public static String dateToString(Date date) {
        return dateToString(date, DATE_TIME_FORMAT);
    }

    /**
     * 将字符串转换为Date对象
     *
     * @param dateString 要转换的字符串
     * @return 转换后的Date对象，转换失败返回null
     */
    public static Date stringToDate(String dateString) {
        return stringToDate(dateString, DATE_TIME_FORMAT);
    }

    /**
     * 将字符串转换为Date对象
     *
     * @param dateString 要转换的字符串
     * @param format 时间格式
     * @return 转换后的Date对象，转换失败返回null
     */
    public static Date stringToDate(String dateString, String format) {
        if (dateString == null || dateString.isEmpty()) {
            return null;
        }
        try {
            DateFormat dateFormat = new SimpleDateFormat(format);
            return dateFormat.parse(dateString);
        } catch (ParseException e) {
            log.error("[时间格式不正确] raw string: {}", dateString);
            return null;
        }
    }

    /**
     * 将Date对象转换为LocalDateTime对象
     *
     * @param date 要转换的Date对象
     * @return 转换后的LocalDateTime对象，转换失败返回null
     */
    public static LocalDateTime dateToLocalDateTime(Date date) {
        if (date == null) {
            return null;
        }
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    /**
     * 将LocalDateTime对象转换为Date对象
     *
     * @param localDateTime 要转换的LocalDateTime对象
     * @return 转换后的Date对象，转换失败返回null
     */
    public static Date localDateTimeToDate(LocalDateTime localDateTime) {
        if (localDateTime == null) {
            return null;
        }
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    /**
     * 将字符串转换为LocalDateTime对象
     *
     * @param dateString 要转换的字符串
     * @return 转换后的LocalDateTime对象，转换失败返回null
     */
    public static LocalDateTime stringToLocalDateTime(String dateString) {
        return stringToLocalDateTime(dateString, DATE_TIME_FORMAT);
    }

    /**
     * 将字符串转换为LocalDateTime对象
     *
     * @param dateString 要转换的字符串
     * @param format 时间格式
     * @return 转换后的LocalDateTime对象，转换失败返回null
     */
    public static LocalDateTime stringToLocalDateTime(String dateString, String format) {
        if (dateString == null || dateString.isEmpty()) {
            return null;
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
        return LocalDateTime.parse(dateString, formatter);
    }

    /**
     * 将字符串转换为LocalDateTime对象
     *
     * @param dateString 要转换的字符串
     * @return 转换后的LocalDate对象，转换失败返回null
     */
    public static LocalDate stringToLocalDate(String dateString) {
        return stringToLocalDate(dateString, DATE_FORMAT);
    }

    /**
     * 将字符串转换为LocalDateTime对象
     *
     * @param dateString 要转换的字符串
     * @return 转换后的LocalDate对象，转换失败返回null
     */
    public static LocalDate stringToLocalDate(String dateString, String format) {
        if (dateString == null || dateString.isEmpty()) {
            return null;
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
        return LocalDate.parse(dateString, formatter);
    }


    /**
     * 将字符串转换为LocalTime对象
     *
     * @param timeString 要转换的字符串
     * @return 转换后的LocalDate对象，转换失败返回null
     */
    public static LocalTime stringToLocalTime(String timeString) {
        return stringToLocalTime(timeString, TIME_FORMAT);
    }

    /**
     * 将字符串转换为LocalTime对象
     *
     * @param timeString 要转换的字符串
     * @param format 格式
     * @return 转换后的LocalDate对象，转换失败返回null
     */
    public static LocalTime stringToLocalTime(String timeString, String format) {
        if (timeString == null || timeString.isEmpty()) {
            return null;
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
        return LocalTime.parse(timeString, formatter);
    }

    /**
     * 将LocalDateTime对象转换为字符串
     *
     * @param localDateTime 要转换的LocalDateTime对象
     * @return 转换后的字符串，转换失败返回null
     */
    public static String localDateTimeToString(LocalDateTime localDateTime) {
        return localDateTimeToString(localDateTime, DATE_TIME_FORMAT);
    }

    /**
     * 将LocalDateTime对象转换为字符串
     *
     * @param localDateTime 要转换的LocalDateTime对象
     * @param format 时间格式
     * @return 转换后的字符串，转换失败返回null
     */
    public static String localDateTimeToString(LocalDateTime localDateTime, String format) {
        if (localDateTime == null) {
            return null;
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
        return localDateTime.format(formatter);
    }

    /**
     * 将LocalDateTime对象转换为字符串
     *
     * @param localDate 要转换的LocalDateTime对象
     * @return 转换后的字符串，转换失败返回null
     */
    public static String localDateToString(LocalDate localDate) {
        return localDateToString(localDate, DATE_FORMAT);
    }

    /**
     * 将LocalDateTime对象转换为字符串
     *
     * @param localDate 要转换的LocalDateTime对象
     * @param format 时间格式
     * @return 转换后的字符串，转换失败返回null
     */
    public static String localDateToString(LocalDate localDate, String format) {
        if (localDate == null) {
            return null;
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
        return localDate.format(formatter);
    }

    /**
     * 将LocalTime对象转换为字符串
     *
     * @param localTime 要转换的LocalTime对象
     * @return 转换后的字符串，转换失败返回null
     */
    public static String localTimeToString(LocalTime localTime) {
        return localTimeToString(localTime, TIME_FORMAT);
    }

    /**
     * 将LocalTime对象转换为字符串
     *
     * @param localTime 要转换的LocalTime对象
     * @param format 时间格式
     * @return 转换后的字符串，转换失败返回null
     */
    public static String localTimeToString(LocalTime localTime, String format) {
        if (localTime == null) {
            return null;
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
        return localTime.format(formatter);
    }

}
