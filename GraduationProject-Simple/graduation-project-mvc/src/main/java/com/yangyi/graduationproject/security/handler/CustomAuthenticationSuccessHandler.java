package com.yangyi.graduationproject.security.handler;

import com.yangyi.graduationproject.entities.User;
import com.yangyi.graduationproject.entities.UserInfo;
import com.yangyi.graduationproject.exception.UpdateException;
import com.yangyi.graduationproject.service.UserInfoService;
import com.yangyi.graduationproject.service.UserService;
import com.yangyi.graduationproject.utils.HTTPUtils;
import com.yangyi.graduationproject.utils.LogUtil;
import com.yangyi.graduationproject.utils.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 *  Security 登录成功后跳转及响应统一处理器
 * 该类继承了SavedRequestAwareAuthenticationSuccessHandler，它会从缓存中提取请求，从而可以恢复之前请求的数据。
 */
@Component
public class CustomAuthenticationSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {
    @Qualifier("userInfoService")
    @Autowired
    private UserInfoService userInfoService;    //可用来获取用户基本信息

    @Qualifier("userService")
    @Autowired
    private UserService userService;  //可做更新用户IP、最后登录时间等操作

    public CustomAuthenticationSuccessHandler() {
    }
    public CustomAuthenticationSuccessHandler(UserService userService,UserInfoService userInfoService) {
        this.userService = userService;
        this.userInfoService = userInfoService;
    }

    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {

        UserDetails userDetails; //认证信息
        UserInfo userInfo = null;       //用户个人信息
        User user = null;               //用户登录信息
        userDetails = (UserDetails) authentication.getPrincipal();

        //避免空指针异常
        if (userDetails != null){
            userInfo = userInfoService.getUserInfoByUsername(userDetails.getUsername());
            user = userService.getUserByUsername(userDetails.getUsername());

            // 认证成功后，获取用户个人信息并添加到session中
            request.getSession().setAttribute("user", userInfo);

            //更新用户的登录IP与最后登录时间
            if (user != null){
                user.setLastLoginTime(new Date());
                user.setLoginIP(HTTPUtils.getClientIpAddress(request));
                try {
                    userService.update(user);
                } catch (UpdateException e) {
                    LogUtil.info(this,"登录成功后处理","更新用户登录IP与最后登录时间失败");
                    e.printStackTrace();
                }
                LogUtil.info(this,"登录成功后处理","用户【"+user.getUsername()+"】登录成功");
            }

            if (HTTPUtils.isAjaxRequest(request)){

                Map<String,Object> data = new ConcurrentHashMap<>(); //要返回的数据

                //获取登录前拦截时存入的 url
                String url = (String) request.getSession().getAttribute("backUrl");

                //若记录的url不为空，且不是直接请求的登录页面，就跳转到之前用户请求的页面，否则跳到首页
                if (StringUtil.isNotEmpty(url) && !(url.endsWith("/user/login") || url.endsWith("/static/login.html"))){
                    data.put("url",url);
                }else {
                    url = request.getContextPath() + "/";   //设置默认跳转的url
                    data.put("url",url);
                }

                Cookie cookie = new Cookie("USERNAME",userDetails.getUsername());
                cookie.setPath("/");    //当前项目路径可访问该cookie
                cookie.setMaxAge(604800);//存在时间
                cookie.setComment("用在前后端分离时，通过username来获取用户信息");
                cookie.setHttpOnly(false);
                response.addCookie(cookie);

                data.put("username",userDetails.getUsername());
                Map<String,Object> map = new ConcurrentHashMap<>();
                map.put("code",1);
                map.put("msg","登录成功");
                map.put("data",data);

                //向前端响应
                HTTPUtils.responseByJacson(request,response,map);
            }else {
                //带有用户请求记忆的跳转
                super.onAuthenticationSuccess(request, response, authentication);
            }
        }
    }
}