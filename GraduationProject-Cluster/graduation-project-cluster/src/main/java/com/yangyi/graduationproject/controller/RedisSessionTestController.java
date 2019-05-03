package com.yangyi.graduationproject.controller;

import com.yangyi.graduationproject.utils.AjaxJson;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by IntelliJ IDEA.
 * User: YangYi
 * Date: 2019/4/29
 * Time: 17:08
 *
 * 测试 SpringSession
 */
@RequestMapping("/session")
@RestController
public class RedisSessionTestController {

    @GetMapping("/set")
    public AjaxJson setSession(HttpServletRequest request, @RequestParam("key") String key, @RequestParam("value") String value){
        AjaxJson ajaxJson = new AjaxJson();
        request.getSession().setAttribute(key,value);

        ajaxJson.setCode(1);
        ajaxJson.setMsg("设置Session，key="+key+"，value="+value);
        return ajaxJson;
    }

    @GetMapping("/get")
    public AjaxJson getSession(HttpServletRequest request,@RequestParam("key") String key){
        AjaxJson ajaxJson = new AjaxJson();
        String value = request.getSession().getAttribute(key).toString();

        ajaxJson.setCode(1);
        ajaxJson.setMsg("获取Session，key="+key+"，value="+value);
        return ajaxJson;
    }
} 