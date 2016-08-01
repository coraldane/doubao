### doubao(豆包)   
功能模拟LogStash(https://www.elastic.co/products/logstash)   
现只完成其中很少的一部分, 也可以与LogStash协同，只负责数据的转换。   
该项目的目标是为了弥补LogStash通过复杂配置文件进行数据转换时CPU高负载、低效率，且对一些复杂的转换逻辑无法支持的问题。   
特意设计了Plugin(插件)机制，用户可以根据自己的情况进行扩展。   
整个项目借鉴优秀的开源框架Dubbo(https://github.com/alibaba/dubbo) 中的SPI来实现。   
所有的input, output, filter和plugin都可以扩展。灵活度高。   
消息传递使用RingBuffer(源自优秀的高并发框架Disruptor)   

## Quick Start

1. 编译安装   
工程使用Java编写，最低版本JDK 1.6   
编译打包需要使用Maven, 请先安装(http://maven.apache.org/)   

```
git clone https://github.com/coraldane/doubao.git
cd doubao
sh package_war.sh  
```
执行后会在target子目录下生成文件doubao-1.0.0-standalone.jar   
这个文件是可执行的JAR文件，里面包含了所有依赖的jar(共4.5MB)  
启动服务：   
```
java -jar doubao-1.0.0-standalone.jar cfg.json
```
也可以将jar文件和工程中的control文件复制到同一目录，以后台模式启动   
```
cd $workdir
./control start
```
停止服务   
```
./control stop
```
或者 kill 进程号(不建议使用kill -9强制退出)，服务就会安全退出。   


默认需要读取当前目录下的配置文件cfg.json   
下面是配置文件的示例：   

## cfg.exmaple.json   

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
