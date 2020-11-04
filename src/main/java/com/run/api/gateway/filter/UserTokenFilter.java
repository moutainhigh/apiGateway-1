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

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.log4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.run.api.gateway.util.RpcResultUtil;
import com.run.authz.api.base.crud.AuthzBaseCurdService;
import com.run.entity.common.RpcResponse;
import com.run.gateway.api.constants.GatewayConstants;
import com.run.usc.base.query.UserBaseQueryService;

/**
 * @Description:token认证Fileter
 * @author: zhabing
 * @version: 1.0, 2017年8月16日
 */

@Component
public class UserTokenFilter extends ZuulFilter {
	private static final Logger		logger	= Logger.getLogger("zuul");

	@Autowired
	private UserBaseQueryService	userQuery;

	@Autowired
	private AuthzBaseCurdService	authzCrud;

	/** 1000*60*30 */
	@Value("${pcToken.timeOut:1800000}")
	private String					pcTokenTimeOut;
	/** 1000*60*30 */
	@Value("${appToken.timeOut:1800000}")
	private String					appTokenTimeOut;



	@SuppressWarnings("rawtypes")
	@Override
	public Object run() {
		RequestContext ctx = RequestContext.getCurrentContext();
		String token = ctx.getRequest().getHeader("Token");
		if (!ctx.getBoolean(GatewayConstants.WHITE)) {
			MDC.put("type", "TOKEN");
			if (!StringUtils.isEmpty(token)) {
				// 查询token是否存在用户信息
				try {
					RpcResponse res = userQuery.getUserByToken(token);
					logger.error(res.getMessage());
					if (res.isSuccess()) {
						
						
						if (null != res.getSuccessValue()) {
							Map userMess = (Map) res.getSuccessValue();
							
							//token异步登录修改
							String queryUserId=userMess.get(GatewayConstants.ID) + "";
							//根据用户id查询最新的 token
							String substring = token.substring(0, 3);
							if("app".equals(substring)) {
								queryUserId="app"+queryUserId;
							}else {
								queryUserId="pc"+queryUserId;
							}
							logger.error("token校验成功,开始访问真实接口！" + ctx.getRequest().getRequestURI());
							//获取redis（userId，token）的最新token
							
							RpcResponse<String> valueByKey = authzCrud.getValueByKey(queryUserId);
							if(valueByKey.isSuccess()) {
								String newToken = valueByKey.getSuccessValue();
								logger.error("token:"+token+"----"+"newtoken:"+newToken);
								if(token.equals(newToken)) {
									RpcResultUtil.getRpcSuccResponse(ctx);
									ctx.set(GatewayConstants.USERID, userMess.get(GatewayConstants.ID));
									MDC.put("person", userMess.get("loginAccount"));
									ctx.addZuulRequestHeader("userId", userMess.get(GatewayConstants.ID) + "");
									
									// 刷新token缓存
									authzCrud.refreshKey(token, Long.parseLong(pcTokenTimeOut));
									// 判断是app接口访问还是pc接口访问,
//									String check = token.substring(0, token.indexOf("-"));
//									if ("token".equals(check)) {
//										authzCrud.refreshKey(token, Long.parseLong(pcTokenTimeOut));
//									} else if ("appToken".equals(check)) {
//										authzCrud.refreshKey(token, Long.parseLong(appTokenTimeOut));
//									}
									
									logger.info("token校验成功,开始访问真实接口！");
								}else {
									//如果这个token和新token不相同，则删除token
									authzCrud.removeToken(token);
									logger.error(String.format("Token不相同，该账号在其它%s设备上登录！", substring));
									
									if("app".equals(substring)) {
										RpcResultUtil.getRpcErrorResponse(ctx, GatewayConstants.OTHERAPPLOGIN_CODE,
											"该账号在其它App端登录！");
									}else {
										RpcResultUtil.getRpcErrorResponse(ctx, GatewayConstants.OTHERPCLOGIN_CODE,
												"该账号在其它PC端登录！");
									}
								}
								
							}else {
								logger.error("userID查询最新token失败！");
								RpcResultUtil.getRpcErrorResponse(ctx, GatewayConstants.TOKE_CODE,
										GatewayConstants.TOKEN_FAIL);
							}
							//
							
						} else {
							logger.error("用户中心查询成功,但返回值为null");
							RpcResultUtil.getRpcErrorResponse(ctx, GatewayConstants.TOKE_CODE,
									GatewayConstants.TOKEN_FAIL);
						}
						
					} else {
						logger.error("用户中心查询失败");
						RpcResultUtil.getRpcErrorResponse(ctx, GatewayConstants.TOKE_CODE, GatewayConstants.TOKEN_FAIL);
					}

				} catch (Exception e) {

					logger.error("userQuery.getUserByToken(token);异常情况" + e.getMessage());
					RpcResultUtil.getRpcErrorResponse(ctx, GatewayConstants.TOKE_CODE, GatewayConstants.TOKEN_FAIL);
				}
			} else {
				logger.error("TOKEN为空");
				RpcResultUtil.getRpcErrorResponse(ctx, GatewayConstants.TOKE_CODE, GatewayConstants.TOKEN_FAIL);
			}
		}
		return null;
	}



	@Override
	public boolean shouldFilter() {
		return true;
	}



	@Override
	public int filterOrder() {
		return 1;
	}



	@Override
	public String filterType() {
		return "pre";
	}

}
