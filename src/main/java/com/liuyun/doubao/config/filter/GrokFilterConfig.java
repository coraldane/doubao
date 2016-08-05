package com.liuyun.doubao.config.filter;

import com.alibaba.fastjson.annotation.JSONField;
import com.liuyun.doubao.common.Identified;
import com.liuyun.doubao.config.filter.bean.GrokMatchBean;

@Identified(name="grok")
public class GrokFilterConfig extends DefaultFilterConfig {

	@JSONField(name="match")
	private GrokMatchBean matchBean = new GrokMatchBean();

	public GrokMatchBean getMatchBean() {
		return matchBean;
	}

	public void setMatchBean(GrokMatchBean matchBean) {
		this.matchBean = matchBean;
	}

}

