package com.liuyun.doubao.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import com.alibaba.fastjson.JSON;

public class FilePathTest {

	public static void main(String[] args) throws IOException{
		String filepath = "/Users/coral/Downloads/**.log";
		
		PathResolver resolver = new PathResolver(filepath);
		System.out.println(JSON.toJSONString(resolver));
		
		final PathMatcher pathMatcher = FileSystems.getDefault().getPathMatcher("glob:" + resolver.getGlob());
		final Path topDir = Paths.get(resolver.getDir());
		Files.walkFileTree(topDir, new SimpleFileVisitor<Path>(){
			@Override
			public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
				if(!dir.equals(topDir)){
					return FileVisitResult.SKIP_SUBTREE;
				}
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
				if (pathMatcher.matches(file)) {
                    System.out.println(file);
                }
				return FileVisitResult.CONTINUE;
			}
			
			@Override
			public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
				return FileVisitResult.CONTINUE;
			}
		});
	}
	
}

class PathResolver {
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