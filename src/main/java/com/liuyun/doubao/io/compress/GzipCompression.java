package com.liuyun.doubao.io.compress;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import com.liuyun.doubao.io.Compression;
import com.liuyun.doubao.utils.StringUtils;

public class GzipCompression implements Compression {

	@Override
	public String compress(String source) throws IOException {
		if(StringUtils.isBlank(source)){
			return null;
		}
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		GZIPOutputStream gos = new GZIPOutputStream(baos);
		gos.write(source.getBytes());
		gos.close();
		return baos.toString("ISO-8859-1");
	}

	@Override
	public String uncompress(String source, String charsetName) throws IOException {
		if(StringUtils.isBlank(source)){
			return null;
		}
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ByteArrayInputStream bais = new ByteArrayInputStream(source.getBytes("ISO-8859-1"));
		GZIPInputStream gis = new GZIPInputStream(bais);
		byte[] buffer = new byte[256];
		int c;
		while((c = gis.read(buffer)) >= 0){
			baos.write(buffer, 0, c);
		}
		return baos.toString(charsetName);
	}

}
