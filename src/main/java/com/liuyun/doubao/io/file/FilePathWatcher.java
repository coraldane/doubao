package com.liuyun.doubao.io.file;

import static java.nio.file.LinkOption.NOFOLLOW_LINKS;
import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;

public class FilePathWatcher implements Runnable {
	
	private final WatchService watcher;
	private final Map<WatchKey,Path> keys;
	
	private boolean recursive = false;

	private List<Path> matchedPaths = Lists.newArrayList(); 
	
	public FilePathWatcher(Path path, String glob, boolean recursive) throws IOException{
		this.watcher = FileSystems.getDefault().newWatchService();
        this.keys = new HashMap<WatchKey,Path>();
        this.recursive = recursive;
        
		final Path topDir = path;
		this.initMatcher(topDir, glob, recursive);
		
		if(recursive){
			registerAll(path);
		} else {
			register(path);
		}
	}
	
	private void initMatcher(final Path start, String glob, final boolean recursive) throws IOException{
		final PathMatcher pathMatcher = FileSystems.getDefault().getPathMatcher("glob:" + glob);
		Files.walkFileTree(start, new SimpleFileVisitor<Path>(){
			@Override
			public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
				if(false == recursive && !dir.equals(start)){
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
        WatchKey key = dir.register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
        Path prev = keys.get(key);
        if (prev == null) {
            System.out.printf("register: %s\n", dir);
        } else {
            if (!dir.equals(prev)) {
                System.out.printf("update: %s -> %s\n", prev, dir);
            }
        }
        keys.put(key, dir);
    }
	
	/**
     * Process all events for keys queued to the watcher
     */
    @SuppressWarnings("rawtypes")
    @Override
	public void run() {
        while(true) {
            // wait for key to be signalled
            WatchKey key;
            try {
                key = watcher.take();
            } catch (InterruptedException x) {
                return;
            }
 
            Path dir = keys.get(key);
            if (dir == null) {
                System.err.println("WatchKey not recognized!!");
                continue;
            }
            
            for (WatchEvent<?> event: key.pollEvents()) {
                WatchEvent.Kind kind = event.kind();
 
                // TBD - provide example of how OVERFLOW event is handled
                if (kind == OVERFLOW) {
                    continue;
                }
                
                // Context for directory entry event is the file name of entry
                WatchEvent<Path> ev = cast(event);
                Path name = ev.context();
                Path child = dir.resolve(name);
 
                // print out event
                System.out.printf("%s: %s\n", event.kind().name(), child);
 
                // if directory is created, and watching recursively, then
                // register it and its sub-directories
                if (recursive && (kind == ENTRY_CREATE)) {
                    try {
                        if (Files.isDirectory(child, NOFOLLOW_LINKS)) {
                            registerAll(child);
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
    
    @SuppressWarnings("unchecked")
    public static <T> WatchEvent<T> cast(WatchEvent<?> event) {
        return (WatchEvent<T>)event;
    }
    
    private void registerAll(final Path start) throws IOException {
        // register directory and sub-directories
        Files.walkFileTree(start, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
                throws IOException
            {
                register(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }
}
