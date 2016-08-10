package com.liuyun.doubao.plugin.taskResult;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.liuyun.doubao.config.PluginConfig;
import com.liuyun.doubao.config.redis.RedisConfig;
import com.liuyun.doubao.plugin.DefaultPlugin;
import com.liuyun.doubao.service.JedisService;
import com.liuyun.doubao.utils.StringUtils;

import redis.clients.jedis.ShardedJedis;

public class TaskResultPlugin extends DefaultPlugin {
	
//	private static final DateFormat timeDateFormat = new SimpleDateFormat("HH:mm:ss");
	
	private JedisService jedisService;
	private String taskResultBufferKey = null;
	
	@Override
	public void init(PluginConfig pluginConfig) {
		super.init(pluginConfig);
		
		JSONObject params = pluginConfig.getParams();
		RedisConfig redisConfig = new RedisConfig();
		redisConfig.setHost(params.getString("redis_host"));
		redisConfig.setPort(params.getIntValue("redis_port"));
		redisConfig.setPasswd(params.getString("redis_passwd"));
		
		taskResultBufferKey = params.getString("redis_key");
		
		this.jedisService = new JedisService(redisConfig);
	}

	@Override
	public void destroy() {
		if(null != this.jedisService){
			this.jedisService.destroy();
		}
	}

	@Override
	public JSONObject doFilter(JSONObject data) throws Exception {
		data.remove("hostname");
		data.remove("message");
		
		String loggerIndex = data.getString("logger_index");
		if(StringUtils.isNotBlank(loggerIndex)){
			if(false == this.dealWithPartResult(loggerIndex, data)){
				return null;
			}
		} else {
			String taskMessage = data.getString("task_message");
			try{
				TaskInfo taskInfo = JSON.parseObject(taskMessage, TaskInfo.class);
				data.put("task_info", taskInfo);
				return data;
			} catch(Exception e){
				logger.error("ParseJsonError===>>>" + taskMessage, e);
			}
		}
		return null;
	}
	
	private boolean dealWithPartResult(String loggerIndex, JSONObject data){
		ShardedJedis jedis = this.jedisService.getJedis();
		
		boolean bParseOk = false;
		try{
			String field = data.getString("host") + "_" + data.getString("tid");
			String taskMessage = data.getString("task_message");
			
			String lastMessage = jedis.hget(this.taskResultBufferKey, field);
			String wholeMessage = ((null == lastMessage)?"":lastMessage) + taskMessage;
			try{
				TaskInfo taskInfo = JSON.parseObject(wholeMessage, TaskInfo.class);
				data.put("task_info", taskInfo);
				data.put("task_message", wholeMessage);
				bParseOk = true;
				
				jedis.hdel(this.taskResultBufferKey, field);
			} catch(Exception e){
				
			}
			
			if(bParseOk){
				return true;
			} else {
				jedis.hset(this.taskResultBufferKey, field, wholeMessage);
				return false;
			}
		} catch (Exception e){
			logger.error("parse_error:", e);
		} finally {
			this.jedisService.returnRes(jedis);
		}
		
		return true;
	}

}
