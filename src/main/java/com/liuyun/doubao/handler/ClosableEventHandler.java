package com.liuyun.doubao.handler;

import com.liuyun.doubao.common.InitializingBean;
import com.liuyun.doubao.ctx.JsonEvent;
import com.lmax.disruptor.EventHandler;

public interface ClosableEventHandler extends EventHandler<JsonEvent>, InitializingBean {

}
