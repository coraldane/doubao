package com.liuyun.doubao.plugin.resourceInfo;

public class DomainStat {

	private String domainName;
	private int seedCount;
	
	public DomainStat(String domainName, int seedCount) {
		this.domainName = domainName;
		this.seedCount = seedCount;
	}
	public String getDomainName() {
		return domainName;
	}
	public void setDomainName(String domainName) {
		this.domainName = domainName;
	}
	public int getSeedCount() {
		return seedCount;
	}
	public void setSeedCount(int seedCount) {
		this.seedCount = seedCount;
	}
	
}
