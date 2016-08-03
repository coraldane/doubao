package com.liuyun.doubao.utils;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * 
 * @Date: 2014年3月10日 下午9:44:31<br>
 * @Copyright (c) 2014 udai.com <br> * 
 * @since 1.0
 * @author coral
 */
public class DateUtils extends org.apache.commons.lang.time.DateUtils{
	
	public static final String DATE_FORMAT_YEAR_MONTH_DAY = "yyyy-MM-dd";
	public static final String DATE_FORMAT_HOUR_MINUTE_SECOND = "HH:mm:ss";
	public static final String DATE_FORMAT_DEFAULT = "yyyy-MM-dd HH:mm:ss";
	
	public static final String DATE_FORMAT_SIMPLE = "yyyyMMddHHmmss";
	
	public static Timestamp now(){
		return new Timestamp(System.currentTimeMillis());
	}
	
	public static String formatNow(String format){
		DateFormat sdf = new SimpleDateFormat(format);
		return sdf.format(new Date());
	}
	
	public static String formatTime(Timestamp date, String format){
		return formatTime(date.getTime(), format);
	}
	
	public static String formatTime(Date date, String format){
		return formatTime(date.getTime(), format);
	}
	
	public static String formatTime(long timeInMills, String format){
		DateFormat dateFormat = new SimpleDateFormat(format);
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(timeInMills);
		return dateFormat.format(calendar.getTime());
	}
	
	public static Timestamp truncate(Timestamp date, int field){
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(date.getTime());
		Date retDate = truncate(calendar.getTime(), field);
		return new Timestamp(retDate.getTime());
	}

	/**
	 * ceil(t1-t2)
	 * @param t1
	 * @param t2
	 * @return
	 */
	public static int dayDiff(Date t1, Date t2) {
		t1 = truncateTime(t1);
		t2 = truncateTime(t2);
		return (int)((t1.getTime()-t2.getTime())/(1000*60*60*24));
	}

	public static Date truncateTime(Date date) {
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		c.set(Calendar.HOUR_OF_DAY, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);
		return c.getTime();
	}
}
