/*
 * File name: RpcResultUtil.java
 *
 * Purpose:
 *
 * Functions used and called: Name Purpose ... ...
 *
 * Additional Information:
 *
 * Development History: Revision No. Author Date 1.0 zhabing 2017年8月17日 ... ...
 * ...
 *
 ***************************************************/

package com.run.api.gateway.util;

import com.alibaba.fastjson.JSON;
import com.netflix.zuul.context.RequestContext;
import com.run.entity.common.Result;
import com.run.entity.common.ResultStatus;
import com.run.entity.tool.DateUtils;

/**
 * @Description:
 * @author: zhabing
 * @version: 1.0, 2017年8月17日
 */

public class RpcResultUtil {

	/**
	 * 
	 * @Description:异常封装工具类
	 * @param ctx
	 * @param errorCode
	 * @param failMess
	 */
	public static void getRpcErrorResponse(RequestContext ctx, String errorCode, String failMess) {
		ResultStatus status = new ResultStatus();
		status.setResultCode(errorCode);
		status.setResultMessage(failMess);
		status.setTimeStamp(DateUtils.stampToDate(System.currentTimeMillis() + ""));
		Result<String> result = new Result<String>();
		result.setResultStatus(status);
		String json = JSON.toJSONString(result);
		ctx.setSendZuulResponse(false);
		ctx.setResponseStatusCode(Integer.parseInt(errorCode));
		ctx.setResponseBody(json);
		ctx.getResponse().setHeader("Access-Control-Allow-Origin", "*");
		ctx.getResponse().setCharacterEncoding("utf-8");
		ctx.getResponse().setContentType("application/json");
		ctx.set("isSuccess", false);
	}



	public static void getRpcSuccResponse(RequestContext ctx) {
		ctx.setSendZuulResponse(true);
		ctx.setResponseStatusCode(200);
		ctx.set("isSuccess", true);
	}
}
