package com.liuyun.doubao.utils;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class SysUtils {

	public static void sleep(long ms) {
		if (ms < 0) {
			return;
		}
		try {
			Thread.sleep(ms);
		} catch (InterruptedException e) {
		}
	}
	
	public static String getHostName(){
		try {
			InetAddress ia = InetAddress.getLocalHost();
			return ia.getHostName();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		return "";
	}
}
