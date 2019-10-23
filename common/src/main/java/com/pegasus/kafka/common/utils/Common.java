package com.pegasus.kafka.common.utils;


import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

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
}
