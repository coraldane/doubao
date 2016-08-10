package com.liuyun.doubao.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;

public class FileUtils {
	
	public static String getInodeAndDevice(Path path) {
		try {
			BasicFileAttributes attr = Files.readAttributes(path, BasicFileAttributes.class);
			Object fileKey = attr.fileKey();
			String s = fileKey.toString();
			String inode = s.substring(s.indexOf("ino=") + 4, s.indexOf(")"));
			String device = s.substring(s.indexOf("dev=") + 4, s.indexOf(","));
			return inode + "," + device;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return ",";
	}
	
}
