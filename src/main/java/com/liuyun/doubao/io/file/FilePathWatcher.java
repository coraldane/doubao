package com.liuyun.doubao.io.file;

import static java.nio.file.LinkOption.NOFOLLOW_LINKS;
import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchEvent.Kind;
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

public class FilePathWatcher implements Runnable {

	protected final Logger logger = LoggerFactory.getLogger(this.getClass());

	private final WatchService watcher;
	private final Map<WatchKey, Path> keys;
	private final PathMatcher pathMatcher;

	private boolean recursive = false;

	private List<Path> matchedPaths = Lists.newArrayList();

	public FilePathWatcher(Path path, String glob, boolean recursive) throws IOException {
		this.watcher = FileSystems.getDefault().newWatchService();
		this.pathMatcher = FileSystems.getDefault().getPathMatcher("glob:" + glob);
		this.keys = new HashMap<WatchKey, Path>();
		this.recursive = recursive;

		final Path topDir = path;
		this.initMatcher(topDir, glob, recursive);

		if (recursive) {
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
				if (pathMatcher.matches(file)) {
					matchedPaths.add(file);
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
		Modifier high = get_com_sun_nio_file_SensitivityWatchEventModifier_HIGH();
		WatchKey key = dir.register(watcher, new WatchEvent.Kind[] { StandardWatchEventKinds.ENTRY_CREATE,
						StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_MODIFY }, high);
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
				// key = this.watcher.poll(3, TimeUnit.SECONDS);
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

				this.handleWatchEvent(event.kind(), child);

				// if directory is created, and watching recursively, then
				// register it and its sub-directories
				if (recursive && (kind == ENTRY_CREATE)) {
					try {
						if (Files.isDirectory(child, NOFOLLOW_LINKS)) {
							registerAll(child);
						} else if (pathMatcher.matches(child)) {
							matchedPaths.add(child);
						}
					} catch (IOException x) {
						// ignore to keep sample readbale
					}
				}
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

	private void handleWatchEvent(Kind<?> kind, Path child) {
		logger.info("{}: {}", kind.name(), child);
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
