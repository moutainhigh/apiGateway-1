/*
 * File name: GatewayConstants.java
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

package com.run.gateway.api.constants;

/**
 * @Description: 统一网关常量类
 * @author: zhabing
 * @version: 1.0, 2017年8月16日
 */

public class GatewayConstants {

	public static final String	NO_BUSINESS			= "没有业务数据！";
	public static final String	GET_SUCC			= "查询成功！";
	public static final String	GET_FAIL			= "查询失败！";

	public static final String	SAVE_SUCC			= "保存成功！";
	public static final String	SAVE_FAIL			= "保存失败！";

	public static final String	UPDATE_SUCC			= "修改成功！";
	public static final String	UPDATE_FAIL			= "修改失败！";

	public static final String	DEL_SUCC			= "删除成功！";
	public static final String	DEL_FAIL			= "删除失败！";

	public static final String	TOKEN_FAIL			= "token已失效！";
	public static final String	ACCESS_AUTH_FAIL	= "接入方鉴权失败！";
	public static final String	GATEWAY_CODE_FAIL	= "无效网关！";
	public static final String	INTERFACE_CODE_FAIL	= "接口鉴权失败！";

	/** 黑白名单类型 */
	public static final String	TYPE				= "type";
	public static final String	STATE				= "state";
	public static final String	IS_DELETE			= "isDelete";
	/** 正常 */
	public static final String	STATE_NORMAL		= "valid";
	/** 已删除或者已禁用 */
	public static final String	STATE_ABNORMAL		= "invalid";
	/** 黑白名单地址 */
	public static final String	ADDRESS				= "address";
	/** 黑白名单地址 */
	public static final String	WHITE				= "white";
	/** 是否做接口验证 */
	public static final String	INTERFACE			= "interface";
	
	
	/** 用户id */
	public static final String	USERID				= "userId";
	public static final String	ID					= "_id";
	/** 接入方类型*/
	public static final String	ACCESS_TYPE			= "accessType";
	/** 接口地址 */
	public static final String	URL_ADDRESS			= "urlAddress";

	/** -------------------------错误编码 ----------------------- */
	/** 黑白名单获取失败 */
	public static final String	BLACKWHITE_CODE		= "300";
	/** token失效 */
	public static final String	TOKE_CODE			= "301";
	/** 接入方鉴权失败 */
	public static final String	ACCESS_AUTH_CODE	= "302";
	/** 网关无效code */
	public static final String	GATEWAY_CODE		= "303";
	/** 接口鉴权失败 */
	public static final String	INTERFACE_CODE		= "305";
	/** 人员状态不正常 */
	public static final String	USERFAIL_CODE		= "306";
	/**其它pc端登录*/
	public static final String	OTHERPCLOGIN_CODE	= "307";
	/**其它App设备登录*/
	public static final String	OTHERAPPLOGIN_CODE	= "308";
	
}
