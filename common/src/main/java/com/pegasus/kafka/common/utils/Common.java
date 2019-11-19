package com.pegasus.kafka.common.utils;


import lombok.Data;
import org.springframework.util.StringUtils;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * providing the tool function.
 * <p>
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize         11/7/2019      Initialize   *
 * *****************************************************************
 */
public class Common {
    private final static long KB_IN_BYTES = 1024;
    private final static long MB_IN_BYTES = 1024 * KB_IN_BYTES;
    private final static long GB_IN_BYTES = 1024 * MB_IN_BYTES;
    private final static long TB_IN_BYTES = 1024 * GB_IN_BYTES;
    private final static DecimalFormat df = new DecimalFormat("0.00");
    private static ThreadLocal<SimpleDateFormat> threadLocal = ThreadLocal.withInitial(() -> new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));

    public static Date parse(String value) throws ParseException {
        return threadLocal.get().parse(value);
    }

    public static String format(Date value) {
        return threadLocal.get().format(value);
    }

    public static String convertSize(long byteNumber) {
        if (byteNumber / TB_IN_BYTES > 0) {
            return String.format("%sTB", df.format((double) byteNumber / (double) TB_IN_BYTES));
        } else if (byteNumber / GB_IN_BYTES > 0) {
            return String.format("%sGB", df.format((double) byteNumber / (double) GB_IN_BYTES));
        } else if (byteNumber / MB_IN_BYTES > 0) {
            return String.format("%sMB", df.format((double) byteNumber / (double) MB_IN_BYTES));
        } else if (byteNumber / KB_IN_BYTES > 0) {
            return String.format("%sKB", df.format((double) byteNumber / (double) KB_IN_BYTES));
        } else {
            return String.format("%sB", String.valueOf(byteNumber));
        }
    }

    public static String convertSize(String number) {
        return convertSize(Math.round(numberic(number)));
    }

    public static double numberic(String number) {
        DecimalFormat formatter = new DecimalFormat("###.##");
        return Double.parseDouble(formatter.format(Double.valueOf(number)));
    }

    public static double numberic(Double number) {
        DecimalFormat formatter = new DecimalFormat("###.##");
        return Double.parseDouble(formatter.format(number));
    }

    public static TimeRange splitTime(String timeRange) throws ParseException {
        if (timeRange == null) {
            return null;
        }
        timeRange = timeRange.trim();
        if (StringUtils.isEmpty(timeRange)) {
            return null;
        }
        TimeRange result = new TimeRange();
        String[] createTimeRanges = timeRange.split(" - ");
        result.setStart(Common.parse(createTimeRanges[0]));
        result.setEnd(Common.parse(createTimeRanges[1]));
        return result;
    }

    public static String trim(String value, char c) {
        int len = value.length();
        int st = 0;
        char[] val = value.toCharArray();    /* avoid getfield opcode */

        while ((st < len) && (val[st] <= c)) {
            st++;
        }
        while ((st < len) && (val[len - 1] <= c)) {
            len--;
        }
        return ((st > 0) || (len < value.length())) ? value.substring(st, len) : value;
    }

    @Data
    public static class TimeRange {
        private Date start;
        private Date end;
    }
}
