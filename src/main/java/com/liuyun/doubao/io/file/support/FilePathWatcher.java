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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.liuyun.doubao.config.file.FileInputConfig;

public class FilePathWatcher implements Runnable {

	protected final Logger logger = LoggerFactory.getLogger(this.getClass());

	private final WatchService watcher;
	private final Map<WatchKey, Path> keys;
	private final PathMatcher pathMatcher;
	private final List<PathMatcher> excludePathMatchers = Lists.newArrayList();
	
	private PathChangeEventListener eventListener = null;
	private FileInputConfig fileInputConfig = null;

	public FilePathWatcher(Path path, String glob, FileInputConfig inputConfig,
			PathChangeEventListener eventListener) throws IOException {
		this.watcher = FileSystems.getDefault().newWatchService();
		this.pathMatcher = FileSystems.getDefault().getPathMatcher("glob:" + glob);
		
		this.keys = new HashMap<WatchKey, Path>();
		this.fileInputConfig = inputConfig;
		this.eventListener = eventListener;

		final Path topDir = path;
		
		if(null != this.fileInputConfig.getExclude()){
			for(String exclude: this.fileInputConfig.getExclude()){
				this.excludePathMatchers.add(FileSystems.getDefault().getPathMatcher("glob:" + exclude));
			}
		}
		
		this.initMatcher(topDir, glob, this.fileInputConfig.isRecursive());

		if (this.fileInputConfig.isRecursive()) {
			registerAll(path);
		} else {
			register(path);
		}
	}

	private void initMatcher(final Path start, String glob, final boolean recursive) throws IOException {
		Files.walkFileTree(start, new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
				if (false == recursive && !dir.equals(start)) {
					return FileVisitResult.SKIP_SUBTREE;
				}
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
				if(matchPath(file)){
					eventListener.addFileReader(file);
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
			logger.debug("register: {}", dir);
		} else {
			if (!dir.equals(prev)) {
				logger.debug("update: {} -> {}", prev, dir);
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
	
	private boolean matchPath(Path file) throws IOException{
		boolean bAllow = false;
		for(PathMatcher excludePathMatcher: excludePathMatchers){
			if(excludePathMatcher.matches(file)){
				return false;
			}
		}
		if (pathMatcher.matches(file)) {
			bAllow = true;
		}
		return bAllow;
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
				
				this.eventListener.handleEvent(event.kind(), child);
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
				} else if (matchPath(child)) {
					eventListener.addFileReader(child);
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
