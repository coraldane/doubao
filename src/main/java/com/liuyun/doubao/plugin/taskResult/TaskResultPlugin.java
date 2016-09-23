package com.liuyun.doubao.plugin.taskResult;

import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.liuyun.doubao.config.PluginConfig;
import com.liuyun.doubao.config.redis.RedisConfig;
import com.liuyun.doubao.plugin.DefaultPlugin;
import com.liuyun.doubao.service.JedisService;
import com.liuyun.doubao.utils.StringUtils;

import redis.clients.jedis.ShardedJedis;

public class TaskResultPlugin extends DefaultPlugin {
	
	private final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(5);
	
	private JedisService jedisService;
	private String taskResultBufferKey = null;
	private String timeoutRedisKey = null;
	
	@Override
	public void init(PluginConfig pluginConfig) {
		super.init(pluginConfig);
		
		JSONObject params = pluginConfig.getParams();
		RedisConfig redisConfig = new RedisConfig();
		redisConfig.setHost(params.getString("redis_host"));
		redisConfig.setPort(params.getIntValue("redis_port"));
		redisConfig.setPasswd(params.getString("redis_passwd"));
		
		taskResultBufferKey = params.getString("redis_key");
		this.timeoutRedisKey = this.taskResultBufferKey + "_timeout";
		
		this.jedisService = new JedisService(redisConfig);
		
		this.executorService.scheduleAtFixedRate(new TaskResultCleaner(), 1, 10, TimeUnit.MINUTES);
	}

	@Override
	public void destroy() {
		if(null != this.jedisService){
			this.jedisService.destroy();
		}
		this.executorService.shutdown();
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
				logger.error("ParseJsonError===>>>" + taskMessage + "         " + e.getMessage());
			}
		}
		return data;
	}
	
	private boolean dealWithPartResult(String loggerIndex, JSONObject data){
		ShardedJedis jedis = this.jedisService.getJedis();
		
		boolean bParseOk = false;
		try{
			String field = data.getString("host") + "_" + data.getString("tid");
			String taskMessage = data.getString("task_message");
			
			jedis.zadd(this.timeoutRedisKey, System.currentTimeMillis() + 3600 * 1000, field);
			String lastMessage = jedis.hget(this.taskResultBufferKey, field);
			String wholeMessage = ((null == lastMessage)?"":lastMessage) + taskMessage;
			
			if(wholeMessage.endsWith("}")){
				try{
					TaskInfo taskInfo = JSON.parseObject(wholeMessage, TaskInfo.class);
					data.put("task_info", taskInfo);
					data.put("task_message", wholeMessage);
					bParseOk = true;
					
					jedis.hdel(this.taskResultBufferKey, field);
				} catch(Exception e){
					
				}
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

	class TaskResultCleaner implements Runnable {
		@Override
		public void run() {
			ShardedJedis jedis = jedisService.getJedis();
			try {
				Set<String> keySet = jedis.zrangeByScore(timeoutRedisKey, 0, System.currentTimeMillis());
				if(null == keySet){
					return;
				}
				for(String key: keySet){
					jedis.hdel(taskResultBufferKey, key);
					jedis.zrem(timeoutRedisKey, key);
				}
			} catch(Exception e){
				logger.error("TaskResultCleaner error:", e);
			} finally{
				if(null != jedis){
					jedis.close();
				}
			}
		}
	}
}
