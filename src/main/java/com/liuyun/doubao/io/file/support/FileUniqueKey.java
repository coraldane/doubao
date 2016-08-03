package com.liuyun.doubao.io.file.support;

public class FileUniqueKey {
	
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
	public boolean equals(Object obj) {
		if(obj instanceof FileUniqueKey){
			FileUniqueKey fileKey = (FileUniqueKey)obj;
			return this.inode.equals(fileKey.getInode()) && this.device.equals(fileKey.getDevice());
		}
		return false;
	}
	
	@Override
	public String toString(){
		return this.inode + "," + this.device;
	}
}