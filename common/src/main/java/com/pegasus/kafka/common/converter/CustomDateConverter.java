package com.pegasus.kafka.common.converter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.util.StringUtils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CustomDateConverter implements Converter<String, Date> {

    private static final List<String> formarts = new ArrayList<>();

    static {
        formarts.add("yyyy-MM");
        formarts.add("yyyy-MM-dd");
        formarts.add("yyyy-MM-dd hh:mm");
        formarts.add("yyyy-MM-dd hh:mm:ss");

        formarts.add("yyyy/MM");
        formarts.add("yyyy/MM/dd");
        formarts.add("yyyy/MM/dd hh:mm");
        formarts.add("yyyy/MM/dd hh:mm:ss");

        formarts.add("yyyy.MM");
        formarts.add("yyyy.MM.dd");
        formarts.add("yyyy.MM.dd hh:mm");
        formarts.add("yyyy.MM.dd hh:mm:ss");
    }

    @Override
    public Date convert(String source) {
        String value = source.trim();
        if (StringUtils.isEmpty(value)) {
            return null;
        }
        Date result = null;
        for (String format : formarts) {
            try {
                result = parseDate(value, format);
            } catch (ParseException ignored) {
            }
        }

        if (result == null) {
            throw new IllegalArgumentException("Invalid date value \"" + source + "\"");
        }
        return result;
    }

    private Date parseDate(String dateStr,
                           String format) throws ParseException {
        DateFormat dateFormat = new SimpleDateFormat(format);
        return dateFormat.parse(dateStr);
    }

}