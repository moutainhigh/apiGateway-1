/*
 * File name: AccessAuthzFileter.java
 *
 * Purpose:
 *
 * Functions used and called: Name Purpose ... ...
 *
 * Additional Information:
 *
 * Development History: Revision No. Author Date 1.0 zhabing 2017年8月16日 ... ...
 * ...
 *
 ***************************************************/

package com.run.api.gateway.filter;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.run.api.gateway.util.RpcResultUtil;
import com.run.entity.common.RpcResponse;
import com.run.gateway.api.constants.GatewayConstants;
import com.run.gateway.entity.ZuulRouteVO;
import com.run.governance.service.query.GovernanceServices;
import com.run.usc.base.query.AccUserBaseQueryService;

/**
 * @Description:接人方授权认证Filter
 * @author: zhabing
 * @version: 1.0, 2017年8月16日
 */
@Component
public class AccessAuthzFileter extends ZuulFilter {

	@Autowired
	private AccUserBaseQueryService	accUser;

	@Autowired
	private GovernanceServices		goverQuery;

	@Autowired
	private JdbcTemplate			jdbcTemplate;



	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public Object run() {
		// 得到请求的网关根地址
		RequestContext ctx = RequestContext.getCurrentContext();
		if (!ctx.getBoolean(GatewayConstants.WHITE)&&ctx.getBoolean(GatewayConstants.INTERFACE)) {
			String url = ctx.getRequest().getRequestURI();
			String routeUrl = url.substring(1, url.length());
			String rootUrl = routeUrl.substring(0, routeUrl.indexOf("/"));

			String sql = "select * from gateway_api_define where enabled = 1 and path like '%" + rootUrl + "%'";
			List<ZuulRouteVO> results = jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(ZuulRouteVO.class));
			String serviceId = null;
			if (null == results || results.isEmpty()) {
				RpcResultUtil.getRpcErrorResponse(ctx, GatewayConstants.GATEWAY_CODE,
						GatewayConstants.GATEWAY_CODE_FAIL);
				return null;
			} else {
				serviceId = results.get(0).getServiceId();
			}
			// 用户id
			String userId = (String) ctx.get(GatewayConstants.USERID);
			RpcResponse<List<Map>> res = accUser.getListAccessByUserId(userId);
			if (res.isSuccess()) {
				// 获取接入方列表
				List<Map> listAcc = res.getSuccessValue();
				if (null != listAcc && !listAcc.isEmpty()) {
					// 查询这个接入方是否已经被授权
					Boolean check = false;
					checkAuth: for (Map<String, String> map : listAcc) {
						String accessType = map.get(GatewayConstants.ACCESS_TYPE);
						// 如果成功，代表这个人所属的接入方已经购买了我们的微服务
						RpcResponse<String[]> resServer = goverQuery.getAllGovernanceByaccessId(accessType);
						if (resServer.isSuccess()) {
							// 判断购买的我们的网关服务是否是访问网关
							String[] serviceIds = resServer.getSuccessValue();
							if (null != serviceIds) {
								for (String serId : serviceIds) {
									if (serviceId.equals(serId)) {
										check = true;
										break checkAuth;
									}
								}
							}
						}
					}

					if (!check) {
						RpcResultUtil.getRpcErrorResponse(ctx, GatewayConstants.ACCESS_AUTH_CODE,
								GatewayConstants.ACCESS_AUTH_FAIL);
					} else {
						// 如果成功则进行下个filter进行校验
						RpcResultUtil.getRpcSuccResponse(ctx);
					}
				}

			} else {
				return null;
			}
		}
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
		return 2;
	}



	@Override
	public String filterType() {
		return "pre";
	}

}
