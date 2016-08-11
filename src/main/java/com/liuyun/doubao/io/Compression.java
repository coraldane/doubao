package com.liuyun.doubao.io;

import java.io.IOException;

import com.liuyun.doubao.extension.SPI;

@SPI("gzip")
public interface Compression {

	String compress(String source) throws IOException;
	
	String uncompress(String source, String charsetName) throws IOException;
	
}
