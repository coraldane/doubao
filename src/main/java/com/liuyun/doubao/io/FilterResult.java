package com.liuyun.doubao.io;

public class FilterResult {
	
	private boolean matched = false;
	private boolean continued = true;
	public boolean isMatched() {
		return matched;
	}
	public boolean isContinued() {
		return continued;
	}
	public FilterResult(boolean matched, boolean continued) {
		this.matched = matched;
		this.continued = continued;
	}

	public static FilterResult newMatched(boolean continued){
		return new FilterResult(true, continued);
	}
	
	public static FilterResult newNotMatch(){
		return new FilterResult(false, true);
	}
	
	public static FilterResult newDrop(){
		return new FilterResult(false, false);
	}
}
