package com.liuyun.doubao.plugin.taskResult;

public class TaskInfo {

	private long task_id;
	private Video video;
	private int download_flow_rate;
	private int error_code;
	private String error_extra_info;
	
	public long getTask_id() {
		return task_id;
	}
	public void setTask_id(long task_id) {
		this.task_id = task_id;
	}
	public Video getVideo() {
		return video;
	}
	public void setVideo(Video video) {
		this.video = video;
	}
	public int getDownload_flow_rate() {
		return download_flow_rate;
	}
	public void setDownload_flow_rate(int download_flow_rate) {
		this.download_flow_rate = download_flow_rate;
	}
	public int getError_code() {
		return error_code;
	}
	public void setError_code(int error_code) {
		this.error_code = error_code;
	}
	public String getError_extra_info() {
		return error_extra_info;
	}
	public void setError_extra_info(String error_extra_info) {
		this.error_extra_info = error_extra_info;
	}
	
}

class Video {
	
	private long id;
	private long trackingWebsite_id;
	private String website;
	private String key_id;
	private String clip_url;
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public long getTrackingWebsite_id() {
		return trackingWebsite_id;
	}
	public void setTrackingWebsite_id(long trackingWebsite_id) {
		this.trackingWebsite_id = trackingWebsite_id;
	}
	public String getWebsite() {
		return website;
	}
	public void setWebsite(String website) {
		this.website = website;
	}
	public String getKey_id() {
		return key_id;
	}
	public void setKey_id(String key_id) {
		this.key_id = key_id;
	}
	public String getClip_url() {
		return clip_url;
	}
	public void setClip_url(String clip_url) {
		this.clip_url = clip_url;
	}
	
}