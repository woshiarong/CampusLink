package com.campus.forum.config;

import com.campus.forum.utils.ApiResponse;
import com.campus.forum.utils.BusinessException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ApiResponse<Object> handleBusiness(BusinessException exception) {
        return ApiResponse.fail(exception.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ApiResponse<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException exception) {
        String message = exception.getBindingResult().getAllErrors().get(0).getDefaultMessage();
        return ApiResponse.fail(message);
    }

    @ExceptionHandler(BindException.class)
    public ApiResponse<Object> handleBind(BindException exception) {
        String message = exception.getBindingResult().getAllErrors().get(0).getDefaultMessage();
        return ApiResponse.fail(message);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ApiResponse<Object> handleConstraint(ConstraintViolationException exception) {
        return ApiResponse.fail(exception.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ApiResponse<Object> handleException(Exception exception) {
        return ApiResponse.fail("系统异常: " + exception.getMessage());
    }
}
