package com.liuyun.doubao.input;

import com.liuyun.doubao.io.Closable;
import com.liuyun.doubao.io.Stopable;

public interface DataReaderFactory extends Stopable, Closable, Runnable {

	void addReader(String key, AbstractStopableDataReader dataReader);
	
	boolean containsReader(String key);
	
	void notifyForRead(String key);
	
	void destroy(String key);
	
	void resetReader(String oldKey, String newKey, Object param);
	
}
