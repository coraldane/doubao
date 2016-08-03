package com.liuyun.doubao.io.file.support;

public class FileUniqueKey implements Comparable<FileUniqueKey>{
	
	private String inode;
	private String device;
	
	public FileUniqueKey(String inode, String device) {
		this.inode = inode;
		this.device = device;
	}
	public String getInode() {
		return inode;
	}
	public void setInode(String inode) {
		this.inode = inode;
	}
	public String getDevice() {
		return device;
	}
	public void setDevice(String device) {
		this.device = device;
	}
	
	@Override
	public String toString(){
		return this.inode + "," + this.device;
	}
	@Override
	public int compareTo(FileUniqueKey o) {
		return (this.inode + this.device).compareTo(o.getInode() + o.getDevice());
	}
}