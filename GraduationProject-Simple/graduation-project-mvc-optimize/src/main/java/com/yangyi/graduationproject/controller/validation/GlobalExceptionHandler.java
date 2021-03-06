package com.yangyi.graduationproject.controller.validation;

import com.yangyi.graduationproject.exception.OperationException;
import com.yangyi.graduationproject.utils.AjaxJson;
import com.yangyi.graduationproject.utils.LogUtil;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * SpringMVC全局统一异常处理
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理Controller层的所有业务异常
     *
     * @param e
     * @return
     */
    @ExceptionHandler(OperationException.class)
    @ResponseBody
    private AjaxJson handleBusinessException(OperationException e) {
        LogUtil.debug(this, "业务处理异常", e.getMessage());

        AjaxJson ajaxJson = new AjaxJson();
        ajaxJson.setCode(0);
        ajaxJson.setMsg(e.getMessage());
        return ajaxJson;
    }

    /**
     * 处理 SpringSecurity 访问无权限异常
     *
     * @param e
     * @return
     */
    @ExceptionHandler(AccessDeniedException.class)
    private void handleAccessDeniedException(AccessDeniedException e, HttpServletResponse response) {
        LogUtil.debug(this, "访问权限异常", e.getMessage());
        try {
            response.sendError(403);
        } catch (IOException e1) {
            e.printStackTrace();
        }
    }

    /**
     * 处理所有接口数据验证异常
     *
     * @param e
     * @return
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseBody
    private AjaxJson handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        LogUtil.debug(this, "数据效验异常", e.getMessage());

        AjaxJson ajaxJson = new AjaxJson();
        ajaxJson.setCode(0);
        ajaxJson.setMsg(e.getMessage());
        return ajaxJson;
    }

    /**
     * 处理数据绑定异常
     *
     * @param e
     * @return
     */
    @ExceptionHandler(BindException.class)
    @ResponseBody
    private AjaxJson handleMethodDataBindException(BindException e) {
        LogUtil.debug(this, "数据绑定异常", e.getMessage());

        AjaxJson ajaxJson = new AjaxJson();
        ajaxJson.setCode(0);
        ajaxJson.setMsg(e.getMessage());
        return ajaxJson;
    }

    /**
     * 处理url请求参数不匹配异常
     *
     * @param e
     * @return
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseBody
    private AjaxJson handleMissingServletRequestParameterException(MissingServletRequestParameterException e) {
        LogUtil.debug(this, "url请求参数匹配异常", e.getMessage());

        AjaxJson ajaxJson = new AjaxJson();
        ajaxJson.setCode(0);
        ajaxJson.setMsg("请求错误，参数不匹配");
        return ajaxJson;
    }

    /**
     * 处理所有不可知的异常
     *
     * @param e
     * @return
     */
    @ExceptionHandler(Exception.class)
    private void handleException(Exception e, HttpServletResponse response) {
        LogUtil.error(this, "未知异常", "遇到未知异常", e);
        e.printStackTrace();
        try {
            response.sendError(500);
        } catch (IOException e1) {
            LogUtil.error(this, "异常跳转错误异常", "捕获未知异常，跳转到500页面时出错", e);
            e.printStackTrace();
        }
    }
}