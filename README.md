### doubao(豆包)   
功能模拟LogStash(https://www.elastic.co/products/logstash)   
现只完成其中很少的一部分, 也可以与LogStash协同，只负责数据的转换。   
该项目的目标是为了弥补LogStash通过复杂配置文件进行数据转换时CPU高负载、低效率，且对一些复杂的转换逻辑无法支持的问题。   
特意设计了Plugin(插件)机制，用户可以根据自己的情况进行扩展。   
整个项目借鉴优秀的开源框架Dubbo(https://github.com/alibaba/dubbo) 中的SPI来实现。   
所有的input, output, filter和plugin都可以扩展。灵活度高。   

## 配置文件示例   

```
{
	"input": {
		"redis": {
			"host": "127.0.0.1",
			"passwd": "******",
			"key": "logstash-taisan"
		}
	},
	"filter": [
		{
			"match": {
				"pattern": {
					"logger_title": "TASK ENDS",
					"log_level": "DEBUG"
				},
				"plugin": {
					"name": "taskResultPlugin",
					"params": {
						"redis_host": "127.0.0.1",
						"redis_port": 6379,
						"redis_passwd": "******",
						"redis_key": "task_result_buffer"
					}
				}
			}
		}
	],
	"output": {
		"redis": {
			"host": "127.0.0.1",
			"passwd": "******",
			"key": "logstash-taisan2"
		},
		"stdout": {
		}
	}
}
```

## Main Contributors

* coraldane [liuyun.com](http://www.liuyun.com/) coraldane@163.com   
