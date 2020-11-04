/*
* File name: UnExpectedExceptionAdvice.java								
*
* Purpose:
*
* Functions used and called:	
* Name			Purpose
* ...			...
*
* Additional Information:
*
* Development History:
* Revision No.	Author		Date
* 1.0			guofeilong		2019年2月28日
* ...			...			...
*
***************************************************/

package com.run.api.gateway.aop;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartException;

import com.run.entity.common.Result;
import com.run.entity.tool.ResultBuilder;

/**
* @Description:	
* @author: guofeilong
* @version: 1.0, 2019年2月28日
*/

@ControllerAdvice
@ResponseBody
public class UnExpectedExceptionAdvice {

	private static Logger LOG = LoggerFactory.getLogger(UnExpectedExceptionAdvice.class);



	@ExceptionHandler(Exception.class)
	public Result<?> handleExp(Exception e) {
		if (e instanceof MultipartException) {
			String message = e.getMessage();
			LOG.error("UnExpectedException:" + e.getMessage());
			if (null != message && message.contains("FileSizeLimitExceededException")) {
				return ResultBuilder.failResult("传入文件超出限制大小");
			}
		}
		return ResultBuilder.failResult(null);
	}
	
	
}
