package com.liuyun.doubao.common;

import com.liuyun.doubao.ctx.Context;

public interface InitializingBean {

	void init(Context context);
	
	void destroy(Context context);
	
}
