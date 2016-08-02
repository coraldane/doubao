package com.liuyun.doubao.utils;

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
	
}
