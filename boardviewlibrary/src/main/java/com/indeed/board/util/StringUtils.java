package com.indeed.board.util;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlarmManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static android.content.Context.MODE_PRIVATE;

/**
 * 字符串操作工具包
 *
 * @author liux (http://my.oschina.net/liux)
 * @author thanatosx
 * @version 2.0
 *          Updated 2016-08-11
 */
public class StringUtils {

    private static boolean sIsAtLeastGB;
    private final static String number = "0123456789";

    //    /^([a-zA-Z0-9_\.\-])+\@(([a-zA-Z0-9\-])+\.)+([a-zA-Z0-9]{2,4})+$/
    private final static Pattern emailer = Pattern
            .compile("\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*");
    private final static Pattern phone3 = Pattern.compile("^1[34578][0123456789]\\d{8}$");
    private final static Pattern password = Pattern.compile("^[\\@A-Za-z0-9\\!\\#\\$\\%\\^\\&\\*\\.\\~]{6,14}$");
    private final static Pattern passwordMode2 = Pattern.compile("[a-z]+");
    private final static Pattern passwordMode1 = Pattern.compile("\\d+");
    private final static Pattern passwordMode3 = Pattern.compile("[A-Z]+");
    private final static Pattern passwordMode4 = Pattern.compile("\\W+");
    private final static Pattern password1 = Pattern.compile("^[\\@A-Za-z0-9\\!\\#\\$\\%\\^\\&\\*\\.\\~]{6,14}$");
    private final static Pattern IMG_URL = Pattern
            .compile(".*?(gif|jpeg|png|jpg|bmp)");

    private final static Pattern URL = Pattern
            .compile("^(https|http)://.*?$(net|com|.com.cn|org|me|)");

