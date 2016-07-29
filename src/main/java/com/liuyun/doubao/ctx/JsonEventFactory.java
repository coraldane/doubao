package com.liuyun.doubao.ctx;

import com.lmax.disruptor.EventFactory;

public class JsonEventFactory implements EventFactory<JsonEvent> {

	@Override
	public JsonEvent newInstance() {
		return new JsonEvent();
	}

}
