/*
 * File name: GatewayQueryController.java
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

package com.run.usc.service.query.controller;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.run.entity.common.Result;
import com.run.entity.tool.ResultBuilder;
import com.run.gateway.api.constants.GatewayUrlConstants;

/**
 * @Description: 统一网关查询token
 * @author: zhabing
 * @version: 1.0, 2017年8月16日
 */
@RestController
@CrossOrigin(origins = "*")
@RequestMapping(value = GatewayUrlConstants.GATEWAY)
public class GatewayQueryController {
	
	
	@RequestMapping(value = "/test")
	public Result<String> test() {
		return ResultBuilder.successResult("123123", "123123123");
	}
}
