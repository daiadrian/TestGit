package com.dai.dateCalendar;

import org.junit.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Date 类封装当前的日期和时间
 *
 *
 */
public class MyTimer {

    /**
     * 常用方法:
     *      boolean after(Date date) 若当调用此方法的Date对象在指定日期之后返回true,否则返回false
     *
     *      boolean before(Date date) 若当调用此方法的Date对象在指定日期之前返回true,否则返回false。
     *
     *  int compareTo(Date date) 比较当调用此方法的Date对象和指定日期。两者相等时候返回0。
     *      调用对象在指定日期之前则返回负数。调用对象在指定日期之后则返回正数
     *
     *      long getTime( ) 返回自 1970 年 1 月 1 日 00:00:00 GMT 以来此 Date 对象表示的毫秒数
     *
     */
    @Test
    public void usuallyMethod() {
        Date date = new Date();
        System.out.println("毫秒数: "  + date.getTime());
        System.out.println("日期时间: " + date.toString());
    }

    /**
     *  Calendar类:
     *          Calendar.YEAR	        年份
     *          Calendar.MONTH	        月份
     *          Calendar.DATE	        日期
     *          Calendar.DAY_OF_MONTH	日期，和上面的字段意义完全相同
     *          Calendar.HOUR	        12小时制的小时
     *          Calendar.HOUR_OF_DAY	24小时制的小时
     *          Calendar.MINUTE	        分钟
     *          Calendar.SECOND	        秒
     *          Calendar.DAY_OF_WEEK	星期几
     *
     *      static Calendar getInstance() 创建一个代表系统当前日期的Calendar对象
     */
    @Test
    public void calendarMethod() {
        Calendar calendar = Calendar.getInstance();
        /**
         * void set(int year, int month, int date) {
         *          set(YEAR, year);
         *          set(MONTH, month);
         *          set(DATE, date);
         * }
         * 月份是从 0 开始,但日期和年份是从1开始的
         * 月份是从 0 开始,但日期和年份是从1开始的
         * 月份是从 0 开始,但日期和年份是从1开始的
         */
        calendar.set(2019, 3 - 1, 22);
        System.out.println("当前设置的日期: " + calendar.getTime().toString());

        /**
         *  void set(int field, int value)
         *      根据指定的某个字段设置值
         */
        calendar.set(Calendar.YEAR, 2020);
        System.out.println("当前设置的日期: " + calendar.getTime().toString());

        /**
         *  void add(int field, int amount)
         *       根据指定的字段加上特定的值
         */
        calendar.add(Calendar.DATE, 10);
        System.out.println("加十天后的日期: " + calendar.getTime().toString());
        calendar.add(Calendar.DATE, -10);//减十天

        /**
         *  get(int field)
         *      根据字段获取该字段对应的信息
         */
        int month = calendar.get(Calendar.MONTH);
        System.out.println("当前月份是: " + month);
    }


    /**
     * SimpleDateFormat :
     *   G 年代标志符
     *   y 年
     *   M 月
     *   d 日
     *   h 时 在上午或下午 (1~12)
     *   H 时 在一天中 (0~23)
     *   m 分
     *   s 秒
     *   S 毫秒
     *   E 星期
     *   D 一年中的第几天
     *   F 一月中第几个星期几
     *   w 一年中第几个星期
     *   W 一月中第几个星期
     *   a 上午 / 下午 标记符
     *   k 时 在一天中 (1~24)
     *   K 时 在上午或下午 (0~11)
     *   z 时区
     *
     *      String format(Date date) 将Date日期格式化成指定格式的时间字符串
     *
     *      Date parse(String source) 将给定的字符串按照指定格式转成Date对象
     *
     *  SimpleDateFormat的线程安全问题:
     *      SimpleDateFormat继承自DateFormat类,DateFormat类中维护一个Calendar对象;
     *   SimpleDateFormat的format和parse中都使用了这个Calendar对象,在parse方法调用的establish方法中调用了cal.clear();
     *      这个方法会清空calendar,那么也会导致其他线程设置的calendar数据被清空了
     *
     */
    @Test
    public void simpleDateFormatMethod() throws ParseException {
        Date date = new Date();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        String str = format.format(date);
        System.out.println("当前时间(格式化显示): " + str);
        System.out.println("当前时间: " + format.parse(str).toString());
    }
    
    
}
