package com.edu.hnu.sparrow.gateway.web.exceptionresolver;

import com.edu.hnu.sparrow.common.entity.Result;
import com.edu.hnu.sparrow.common.entity.StatusCode;
import com.edu.hnu.sparrow.gateway.web.exception.NoLogException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

@ControllerAdvice
public class GlobalExceptionResolver {

    /**
     * 处理所有业务异常
     * @param e 业务异常
     * @return json结果
     */
    @ExceptionHandler(NoLogException.class)
    @ResponseBody
    public Result<String> handleOpdRuntimeException(NoLogException e) {
        // 不打印异常堆栈信息
        Result<String> result=new Result<>(false, StatusCode.ERROR,"用户未登陆",null);
        return result;
    }
}
