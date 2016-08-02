package com.liuyun.doubao.io.file;

import java.io.File;

public class PathResolver {
	private String dir;
	private String glob = "*";
	
	public PathResolver(String text){
		int lastDelimiter = text.lastIndexOf(File.separator);
		if(lastDelimiter == text.length()-1){
			this.dir = text;
		} else if(-1 == lastDelimiter){
			this.glob = text;
		} else {
			this.dir = text.substring(0, lastDelimiter + 1);
			this.glob = text.substring(lastDelimiter + 1);
		}
	}

	public String getDir() {
		return dir;
	}

	public String getGlob() {
		return glob;
	}

}