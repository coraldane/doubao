package com.liuyun.doubao.processor;

import com.liuyun.doubao.common.InitializingBean;
import com.liuyun.doubao.ctx.JsonEvent;
import com.lmax.disruptor.EventHandler;

public interface ClosableProcessor extends EventHandler<JsonEvent>, InitializingBean {

}
