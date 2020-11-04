/*
 * File name: WhiteblackFileter.java
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

import java.util.Comparator;
import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.log4j.Logger;
import org.apache.log4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.run.api.gateway.util.HttpClientUtil;
import com.run.api.gateway.util.RpcResultUtil;
import com.run.authz.base.query.WhiteBlackBaseQueryService;
import com.run.entity.common.RpcResponse;
import com.run.gateway.api.constants.GatewayConstants;

/**
 * @Description: 黑白名单过滤器
 * @author: zhabing
 * @version: 1.0, 2017年8月16日
 */
@Component
public class WhiteblackFileter extends ZuulFilter {
	private static final Logger			logger	= Logger.getLogger("zuul");
	@Autowired
	private WhiteBlackBaseQueryService	wbBaseQuery;

	@Autowired
	private JdbcTemplate				jdbcTemplate;

	/** 统一认证开关 0:关 1:开 */
	@Value("${authzFilter:0}")
	private String						authzFilter;

	@Value(value = "")
	private List<String>				list;

	
	private List<String> suffixList = Lists.newArrayList(".css",".js",".jsp",".jpg",".ico",".png");

	@PostConstruct
	private void setList() {
		String sql = "select path from gateway_api_define where enabled = 1";
		this.list = jdbcTemplate.queryForList(sql, String.class);
		// 根据路径长度排序,以便于从长到短匹配
		this.list.sort(new Comparator<String>() {
			@Override
			public int compare(String var1, String var2) {
				if (var1.length() < var2.length()) {
					return 1;
				} else if (var1.length() == var2.length()) {
					return 0;
				} else {
					return -1;
				}
			}

		});
	}



	@Override
	public Object run() {

		RequestContext ctx = RequestContext.getCurrentContext();
		String ipAddress = HttpClientUtil.getIpAddr(ctx.getRequest());
		MDC.put("ip", ipAddress);
		MDC.put("person","null");
		// 获取请求方式
		String method = ctx.getRequest().getMethod();

		if (!"OPTIONS".equals(method)) {
			// 得到请求的url地址
			String url = ctx.getRequest().getRequestURI();
			String routeUrl = url.substring(1, url.length());
			String rootUrl = "";

			for (String path : list) {
				String substring = path.replaceFirst("/", "").replaceAll("\\*", "");
				if (routeUrl.startsWith(substring)) {
					// 最后一个"/"不替换substring.subSequence(0,
					// substring.lastIndexOf("/"))
					rootUrl = routeUrl.replace(substring.subSequence(0, substring.lastIndexOf("/")), "");
					break;
				}
			}
			MDC.put("address", rootUrl);
			// 查询白名单url地址
			RpcResponse<List<String>> res = wbBaseQuery.getWhileBlackListByType(GatewayConstants.WHITE);
			List<String> addressList = null;
			logger.error("白名单：" + res.getMessage() + "====" + res.getException() + "====" + res.isSuccess());
			if (res.isSuccess()) {
				addressList = res.getSuccessValue();
			} else {
				RpcResultUtil.getRpcErrorResponse(ctx, GatewayConstants.BLACKWHITE_CODE, res.getMessage());
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
			if (rootUrl.contains(".")) {
				String suffix = rootUrl.substring(rootUrl.lastIndexOf("."));
				System.out.println(suffix);
				if (suffixList.contains(suffix)) {
					contails = true;
				}
			}
			

			// 如果成功则进行下个filter进行校验
			RpcResultUtil.getRpcSuccResponse(ctx);
			if (contails) {
				// 如果是白名单，后续filter将不会再拦截
				MDC.put("type","WHITE");
				logger.info("进入白名单，开始访问接口!");
				ctx.set(GatewayConstants.WHITE, true);
			} else {
				ctx.set(GatewayConstants.WHITE, false);
			}
		} else {
			RpcResultUtil.getRpcSuccResponse(ctx);
			// 如果是不进行统一验证，后续接口验证和接入方验证将不会再拦截
			ctx.set(GatewayConstants.INTERFACE, false);
			ctx.set(GatewayConstants.WHITE, true);
		}

		if ("0".equals(authzFilter)) {
			RpcResultUtil.getRpcSuccResponse(ctx);
			// 如果是不进行统一验证，后续接口验证和接入方验证将不会再拦截
			ctx.set(GatewayConstants.INTERFACE, false);
		} else {
			RpcResultUtil.getRpcSuccResponse(ctx);
			// 如果进行统一验证，后续接口验证和接入方验证将会拦截
			ctx.set(GatewayConstants.INTERFACE, true);
		}
		return null;
	}



	@Override
	public boolean shouldFilter() {
		return true;
	}



	@Override
	public int filterOrder() {
		return 0;
	}



	@Override
	public String filterType() {
		return "pre";
	}
}
