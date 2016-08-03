package com.liuyun.doubao.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 读取properties配置文件的工具类
 * @author coral coraldane@gmail.com
 * @date Oct 3, 2010 10:49:49 AM
 * @version 1.0
 */
public class Configuration {
	
	private static Logger logger = LoggerFactory.getLogger(Configuration.class);
	
	private static Configuration instance = null;

	private Properties props = null;
	
	private String configFileName = "conf/config.properties";
	
	static {
		instance = new Configuration();
	}

	public static Configuration getInstance() {
		if (instance == null){
			instance = new Configuration();
		}
		return instance;
	}
	
	public static Configuration getInstance(String configFileName) {
		return new Configuration(configFileName);
	}
	
	private Configuration(){
		
	}
	
	private Configuration(String configFileName){
		this.configFileName = configFileName;
	}
	
	public synchronized void init() {
		if (null == this.props) {
			this.props = new Properties();
			
			File resource = new File(this.configFileName);
			FileInputStream fis = null;
			try {
				if(resource.exists()){
					fis = new FileInputStream(resource);
					this.props.load(fis);
				}
			} catch (Exception e) {
				logger.error("error", e);
			} finally {
				try {
					if(null != fis){
						fis.close();
						fis = null;
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public int getIntProperty(String key){
		String value = this.getProperty(key);
		return Integer.parseInt(value);
	}

	public String getProperty(String key) {
		if (null == this.props || this.props.isEmpty()){
			init();
		}
		return this.props.getProperty(key);
	}
	
	public static String getProperty(InputStream inputStream, String key, String encoding){
		Properties props = new Properties();
		try {
			props.load(inputStream);
		} catch (IOException e) {
			logger.error("error", e);
		}
		try {
			return new String(props.getProperty(key).getBytes(), encoding);
		} catch (UnsupportedEncodingException e) {
			logger.error("error", e);
		}
		return null;
	}
	
	protected void finalize() throws Throwable {
		if (this.props != null){
			this.props.clear();
		}
	}

	public Properties getProps() {
		return props;
	}
	
}