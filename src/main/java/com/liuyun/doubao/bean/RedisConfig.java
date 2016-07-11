package com.liuyun.doubao.bean;

public class RedisConfig {

	private static final int PORT_DEFAULT = 6379;

	private String host;
	private int port = PORT_DEFAULT;
	private String passwd;
	
	public String getHost() {
		return host;
	}
	public void setHost(String host) {
		this.host = host;
	}
	public int getPort() {
		return port;
	}
	public void setPort(int port) {
		this.port = port;
	}
	public String getPasswd() {
		return passwd;
	}
	public void setPasswd(String passwd) {
		this.passwd = passwd;
	}
	
}
