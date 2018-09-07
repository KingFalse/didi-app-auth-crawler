package me.kagura.crawler.utils;

import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.*;

public class DidiUtil {

    public static Map getSystemInfo() {
        return new HashMap<String, String>() {{
            put("rid", getUUID().toLowerCase());
            put("omgid", randomBase64UUID());
            put("suuid", getUUID());
            put("imei", "868754024006367" + getUUID());
        }};
    }


    public static String getIMEI() {
        return "868754024006390" + getUUID();
    }

    public static String getUUID() {
        return UUID.randomUUID().toString().toUpperCase().replaceAll("-", "");
    }

    public static String randomBase64UUID() {
        UUID randomUUID = UUID.randomUUID();
        ByteBuffer wrap = ByteBuffer.wrap(new byte[16]);
        wrap.putLong(randomUUID.getMostSignificantBits());
        wrap.putLong(randomUUID.getLeastSignificantBits());
        return Base64.getEncoder().encodeToString(wrap.array()).replace('_', '-');
    }

    /**
     * 获取4年的日期
     *
     * @return
     */
    public static List<String> get4YearDateList() {
        List<String> list = new ArrayList<>();
        Calendar now = Calendar.getInstance();
        int year = now.get(Calendar.YEAR);
        int month = now.get(Calendar.MONTH) + 1;

        for (int i = year - 3; i <= year; i++) {
            for (int j = 0; j < 12; j++) {
                int i1 = month - j;
                if (i1 < 1) {
                    i1 += 12;
                    list.add((i - 1) + "" + (i1 > 9 ? i1 : "0" + i1));
                } else {
                    list.add(i + "" + (i1 > 9 ? i1 : "0" + i1));
                }
            }
        }
        return list;
    }

    /**
     * 将10 or 13 位时间戳转为时间字符串
     *
     * @param str_num
     * @param format
     * @return
     */
    public static String timestamp2Date(String str_num, String format) {
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        if (str_num.length() == 13) {
            String date = sdf.format(new Date(Long.parseLong(str_num)));
            return date;
        } else {
            String date = sdf.format(new Date(Integer.parseInt(str_num) * 1000L));
            return date;
        }
    }
}
