package com.pegasus.kafka.common.utils;


import lombok.Data;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.shiro.crypto.hash.Md5Hash;
import org.springframework.util.StringUtils;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
    private final static String SALT = "PEgASuS";
    private static ThreadLocal<SimpleDateFormat> threadLocal = ThreadLocal.withInitial(() -> new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));

    public static <T> List<List<T>> averageAssign(List<T> source, int n) {
        List<List<T>> result = new ArrayList<>();
        int remainder = source.size() % n;
        int number = source.size() / n;
        int offset = 0;
        for (int i = 0; i < n; i++) {
            List<T> value;
            if (remainder > 0) {
                value = source.subList(i * number + offset, (i + 1) * number + offset + 1);
                remainder--;
                offset++;
            } else {
                value = source.subList(i * number + offset, (i + 1) * number + offset);
            }
            result.add(value);
        }
        return result;
    }

    public static String hash(String value) {
        return new Md5Hash(value, SALT).toString();
    }

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

    public static Long toLong(String value) {
        if (StringUtils.isEmpty(value)) {
            return null;
        }
        try {
            return Long.valueOf(value.trim());
        } catch (Exception e) {
            return null;
        }
    }

    public static <T> T toVo(Object source, Class<T> target) {
        if (source == null) {
            return null;
        }

        try {
            T result = target.newInstance();
            BeanUtils.copyProperties(result, source);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Long calculateHistoryLogSize(Long[] daysValue, int index) {
        if (index > daysValue.length - 1) {
            return 0L;
        }
        Long a = daysValue[index];
        if (a == null) {
            return 0L;
        }

        Long b = null;
        for (int i = index + 1; i < daysValue.length; i++) {
            if (daysValue[i] != null) {
                b = daysValue[i];
                break;
            }
        }
        if (b == null) {
            b = 0L;
        }
        return a - b;
    }


    @Data
    public static class TimeRange {
        private Date start;
        private Date end;
    }
}