    private final static ThreadLocal<SimpleDateFormat> YYYYMMDDHHMMSS = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        }
    };
    private final static ThreadLocal<SimpleDateFormat> YYYYMMDDHHMMSS1 = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault());
        }
    };
    private final static ThreadLocal<SimpleDateFormat> YYYYMMDD = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        }
    };

    private final static ThreadLocal<SimpleDateFormat> YYYYMMDDHHMM = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        }
    };

    private static final Pattern UniversalDatePattern = Pattern.compile(
            "([0-9]{4})-([0-9]{2})-([0-9]{2})[\\s]+([0-9]{2}):([0-9]{2}):([0-9]{2})"
    );
    private static final Pattern UniversalDatePattern1 = Pattern.compile(
            "([0-9]{4})/([0-9]{2})/([0-9]{2})[\\s]+([0-9]{2}):([0-9]{2}):([0-9]{2})"
    );

    /**
     * 将字符串转位日期类型
     *
     * @param sdate string date that's type like YYYY-MM-DD HH:mm:ss
     * @return {@link Date}
     */
    public static Date toDate(String sdate) {
        return toDate(sdate, YYYYMMDDHHMMSS.get());
    }

    public static Date toDate(String sdate, SimpleDateFormat formatter) {
        try {
            return formatter.parse(sdate);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * YYYY-MM-DD HH:mm:ss格式的时间字符串转换为{@link Calendar}类型
     *
     * @param str YYYY-MM-DD HH:mm:ss格式字符串
     * @return {@link Calendar}
     */
    public static Calendar parseCalendar(String str) {
        Matcher matcher = UniversalDatePattern.matcher(str);
        Calendar calendar = Calendar.getInstance();
        if (!matcher.find())
            return null;
        calendar.set(
                matcher.group(1) == null ? 0 : toInt(matcher.group(1)),
                matcher.group(2) == null ? 0 : toInt(matcher.group(2)) - 1,
                matcher.group(3) == null ? 0 : toInt(matcher.group(3)),
                matcher.group(4) == null ? 0 : toInt(matcher.group(4)),
                matcher.group(5) == null ? 0 : toInt(matcher.group(5)),
                matcher.group(6) == null ? 0 : toInt(matcher.group(6))
        );
        return calendar;
    }

    /**
     * YYYY/MM/DD HH:mm:ss格式的时间字符串转换为{@link Calendar}类型
     *
     * @param str YYYY-MM-DD HH:mm:ss格式字符串
     * @return {@link Calendar}
     */
    public static Calendar parseCalendar2(String str) {
        Matcher matcher = UniversalDatePattern1.matcher(str);
        Calendar calendar = Calendar.getInstance();
        if (!matcher.find())
            return null;
        calendar.set(
                matcher.group(1) == null ? 0 : toInt(matcher.group(1)),
                matcher.group(2) == null ? 0 : toInt(matcher.group(2)) - 1,
                matcher.group(3) == null ? 0 : toInt(matcher.group(3)),
                matcher.group(4) == null ? 0 : toInt(matcher.group(4)),
                matcher.group(5) == null ? 0 : toInt(matcher.group(5)),
                matcher.group(6) == null ? 0 : toInt(matcher.group(6))
        );
        return calendar;
    }

    /**
     * YYYY-MM-DD HH:mm:ss格式的时间字符串转换为{@link Calendar}类型
     *
     * @param str YYYY-MM-DD 格式字符串
     * @return {@link Calendar}
     */
    public static Calendar parseCalendar1(String str) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date date = null;
        try {
            date = sdf.parse(str);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar;
    }

    /**
     * transform date to string that's type like YYYY-MM-DD HH:mm:ss
     *
     * @param date {@link Date}
     * @return
     */
    public static String getDateString(Date date) {
        return YYYYMMDDHHMMSS.get().format(date);
    }

    /**
     * transform date to string that's type like YYYY-MM-DD HH:mm
     * 将字符串转换为YYYY-MM-DD HH:mm
     *
     * @param sdate
     * @return
     */
    public static String getDateString(String sdate) {
        if (TextUtils.isEmpty(sdate)) return "";
        return YYYYMMDDHHMM.get().format(toDate(sdate));
    }

    /**
     * 将字符串转换为%s年%s月%s日
     *
     * @param st
     * @return
     */

    public static String formatYearMonthDay(String st) {
        Matcher matcher = UniversalDatePattern.matcher(st);
        if (!matcher.find()) return st;
        return String.format("%s年%s月%s日",
                matcher.group(1) == null ? 0 : matcher.group(1),
                matcher.group(2) == null ? 0 : matcher.group(2),
                matcher.group(3) == null ? 0 : matcher.group(3));
    }

    public static String formatYearMonthDayNew(String st) {
        Matcher matcher = UniversalDatePattern.matcher(st);
        if (!matcher.find()) return st;
        return String.format("%s/%s/%s",
                matcher.group(1) == null ? 0 : matcher.group(1),
                matcher.group(2) == null ? 0 : matcher.group(2),
                matcher.group(3) == null ? 0 : matcher.group(3));
    }


    /**
     * format time friendly
     *
     * @return n分钟前, n小时前, 1天前, 2天前, n天前, n个月前
     */
    public static String formatSomeAgo(long trim) {

        Calendar mCurrentDate = Calendar.getInstance();
        long crim = mCurrentDate.getTimeInMillis(); // current
        trim = trim * 1000;
        long diff = crim - trim;
        int year = mCurrentDate.get(Calendar.YEAR);
        int month = mCurrentDate.get(Calendar.MONTH);
        int day = mCurrentDate.get(Calendar.DATE);
        if (diff <= 0) {
            return "刚刚";
        }
        if (diff < 60 * 1000) {
            return String.format("%s秒前", diff / 1000);
        }
        if (diff >= 60 * 1000 && diff < AlarmManager.INTERVAL_HOUR) {
            return String.format("%s分钟前", diff / 60 / 1000);
        }
        mCurrentDate.set(year, month, day, 0, 0, 0);
        if (trim >= mCurrentDate.getTimeInMillis()) {
            return String.format("%s小时前", diff / AlarmManager.INTERVAL_HOUR);
        }
        mCurrentDate.set(year, month, day - 1, 0, 0, 0);
        if (trim >= mCurrentDate.getTimeInMillis()) {
            return "1天前";
        }
        mCurrentDate.set(year, month, day - 2, 0, 0, 0);
        if (trim >= mCurrentDate.getTimeInMillis()) {
            return "2天前";
        }
        if (diff < AlarmManager.INTERVAL_DAY * 30) {
            return String.format("%s天前", diff / AlarmManager.INTERVAL_DAY);
        }
        if (diff < AlarmManager.INTERVAL_DAY * 30 * 12) {
            return String.format("%s月前", diff / (AlarmManager.INTERVAL_DAY * 30));
        }
        return String.format("%s年前", diff / (AlarmManager.INTERVAL_DAY * 30 * 12));
    }

    /**
     * format time friendly
     *
     * @return n分钟前, n小时前, 1天前, 2天前, n天前, n个月前
     */
    public static String formatSomeAgoWithMills(long diff) {

        Calendar mCurrentDate = Calendar.getInstance();
        long crim = mCurrentDate.getTimeInMillis(); // current
        diff = diff * 1000;
        long trim = crim - diff;
        int year = mCurrentDate.get(Calendar.YEAR);
        int month = mCurrentDate.get(Calendar.MONTH);
        int day = mCurrentDate.get(Calendar.DATE);
        if (diff <= 0) {
            return "刚刚";
        }
        if (diff < 60 * 1000) {
            return String.format("%s秒前", diff / 1000);
        }
        if (diff >= 60 * 1000 && diff < AlarmManager.INTERVAL_HOUR) {
            return String.format("%s分钟前", diff / 60 / 1000);
        }
        mCurrentDate.set(year, month, day, 0, 0, 0);
        if (trim >= mCurrentDate.getTimeInMillis()) {
            return String.format("%s小时前", diff / AlarmManager.INTERVAL_HOUR);
        }
        mCurrentDate.set(year, month, day - 1, 0, 0, 0);
        if (trim >= mCurrentDate.getTimeInMillis()) {
            return "1天前";
        }
        mCurrentDate.set(year, month, day - 2, 0, 0, 0);
        if (trim >= mCurrentDate.getTimeInMillis()) {
            return "2天前";
        }
        if (diff < AlarmManager.INTERVAL_DAY * 30) {
            return String.format("%s天前", diff / AlarmManager.INTERVAL_DAY);
        }
        if (diff < AlarmManager.INTERVAL_DAY * 30 * 12) {
            return String.format("%s月前", diff / (AlarmManager.INTERVAL_DAY * 30));
        }
        return String.format("%s年前", diff / (AlarmManager.INTERVAL_DAY * 30 * 12));
    }

    /**
     * format time friendly
     *
     * @param sdate YYYY-MM-DD HH:mm:ss
     * @return n分钟前, n小时前, 1天前, 2天前, n天前, n个月前
     */
    public static String formatSomeAgo(String sdate) {
        if (sdate == null) return "";
        Calendar calendar = parseCalendar(sdate);
        if (calendar == null) return sdate;
        Calendar mCurrentDate = Calendar.getInstance();
        long crim = mCurrentDate.getTimeInMillis(); // current
        long trim = calendar.getTimeInMillis(); // target
        long diff = crim - trim;

        int year = mCurrentDate.get(Calendar.YEAR);
        int month = mCurrentDate.get(Calendar.MONTH);
        int day = mCurrentDate.get(Calendar.DATE);

        if (diff < 60 * 1000) {
            return "刚刚";
        }
        if (diff >= 60 * 1000 && diff < AlarmManager.INTERVAL_HOUR) {
            return String.format("%s分钟前", diff / 60 / 1000);
        }
        mCurrentDate.set(year, month, day, 0, 0, 0);
        if (trim >= mCurrentDate.getTimeInMillis()) {
            return String.format("%s小时前", diff / AlarmManager.INTERVAL_HOUR);
        }
        mCurrentDate.set(year, month, day - 1, 0, 0, 0);
        if (trim >= mCurrentDate.getTimeInMillis()) {
            return "1天前";
        }
        mCurrentDate.set(year, month, day - 2, 0, 0, 0);
        if (trim >= mCurrentDate.getTimeInMillis()) {
            return "2天前";
        }
        if (diff < AlarmManager.INTERVAL_DAY * 30) {
            return String.format("%s天前", diff / AlarmManager.INTERVAL_DAY);
        }
        if (diff < AlarmManager.INTERVAL_DAY * 30 * 12) {
            return String.format("%s月前", diff / (AlarmManager.INTERVAL_DAY * 30));
        }
        return String.format("%s年前", mCurrentDate.get(Calendar.YEAR) - calendar.get(Calendar.YEAR));
    }

    /**
     * format time friendly
     *
     * @param sdate YYYY/MM/DD HH:mm:ss
     * @return n分钟前, n小时前, 1天前, 2天前, n天前, n个月前
     */
    public static String formatSomeAgo1(String sdate) {
        if (sdate == null) return "";
        Calendar calendar = parseCalendar2(sdate);
        if (calendar == null) return sdate;

        Calendar mCurrentDate = Calendar.getInstance();
        long crim = mCurrentDate.getTimeInMillis(); // current
        long trim = calendar.getTimeInMillis(); // target
        long diff = crim - trim;

        int year = mCurrentDate.get(Calendar.YEAR);
        int month = mCurrentDate.get(Calendar.MONTH);
        int day = mCurrentDate.get(Calendar.DATE);

        if (diff < 60 * 1000) {
            return "刚刚";
        }
        if (diff >= 60 * 1000 && diff < AlarmManager.INTERVAL_HOUR) {
            return String.format("%s分钟前", diff / 60 / 1000);
        }
        mCurrentDate.set(year, month, day, 0, 0, 0);
        if (trim >= mCurrentDate.getTimeInMillis()) {
            return String.format("%s小时前", diff / AlarmManager.INTERVAL_HOUR);
        }
        mCurrentDate.set(year, month, day - 1, 0, 0, 0);
        if (trim >= mCurrentDate.getTimeInMillis()) {
            return "1天前";
        }
        mCurrentDate.set(year, month, day - 2, 0, 0, 0);
        if (trim >= mCurrentDate.getTimeInMillis()) {
            return "2天前";
        }
        if (diff < AlarmManager.INTERVAL_DAY * 30) {
            return String.format("%s天前", diff / AlarmManager.INTERVAL_DAY);
        }
        if (diff < AlarmManager.INTERVAL_DAY * 30 * 12) {
            return String.format("%s月前", diff / (AlarmManager.INTERVAL_DAY * 30));
        }
        return String.format("%s年前", mCurrentDate.get(Calendar.YEAR) - calendar.get(Calendar.YEAR));
    }


    /**
     * @param calendar {@link Calendar}
     * @return 今天, 1天前, 2天前, n天前
     */
    public static String formatSomeDay(Calendar calendar) {
        if (calendar == null) return "?天前";
        Calendar mCurrentDate = Calendar.getInstance();
        long crim = mCurrentDate.getTimeInMillis(); // current
        long trim = calendar.getTimeInMillis(); // target
        long diff = crim - trim;

        int year = mCurrentDate.get(Calendar.YEAR);
        int month = mCurrentDate.get(Calendar.MONTH);
        int day = mCurrentDate.get(Calendar.DATE);

        mCurrentDate.set(year, month, day, 0, 0, 0);
        if (trim >= mCurrentDate.getTimeInMillis()) {
            return "今天";
        }
        mCurrentDate.set(year, month, day - 1, 0, 0, 0);
        if (trim >= mCurrentDate.getTimeInMillis()) {
            return "1天前";
        }
        mCurrentDate.set(year, month, day - 2, 0, 0, 0);
        if (trim >= mCurrentDate.getTimeInMillis()) {
            return "2天前";
        }
        return String.format("%s天前", diff / AlarmManager.INTERVAL_DAY);
    }

    /**
     * format 今天，一周内，一周前
     *
     * @param calendar
     * @return
     */
    public static String formatWeekDay(Calendar calendar) {
        if (calendar == null) return "?天前";
        Calendar mCurrentDate = Calendar.getInstance();
        Calendar mCurrentDate1 = Calendar.getInstance();
        long trim = calendar.getTimeInMillis(); // target

        int year = mCurrentDate.get(Calendar.YEAR);
        int month = mCurrentDate.get(Calendar.MONTH);
        int day = mCurrentDate.get(Calendar.DATE);

        mCurrentDate.set(year, month, day - 1, 0, 0, 0);
        if (trim >= mCurrentDate.getTimeInMillis()) {
            return "今天";
        }
        mCurrentDate.set(year, month, day - 7, 0, 0, 0);
        if (trim >= mCurrentDate.getTimeInMillis()) {
            return "一周内";
        }
        mCurrentDate.set(year, month, day - 30, 0, 0, 0);
        if (trim >= mCurrentDate.getTimeInMillis()) {
            return "一月内 ";
        }
        mCurrentDate.set(year, month, day - 30, 0, 0, 0);
        if (trim < mCurrentDate.getTimeInMillis()) {
            return "一月前";
        }
        return "";
    }

    /**
     * @param calendar {@link Calendar}
     * @return 星期n
     */
    public static String formatWeek(Calendar calendar) {
        if (calendar == null) return "星期?";
        return new String[]{"星期日", "星期一", "星期二", "星期三", "星期四", "星期五", "星期六"}[calendar.get(Calendar.DAY_OF_WEEK) - 1];
    }

    /**
     * @param str YYYY-MM-DD HH:mm:ss string
     * @return 星期n
     */
    public static String formatWeek(String str) {
        return formatWeek(parseCalendar(str));
    }


    /**
     * format to HH
     *
     * @param i integer
     * @return HH
     */
    public static String formatInt(int i) {
        return (i < 10 ? "0" : "") + i;
    }


    public static String friendly_time(String sdate) {
        int hour = toInt(sdate.substring(0, 2));
        String s = hour >= 0 && hour < 12 ? "上午" : "下午";
        String hour1 = hour >= 0 && hour < 12 ? hour + "" : hour - 12 + "";
        s = s + hour1 + ":" + sdate.substring(3, 5);
        return s;
    }

    public static String friendly_time1(int hour, int minute) {
        String s = hour >= 0 && hour <= 12 ? "上午" : "下午";
        String hour1 = hour >= 0 && hour <= 12 ?   hour + "":hour - 12 + "";
        String m = minute >= 0 && minute < 10 ? "0" + minute : minute + "";
        s = s + hour1 + ":" + m;

        return s;
    }

    /**
     * 秒转换成十分秒
     *
     * @param value
     * @return
     */
    /**
     * 秒转换成时分秒
     *
     * @return
     */
    public static String formatSeconds(int theTime) {
        int theTime1 = 0;//分
        int theTime2 = 0;//小时
        if (theTime > 60) {
            theTime1 = theTime / 60;
            theTime = theTime % 60;
            if (theTime1 >= 60) {
                theTime2 = theTime1 / 60;
                theTime1 = theTime1 % 60;
            }
        }
        String ss = theTime + "";
        String mm = theTime1 + "";
        String hh = theTime2 + "";
        if (theTime2 < 10) {
            hh = "0" + theTime2;
        }
        if (theTime1 < 10) {
            mm = "0" + theTime1;
        }
        if (theTime < 10) {
            ss = "0" + theTime;
        }
        return hh + ":" + mm + ":" + ss;
    }


    /**
     * 转换成00：00：00
     * 大于一天的转化成9天5时3分
     *
     * @param value
     * @return
     */
    public static String formatSecondsAndDay(String value) {
        int theTime = StringUtils.toInt(value);  //秒
        int theTime1 = 0;//分
        int theTime2 = 0;//小时
        if (theTime > 60) {
            theTime1 = theTime / 60;
            theTime = theTime % 60;
            if (theTime1 >= 60) {
                theTime2 = theTime1 / 60;
                theTime1 = theTime1 % 60;
            }
        }
        String ss = theTime + "";
        String mm = theTime1 + "";
        String hh = theTime2 + "";
        if (theTime2 < 10) {
            hh = "0" + theTime2;
        } else if (theTime2 >= 24) {
            hh = theTime2 / 24 + "天" + theTime2 % 24 + "时";
        }
        if (theTime2 < 24) {
            if (theTime1 < 10) {
                mm = "0" + theTime1;
            }
        } else {
            mm = theTime1 + "分";
        }

        if (theTime < 10) {
            ss = "0" + theTime;
        }
        if (theTime2 >= 24) {
            return hh + mm;
        } else {
            return hh + ":" + mm + ":" + ss;
        }

    }

    public static String formatSecondsAndDayOffline(String value) {
        int theTime = StringUtils.toInt(value);  //秒
        int theTime1 = 0;//分
        int theTime2 = 0;//小时
        if (theTime > 60) {
            theTime1 = theTime / 60;
            theTime = theTime % 60;
            if (theTime1 >= 60) {
                theTime2 = theTime1 / 60;
                theTime1 = theTime1 % 60;
            }
        }
        String ss = theTime + "";
        String mm = theTime1 + "";
        String hh = theTime2 + "";
        if (theTime2 < 10) {
            hh = "0" + theTime2;
        } else if (theTime2 >= 24) {
            hh = theTime2 / 24 + "天" + theTime2 % 24 + "时";
        }
        if (theTime2 < 24) {
            if (theTime1 < 10) {
                mm = "0" + theTime1;
            }
        } else {
            mm = theTime1 + "分";
        }

        if (theTime < 10) {
            ss = "0" + theTime;
        }
        if (theTime2 == 0) {
            return mm + ":" + ss;
        }
        if (theTime2 >= 24) {
            return hh + mm;
        } else {
            return hh + ":" + mm + ":" + ss;
        }

    }

    /**
     * 转化成1h3m5s
     *
     * @param value
     * @return
     */
    public static String formatSecondsAndHorus(String value) {
        int theTime = StringUtils.toInt(value);  //秒
        int theTime1 = 0;//分
        int theTime2 = 0;//小时
        int theTime3 = 0;//天
        boolean isShowSecond0 = true;
        if (theTime > 60) {
            theTime1 = theTime / 60;
            theTime = theTime % 60;
            if (theTime1 >= 60) {
                theTime2 = theTime1 / 60;
                theTime1 = theTime1 % 60;
            }
        }

        String ss;
        String mm;
        String hh;
        String dd = "";
        if (theTime2 >= 24) {
            dd = theTime2 / 24 + "d";
            theTime2 = theTime2 % 24;
            isShowSecond0 = false;
        }
        if (theTime2 == 0) {
            hh = "";
        } else {
            hh = theTime2 + "h";
            isShowSecond0 = false;
        }
        if (theTime1 == 0) {
            mm = "";
        } else {
            mm = theTime1 + "m";
            isShowSecond0 = false;
        }

        if (!isShowSecond0 && theTime == 0) {
            ss = "";
        } else {
            ss = theTime + "s";
        }
        return dd + hh + mm + ss;

    }

    /**
     * 判断给定字符串时间是否为今日
     *
     * @param sdate
     * @return boolean
     */
    public static boolean isToday(String sdate) {
        Date time = toDate(sdate);
        Date today = new Date();
        if (time != null) {
            String nowDate = YYYYMMDD.get().format(today);
            String timeDate = YYYYMMDD.get().format(time);
            if (nowDate.equals(timeDate)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 是否是相同的一天
     *
     * @param sdate1 sdate1
     * @param sdate2 sdate2
     * @return
     */
    public static boolean isSameDay(String sdate1, String sdate2) {
        if (TextUtils.isEmpty(sdate1) || TextUtils.isEmpty(sdate2)) {
            return false;
        }
        Date date1 = toDate(sdate1);
        Date date2 = toDate(sdate2);
        if (date1 != null && date2 != null) {
            String d1 = YYYYMMDD.get().format(date1);
            String d2 = YYYYMMDD.get().format(date2);
            if (d1.equals(d2)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 将当前时间转换为stringyyyy-MM-dd HH:mm:ss
     *
     * @return
     */
    public static String getCurrentTimeStr() {
        return YYYYMMDDHHMMSS.get().format(new Date());
    }

    /***
     * 计算两个时间差，返回的是的秒s
     *
     * @param date1
     * @param date2
     * @return
     * @author 火蚁 2015-2-9 下午4:50:06
     */
    public static long calDateDifferent(String date1, String date2) {
        try {
            Date d1 = YYYYMMDDHHMMSS.get().parse(date1);
            Date d2 = YYYYMMDDHHMMSS.get().parse(date2);
            // 毫秒ms
            long diff = d2.getTime() - d1.getTime();
            return diff / 1000;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * 判断给定字符串是否空白串。 空白串是指由空格、制表符、回车符、换行符组成的字符串 若输入字符串为null或空字符串，返回true
     *
     * @param input
     * @return boolean
     */
    @Deprecated
    public static boolean isEmpty(String input) {
        if (input == null || "".equals(input))
            return true;

        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (c != ' ' && c != '\t' && c != '\r' && c != '\n') {
                return false;
            }
        }
        return true;
    }

    /**
     * 判断是不是一个合法的电子邮件地址
     *
     * @param email
     * @return
     */
    public static boolean isEmail(String email) {
        if (email == null || email.trim().length() == 0)
            return false;
        return emailer.matcher(email).matches();
    }

    public static boolean isPattern(String email, Pattern pattern) {
        if (email == null || email.trim().length() == 0)
            return false;
        return pattern.matcher(email).matches();
    }

    /**
     * 判断是不是一个合法的手机号
     *
     * @return
     */
    public static boolean isPhone(String telPhone) {
        if (telPhone == null || telPhone.trim().length() == 0)
            return false;
        return phone3.matcher(telPhone).matches();
    }

    /**
     * 判断密码为6-14位的英文字母、数字或符号的组合
     *
     * @return
     */
    public static boolean isPassword(String passWord) {
        if (passWord == null || passWord.trim().length() == 0)
            return false;
        return password.matcher(passWord).matches();
    }

    public static int passwordMode(String passWord) {
        if (passWord.length() < 6) return 0;
        if (passWord.length() > 14) return 4;//超过12位
        int modes = 0;
        if (passwordMode1.matcher(passWord).find()) modes++; //数字、相同数字、字母等-弱
        if (passwordMode2.matcher(passWord).find()) {
            modes++; //数字+字母-中
        }
        if (passwordMode3.matcher(passWord).find() || passwordMode4.matcher(passWord).find())
            modes++; //数字+小写+大写 或者特殊字符-强
        return modes;
    }

    /**
     * 判断一个url是否为图片url
     *
     * @param url
     * @return
     */
    public static boolean isImgUrl(String url) {
        if (url == null || url.trim().length() == 0)
            return false;
        return IMG_URL.matcher(url).matches();
    }

    /**
     * 判断是否为一个合法的url地址
     *
     * @param str
     * @return
     */
    public static boolean isUrl(String str) {
        if (str == null || str.trim().length() == 0)
            return false;
        return URL.matcher(str).matches();
    }

    /**
     * 字符串转整数
     *
     * @param str
     * @param defValue
     * @return
     */
    public static int toInt(String str, int defValue) {
        try {
            return Integer.parseInt(str);
        } catch (Exception e) {
//            Log.d("oschina", e.getMessage());
        }
        return defValue;
    }

    /**
     * 字符串转double
     *
     * @param str
     * @param defValue
     * @return
     */
    public static double toDouble(String str, double defValue) {
        try {
            if (TextUtils.isEmpty(str))
                return defValue;
            double d = Double.parseDouble(str);
            return d;
        } catch (Exception e) {
        }
        return defValue;
    }


    /**
     *  
     *  提供精确的加法运算。  
     *  @param v1 被加数  
     *  @param v2 加数  
     *  @return 两个参数的和  
     */
    public static double add(double v1, double v2) {
        BigDecimal b1 = new BigDecimal(Double.toString(v1));
        BigDecimal b2 = new BigDecimal(Double.toString(v2));
        return b1.add(b2).doubleValue();
    }

    /**
     *  
     *  提供精确的剑法运算。  
     *  @param v1 被乘数  
     *  @param v2 乘数  
     *  @return 两个参数的积  
     */
    public static double sub(double v1, double v2) {
        BigDecimal b1 = new BigDecimal(Double.toString(v1));
        BigDecimal b2 = new BigDecimal(Double.toString(v2));
        return b1.subtract(b2).doubleValue();
    }

    /**
     * 对象转整数
     *
     * @param obj
     * @return 转换异常返回 0
     */
    public static int toInt(Object obj) {
        if (obj == null)
            return 0;
        return toInt(obj.toString(), 0);
    }

    /**
     * 对象转整数
     *
     * @param obj
     * @return 转换异常返回 0
     */
    public static double toDouble(Object obj) {
        if (obj == null)
            return 0;
        double st = toDouble(obj.toString(), 0);
        return st;
    }

    /**
     * 对象转整数
     *
     * @param obj
     * @return 转换异常返回 0
     */
    public static long toLong(String obj) {
        try {
            return Long.parseLong(obj);
        } catch (Exception e) {
        }
        return 0;
    }

    /**
     * 字符串转布尔值
     *
     * @param b
     * @return 转换异常返回 false
     */
    public static boolean toBool(String b) {
        try {
            return Boolean.parseBoolean(b);
        } catch (Exception e) {
        }
        return false;
    }

    public static String getString(String s) {
        return s == null ? "" : s;
    }

    /**
     * 将一个InputStream流转换成字符串
     *
     * @param is
     * @return
     */
    public static String toConvertString(InputStream is) {
        StringBuilder res = new StringBuilder();
        BufferedReader read = new BufferedReader(new InputStreamReader(is));
        try {
            String line;
            while ((line = read.readLine()) != null) {
                res.append(line).append("<br>");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                read.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return res.toString();
    }

    /***
     * 截取字符串
     *
     * @param start 从那里开始，0算起
     * @param num   截取多少个
     * @param str   截取的字符串
     * @return
     */
    public static String getSubString(int start, int num, String str) {
        if (str == null) {
            return "";
        }
        int length = str.length();
        if (start < 0) {
            start = 0;
        }
        if (start > length) {
            start = length;
        }
        if (num < 0) {
            num = 1;
        }
        int end = start + num;
        if (end > length) {
            end = length;
        }
        return str.substring(start, end);
    }

    /**
     * 获取当前时间为每年第几周
     *
     * @return
     */
    public static int getWeekOfYear() {
        return getWeekOfYear(new Date());
    }

    /**
     * 获取当前时间为每年第几周
     *
     * @param date
     * @return
     */
    public static int getWeekOfYear(Date date) {
        Calendar c = Calendar.getInstance();
        c.setFirstDayOfWeek(Calendar.MONDAY);
        c.setTime(date);
        int week = c.get(Calendar.WEEK_OF_YEAR) - 1;
        week = week == 0 ? 52 : week;
        return week > 0 ? week : 1;
    }

    public static int[] getCurrentDate(String date) {
        int[] dateBundle = new int[3];
        String[] temp = date.split("-");

        for (int i = 0; i < 3; i++) {
            try {
                dateBundle[i] = Integer.parseInt(temp[i]);
            } catch (Exception e) {
                dateBundle[i] = 0;
            }
        }
        return dateBundle;
    }

    /**
     * 返回当前系统时间
     */
    public static String getDataTime(String format) {
        return new SimpleDateFormat(format, Locale.getDefault()).format(new Date());
    }

    /**
     * 判断两个时间哪个早
     */
    public static boolean compareDate(String date1, String date2) {
        java.text.SimpleDateFormat dat = new java.text.SimpleDateFormat("yyyy-MM-dd");
        try {
            Date stardate = dat.parse(date1);
            Date enddate = dat.parse(date2);
            if (enddate.getTime() < stardate.getTime()) {
                return true;
            } else {
                return false;
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * yyyy-MM-dd转为date形式
     *
     * @param date
     * @return
     */
    public static Date parseToDate(String date) {
        java.text.SimpleDateFormat dat = new java.text.SimpleDateFormat("yyyy-MM-dd");
        try {
            Date stardate = dat.parse(date);
            return stardate;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * yyyy-MM-dd转为毫秒
     *
     * @param date
     * @return
     */
    public static long parseToMill(String date) {
        java.text.SimpleDateFormat dat = new java.text.SimpleDateFormat("yyyy-MM-dd");
        try {
            Date stardate = dat.parse(date);
            return stardate.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * yyyy-MM-dd转为毫秒
     *
     * @param date
     * @return
     */
    public static long parseToMill3(String date) {
        java.text.SimpleDateFormat dat = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            Date stardate = dat.parse(date);
            return stardate.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * yyyy-MM-dd加几个月
     *
     * @param date
     * @return
     */
    public static long addMonth(String date, int month) {
        java.text.SimpleDateFormat dat = new java.text.SimpleDateFormat("yyyy-MM-dd");
        try {
            Date stardate = dat.parse(date);
            stardate.setMonth(stardate.getMonth() + month);
            return stardate.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0;
    }

    //判断相差几个月
    public static int getMonthSpace(String date1, String date2)
            throws ParseException {

        int result = 0;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Calendar c1 = Calendar.getInstance();
        Calendar c2 = Calendar.getInstance();

        c1.setTime(sdf.parse(date1));
        c2.setTime(sdf.parse(date2));

        result = c2.get(Calendar.MONTH) - c1.get(Calendar.MONTH);

        return result == 0 ? 1 : Math.abs(result);

    }

    /**
     * YYYY/MM/DD HH:mm:ss格式的时间字符串转换为毫秒数
     *
     * @param date
     * @return
     */
    public static long parseToMill1(String date) {
        try {
            Date d1 = YYYYMMDDHHMMSS1.get().parse(date);
            return d1.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * YYYY/MM/DD HH:mm:ss格式的时间字符串转换为毫秒数
     *
     * @param date
     * @return
     */
    public static long parseToMill2(String date) {
        try {
            Date d1 = YYYYMMDDHHMMSS.get().parse(date);
            return d1.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static String getRandomCode(int length) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < length; i++) {
            int v = (int) (Math.random() * 10);
            sb.append(number.charAt(v));
        }
        return sb.toString();
    }


    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    public static void apply(SharedPreferences.Editor editor) {
        if (sIsAtLeastGB) {
            editor.apply();
        } else {
            editor.commit();
        }
    }

    public static void set(Activity activity,String key, int value) {
        SharedPreferences.Editor editor = activity.getPreferences(MODE_PRIVATE).edit();
        editor.putInt(key, value);
        apply(editor);
    }
    public static void set(Activity activity,String key, long value) {
        SharedPreferences.Editor editor = activity.getPreferences(MODE_PRIVATE).edit();
        editor.putLong(key, value);
        apply(editor);
    }

    public static void set(Activity activity,String key, boolean value) {
        SharedPreferences.Editor editor = activity.getPreferences(MODE_PRIVATE).edit();
        editor.putBoolean(key, value);
        apply(editor);
    }

    public static void set(Activity activity,String key, String value) {
        SharedPreferences.Editor editor = activity.getPreferences(MODE_PRIVATE).edit();
        editor.putString(key, value);
        apply(editor);
    }

    public static boolean get(Activity activity,String key, boolean defValue) {
        if(activity == null) return defValue;
        return activity.getPreferences(MODE_PRIVATE).getBoolean(key, defValue);
    }

    public static String get(Activity activity,String key, String defValue) {
        if(activity == null) return defValue;
        return activity.getPreferences(MODE_PRIVATE).getString(key, defValue);
    }

    public static int get(Activity activity,String key, int defValue) {
        if(activity == null) return defValue;
        return activity.getPreferences(MODE_PRIVATE).getInt(key, defValue);
    }

    public static long get(Activity activity,String key, long defValue) {
        if(activity == null) return defValue;
        return activity.getPreferences(MODE_PRIVATE).getLong(key, defValue);
    }

    public static float get(Activity activity,String key, float defValue) {
        if(activity == null) return defValue;
        return activity.getPreferences(MODE_PRIVATE).getFloat(key, defValue);
    }
}
