/*
 * File name: InterfaceFileter.java
 *
 * Purpose:
 *
 * Functions used and called: Name Purpose ... ...
 *
 * Additional Information:
 *
 * Development History: Revision No. Author Date 1.0 zhabing 2017年8月22日 ... ...
 * ...
 *
 ***************************************************/

package com.run.api.gateway.filter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.run.api.gateway.util.RpcResultUtil;
import com.run.authz.base.query.PermiBaseQueryService;
import com.run.entity.common.RpcResponse;
import com.run.gateway.api.constants.GatewayConstants;
import com.run.usc.base.query.AccUserBaseQueryService;

/**
 * @Description: url地址认证
 * @author: zhabing
 * @version: 1.0, 2017年8月22日
 */
@Component
public class InterfaceFileter extends ZuulFilter {

	@Autowired
	private PermiBaseQueryService	permiQuery;

	@Autowired
	private AccUserBaseQueryService	accUser;
	private static final Logger			logger	= Logger.getLogger("zuul");


	@SuppressWarnings({ "rawtypes" })
	@Override
	public Object run() {
		RequestContext ctx = RequestContext.getCurrentContext();
		logger.error("进入interface 校验：" + ctx.getRequest().getRequestURI());
		if(ctx.getBoolean(GatewayConstants.WHITE)) {
			//白名单直接放行
			logger.error("进入interface 校验   白名单直接放行");
			RpcResultUtil.getRpcSuccResponse(ctx);
		} else if (!ctx.getBoolean(GatewayConstants.WHITE)&&ctx.getBoolean(GatewayConstants.INTERFACE)) {
			// 用户id
			String userId = (String) ctx.get(GatewayConstants.USERID);
			logger.error("进入interface 校验   userId：" + userId);
			// 得到请求的url地址
			String url = ctx.getRequest().getRequestURI();
			String routeUrl = url.substring(1, url.length());
			String rootUrl = routeUrl.substring(routeUrl.indexOf("/"));
			// 查询该用户已经拥有的接口权限
			try {
				// 查询该用户是否已经被禁用或者删除
				Boolean check = accUser.checkUserState(userId);
				if (!check) {
					RpcResultUtil.getRpcErrorResponse(ctx, GatewayConstants.USERFAIL_CODE, null);
					return null;
				}

				RpcResponse<List<Map>> res = permiQuery.getInterListByUserId(userId);
				logger.error(res + "===============" + res.getMessage() + "===============" + res.getSuccessValue());
				List<String> addressList = new ArrayList<String>();
				if (res.isSuccess()) {
					List<Map> addressMap = res.getSuccessValue();
					if (null != addressMap && !addressMap.isEmpty()) {
						for (Map map : addressMap) {
							addressList.add((String) map.get(GatewayConstants.URL_ADDRESS));
						}
					}
				} else {
					logger.error("interface 校验结束res：" + res);
					RpcResultUtil.getRpcErrorResponse(ctx, GatewayConstants.INTERFACE_CODE, res.getMessage());
					return null;
				}

				Boolean contails = false;
				// 判断请求的url地址是否包含在白名单里面
				for (String address : addressList) {
					// 如果参数形式没有{}这种的形式用equals去判断
					if (rootUrl.equals(address)) {
						contails = true;
						break;
						// 如果参数形式以{}开头的做特殊处理(/auth/{id})
					} else if (address.indexOf("{") > 0) {
						contails = true;
						String[] adds = address.split("/", -1);
						String[] roots = rootUrl.split("/", -1);
						// 首先判断长度是否一样
						if (adds.length == roots.length) {
							for (int i = 0; i < adds.length; i++) {
								String ro = roots[i];
								String ad = adds[i];
								// 如果不是以{}形式出现，要全匹配，否则就不匹配
								if (ad.indexOf("}") == -1) {
									if (!ro.equals(ad)) {
										contails = false;
										break;
									}
								}
							}
						} else {
							contails = false;
						}
						// 如果匹配成功，则跳出循环
						if (contails) {
							break;
						}
					}
				}
				logger.error("interface 校验结束contails：" + contails);
				// 如果该用户拥有该访问权限
				if (contails) {
					RpcResultUtil.getRpcSuccResponse(ctx);
				} else {
					RpcResultUtil.getRpcErrorResponse(ctx, GatewayConstants.INTERFACE_CODE,
							GatewayConstants.INTERFACE_CODE_FAIL);
				}
			} catch (Exception e) {
				logger.error("interface 校验结束Exception：" + ctx);
				RpcResultUtil.getRpcErrorResponse(ctx, GatewayConstants.INTERFACE_CODE, e.getMessage());
				return null;
			}
		}
		logger.error("interface 校验结束===================");
		return null;

	}



	@Override
	public boolean shouldFilter() {
		RequestContext ctx = RequestContext.getCurrentContext();
		// 如果前一个过滤器的结果为true，则说明上一个过滤器成功了，需要进入当前的过滤，如果前一个过滤器的结果为false，则说明上一个过滤器没有成功，则无需进行下面的过滤动作了，直接跳过后面的所有过滤器并返回结果
		return (boolean) ctx.get("isSuccess");
	}



	@Override
	public int filterOrder() {
		return 3;
	}



	@Override
	public String filterType() {
		return "pre";
	}
}
