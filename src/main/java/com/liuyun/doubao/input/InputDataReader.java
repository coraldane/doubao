package com.liuyun.doubao.input;

import java.io.IOException;

public interface InputDataReader {

	void notifyForRead();
	
	void readData() throws IOException ;
	
}
