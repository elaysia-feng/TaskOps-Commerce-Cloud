package com.elias.common.exception;

import com.elias.common.ApiResponse;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
/**
 * 文件说明： GlobalExceptionHandler.
 * 组件职责： 项目中的通用组件。
 */
public class GlobalExceptionHandler {

    @ExceptionHandler(BizException.class)
    public ApiResponse<Void> handleBiz(BizException e) {
        return ApiResponse.fail(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ApiResponse<Void> handleError(Exception e) {
        return ApiResponse.fail(ErrorCode.SERVER_ERROR.getCode(), ErrorCode.SERVER_ERROR.getMessage() + "：" + e.getMessage());
    }
}
