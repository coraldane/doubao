package com.liuyun.doubao.io.file.support;

import static java.nio.file.LinkOption.NOFOLLOW_LINKS;
import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.WatchEvent;
import java.nio.file.WatchEvent.Modifier;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.liuyun.doubao.config.file.FileInputConfig;

public class FilePathWatcher implements Runnable {

	protected final Logger logger = LoggerFactory.getLogger(this.getClass());

	private final WatchService watcher;
	private final Map<WatchKey, Path> keys = Maps.newConcurrentMap();
	private final Map<String, PathMatcher> pathMatcherMap = Maps.newConcurrentMap();
	private final List<PathMatcher> excludePathMatchers = Lists.newArrayList();
	
	private PathChangedEventProcessor pathChangedProcessor = null;
	private FileInputConfig fileInputConfig = null;
	private Path watchPath = null;
	
	public FilePathWatcher(Path start, FileInputConfig inputConfig, 
			PathChangedEventProcessor eventProcessor) throws IOException {
		this.watcher = FileSystems.getDefault().newWatchService();
		
		this.watchPath = start;
		this.fileInputConfig = inputConfig;
		this.pathChangedProcessor = eventProcessor;
		
		if(null != this.fileInputConfig.getExclude()){
			for(String exclude: this.fileInputConfig.getExclude()){
				this.excludePathMatchers.add(FileSystems.getDefault().getPathMatcher("glob:" + exclude));
			}
		}
	}
	
	public void addFileMatcher(String hashKey, String filepath){
		this.pathMatcherMap.put(hashKey, FileSystems.getDefault().getPathMatcher("glob:" + filepath));
	}
	
	public void startup() throws IOException{
		this.initMatcher(this.watchPath, this.fileInputConfig.isRecursive());

		if (this.fileInputConfig.isRecursive()) {
			registerAll(this.watchPath);
		} else {
			register(this.watchPath);
		}
	}

	private void initMatcher(final Path start, final boolean recursive) throws IOException {
		Files.walkFileTree(start, new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
				if (false == recursive && !dir.equals(start)) {
					return FileVisitResult.SKIP_SUBTREE;
				}
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult visitFile(Path child, BasicFileAttributes attrs) throws IOException {
				String hashKey = matchPath(child);
				if(StringUtils.isNotBlank(hashKey)){
					pathChangedProcessor.addFileReader(hashKey, child);
				}
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
				return FileVisitResult.CONTINUE;
			}
		});
	}
	
	private void register(Path dir) throws IOException {
		WatchKey key = dir.register(watcher, new WatchEvent.Kind[] { 
						ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY }, 
						get_com_sun_nio_file_SensitivityWatchEventModifier_HIGH());
		Path prev = keys.get(key);
		if (prev == null) {
			logger.debug("register folder: {}", dir);
		} else {
			if (!dir.equals(prev)) {
				logger.debug("update folder: {} -> {}", prev, dir);
			}
		}
		keys.put(key, dir);
	}

	private Modifier get_com_sun_nio_file_SensitivityWatchEventModifier_HIGH() {
		try {
			Class<?> c = Class.forName("com.sun.nio.file.SensitivityWatchEventModifier");
			Field f = c.getField("HIGH");
			return (Modifier) f.get(c);
		} catch (Exception e) {
			return null;
		}
	}
	
	/**
	 * 
	 * @param file
	 * @return 匹配的grob表达式的Hash值
	 * @throws IOException
	 */
	private String matchPath(Path file) throws IOException{
		for(PathMatcher excludePathMatcher: excludePathMatchers){
			if(excludePathMatcher.matches(file)){
				return null;
			}
		}
		
		for(String key: this.pathMatcherMap.keySet()){
			PathMatcher pathMatcher = this.pathMatcherMap.get(key);
			if(pathMatcher.matches(file)){
				return key;
			}
		}
		return null;
	}

	/**
	 * Process all events for keys queued to the watcher
	 */
	@SuppressWarnings("rawtypes")
	@Override
	public void run() {
		while (true) {
			// wait for key to be signalled
			WatchKey key;
			try {
				key = watcher.take();
			} catch (InterruptedException x) {
				return;
			}

			Path dir = keys.get(key);
			if (dir == null) {
				continue;
			}

			for (WatchEvent<?> event : key.pollEvents()) {
				WatchEvent.Kind kind = event.kind();

				// TBD - provide example of how OVERFLOW event is handled
				if (kind == OVERFLOW) {
					continue;
				}

				// Context for directory entry event is the file name of entry
				WatchEvent<Path> ev = cast(event);
				Path name = ev.context();
				Path child = dir.resolve(name);
				
				// if directory is created, and watching recursively, then
				// register it and its sub-directories
				this.registerSubDirectories(kind, child);
				
				this.pathChangedProcessor.handleEvent(event.kind(), child);
			}

			// reset key and remove from set if directory no longer accessible
			boolean valid = key.reset();
			if (!valid) {
				keys.remove(key);

				// all directories are inaccessible
				if (keys.isEmpty()) {
					break;
				}
			}
		}
	}
	
	private void registerSubDirectories(WatchEvent.Kind<?> kind, Path child){
		if (this.fileInputConfig.isRecursive() && (kind == ENTRY_CREATE)) {
			try {
				if (Files.isDirectory(child, NOFOLLOW_LINKS)) {
					registerAll(child);
				} else {
					String hashKey = this.matchPath(child);
					if(StringUtils.isNotBlank(hashKey)){
						pathChangedProcessor.addFileReader(hashKey, child);
					}
				}
			} catch (IOException x) {
				// ignore to keep sample readbale
			}
		}
	}

	@SuppressWarnings("unchecked")
	public static <T> WatchEvent<T> cast(WatchEvent<?> event) {
		return (WatchEvent<T>) event;
	}

	private void registerAll(final Path start) throws IOException {
		// register directory and sub-directories
		Files.walkFileTree(start, new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
				register(dir);
				return FileVisitResult.CONTINUE;
			}
		});
	}

	public void destroy() {
		try {
			this.watcher.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
